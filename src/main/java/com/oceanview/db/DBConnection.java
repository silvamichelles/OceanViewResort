package com.oceanview.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // 1. Extract constants. Better yet, load these from environment variables or a .properties file!
private static final String URL = "jdbc:mysql://localhost:3306/OceanViewDB";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // 2. Use 'volatile' to ensure thread visibility
    private static volatile Connection connection;

    private DBConnection() {
        // Prevent instantiation
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            // 3. Synchronized block for thread safety (Double-checked locking)
            synchronized (DBConnection.class) {
                if (connection == null || connection.isClosed()) {
                    try {
                        // Note: Class.forName is technically optional in modern JDBC (4.0+), 
                        // but safe to keep for backwards compatibility.
                        Class.forName("com.mysql.cj.jdbc.Driver");
                        connection = DriverManager.getConnection(URL, USER, PASSWORD);
                        System.out.println("Database Connected Successfully!");
                    } catch (ClassNotFoundException e) {
                        // 4. Properly wrap and throw the exception so the app knows it failed
                        throw new SQLException("MySQL JDBC Driver not found.", e);
                    }
                }
            }
        }
        return connection;
    }
}