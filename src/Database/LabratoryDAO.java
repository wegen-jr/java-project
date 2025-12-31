package Database;

import java.sql.*;
import java.util.*;

public class LabratoryDAO {

    // 1. Fetch Full Name for Greeting
    public static String getFullNameByUsername(String username) {
        // FIXED: name corrected to laboratory_technician
        String sql = "SELECT full_name FROM laboratory_technician WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("full_name");
        } catch (SQLException e) { e.printStackTrace(); }
        return "Staff Member";
    }

    // 2. Fetch Work Statistics
    public static Map<String, Integer> getWorkStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("pending", 0);
        stats.put("completed", 0);

        String sql = "SELECT status, COUNT(*) as count FROM lab_requests GROUP BY status";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String status = rs.getString("status").toLowerCase();
                if (status.contains("pending")) stats.put("pending", rs.getInt("count"));
                else if (status.contains("completed")) stats.put("completed", rs.getInt("count"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return stats;
    }

    // 3. Fetch Pending Requests
    public static List<Object[]> getPendingRequests() {
        List<Object[]> list = new ArrayList<>();
        // We JOIN with patients table to get the full_name using patient_id
        String sql = "SELECT r.request_id, p.full_name, r.test_type, r.notes_from_doctor " +
                "FROM lab_requests r " +
                "JOIN patients p ON r.patient_id = p.patient_id " +
                "WHERE r.status = 'Pending' " +
                "ORDER BY r.request_date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Object[]{
                        rs.getString("request_id"),       // Index 0
                        rs.getString("full_name"),        // Index 1 (from patients table)
                        rs.getString("test_type"),        // Index 2
                        rs.getString("notes_from_doctor") // Index 3
                });
            }
        } catch (SQLException e) {
            System.err.println("❌ Fetch Error: " + e.getMessage());
        }
        return list;
    }

    // 5. Get Technician Profile Data
    public static Map<String, String> getTechnicianProfile(String username) {
        Map<String, String> data = new HashMap<>();
        // FIXED: Changed labtechnician-id to labtechnician_id and fixed table name typo
        String sql = "SELECT full_name, labtechnician_id, phone, job_title, department, email, qualification FROM laboratory_technician WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                data.put("full_name", rs.getString("full_name"));
                data.put("id", rs.getString("labtechnician_id"));
                data.put("phone", rs.getString("phone"));
                data.put("department", rs.getString("department"));
                data.put("email", rs.getString("email"));
                data.put("license", rs.getString("qualification")); // Mapping qualification to license box
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return data;
    }

    // 6. Real-Time Queue
    public static List<Object[]> getAllRecentRequests() {
        List<Object[]> list = new ArrayList<>();
        // JOIN allows us to get the Patient Name even though the lab_requests only has ID
        String sql = "SELECT r.request_id, p.full_name, r.test_type, r.priority, r.status " +
                "FROM lab_requests r " +
                "JOIN patients p ON r.patient_id = p.patient_id " +
                "ORDER BY r.request_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{
                        rs.getString("request_id"),
                        rs.getString("full_name"),
                        rs.getString("test_type"),
                        rs.getString("priority"),
                        rs.getString("status")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    // 7. Lab Archive
    public static List<Object[]> getFullLabArchive() {
        List<Object[]> list = new ArrayList<>();
        // JOIN is required to get the name from the patients table
        String sql = "SELECT r.request_id, p.full_name, r.test_type, r.result_details, r.request_date " +
                "FROM lab_requests r " +
                "JOIN patients p ON r.patient_id = p.patient_id " +
                "WHERE r.status = 'Completed' " +
                "ORDER BY r.request_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Object[]{
                        rs.getString("request_id"),
                        rs.getString("full_name"),    // Successfully fetched via JOIN
                        rs.getString("test_type"),
                        rs.getString("result_details"),
                        rs.getTimestamp("request_date")
                });
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error in Archive: " + e.getMessage());
        }
        return list;
    }

    public static boolean submitLabResult(String reqID, String finalResultDetails) {

        return false;
    }
}