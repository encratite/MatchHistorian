package com.github.epicvrvs.matchhistorian;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerStatistics {
	public final int kills;
	public final int deaths;
	public final int assists;
	public final int gold;
	public final int minionsKilled;
	public final int duration;
	
	PlayerStatistics(int kills, int deaths, int assists, int gold, int minionsKilled, int duration) {
		this.kills = kills;
		this.deaths = deaths;
		this.assists = assists;
		this.gold = gold;
		this.minionsKilled = minionsKilled;
		this.duration = duration;
	}
	
	PlayerStatistics(ResultSet result) throws SQLException {
		kills = result.getInt("kills");
		deaths = result.getInt("deaths");
		assists = result.getInt("assists");
		gold = result.getInt("gold");
		minionsKilled = result.getInt("minions_killed");
		duration = result.getInt("duration");
	}
}
