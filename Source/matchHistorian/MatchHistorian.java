package matchHistorian;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

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
	
	ArrayList<GameResult> getGames(String region, int summonerId) throws Exception {
		String url = "http://www.lolking.net/summoner/" + region + "/" + summonerId;
		Document document = Jsoup.connect(url).get();
		return Parser.parseGames(document, summonerId);
	}
}
