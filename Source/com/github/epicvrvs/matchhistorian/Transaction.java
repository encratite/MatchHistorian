package com.github.epicvrvs.matchhistorian;

import java.sql.Connection;
import java.sql.SQLException;

public class Transaction implements AutoCloseable {
	private Connection database;
	
	public Transaction(Connection database) throws SQLException {
		this.database = database;
		database.setAutoCommit(false);
	}
	
	public void close() throws SQLException {
		database.commit();
		database.setAutoCommit(true);
	}
}
