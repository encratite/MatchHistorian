package matchHistorian;

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
}

class GameResult {
	public int gameId;
	public int summonerId;
	public boolean victory;
	public int championId;
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
}
