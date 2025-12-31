package Database;
import java.sql.*;
import java.util.Properties;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/HMS";
    private static final String USERNAME = Constants.USERNAME;
    private static final String PASSWORD = Constants.PASSWORD;
    private static Connection connection = null;
    private static boolean isInitialized = false;

    // Static initializer to run setup once
//    static {
//        initializeDatabase();
//    }

    private DatabaseConnection() {
        // Private constructor to prevent instantiation
    }

    private static void initializeDatabase() {
        if (!isInitialized) {
            try {
                // Initialize database structure
                DatabaseSetup.initializeDatabase();
                isInitialized = true;
                System.out.println("✅ Database initialization complete");
            } catch (Exception e) {
                System.err.println("❌ Failed to initialize database: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Load MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Create connection properties
                Properties properties = new Properties();
                properties.setProperty("user", USERNAME);
                properties.setProperty("password", PASSWORD);
                properties.setProperty("useSSL", "false");
                properties.setProperty("serverTimezone", "UTC");
                properties.setProperty("allowPublicKeyRetrieval", "true");
                properties.setProperty("autoReconnect", "true");

                // Establish connection
                connection = DriverManager.getConnection(URL, properties);

                // Test connection
                if (connection.isValid(2)) {
                    System.out.println("✅ Database connection established");
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found!");
            System.err.println("Please add mysql-connector-java-8.x.x.jar to your classpath");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed!");
            System.err.println("URL: " + URL);
            System.err.println("Username: " + USERNAME);
            System.err.println("Error: " + e.getMessage());

            // Try to create database if it doesn't exist
            if (e.getMessage().contains("Unknown database")) {
                System.out.println("🔄 Attempting to create database...");
                createDatabaseIfNotExists();
                return getConnection(); // Retry
            }
        }
        return connection;
    }

    private static void createDatabaseIfNotExists() {
        String baseUrl = "jdbc:mysql://localhost:3306/";

        try (Connection tempConn = DriverManager.getConnection(baseUrl, USERNAME, PASSWORD);
             Statement stmt = tempConn.createStatement()) {

            // Create database
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS HMS");
            System.out.println("✅ Database 'HMS' created");

        } catch (SQLException e) {
            System.err.println("❌ Failed to create database: " + e.getMessage());
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
                System.out.println("✅ Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Database connection test successful");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Database connection test failed");
            System.err.println("Error: " + e.getMessage());
        }
        return false;
    }

    public static void executeUpdate(String query) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
        }
    }

    public static ResultSet executeQuery(String query) throws SQLException {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }

    // Helper method for prepared statements
    public static PreparedStatement prepareStatement(String query) throws SQLException {
        Connection conn = getConnection();
        return conn.prepareStatement(query);
    }

    // Transaction support
    public static void beginTransaction() throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
    }

    public static void commitTransaction() throws SQLException {
        Connection conn = getConnection();
        conn.commit();
        conn.setAutoCommit(true);
    }

    public static void rollbackTransaction() throws SQLException {
        Connection conn = getConnection();
        conn.rollback();
        conn.setAutoCommit(true);
    }

    // Check if table exists
    public static boolean tableExists(String tableName) {
        String query = "SELECT COUNT(*) as count FROM information_schema.tables " +
                "WHERE table_schema = 'HMS' AND table_name = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, tableName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count") > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error checking table existence: " + e.getMessage());
        }
        return false;
    }

    // Get database metadata
    public static void printDatabaseInfo() {
        try (Connection conn = getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            System.out.println("\n📊 Database Information:");
            System.out.println("Database: " + metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());
            System.out.println("Driver: " + metaData.getDriverName() + " " + metaData.getDriverVersion());
            System.out.println("URL: " + metaData.getURL());
            System.out.println("User: " + metaData.getUserName());

            // List tables
            ResultSet tables = metaData.getTables("HMS", null, "%", new String[]{"TABLE"});
            System.out.println("\n📋 Tables in HMS database:");
            while (tables.next()) {
                System.out.println("  - " + tables.getString("TABLE_NAME"));
            }

        } catch (SQLException e) {
            System.err.println("Error getting database info: " + e.getMessage());
        }
    }
}