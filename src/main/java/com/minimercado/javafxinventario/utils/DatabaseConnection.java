package com.minimercado.javafxinventario.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for managing database connections
 */
public class DatabaseConnection {
    // Database connection properties
    private static final String URL = "jdbc:mysql://localhost:3306/inventory_db";
    private static final String USER = "InventoryDAO";
    private static final String PASSWORD = "2007absalom"; // Change to your MySQL password
    
    // Connection pool configuration
    private static final int MAX_POOL_SIZE = 2000;
    private static int connectionCount = 0;
    
    static {
        try {
            // Load the JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }
    
    /**
     * Gets a connection from the pool
     * @return A database connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (connectionCount >= MAX_POOL_SIZE) {
            throw new SQLException("Connection pool limit reached");
        }
        
        connectionCount++;
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    /**
     * Releases a connection back to the pool
     * @param connection The connection to release
     */
    public static void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                connectionCount--;
            } catch (SQLException e) {
                System.err.println("Error closing connection");
                e.printStackTrace();
            }
        }
    }
}
