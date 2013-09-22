package matchHistorian;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
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
		// this.webSocketServer.start();
	}
	
	// Returns true if the summoner was added to the database or was already stored
	public boolean addSummoner(String region, int summonerId) throws Exception {
		SummonerProfile profile = getProfile(region, summonerId);
		if(profile == null) {
			// The summoner could not be found
			return false;
		}
		try {
			// Check if the summoner needs to be added to the database
			// Start a new transaction
			this.database.setAutoCommit(false);
			String query = "select count(*) from summoner where region = ? and summoner_id = ?";
			PreparedStatement statement = this.database.prepareStatement(query);
			int index = getIndex();
			statement.setString(index++, region);
			statement.setInt(index++, summonerId);
			ResultSet result = statement.executeQuery();
			result.first();
			int count = result.getInt(1);
			result.close();
			if(count != 1) {
				// Necessary due to resource leak detection bug
				statement.close();
				// The summoner is not in the database yet
				query = "insert into summoner (region, summoner_id, name, update_automatically) values (?, ?, ?, true)";
				statement = this.database.prepareStatement(query);
				index = getIndex();
				statement.setString(index++, region);
				statement.setInt(index++, summonerId);
				statement.setString(index++, profile.name);
				statement.executeUpdate();
				statement.close();
			}
			else {
				// Necessary due to resource leak detection bug
				statement.close();
				// The summoner was already stored in the database
				// Still need to make sure that automatic updates are enabled
				query = "update table summoner set automatic_updates = true where region = ? and summoner_id = ?";
				statement = this.database.prepareStatement(query);
				index = getIndex();
				statement.setString(index++, region);
				statement.setInt(index++, summonerId);
				statement.executeUpdate();
				statement.close();
			}
			for(GameResult game : profile.games) {
				processGameResult(region, game);
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
	
	static int getIndex() {
		return 1;
	}
	
	static int getInsertId(PreparedStatement statement) throws Exception {
		ResultSet keys = statement.getGeneratedKeys();
		if(!keys.first())
			throw new Exception("Unable to retrieve generated keys");
		return keys.getInt(1);
	}
	
	void processGameResult(String region, GameResult game) throws Exception {
		// Check if the game is in the database yet
		String query = "select id from game where region = ? and game_id = ?";
		PreparedStatement statement = this.database.prepareStatement(query);
		int index = getIndex();
		statement.setString(index++, region);
		statement.setInt(index++, game.gameId);
		ResultSet result = statement.executeQuery();
		int gameId;
		boolean gameInDatabase = result.first();
		if(gameInDatabase) {
			gameId = result.getInt(getIndex());
			result.close();
			statement.close();
			// The game was already in the database so we have to make sure that these entries for the player are set
			query = "update game_player set spells = ?, kills = ?, deaths = ?, items = ?, gold = ?, minions_killed = ? where game_id = ? and summoner_id = ?";
			statement = this.database.prepareStatement(query);
			index = getIndex();
			statement.executeUpdate();
			statement.close();
		}
		else {
			result.close();
			statement.close();
			// The game wasn't in the database yet, add it
			query = "insert into table game (region, game_id, map, game_mode, time, duration, losing_team, winning_team) values (?, ?, ?::map_type, ?::game_mode_type, ?, ?, ?, ?)";
			statement = this.database.prepareStatement(query);
			index = getIndex();
			statement.setString(index++, region);
			statement.setInt(index++, game.gameId);
			if(game.mapUnknown)
				statement.setNull(index++, Types.VARCHAR);
			else
				statement.setString(index++, GameResult.getMapString(game.map));
			statement.setString(index++, GameResult.getGameModeString(game.mode));
			statement.setDate(index++, new java.sql.Date(game.date.getTime()));
			statement.setInt(index++, game.duration);
			statement.setArray(index++, GameResult.getTeamIds(this.database, game.losingTeam));
			statement.setArray(index++, GameResult.getTeamIds(this.database, game.winningTeam));
			statement.executeUpdate();
			gameId = getInsertId(statement);
			statement.close();
		}
		throw new Exception("Not implemented");
	}
	
	SummonerProfile getProfile(String region, int summonerId) throws Exception {
		try {
			String url = "http://www.lolking.net/summoner/" + region + "/" + summonerId;
			Document document = Jsoup.connect(url).get();
			String summonerName = Parser.getName(document);
			ArrayList<GameResult> games = Parser.parseGames(document, summonerId);
			SummonerProfile profile = new SummonerProfile(region, summonerName, summonerId, games);
			return profile;
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
