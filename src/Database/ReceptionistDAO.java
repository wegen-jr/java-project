package Database;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ReceptionistDAO {

    // CHANGED: Now accepts 'int authId' (e.g., 1, 5, 20)
    public static Map<String, String> getReceptionistProfile(int authId) {
        Map<String, String> data = new HashMap<>();

        // FASTER SQL: No need to JOIN 'authentication' table.
        // We just look for the row in 'receptionists' where auth_id matches.
        String sql = "SELECT receptionist_id, first_name, last_name, email, contact_number " +
                "FROM receptionists " +
                "WHERE auth_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, authId); // Set the ID
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String fullName = rs.getString("first_name") + " " + rs.getString("last_name");

                data.put("name", fullName);
                data.put("email", rs.getString("email") != null ? rs.getString("email") : "N/A");
                data.put("phone", rs.getString("contact_number") != null ? rs.getString("contact_number") : "N/A");
                data.put("id", String.valueOf(rs.getInt("receptionist_id")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
}