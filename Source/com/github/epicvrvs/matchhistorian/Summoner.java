package com.github.epicvrvs.matchhistorian;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * General information about a summoner, including aggregated statistics, as stored in the database.
 * This class is not initially used while the web content of a summoner is still being processed.
 */
class Summoner {
	// Identifier of the summoner in the database
	int id;
	// String that identifies the region the summoner resides on
	String region;
	// Identifier of the summoner on the server of that region
	int summonerId;
	// Name of the summoner
	String name;
	// Flag that determines if a summoner is currently being updated automatically
	boolean updateAutomatically;
	// All aggregated statistics available for this summoner
	ArrayList<AggregatedStatistics> aggregatedStatistics;
	
	public Summoner(ResultSet result) throws SQLException {
		id = result.getInt("id");
		region = result.getString("region");
		summonerId = result.getInt("summoner_id");
		name = result.getString("name");
		updateAutomatically = result.getBoolean("update_automatically");
	}
}
