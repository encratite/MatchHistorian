package matchHistorian;

import java.sql.*;
import java.util.Properties;

class PostgreSQLTest {
	public static void runTest() {
		try {
			final String url = "jdbc:postgresql://localhost/match_historian";
			Properties properties = new Properties();
			properties.setProperty("user", "void");
			Connection connection = DriverManager.getConnection(url, properties);
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery("select 'string' as string, 1 as integer");
			while (result.next()) {
				System.out.println("Integer: " + result.getInt("integer"));
			    System.out.println("String: " + result.getString("string"));
			}
			result.close();
			statement.close();
		}
		catch(SQLException exception) {
			exception.printStackTrace();
		}
	}
}
