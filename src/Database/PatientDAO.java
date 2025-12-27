package Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientDAO {

    // Add new patient
    public static boolean addPatient(String patientId, String fullName, String dateOfBirth,
                                     String gender, String contact, String email,
                                     String address, String emergencyContact, String bloodType) {
        String query = "INSERT INTO patients (patient_id, full_name, date_of_birth, gender, " +
                "contact_number, email, address, emergency_contact, blood_type, registration_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURDATE())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, patientId);
            pstmt.setString(2, fullName);
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
        String query = "SELECT * FROM patients ORDER BY full_name ASC";

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
        String query = "SELECT * FROM patients WHERE patient_id LIKE ? OR full_name LIKE ? " +
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

    // Update patient
    public static boolean updatePatient(String patientId, String fullName, String dateOfBirth,
                                        String gender, String contact, String email,
                                        String address, String emergencyContact, String bloodType) {
        String query = "UPDATE patients SET full_name = ?, date_of_birth = ?, gender = ?, " +
                "contact_number = ?, email = ?, address = ?, emergency_contact = ?, " +
                "blood_type = ? WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, fullName);
            pstmt.setDate(2, Date.valueOf(dateOfBirth));
            pstmt.setString(3, gender);
            pstmt.setString(4, contact);
            pstmt.setString(5, email);
            pstmt.setString(6, address);
            pstmt.setString(7, emergencyContact);
            pstmt.setString(8, bloodType);
            pstmt.setString(9, patientId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating patient: " + e.getMessage());
            return false;
        }
    }

    // Delete patient
    public static boolean deletePatient(String patientId) {
        String query = "DELETE FROM patients WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, patientId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting patient: " + e.getMessage());
            return false;
        }
    }

    // Generate new patient ID
    public static String generatePatientId() {
        String query = "SELECT MAX(patient_id) as max_id FROM patients WHERE patient_id LIKE 'P%'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                String maxId = rs.getString("max_id");
                if (maxId != null && maxId.startsWith("P")) {
                    try {
                        int num = Integer.parseInt(maxId.substring(1)) + 1;
                        return String.format("P%03d", num);
                    } catch (NumberFormatException e) {
                        return "P001";
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating patient ID: " + e.getMessage());
        }
        return "P001";
    }
}