package com.github.epicvrvs.matchhistorian;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AggregatedStatistics {
	public final GameSettings gameSettings;
	public final int championId;
	public final int wins;
	public final int losses;
	public final PlayerStatistics playerStatistics;
	
	public AggregatedStatistics(ResultSet result) throws SQLException {
		gameSettings = new GameSettings(result);
		championId = result.getInt("champion_id");
		wins = result.getInt("wins");
		losses = result.getInt("losses");
		playerStatistics = new PlayerStatistics(result);
	}
}
