package Database;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.*;

public class PatientDAO {

    // Add new patient
    public static boolean addPatient(String patientId, String firstName,String middleName,String lastName, String dateOfBirth,
                                     String gender, String contact, String email,
                                     String address, String emergencyContact, String bloodType) {
        String query = "INSERT INTO patients (patient_id, first_name,middle_name,last_name, date_of_birth, gender, " +
                "contact_number, email, address, emergency_contact, blood_type, registration_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURDATE())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, patientId);
            pstmt.setString(2, firstName);
            pstmt.setString(2, middleName);
            pstmt.setString(2, lastName);
            pstmt.setDate(3, Date.valueOf(dateOfBirth));
            pstmt.setString(4, gender);
            pstmt.setString(5, contact);
            pstmt.setString(6, email);
            pstmt.setString(7, address);
            pstmt.setString(8, emergencyContact);
            pstmt.setString(9, bloodType);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error adding patient: " + e.getMessage());
            return false;
        }
    }

    // Get all patients
    public static List<Map<String, Object>> getAllPatients() {
        List<Map<String, Object>> patients = new ArrayList<>();
        String query = "SELECT * FROM patients ORDER BY first_name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> patient = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    patient.put(metaData.getColumnName(i), rs.getObject(i));
                }
                patients.add(patient);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching patients: " + e.getMessage());
        }
        return patients;
    }

    // Search patients
    public static List<Map<String, Object>> searchPatients(String searchTerm) {
        List<Map<String, Object>> patients = new ArrayList<>();
        String query = "SELECT * FROM patients WHERE patient_id LIKE ? OR first_name LIKE ? " +
                "OR contact_number LIKE ? OR email LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            String likeTerm = "%" + searchTerm + "%";
            pstmt.setString(1, likeTerm);
            pstmt.setString(2, likeTerm);
            pstmt.setString(3, likeTerm);
            pstmt.setString(4, likeTerm);

            ResultSet rs = pstmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> patient = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    patient.put(metaData.getColumnName(i), rs.getObject(i));
                }
                patients.add(patient);
            }

        } catch (SQLException e) {
            System.err.println("Error searching patients: " + e.getMessage());
        }
        return patients;
    }

    // Get patient by ID
    public static Map<String, Object> getPatientById(String patientId) {
        String query = "SELECT * FROM patients WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> patient = new HashMap<>();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    patient.put(metaData.getColumnName(i), rs.getObject(i));
                }
                return patient;
            }

        } catch (SQLException e) {
            System.err.println("Error getting patient: " + e.getMessage());
        }
        return null;
    }
    public static String getLastId() {
        String query = "SELECT patient_id FROM patients ORDER BY patient_id DESC LIMIT 1";
        String lastId = null; // default if no record found

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                lastId = rs.getString("patient_id");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        return lastId;
    }
    public static boolean isPatientIdExists(String patientId) {
        String query = "SELECT 1 FROM patients WHERE patient_id = ? LIMIT 1";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, patientId);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return true;   // ID exists
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        return false;  // ID does not exist
    }

    // Update patient
    public static boolean updatePatient(Map<String, Object> patientData) {
        String sql = "UPDATE patients SET first_name = ?, middle_name = ?, last_name = ?, gender = ?, date_of_birth = ?, contact_number = ?, email = ?, address = ? WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, (String) patientData.getOrDefault("first_name", ""));
            pstmt.setString(2, (String) patientData.getOrDefault("middle_name", ""));
            pstmt.setString(3, (String) patientData.getOrDefault("last_name", ""));
            pstmt.setString(4, (String) patientData.getOrDefault("gender", ""));

            // Handle date_of_birth
            Object dobObj = patientData.get("date_of_birth"); // note key matches column name
            if (dobObj instanceof LocalDate) {
                pstmt.setDate(5, Date.valueOf((LocalDate) dobObj));
            } else if (dobObj instanceof String) {
                try {
                    pstmt.setDate(5, Date.valueOf(LocalDate.parse((String) dobObj)));
                } catch (DateTimeParseException ex) {
                    pstmt.setDate(5, null);
                }
            } else {
                pstmt.setDate(5, null);
            }

            pstmt.setString(6, (String) patientData.getOrDefault("contact_number", ""));
            pstmt.setString(7, (String) patientData.getOrDefault("email", ""));
            pstmt.setString(8, (String) patientData.getOrDefault("address", ""));
            pstmt.setString(9, (String) patientData.get("patient_id"));

            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            System.err.println("Error updating patient: " + e.getMessage());
            return false;
        }
    }
    // 1. Fetch Patient Basic Info
    public static String[] getPatientProfile(String id) {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                LocalDate dob = rs.getDate("date_of_birth").toLocalDate();
                int age = Period.between(dob, LocalDate.now()).getYears();

                return new String[] {
                        rs.getString("first_name") + " " + rs.getString("middle_name"),
                        rs.getString("contact_number"),
                        String.valueOf(age),          // ✅ AGE instead of DOB
                        rs.getString("gender"),
                        rs.getString("address")
                };
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Patient not found
    }

    // 2. Fetch Patient History (Appointments)
    public static void loadPatientHistory(String patientId, DefaultTableModel model) {
        model.setRowCount(0);

        // This query joins appointments with doctors to give readable names
        String sql = "SELECT a.appointment_date, d.first_name, d.last_name, a.reason, a.status " +
                "FROM appointments a " +
                "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                "WHERE a.patient_id = ? " +
                "ORDER BY a.appointment_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getDate("appointment_date"));
                row.add("Dr. " + rs.getString("first_name") + " " + rs.getString("last_name"));
                row.add(rs.getString("reason"));
                row.add("-"); // Placeholder for prescription if you don't have that table yet
                row.add(rs.getString("status"));
                model.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}