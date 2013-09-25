package com.github.epicvrvs.matchhistorian;

class GamePlayer {
	public final String summonerName;
	public final int summonerId;
	public final int championId;
	
	public GamePlayer(String summonerName, int summonerId, int championId) {
		this.summonerName = summonerName;
		this.summonerId = summonerId;
		this.championId = championId;
	}
}
