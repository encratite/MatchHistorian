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
		int index = 1;
		map = Map.valueOf(result.getString(index++).toUpperCase());
		gameMode = GameMode.valueOf(result.getString(index++).toUpperCase());
		championId = result.getInt(index++);
		wins = result.getInt(index++);
		losses = result.getInt(index++);
		kills = result.getInt(index++);
		deaths = result.getInt(index++);
		assists = result.getInt(index++);
		gold = result.getInt(index++);
		minionsKilled = result.getInt(index++);
		duration = result.getInt(index++);
	}
}
