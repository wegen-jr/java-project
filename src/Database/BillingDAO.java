package Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class BillingDAO {

    // ===== Add a new bill =====
    public static boolean addBill(String patientId, LocalDate billDate,
                                  double consultationFee, double medicationFee,
                                  double labFee, double otherFee, double totalAmount,
                                  String paymentStatus, String paymentMethod,
                                  String createdBy) {
        String sql = "INSERT INTO billing (patient_id, bill_date, consultation_fee, medication_fee, " +
                "lab_fee, other_fee, total_amount, payment_status, payment_method, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patientId);
            pstmt.setDate(2, java.sql.Date.valueOf(billDate));
            pstmt.setDouble(3, consultationFee);
            pstmt.setDouble(4, medicationFee);
            pstmt.setDouble(5, labFee);
            pstmt.setDouble(6, otherFee);
            pstmt.setDouble(7, totalAmount);
            pstmt.setString(8, paymentStatus);
            pstmt.setString(9, paymentMethod);
            pstmt.setString(10, createdBy);

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error adding bill: " + e.getMessage());
            return false;
        }
    }

    // ===== Get all bills =====
    public static List<Map<String, Object>> getAllBills() {
        List<Map<String, Object>> bills = new ArrayList<>();

        String sql = """
        SELECT b.bill_id,
               b.patient_id,
               CONCAT(p.first_name, ' ', p.last_name) AS patient_name,
               b.bill_date,
               b.total_amount,
               b.payment_status
        FROM billing b
        JOIN patients p ON b.patient_id = p.patient_id
        ORDER BY b.bill_id DESC
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> bill = new HashMap<>();
                bill.put("bill_id", rs.getInt("bill_id"));
                bill.put("patient_id", rs.getString("patient_id"));
                bill.put("patient_name", rs.getString("patient_name"));
                bill.put("bill_date", rs.getDate("bill_date").toLocalDate());
                bill.put("total_amount", rs.getDouble("total_amount"));
                bill.put("payment_status", rs.getString("payment_status"));
                bills.add(bill);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching bills: " + e.getMessage());
        }

        return bills;
    }


    // ===== Get a bill by ID =====
    public static Map<String, Object> getBillById(int billId) {
        String sql = "SELECT * FROM billing WHERE bill_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, billId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> bill = new HashMap<>();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    bill.put(metaData.getColumnName(i), rs.getObject(i));
                }
                return bill;
            }

        } catch (SQLException e) {
            System.err.println("Error getting bill: " + e.getMessage());
        }

        return null;
    }

    // ===== Update payment status =====
    public static boolean updatePaymentStatus(int billId, String newStatus) {
        String sql = "UPDATE billing SET payment_status = ? WHERE bill_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, billId);

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating payment status: " + e.getMessage());
            return false;
        }
    }

    // ===== Delete bill =====
    public static boolean deleteBill(int billId) {
        String sql = "DELETE FROM billing WHERE bill_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, billId);
            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting bill: " + e.getMessage());
            return false;
        }
    }

}
