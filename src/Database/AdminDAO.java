package Database;

import javax.swing.*;
import java.sql.*;
import java.util.Vector;
import java.util.Map;
import java.util.StringJoiner;

public class AdminDAO {

    // 1. READ: Fetch any table's data and column names dynamically
    public static void loadTableData(String tableName, Vector<Vector<Object>> data, Vector<String> columnNames) {
        data.clear();
        columnNames.clear();
        String sql = tableName.equals("authentication") ? "SELECT * FROM " + tableName + " WHERE role != 'ADMIN'" : "SELECT * FROM " + tableName;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i));
            }

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getObject(i));
                }
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean createStaffWithAuth(Map<String, String> authData, Map<String, String> staffData, String role, String tableName) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start Transaction

            // 1. Insert into Authentication
            String authSql = "INSERT INTO authentication (username, password, role) VALUES (?, ?, ?)";
            int authId = -1;
            try (PreparedStatement ps = conn.prepareStatement(authSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, authData.get("username"));
                ps.setString(2, authData.get("password"));
                ps.setString(2, role);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) authId = rs.getInt(1);
            }

            // 2. Insert into Doctors using the new authId
            StringBuilder cols = new StringBuilder("auth_id, ");
            StringBuilder params = new StringBuilder("?, ");
            for (String key : staffData.keySet()) {
                cols.append(key).append(", ");
                params.append("?, ");
            }
            // Remove trailing commas
            String sql = "INSERT INTO " + tableName  + " (" + cols.substring(0, cols.length()-2) +
                    ") VALUES (" + params.substring(0, params.length()-2) + ")";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, authId);
                int idx = 2;
                for (String val : staffData.values()) {
                    ps.setString(idx++, val);
                }
                ps.executeUpdate();
            }

            conn.commit(); // Save changes
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            return false;
        }
    }

    public static boolean createRecord(String tableName, Map<String, Object> data) {
        if (data == null || data.isEmpty()) return false;

        // Use StringJoiner to build the comma-separated lists for columns and placeholders
        StringJoiner columns = new StringJoiner(", ");
        StringJoiner placeholders = new StringJoiner(", ");

        for (String key : data.keySet()) {
            columns.add(key);
            placeholders.add("?");
        }

        String sql = "INSERT INTO " + tableName + " (" + columns.toString() + ") VALUES (" + placeholders.toString() + ")";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int index = 1;
            for (Object value : data.values()) {
                pstmt.setObject(index++, value);
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Insert Error in table " + tableName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateRecord(String tableName, String pkColumn, Object pkValue, String columnName, Object newValue) {
        // We use a dynamic query: UPDATE table SET column = value WHERE id = value
        String sql = "UPDATE " + tableName + " SET " + columnName + " = ? WHERE " + pkColumn + " = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, newValue);
            pstmt.setObject(2, pkValue);

            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Update Failed: " + e.getMessage());
            return false;
        }
    }

    // 2. DELETE: Generic delete using the Primary Key
    public static boolean deleteRecord(String tableName, String pkColumn, Object pkValue) {
        String sql = "DELETE FROM " + tableName + " WHERE " + pkColumn + " = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, pkValue);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. CREATE/UPDATE: Execute a raw SQL string (generated by the UI)
    public static boolean executeUpdate(String sql) {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql) > 0;
        } catch (SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "SQL Error: " + e.getMessage());
            return false;
        }
    }
}