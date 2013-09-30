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
	private Connection database;
	
	public MatchHistorian(Connection database) {
		this.database = database;
	}
	
	/**
	 * Update the database entries for the given summoner.
	 * If the summoner is not stored in the database yet, a new entry will be added.
	 * This also causes the automatic update flag to be enabled by default. 
	 * @param region the lolking.net lower case region string of the region the summoner resides on 
	 * @param summonerId the numeric ID of the summoner on the game servers
	 */
	public void updateSummoner(String region, int summonerId) throws MatchHistorianException, ParserException, SQLException, HTTPException {
		SummonerWebData summoner = getProfile(region, summonerId);
		try(Transaction transaction = new Transaction(database)) {
			Integer summonerDatabaseId = null;
			// Check if the summoner needs to be added to the database
			try(Statement select = getStatement("select id from summoner where region = ? and summoner_id = ?")) {
				select.setString(region);
				select.setInteger(summonerId);
				ResultSet result = select.query();
				if(result.first())
					summonerDatabaseId = result.getInt("id");
			}
			if(summonerDatabaseId == null) {
				// The summoner is not in the database yet
				try(Statement insert = getStatement("insert into summoner (region, summoner_id, name, update_automatically) values (?, ?, ?, true)")) {
					insert.setString(region);
					insert.setInteger(summonerId);
					insert.setString(summoner.name);
					insert.update();
					summonerDatabaseId = insert.getInsertId();
				}
			}
			else {
				// The summoner was already stored in the database
				// Still need to make sure that automatic updates are enabled
				try(Statement update = getAutomaticUpdateStatement(region, summonerId, true)) {
					update.update();
				}
			}
			for(GameResult game : summoner.games)
				processGameResult(region, summonerDatabaseId, summoner, game);
		}
	}
	
	/**
	 * Sets the automatic update flag of a summoner in the database.
	 * @param region the lolking.net lower case region string of the region the summoner resides on 
	 * @param summonerId the numeric ID of the summoner on the game servers
	 * @param enable new value of the flag that determines if a summoner 
	 */
	public void enableSummonerUpdates(String region, int summonerId, boolean enable) throws MatchHistorianException, SQLException {
		try(Statement statement = getAutomaticUpdateStatement(region, summonerId, enable)) {
			int rowsAffected = statement.update();
			if(rowsAffected == 0)
				throw new MatchHistorianException("No such summoner");
		}
	}
	
	/**
	 * Retrieve general information about a summoner stored in the database, including aggregated statistics.
	 * This does not load the match history of the summoner, though, because it could be arbitrarily large. 
	 * @param region the lolking.net lower case region string of the region the summoner resides on 
	 * @param summonerId the numeric ID of the summoner on the game servers
	 */
	public Summoner getSummoner(String region, int summonerId) throws MatchHistorianException, SQLException {
		try(Transaction transaction = new Transaction(database)) {
			Summoner summoner;
			try(Statement selectSummoner = getStatement("select id, region, summoner_id, name, update_automatically from summoner where region = ? and summoner_id = ?")) {
				selectSummoner.setString(region);
				selectSummoner.setInteger(summonerId);
				ResultSet summonerResult = selectSummoner.query();
				if(!summonerResult.first())
					throw new MatchHistorianException("No such summoner");
				summoner = new Summoner(summonerResult);
			}
			try(Statement selectStatistics = getStatement("select map, game_mode, champion_id, wins, losses, kills, deaths, assists, gold, minions_killed, duration from aggregated_statistics where summoner_id = ?")) {
				selectStatistics.setInteger(summoner.id);
				ResultSet statisticsResult = selectStatistics.query();
				AggregatedStatistics statistics = new AggregatedStatistics(statisticsResult);
				summoner.aggregatedStatistics.add(statistics);
			}
			return summoner;
		}
	}
	
	Statement getStatement(String query) throws SQLException {
		return new Statement(this.database, query);
	}

	Statement getAutomaticUpdateStatement(String region, int summonerId, boolean enable) throws SQLException {
		Statement statement = getStatement("update table summoner set automatic_updates = ? where region = ? and summoner_id = ?");
		statement.setBoolean(enable);
		statement.setString(region);
		statement.setInteger(summonerId);
		return statement;
	}
	
	void setStatisticsVariables(Statement statement, GameResult game) throws SQLException {
		statement.setInteger(game.victory ? 1 : 0);
		statement.setInteger(game.victory ? 0 : 1);
		statement.setInteger(game.playerStatistics.kills);
		statement.setInteger(game.playerStatistics.deaths);
		statement.setInteger(game.playerStatistics.assists);
		statement.setInteger(game.playerStatistics.gold);
		statement.setInteger(game.playerStatistics.minionsKilled);
		statement.setInteger(game.playerStatistics.duration);
	}
	
	void insertTeamPlayers(SummonerWebData summoner, int gameDatabaseId, GameResult game, ArrayList<GamePlayer> team) throws SQLException {
		for(GamePlayer player : team) {
			try(Statement insertPlayer = getStatement("insert into game_player (game_id, summoner_id, champion_id, spells, kills, deaths, assists, items, gold, minions_killed) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				insertPlayer.setInteger(gameDatabaseId);
				insertPlayer.setInteger(player.summonerId);
				insertPlayer.setInteger(player.championId);
				if(player.summonerId == summoner.summonerId) {
					// This is the player whose profile the data was extracted from so more information is available
					insertPlayer.setArray(getArray(game.spells));
					insertPlayer.setInteger(game.playerStatistics.kills);
					insertPlayer.setInteger(game.playerStatistics.deaths);
					insertPlayer.setInteger(game.playerStatistics.assists);
					insertPlayer.setArray(getArray(game.items));
					insertPlayer.setInteger(game.playerStatistics.gold);
					insertPlayer.setInteger(game.playerStatistics.minionsKilled);
				}
				else {
					// It's another player so the other values are set to null
					insertPlayer.setNull(Types.ARRAY);
					insertPlayer.setNull(Types.INTEGER);
					insertPlayer.setNull(Types.INTEGER);
					insertPlayer.setNull(Types.INTEGER);
					insertPlayer.setNull(Types.ARRAY);
					insertPlayer.setNull(Types.INTEGER);
					insertPlayer.setNull(Types.INTEGER);
				}
				insertPlayer.update();
			}
		}
	}
	
	Array getArray(int[] array) throws SQLException {
		Integer[] objectArray = new Integer[array.length];
		for(int i = 0; i < array.length; i++)
			objectArray[i] = array[i];
		return database.createArrayOf("int", objectArray);
	}
	
	Array getTeamIds(ArrayList<GamePlayer> team) throws SQLException {
		Integer[] ids = new Integer[team.size()];
		for(int i = 0; i < team.size(); i++)
			ids[i] = team.get(i).summonerId;
		return database.createArrayOf("int", ids);
	}
	
	void processGameResult(String region, int summonerDatabaseId, SummonerWebData summoner, GameResult game) throws SQLException {
		Integer gameId = null;
		// Check if the game is in the database yet
		try(Statement selectGameId = getStatement("select id from game where region = ? and game_id = ?")) {
			selectGameId.setString(region);
			selectGameId.setInteger(game.gameId);
			ResultSet gameIdResult = selectGameId.query();
			if(gameIdResult.first())
				gameId = gameIdResult.getInt("id");
		}
		if(gameId == null) {
			// The game wasn't in the database yet, add it
			try(Statement insertGame = getStatement("insert into game (region, game_id, map, game_mode, time, duration, losing_team, winning_team) values (?, ?, ?::map_type, ?::game_mode_type, ?, ?, ?, ?)")) {
				insertGame.setString(region);
				insertGame.setInteger(game.gameId);
				insertGame.setString(GameSettings.getMapString(game.gameSettings.map));
				insertGame.setString(GameSettings.getGameModeString(game.gameSettings.gameMode));
				insertGame.setDate(new java.sql.Date(game.date.getTime()));
				insertGame.setInteger(game.playerStatistics.duration);
				insertGame.setArray(getTeamIds(game.losingTeam));
				insertGame.setArray(getTeamIds(game.winningTeam));
				insertGame.update();
				gameId = insertGame.getInsertId();
			}
			insertTeamPlayers(summoner, gameId, game, game.losingTeam);
			insertTeamPlayers(summoner, gameId, game, game.winningTeam);
		}
		else {
			// The game was already in the database so we have to make sure that these entries for the player are set
			try(Statement update = getStatement("update game_player set spells = ?, kills = ?, deaths = ?, assists = ?, items = ?, gold = ?, minions_killed = ? where game_id = ? and summoner_id = ?")) {
				update.setArray(getArray(game.spells));
				update.setInteger(game.playerStatistics.kills);
				update.setInteger(game.playerStatistics.deaths);
				update.setInteger(game.playerStatistics.assists);
				update.setArray(getArray(game.items));
				update.setInteger(game.playerStatistics.gold);
				update.setInteger(game.playerStatistics.minionsKilled);
				update.update();
			}
		}
		Integer statisticsId = null;
		try(Statement selectStatisticsId = getStatement("select id from aggregated_statistics where summoner_id = ? and map = ?::map_type and game_mode = ?::game_mode_type and champion_id = ?")) {
			selectStatisticsId.setInteger(summonerDatabaseId);
			selectStatisticsId.setString(GameSettings.getMapString(game.gameSettings.map));
			selectStatisticsId.setString(GameSettings.getGameModeString(game.gameSettings.gameMode));
			selectStatisticsId.setInteger(game.championId);
			ResultSet statisticsIdResult = selectStatisticsId.query();
			if(statisticsIdResult.first())
				statisticsId = statisticsIdResult.getInt("id");
		}
		if(statisticsId == null) {
			// The entry didn't exist yet, create a new one first
			try(Statement updateStatistics = getStatement("insert into aggregated_statistics (summoner_id, map, game_mode, champion_id, wins, losses, kills, deaths, assists, gold, minions_killed, duration) values (?, ?::map_type, ?::game_mode_type, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				updateStatistics.setInteger(summonerDatabaseId);
				updateStatistics.setString(GameSettings.getMapString(game.gameSettings.map));
				updateStatistics.setString(GameSettings.getGameModeString(game.gameSettings.gameMode));
				updateStatistics.setInteger(game.championId);
				setStatisticsVariables(updateStatistics, game);
				updateStatistics.update();
			}
		}
		else {
			// There is already an entry for that combination of summoner, map, game mode and champion
			// Update the existing entry based on the ID
			try(Statement updateStatistics = getStatement("update aggregated_statistics set wins = wins + ?, losses = losses + ?, kills = kills + ?, deaths = deaths + ?, assists = assists + ?, gold = gold + ?, minions_killed = minions_killed + ?, duration = duration + ? where id = ?")) {
				setStatisticsVariables(updateStatistics, game);
				updateStatistics.update();
			}
		}
	}
	
	SummonerWebData getProfile(String region, int summonerId) throws HTTPException, MatchHistorianException, ParserException {
		try {
			String url = "http://www.lolking.net/summoner/" + region + "/" + summonerId;
			Document document = Jsoup.connect(url).get();
			String summonerName = Parser.getName(document);
			ArrayList<GameResult> games = Parser.parseGames(document, summonerId);
			SummonerWebData profile = new SummonerWebData(region, summonerName, summonerId, games);
			return profile;
		}
		catch(HttpStatusException exception) {
			if(exception.getStatusCode() == 404)
				throw new MatchHistorianException("Unable to find summoner");
			else
				throw new HTTPException("Server error", exception);
		}
		catch(IOException exception) {
			throw new HTTPException("IO exception", exception);
		}
	}
}
