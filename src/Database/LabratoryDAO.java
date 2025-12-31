package Database;

import java.sql.*;
import java.util.*;

public class LabratoryDAO {

    public static Map<String, Integer> getWorkStats() {
        Map<String, Integer> stats = new HashMap<>();
        // Ensure keys exist so UI doesn't crash
        stats.put("pending", 0);
        stats.put("completed", 0);

        String sql = "SELECT status, COUNT(*) as total FROM lab_requests GROUP BY status";

        try (Connection conn = DatabaseConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {

            while (rs.next()) {
                String dbStatus = rs.getString("status");
                int total = rs.getInt("total");

                // Compare ignoring case to match your ENUM ('Pending', 'Completed')
                if ("Pending".equalsIgnoreCase(dbStatus)) {
                    stats.put("pending", total);
                } else if ("Completed".equalsIgnoreCase(dbStatus)) {
                    stats.put("completed", total);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return stats;
    }
    public static List<Object[]> getPendingRequests() {
        List<Object[]> list = new ArrayList<>();

        // Exact SQL based on your SQL Dump
        // notes_from_doctor is lowercase in your schema
        String sql = "SELECT lr.request_id, " +
                "CONCAT(p.first_name, ' ', p.last_name) AS p_name, " +
                "lr.test_type, " +
                "lr.notes_from_doctor, " +
                "lr.priority, " +
                "lr.status " +
                "FROM lab_requests lr " +
                "JOIN patients p ON lr.patient_id = p.patient_id " +
                "WHERE lr.status = 'Pending' " +
                "ORDER BY lr.request_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("request_id"),           // Index 0
                        rs.getString("p_name"),            // Index 1
                        rs.getString("test_type"),         // Index 2
                        rs.getString("notes_from_doctor"), // Index 3 (lowercase as per your dump)
                        rs.getString("priority"),          // Index 4
                        rs.getString("status")             // Index 5
                });
            }
        } catch (SQLException e) {
            System.err.println("Database sync error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
    public static Map<String, String> getTechnicianProfile(int authId) {
        Map<String, String> data = new HashMap<>();
        String sql = "SELECT lt.labtechnician_id, lt.first_name, lt.middle_name, lt.last_name, " +
                "lt.contact_number, lt.department, lt.email, lt.qualification, lt.job_title, " +
                "a.username FROM laboratory_technician lt " +
                "JOIN authentication a ON lt.auth_id = a.id " +
                "WHERE lt.auth_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, authId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String fName = rs.getString("first_name");
                String lName = rs.getString("last_name");
                String mid = rs.getString("middle_name");
                String combined = fName + " " + (mid != null ? mid + " " : "") + lName;
                data.put("full_name", combined.trim());
                data.put("id", rs.getString("labtechnician_id"));
                data.put("username", rs.getString("username"));
                data.put("phone", rs.getString("contact_number"));
                data.put("department", rs.getString("department"));
                data.put("email", rs.getString("email"));
                data.put("license", rs.getString("qualification"));
                data.put("job_title", rs.getString("job_title"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return data;
    }
    public static List<Object[]> getFullLabArchive() {
        List<Object[]> list = new ArrayList<>();
        // MATCHING YOUR SQL DUMP: 'test_type' and 'request_date'
        String sql = "SELECT lr.request_id, " +
                "CONCAT(p.first_name, ' ', p.last_name) AS patient_name, " +
                "lr.test_type, lr.status, lr.request_date " +
                "FROM lab_requests lr " +
                "JOIN patients p ON lr.patient_id = p.patient_id " +
                "ORDER BY lr.request_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("request_id"),      // Column: ID
                        rs.getString("patient_name"), // Column: PATIENT
                        rs.getString("test_type"),    // Column: TEST
                        rs.getString("status"),       // Column: STATUS
                        rs.getTimestamp("request_date") // Column: DATE
                });
            }
        } catch (SQLException e) {
            System.err.println("❌ History Data Error: " + e.getMessage());
        }
        return list;
    }
    public static boolean submitLabResult(String reqID, String finalResultDetails) {
        // Updating 'result_details' triggers your DB trigger to set status to 'Completed'
        String sql = "UPDATE lab_requests SET result_details = ? WHERE request_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, finalResultDetails);
            pstmt.setString(2, reqID);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}