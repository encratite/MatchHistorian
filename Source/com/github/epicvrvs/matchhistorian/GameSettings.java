package com.github.epicvrvs.matchhistorian;

import java.sql.ResultSet;
import java.sql.SQLException;

class GameSettings {
	public final Map map;
	public final GameMode gameMode;
	
	public GameSettings(Map map, GameMode gameMode) {
		this.map = map;
		this.gameMode = gameMode;
	}
	
	public GameSettings(ResultSet result) throws SQLException {
		map = Map.valueOf(result.getString("map").toUpperCase());
		gameMode = GameMode.valueOf(result.getString("game_mode").toUpperCase());
	}
	
	public static String getMapString(Map map) {
		if(map == null)
			return null;
		else
			return map.toString().toLowerCase();
	}
	
	public static String getGameModeString(GameMode mode) {
		if(mode == null)
			return null;
		else
			return mode.toString().toLowerCase();
	}
}
