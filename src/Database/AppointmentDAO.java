package Database;

import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class AppointmentDAO {

    public static boolean addAppointment(
            String patientId,
            String doctorId,
            LocalDate date,
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
            ps.setString(2, doctorId);
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


    public static List<Map<String, Object>> getTodayAppointments() {
        List<Map<String, Object>> list = new ArrayList<>();

        String sql = """
                SELECT a.appointment_id, a.patient_id, p.full_name,
                       a.doctor_id, d.full_name AS doctor_name,
                       a.appointment_time, a.status
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
                Map<String, Object> row = new HashMap<>();
                row.put("appointment_id", rs.getInt("appointment_id"));
                row.put("patient_id", rs.getString("patient_id"));
                row.put("patient_name", rs.getString("full_name"));
                row.put("doctor_id", rs.getString("doctor_id"));
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

    public static void loadTodayAppointmentsForDashboard(DefaultTableModel model) {
        model.setRowCount(0);

        String sql = """
        SELECT a.appointment_time,
               p.full_name,
               d.full_name AS doctor_name,
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
                        rs.getString("appointment_time"),
                        rs.getString("full_name"),
                        rs.getString("doctor_name"),
                        rs.getString("status")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
