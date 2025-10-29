package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database configuration and connection management
 * MODIFY THESE VALUES to match your PostgreSQL setup
 */
public class DatabaseConfig {
    
    // ============ CONFIGURATION - CHANGE THESE VALUES ============
    
    // Database connection details
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/banking_system";
    private static final String DB_USER = "postgres";  // Change to your PostgreSQL username
    private static final String DB_PASSWORD = "postgres";  // Change to your PostgreSQL password
    
    // Connection pool settings (optional but recommended)
    private static final int MAX_CONNECTIONS = 10;
    
    // ============ CONNECTION METHODS ============
    
    /**
     * Get a connection to the database
     * This is used by all database operations
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load PostgreSQL JDBC driver
            Class.forName("org.postgresql.Driver");
            
            // Return connection
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found. Add it to your classpath.", e);
        }
    }
    
    /**
     * Test if database connection works
     * Call this when your program starts to verify setup
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Successfully connected to database: " + DB_URL);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            System.err.println("Error: " + e.getMessage());
            System.err.println("\nTroubleshooting:");
            System.err.println("1. Make sure PostgreSQL is running");
            System.err.println("2. Verify database 'banking_system' exists");
            System.err.println("3. Check username and password in DatabaseConfig.java");
            System.err.println("4. Confirm PostgreSQL is listening on port 5432");
        }
        return false;
    }
    
    /**
     * Close a connection safely
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Get database URL (for reference)
     */
    public static String getDatabaseUrl() {
        return DB_URL;
    }
    
    /**
     * Quick test main method
     */
    public static void main(String[] args) {
        System.out.println("Testing database connection...\n");
        if (testConnection()) {
            System.out.println("\n Database is ready to use!");
        } else {
            System.out.println("\n Please fix the connection issues above.");
        }
    }
}
