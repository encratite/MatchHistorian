package com.github.epicvrvs.matchhistorian;

import java.util.ArrayList;

public class SummonerWebData {
	public final String region;
	public final String name;
	public final int summonerId;
	public final ArrayList<GameResult> games;
	
	public SummonerWebData(String region, String name, int summonerId, ArrayList<GameResult> games) {
		this.region = region;
		this.name = name;
		this.summonerId = summonerId;
		this.games = games;
	}
}
