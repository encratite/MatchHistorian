package com.github.epicvrvs.matchhistorian;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AggregatedStatistics {
	Map map;
	GameMode gameMode;
	int championId;
	int wins;
	int losses;
	int kills;
	int deaths;
	int assists;
	int gold;
	int minionsKilled;
	int duration;
	
	AggregatedStatistics(ResultSet result) throws SQLException {
		map = Map.valueOf(result.getString("map").toUpperCase());
		gameMode = GameMode.valueOf(result.getString("game_mode").toUpperCase());
		championId = result.getInt("champion_id");
		wins = result.getInt("wins");
		losses = result.getInt("losses");
		kills = result.getInt("kills");
		deaths = result.getInt("deaths");
		assists = result.getInt("assists");
		gold = result.getInt("gold");
		minionsKilled = result.getInt("minions_killed");
		duration = result.getInt("duration");
	}
}
