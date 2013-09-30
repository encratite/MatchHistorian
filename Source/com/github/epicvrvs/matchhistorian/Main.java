package com.github.epicvrvs.matchhistorian;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Main {
	private final static String configurationFile = "MatchHistorian.properties";
	
	public static void main(String[] arguments) {
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream(configurationFile));
			String databaseURL = properties.getProperty("databaseURL");
			String databaseUser = properties.getProperty("databaseUser");
			int webSocketPort = Integer.parseInt(properties.getProperty("webSocketPort"));
			
			Properties databaseProperties = new Properties();
			databaseProperties.setProperty("user", databaseUser);
			try(Connection database = DriverManager.getConnection(databaseURL, databaseProperties)) {
				MatchHistorian historian = new MatchHistorian(database);
			}
		}
		catch(Exception exception) {
			exception.printStackTrace();
		}
	}
}
