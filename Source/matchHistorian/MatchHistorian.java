package matchHistorian;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class MatchHistorian {
	final static String configurationFile = "MatchHistorian.properties";
	
	Connection database;
	MatchHistorianServer webSocketServer;
	
	public MatchHistorian() throws IOException, SQLException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(configurationFile));
		String databaseURL = properties.getProperty("databaseURL");
		String databaseUser = properties.getProperty("databaseUser");
		int webSocketPort = Integer.parseInt(properties.getProperty("webSocketPort"));
		
		Properties databaseProperties = new Properties();
		databaseProperties.setProperty("user", databaseUser);
		this.database = DriverManager.getConnection(databaseURL, databaseProperties);
		this.webSocketServer = new MatchHistorianServer(this, webSocketPort);
		this.webSocketServer.start();
	}
	
	// Returns true if the summoner was added to the database or was already stored
	boolean addSummoner(String region, int summonerId) throws Exception {
		ArrayList<GameResult> games = getGames(region, summonerId);
		if(games == null)
			return false;
		try {
			// Check if the summoner needs to be added to the database
			// Start a new transaction
			this.database.setAutoCommit(false);
			String query = "select count(*) from summoner where region = ? and summoner_id = ?";
			PreparedStatement statement = this.database.prepareStatement(query);
			statement.setString(1, region);
			statement.setInt(2, summonerId);
			ResultSet result = statement.executeQuery();
			result.first();
			int count = result.getInt(0);
			if(count != 1) {
				// The summoner is not in the database yet
				throw new Exception("Not implemented");
			}
			else {
				// The summoner was already stored in the database
				// Still need to make sure that automatic updates are enabled
				query = "update table summoner set automatic_updates = true where region = ? and summoner_id = ?";
				statement = this.database.prepareStatement(query);
				statement.setString(1, region);
				statement.setInt(2, summonerId);
				statement.executeUpdate();
				throw new Exception("Not implemented");
			}
			// End of transaction
			this.database.commit();
		}
		finally {
			// Always restore auto-commit
			this.database.setAutoCommit(true);
		}
		return true;
	}
	
	ArrayList<GameResult> getGames(String region, int summonerId) throws Exception {
		try {
			String url = "http://www.lolking.net/summoner/" + region + "/" + summonerId;
			Document document = Jsoup.connect(url).get();
			return Parser.parseGames(document, summonerId);
		}
		catch(HttpStatusException exception) {
			if(exception.getStatusCode() == 404) {
				// This means that the summoner was not found
				return null;
			}
			else
				throw exception;
		}
	}
}
