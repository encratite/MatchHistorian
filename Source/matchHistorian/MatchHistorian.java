package matchHistorian;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class MatchHistorian {
	final static String configurationFile = "MatchHistorian.properties";
	
	public MatchHistorian() {
		
	}
	
	static void writeConfiguration() {
		try {
    		Properties properties = new Properties();
    		properties.setProperty("databaseUser", "user");
    		properties.setProperty("database", "match_historian");
    		properties.setProperty("webSocketPort", "81");
    		properties.store(new FileOutputStream(configurationFile), null);
    	}
    	catch (IOException exception) {
    		exception.printStackTrace();
        }
	}
	
	public static void main(String[] arguments) {
		// RegexTest.runTest();
		// SoupTest.runTest();
		// PostgreSQLTest.runTest();
		// TestServer.runTest();
		writeConfiguration();
	}
}
