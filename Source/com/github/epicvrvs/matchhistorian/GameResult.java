package com.github.epicvrvs.matchhistorian;

import java.util.ArrayList;
import java.util.Date;

class GameResult {
	public final int gameId;
	public final boolean victory;
	public final int championId;
	public final GameSettings gameSettings;
	public final Date date;
	public final PlayerStatistics playerStatistics;
	public final int[] spells;
	public final int[] items;
	public final ArrayList<GamePlayer> losingTeam, winningTeam;
	
	public GameResult(int gameId, boolean victory, int championId, GameSettings gameSettings, Date date, PlayerStatistics playerStatistics, int[] spells, int[] items) {
		this.gameId = gameId;
		this.victory = victory;
		this.championId = championId;
		this.gameSettings = gameSettings;
		this.date = date;
		this.playerStatistics = playerStatistics;
		this.spells = spells;
		this.items = items;
		losingTeam = new ArrayList<>();
		winningTeam = new ArrayList<>();
	}
}
