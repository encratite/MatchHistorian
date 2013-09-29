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
	public final int id;
	// String that identifies the region the summoner resides on
	public final String region;
	// Identifier of the summoner on the server of that region
	public final int summonerId;
	// Name of the summoner
	public final String name;
	// Flag that determines if a summoner is currently being updated automatically
	public final boolean updateAutomatically;
	// All aggregated statistics available for this summoner, not loaded by the constructor
	public final ArrayList<AggregatedStatistics> aggregatedStatistics;
	
	public Summoner(ResultSet result) throws SQLException {
		id = result.getInt("id");
		region = result.getString("region");
		summonerId = result.getInt("summoner_id");
		name = result.getString("name");
		updateAutomatically = result.getBoolean("update_automatically");
		aggregatedStatistics = new ArrayList<>();
	}
}
