package matchHistorian;

import java.sql.Array;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;

enum Map {
	TWISTED_TREELINE,
	SUMMONERS_RIFT,
	THE_CRYSTAL_SCAR,
	HOWLING_ABYSS,
}

enum GameMode {
	NORMAL,
	RANKED_SOLO,
	RANKED_TEAM,
	CUSTOM,
	COOP_VS_AI,
}

class GameResult {
	public int gameId;
	public int summonerId;
	public boolean victory;
	public int championId;
	public boolean mapUnknown; 
	public Map map;
	public GameMode mode;
	public Date date;
	public int duration;
	public int kills;
	public int deaths;
	public int assists;
	public int gold;
	public int minionsKilled;
	public int[] spells;
	public int[] items;
	ArrayList<GamePlayer> losingTeam, winningTeam;
	
	public static String getMapString(Map map) {
		return map.toString().toLowerCase();
	}
	
	public static String getGameModeString(GameMode mode) {
		return mode.toString().toLowerCase();
	}
	
	public static Array getTeamIds(Connection database, ArrayList<GamePlayer> team) throws Exception {
		Integer[] ids = new Integer[team.size()];
		for(int i = 0; i < team.size(); i++)
			ids[i] = team.get(i).summonerId;
		return database.createArrayOf("int", ids);
	}
}
