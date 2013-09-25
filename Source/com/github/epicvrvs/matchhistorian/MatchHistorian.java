package com.github.epicvrvs.matchhistorian;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
			Statement statement = getStatement("select count(*) from summoner where region = ? and summoner_id = ?");
			statement.setString(region);
			statement.setInteger(summonerId);
			ResultSet result = statement.query();
			result.first();
			int count = result.getInt(1);
			statement.close();
			if(count != 1) {
				// The summoner is not in the database yet
				statement = getStatement("insert into summoner (region, summoner_id, name, update_automatically) values (?, ?, ?, true)");
				statement.setString(region);
				statement.setInteger(summonerId);
				statement.setString(profile.name);
				statement.updateAndClose();
			}
			else {
				// The summoner was already stored in the database
				// Still need to make sure that automatic updates are enabled
				statement = getStatement("update table summoner set automatic_updates = true where region = ? and summoner_id = ?");
				statement.setString(region);
				statement.setInteger(summonerId);
				statement.updateAndClose();
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
	
	Statement getStatement(String query) throws SQLException {
		return new Statement(this.database, query);
	}
	
	void processGameResult(String region, GameResult game) throws Exception {
		// Check if the game is in the database yet
		Statement statement = getStatement("select id from game where region = ? and game_id = ?");
		statement.setString(region);
		statement.setInteger(game.gameId);
		ResultSet result = statement.query();
		int gameId;
		boolean gameInDatabase = result.first();
		if(gameInDatabase) {
			gameId = result.getInt(1);
			result.close();
			statement.close();
			// The game was already in the database so we have to make sure that these entries for the player are set
			statement = getStatement("update game_player set spells = ?, kills = ?, deaths = ?, items = ?, gold = ?, minions_killed = ? where game_id = ? and summoner_id = ?");
			statement.updateAndClose();
		}
		else {
			result.close();
			statement.close();
			// The game wasn't in the database yet, add it
			statement = getStatement("insert into table game (region, game_id, map, game_mode, time, duration, losing_team, winning_team) values (?, ?, ?::map_type, ?::game_mode_type, ?, ?, ?, ?)");
			statement.setString(region);
			statement.setInteger(game.gameId);
			if(game.mapUnknown)
				statement.setNull(Types.VARCHAR);
			else
				statement.setString(GameResult.getMapString(game.map));
			statement.setString(GameResult.getGameModeString(game.mode));
			statement.setDate(new java.sql.Date(game.date.getTime()));
			statement.setInteger(game.duration);
			statement.setArray(GameResult.getTeamIds(database, game.losingTeam));
			statement.setArray(GameResult.getTeamIds(database, game.winningTeam));
			statement.update();
			gameId = statement.getInsertId();
			statement.close();
		}
		statement = getStatement("select id from aggregated_statistics where summoner_id = ? and map = ?::map_type and game_mode = ?::game_mode_type and champion_id = ?");
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
