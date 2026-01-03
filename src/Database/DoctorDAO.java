package Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.*;

public class DoctorDAO {


    public List<Object[]> getAllPatientsForDoctor(int doctorId) {
        List<Object[]> patientList = new ArrayList<>();

        // Alternative: Get ALL patients from the system (not just those with appointments)
        String sql = "SELECT " +
                "p.patient_id, " +
                "p.first_name, " +
                "p.middle_name, " +
                "p.last_name, " +
                "p.date_of_birth, " +
                "p.gender, " +
                "p.contact_number, " +
                "p.email, " +
                "p.address, " +
                "p.emergency_contact, " +
                "p.blood_type, " +
                "p.registration_date, " +
                "TIMESTAMPDIFF(YEAR, p.date_of_birth, CURDATE()) AS age " +
                "FROM patients p " +
                "ORDER BY p.registration_date DESC";

        try (Connection con = Database.DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                patientList.add(new Object[]{
                        rs.getString("patient_id"),
                        rs.getString("first_name"),
                        rs.getString("middle_name"),
                        rs.getString("last_name"),
                        rs.getDate("date_of_birth") != null ?
                                rs.getDate("date_of_birth").toString() : "",
                        rs.getString("gender"),
                        rs.getString("contact_number"),
                        rs.getString("email") != null ? rs.getString("email") : "",
                        rs.getString("address") != null ? rs.getString("address") : "",
                        rs.getString("emergency_contact") != null ?
                                rs.getString("emergency_contact") : "",
                        rs.getString("blood_type") != null ? rs.getString("blood_type") : "",
                        rs.getDate("registration_date") != null ?
                                rs.getDate("registration_date").toString() : "",
                        rs.getInt("age")
                });
            }
        } catch (SQLException e) {
            System.err.println("Database Error (Patients Dashboard): " + e.getMessage());
            e.printStackTrace();
        }
        return patientList;
    }
    public List<Object[]> getTodayAppointments(int doctorId) {
        List<Object[]> list = new ArrayList<>();
        // Added p.patient_id to the SELECT list
        String sql = "SELECT a.appointment_id, a.appointment_time, " +
                "CONCAT(p.first_name, ' ', p.last_name) AS patient_name, " +
                "a.reason, a.status, p.patient_id " + // Added patient_id here
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "WHERE a.doctor_id = ? AND a.appointment_date = CURDATE() " +
                "AND a.status != 'Completed' " +
                "ORDER BY a.appointment_time ASC";

        try (Connection con = Database.DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, doctorId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("appointment_id"),        // Index 0
                        rs.getTime("appointment_time").toString(), // Index 1
                        rs.getString("patient_name"),       // Index 2
                        rs.getString("reason"),             // Index 3
                        rs.getString("status").toUpperCase(), // Index 4
                        rs.getString("patient_id")          // Index 5 (THE FIX)
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public boolean submitLabRequest(int docId, String patId, String test, String priority, String notes) {
        String sql = "INSERT INTO lab_requests (doctor_id, patient_id, test_type, priority, notes_from_doctor) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Database.DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, docId);
            pst.setString(2, patId);
            pst.setString(3, test);
            pst.setString(4, priority);
            pst.setString(5, notes);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Object[]> getLabResultsForDoctor(int doctorId) {
        List<Object[]> results = new ArrayList<>();
        // Using COALESCE to prevent the NullPointerException on names
        String sql = "SELECT l.request_id, " +
                "CONCAT(COALESCE(p.first_name,''), ' ', COALESCE(p.last_name,'')) AS patient_name, " +
                "l.test_type, l.priority, l.status, l.result_details " +
                "FROM lab_requests l " +
                "LEFT JOIN patients p ON l.patient_id = p.patient_id " +
                "WHERE l.doctor_id = ? " +
                "ORDER BY l.request_id DESC";

        try (Connection con = Database.DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, doctorId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                results.add(new Object[]{
                        rs.getInt("request_id"),
                        rs.getString("patient_name"),
                        rs.getString("test_type"),
                        rs.getString("priority"),
                        rs.getString("status"),
                        rs.getString("result_details") // This might be NULL initially
                });
            }
        } catch (SQLException e) {
            System.err.println("Lab DAO Error: " + e.getMessage());
        }
        return results;
    }

    // --- 4. CLINICAL PROFILE DATA (Uses INT) ---
    public Map<String, String> getDoctorProfileData(int doctorId) {
        Map<String, String> data = new HashMap<>();
        String sql = "SELECT * FROM doctors WHERE doctor_id = ?";

        try (Connection con = Database.DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, doctorId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                data.put("first_name", rs.getString("first_name"));
                data.put("last_name", rs.getString("last_name"));
                data.put("specialization", rs.getString("specialization"));
                data.put("department", rs.getString("department"));
                data.put("qualification", rs.getString("qualification"));
                data.put("license_number", rs.getString("license_number"));
                data.put("contact_number", rs.getString("contact_number"));
                data.put("email", rs.getString("email"));
                data.put("working_hours", rs.getString("working_hours"));
                data.put("availability", rs.getString("availability"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return data;
    }

    // --- 5. PRESCRIPTIONS & RECORDS (Uses INT) ---
    public boolean submitPrescriptionAndRecord(String pId, int dId, String med, String dose, String instr, String diag) {
        Connection con = null;
        // Try using 'prescriptions' (plural) or check your DB for the exact name
        String sqlPresc = "INSERT INTO prescriptions (patient_id, doctor_id, medication_name, dosage, instructions) VALUES (?, ?, ?, ?, ?)";
        String sqlRecord = "INSERT INTO medical_records (patient_id, doctor_id, visit_date, diagnosis, prescription, notes) VALUES (?, ?, CURDATE(), ?, ?, ?)";

        try {
            con = Database.DatabaseConnection.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement pst1 = con.prepareStatement(sqlPresc)) {
                pst1.setString(1, pId);
                pst1.setInt(2, dId);
                pst1.setString(3, med);
                pst1.setString(4, dose);
                pst1.setString(5, instr);
                pst1.executeUpdate();
            }

            try (PreparedStatement pst2 = con.prepareStatement(sqlRecord)) {
                pst2.setString(1, pId);
                pst2.setInt(2, dId);
                pst2.setString(3, diag);
                pst2.setString(4, med + " [" + dose + "]");
                pst2.setString(5, instr);
                pst2.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) { try { con.rollback(); } catch (SQLException ex) {} }
            System.err.println("CRITICAL DB ERROR: " + e.getMessage());
            return false;
        }
    }
    // --- 6. UTILITY METHODS ---
    public List<String> getDoctorPatientList(int doctorId) {
        List<String> patients = new ArrayList<>();
        // Use CONCAT to merge the name parts so the UI can display them correctly
        String sql = "SELECT DISTINCT p.patient_id, " +
                "CONCAT(p.first_name, ' ', p.middle_name, ' ', p.last_name) AS full_name " +
                "FROM patients p " +
                "JOIN appointments a ON p.patient_id = a.patient_id " +
                "WHERE a.doctor_id = ?";

        try (Connection con = Database.DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, doctorId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                // This format matches what your Prescription Dashboard split logic expects
                patients.add(rs.getString("patient_id") + " - " + rs.getString("full_name"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching Patient List for Dropdown: " + e.getMessage());
        }
        return patients;
    }

    public boolean updateAppointmentStatus(int apptId, String status) {
        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
        try (Connection con = Database.DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, status);
            pst.setInt(2, apptId);
            int affectedRows = pst.executeUpdate();
            return affectedRows > 0; // Returns true if the record was actually found and updated
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public Map<String, String> getDoctorByAuthId(int authId) {
        Map<String, String> doctorMap = new HashMap<>();
        String sql = "SELECT * FROM doctors WHERE auth_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, authId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                doctorMap.put("doctor_id", String.valueOf(rs.getInt("doctor_id")));
                doctorMap.put("first_name", rs.getString("first_name"));
                doctorMap.put("middle_name", rs.getString("middle_name"));
                doctorMap.put("last_name", rs.getString("last_name"));
                doctorMap.put("specialization", rs.getString("specialization"));
                doctorMap.put("contact_number", rs.getString("contact_number"));
                doctorMap.put("email", rs.getString("email"));
                // ... add other fields if needed
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doctorMap;
    }
    public String getNextLabRequestId() {
        String nextId = "1";
        // Checks the next increment value for the lab_requests table
        String sql = "SELECT AUTO_INCREMENT FROM information_schema.TABLES " +
                "WHERE TABLE_SCHEMA = 'HMS' AND TABLE_NAME = 'lab_requests'";
        try (Connection con = Database.DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) nextId = String.valueOf(rs.getInt(1));
        } catch (Exception e) { e.printStackTrace(); }
        return nextId;
    }
    public int getPendingLabsCount(int docId) {
        String sql = "SELECT COUNT(*) FROM lab_requests WHERE doctor_id = ? AND status = 'Pending'";
        try (Connection con = Database.DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, docId);
            ResultSet rs = pst.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { return 0; }
    }

    public int getTodayAppointmentCount(int doctorId) {
        int count = 0;
        // This query counts everything EXCEPT 'Completed' and 'Cancelled'
        String sql = "SELECT COUNT(*) FROM appointments " +
                "WHERE doctor_id = ? " +
                "AND appointment_date = CURDATE() " +
                "AND status NOT IN ('Completed', 'Cancelled')";

        try (Connection con = Database.DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, doctorId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public int getTotalPatientCount(int docId) {
        String sql = "SELECT COUNT(DISTINCT patient_id) FROM appointments WHERE doctor_id = ?";
        try (Connection con = Database.DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, docId);
            ResultSet rs = pst.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { return 0; }
    }
    public List<Object[]> getFullClinicalSchedule(int doctorId) {
        List<Object[]> schedule = new ArrayList<>();

        // We use CONCAT to merge the name parts from the patients table
        String sql = "SELECT a.appointment_time, " +
                "CONCAT(p.first_name, ' ', p.middle_name, ' ', p.last_name) AS patient_name, " +
                "a.status, a.reason " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "WHERE a.doctor_id = ? AND a.appointment_date = CURDATE() " +
                "ORDER BY a.appointment_time ASC";

        try (Connection con = Database.DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, doctorId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                schedule.add(new Object[]{
                        rs.getTime("appointment_time").toString(),
                        rs.getString("patient_name"), // Matches the CONCAT alias
                        rs.getString("status").toUpperCase(),
                        rs.getString("reason") != null ? rs.getString("reason") : "No notes"
                });
            }
        } catch (SQLException e) {
            System.err.println("Schedule Error: " + e.getMessage());
        }
        return schedule;
    }
    // 1. Get the next ID for display purposes
    public String getNextPrescriptionId() {
        String nextId = "1";
        // Checks the next increment value for the prescriptions table
        String sql = "SELECT AUTO_INCREMENT FROM information_schema.TABLES " +
                "WHERE TABLE_SCHEMA = 'HMS' AND TABLE_NAME = 'prescriptions'";
        try (Connection con = Database.DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) nextId = String.valueOf(rs.getInt(1));
        } catch (Exception e) { e.printStackTrace(); }
        return nextId;
    }

    // 2. Submit both the Prescription and the Medical Record update

}