package Database;

import javax.swing.JComboBox;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import Model.DoctorItem;

public class AppointmentDAO {

    // ---------------- ADD APPOINTMENT ----------------
    public static boolean addAppointment(
            String patientId,
            int doctorId,
            String date,
            LocalTime time,
            String reason,
            String createdBy
    ) {
        String sql = """
            INSERT INTO appointments
            (patient_id, doctor_id, appointment_date, appointment_time, reason, created_by)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, patientId);
            ps.setInt(2, doctorId);
            ps.setDate(3, java.sql.Date.valueOf(date));
            ps.setTime(4, Time.valueOf(time));
            ps.setString(5, reason);
            ps.setString(6, createdBy);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------------- TODAY APPOINTMENTS (LIST) ----------------
    public static List<Map<String, Object>> getAppointments() {
        List<Map<String, Object>> list = new ArrayList<>();

        String sql = """
            SELECT a.appointment_id,
                   a.patient_id,
                   CONCAT(p.first_name, ' ', COALESCE(p.middle_name,''), ' ', p.last_name) AS patient_name,
                   a.doctor_id,
                   CONCAT(d.first_name, ' ', COALESCE(d.middle_name,''), ' ', d.last_name) AS doctor_name,
                   a.appointment_time,
                   a.status
            FROM appointments a
            JOIN patients p ON a.patient_id = p.patient_id
            JOIN doctors d ON a.doctor_id = d.doctor_id
            ORDER BY a.appointment_time
        """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("appointment_id", rs.getInt("appointment_id"));
                row.put("patient_id", rs.getString("patient_id"));
                row.put("patient_name", rs.getString("patient_name"));
                row.put("doctor_id", rs.getInt("doctor_id"));
                row.put("doctor_name", rs.getString("doctor_name"));
                row.put("time", rs.getTime("appointment_time").toLocalTime());
                row.put("status", rs.getString("status"));
                list.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ---------------- DASHBOARD TABLE ----------------
    public static void loadTodayAppointmentsForDashboard(DefaultTableModel model) {
        model.setRowCount(0);

        String sql = """
            SELECT a.appointment_time,
                   CONCAT(p.first_name, ' ', COALESCE(p.middle_name,''), ' ', p.last_name) AS patient_name,
                   CONCAT(d.first_name, ' ', COALESCE(d.middle_name,''), ' ', d.last_name) AS doctor_name,
                   a.status
            FROM appointments a
            JOIN patients p ON a.patient_id = p.patient_id
            JOIN doctors d ON a.doctor_id = d.doctor_id
            WHERE a.appointment_date = CURDATE()
            ORDER BY a.appointment_time
        """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getTime("appointment_time").toString(),
                        rs.getString("patient_name"),
                        rs.getString("doctor_name"),
                        rs.getString("status")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- DOCTORS FOR COMBOBOX ----------------
    public static List<DoctorItem> getAllDoctors() {
        List<DoctorItem> list = new ArrayList<>();

        String sql = """
            SELECT doctor_id,
                   CONCAT(first_name, ' ', IFNULL(middle_name,''), ' ', last_name,
                          ' (', specialization, ')') AS doctor_name
            FROM doctors
            ORDER BY first_name
        """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new DoctorItem(
                        rs.getInt("doctor_id"),
                        rs.getString("doctor_name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public static String validateCheckIn(int appointmentId) {
        String sql = "SELECT appointment_date, status FROM appointments WHERE appointment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, appointmentId);
            ResultSet rs = pstmt.executeQuery();

            // A. Check Existence
            if (!rs.next()) {
                return "Error: Appointment ID " + appointmentId + " not found.";
            }

            // B. Check Date (Must be TODAY)
            LocalDate appDate = rs.getDate("appointment_date").toLocalDate();
            LocalDate today = LocalDate.now();

            if (!appDate.equals(today)) {
                return "Error: This appointment is for " + appDate + ". You can only check in on " + today + ".";
            }

            // C. Check Status
            String status = rs.getString("status");
            if ("Checked-In".equalsIgnoreCase(status)) {
                return "Error: Patient is already checked in.";
            }
            if ("Cancelled".equalsIgnoreCase(status) || "No-show".equalsIgnoreCase(status)) {
                return "Error: Cannot check in. Status is " + status + ".";
            }
            if ("Completed".equalsIgnoreCase(status)) {
                return "Error: Appointment is already finished.";
            }

            return "VALID";

        } catch (SQLException e) {
            e.printStackTrace();
            return "Database Error: " + e.getMessage();
        }
    }

    // 2. UPDATE STATUS METHOD
    public static boolean performCheckIn(int appointmentId) {
        String sql = "UPDATE appointments SET status = 'Checked-In' WHERE appointment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, appointmentId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void loadTodayAppointments(DefaultTableModel model) {
        model.setRowCount(0);
        // Only show appointments scheduled for TODAY that haven't been processed yet
        String sql = "SELECT appointment_id, patient_id, doctor_id, appointment_time, status " +
                "FROM appointments " +
                "WHERE appointment_date = CURRENT_DATE() " +
                "ORDER BY appointment_time ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("appointment_id"));
                row.add(rs.getString("patient_id"));
                row.add(rs.getInt("doctor_id"));
                row.add(rs.getTime("appointment_time"));
                row.add(rs.getString("status"));
                model.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // ---------------- LOAD DOCTORS INTO COMBO ----------------
    public static void loadDoctors(JComboBox<DoctorItem> comboBox) {
        comboBox.removeAllItems();
        for (DoctorItem d : getAllDoctors()) {
            comboBox.addItem(d);
        }
    }
}
