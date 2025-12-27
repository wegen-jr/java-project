package Database;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationDAO {

    /**
     * Authenticate user by username and password
     * Returns a map with keys: auth_id, username, role, full_name
     */
    public static Map<String, Object> authenticateUser(String username, String password) {
        Map<String, Object> userData = null;

        String authQuery = "SELECT id, username, role, password FROM authentication " +
                "WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(authQuery)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                userData = new HashMap<>();
                int authId = rs.getInt("id");
                String role = rs.getString("role");

                userData.put("auth_id", authId);
                userData.put("username", rs.getString("username"));
                userData.put("role", role);

                // Fetch full name AND specific ID
                String fullName = null;
                switch (role.toUpperCase()) {
                    case "DOCTOR":
                        // 1. Get the Name
                        fullName = getDoctorName(conn, authId);
                        // 2. GET THE DOCTOR_ID (Primary Key from doctors table)
                        int realDoctorId = getDoctorIdByAuthId(conn, authId);
                        userData.put("doctor_id", realDoctorId);
                        break;
                    case "RECEPTIONIST":
                        fullName = getReceptionistName(conn, authId);
                        break;
                    case "ADMIN":
                        fullName = getAdminName(conn, authId);
                        break;
                }

                if (fullName != null) {
                    userData.put("full_name", fullName);
                }
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        return userData;
    }

    // Helper method to get the Primary Key 'doctor_id'
    private static int getDoctorIdByAuthId(Connection conn, int authId) throws SQLException {
        String sql = "SELECT doctor_id FROM doctors WHERE auth_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, authId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("doctor_id");
            }
        }
        return -1;
    }

    // Helper: Get doctor full name
    private static String getDoctorName(Connection conn, int authId) throws SQLException {
        String query = "SELECT first_name, middle_name, last_name FROM doctors WHERE auth_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, authId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("first_name") +
                        (rs.getString("middle_name") != null ? " " + rs.getString("middle_name") : "") +
                        " " + rs.getString("last_name");
            }
        }
        return null;
    }

    // Helper: Get receptionist full name (create receptionists table similarly)
    private static String getReceptionistName(Connection conn, int authId) throws SQLException {
        String query = "SELECT first_name, last_name FROM receptionists WHERE auth_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, authId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("first_name") + " " + rs.getString("last_name");
            }
        }
        return null;
    }

    // Helper: Get admin full name (create admins table if needed)
    private static String getAdminName(Connection conn, int authId) throws SQLException {
        String query = "SELECT first_name, last_name FROM admins WHERE auth_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, authId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("first_name") + " " + rs.getString("last_name");
            }
        }
        return null;
    }

    /**
     * Change password for a user
     */
    public static boolean changePassword(String username, String oldPassword, String newPassword) {
        String query = "UPDATE authentication SET password = ? WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            pstmt.setString(3, oldPassword);

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error changing password: " + e.getMessage());
            return false;
        }
    }
}
