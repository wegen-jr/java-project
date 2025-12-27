package Database;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
public class AuthenticationDAO {

    public static Map<String, Object> authenticateUser(String username, String password) {
        Map<String, Object> userData = null;
        String query = "SELECT Username, Role, Password FROM authentication WHERE Username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                userData = new HashMap<>();
                userData.put("username", rs.getString("Username"));
                userData.put("role", rs.getString("Role"));
                userData.put("password", rs.getString("Password"));

                // Update last login time
            }

        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }

        return userData;
    }


    public static boolean changePassword(String username, String oldPassword, String newPassword) {
        String query = "UPDATE authentication SET password = ? WHERE Username = ? AND password = ?";

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