package Database;

import java.sql.*;

public class PharmacyDAO {

    // Method to get counts for the cards
    public int getCountByStatus(String status, boolean todayOnly) throws SQLException {
        String query = todayOnly
                ? "SELECT COUNT(*) FROM prescriptions WHERE status = ? AND DATE(issued_at) = CURDATE()"
                : "SELECT COUNT(*) FROM prescriptions WHERE status = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, status);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }
    public ResultSet getPharmacistRecord(int authId) throws SQLException {
        // Uses your existing DatabaseConnection class
        Connection conn = DatabaseConnection.getConnection();
        String query = "SELECT pharmacist_id, first_name, last_name, license_number, shift_type, contact_number FROM pharmacists WHERE auth_id = ?";

        PreparedStatement pst = conn.prepareStatement(query);
        pst.setInt(1, authId);
        return pst.executeQuery();
    }

    // Method to get data for the activity table
    public ResultSet getRecentActivity() throws SQLException {
        // Note: We don't use try-with-resources here because the UI needs to read the ResultSet
        Connection conn = DatabaseConnection.getConnection();
        String query = "SELECT patient_id, medication_name, issued_at, status FROM prescriptions ORDER BY issued_at DESC LIMIT 10";
        return conn.prepareStatement(query).executeQuery();
    }
    public ResultSet getPrescriptionQueue() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String query = "SELECT p.prescription_id, " +
                "CONCAT(pat.first_name, ' ', pat.last_name) AS patient_full_name, " +
                "CONCAT(doc.first_name, ' ', doc.last_name) AS doctor_full_name, " +
                "p.medication_name, p.dosage, p.issued_at, p.status " +
                "FROM prescriptions p " +
                "JOIN patients pat ON p.patient_id COLLATE utf8mb4_general_ci = pat.patient_id COLLATE utf8mb4_general_ci " +
                "JOIN doctors doc ON p.doctor_id = doc.doctor_id " +
                "WHERE p.status = 'Pending' " +
                "ORDER BY p.issued_at DESC";
        return conn.createStatement().executeQuery(query);
    }

    // 2. Update Status
    public boolean updatePrescriptionStatus(int id, String status) throws SQLException {
        String query = "UPDATE prescriptions SET status = ? WHERE prescription_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, status);
            pst.setInt(2, id);
            return pst.executeUpdate() > 0;
        }
    }
    public String getPatientId() throws SQLException {

        String query = "SELECT patient_id FROM prescriptions WHERE status = 'pending' LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            if (rs.next()) {
                return rs.getString("patient_id"); // or rs.getString(1)
            }
        }

        return null;
    }

    public boolean updatePaymentStatus(String id, String status) throws SQLException {
        String query = "UPDATE billing SET payment_status = ? WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, status);
            pst.setString(2, id);
            return pst.executeUpdate() > 0;
        }
    }
    // 3. Create Bill
    public boolean createPrescriptionBill(int prescriptionId, double fee, String creator) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();

        // Get patient_id first
        String getPid = "SELECT patient_id FROM prescriptions WHERE prescription_id = ?";
        String patientId = "";
        try (PreparedStatement pst1 = conn.prepareStatement(getPid)) {
            pst1.setInt(1, prescriptionId);
            try (ResultSet rs = pst1.executeQuery()) {
                if (rs.next()) patientId = rs.getString("patient_id");
            }
        }

        if (patientId.isEmpty()) return false;

        // Insert Bill
        String billQuery = "INSERT INTO billing (patient_id, bill_date, medication_fee, total_amount, payment_status, created_by) " +
                "VALUES (?, CURDATE(), ?, ?, 'Pending', ?)";
        try (PreparedStatement pst2 = conn.prepareStatement(billQuery)) {
            pst2.setString(1, patientId);
            pst2.setDouble(2, fee);
            pst2.setDouble(3, fee);
            pst2.setString(4, creator);
            return pst2.executeUpdate() > 0;
        }
    }
}