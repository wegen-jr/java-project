package Database;

import javax.swing.*;
import java.sql.*;
import java.util.*;

public class StatisticsDAO {

    /* ================= DASHBOARD SUMMARY ================= */

    public static Map<String, Integer> getDashboardStats() {
        Map<String, Integer> stats = new HashMap<>();

        stats.put("totalPatients", getTotalPatients());
        stats.put("patientsWaiting", getPatientsWaiting());
        stats.put("totalDoctors", getTotalDoctors());
        stats.put("availableDoctors", getAvailableDoctors());
        stats.put("todayAppointments", getTodayAppointmentsCount());
        stats.put("todayCompleted", getTodayCompletedAppointments());
        stats.put("totalAppointment", getTotalAppointments());
        return stats;
    }

    public static int getTotalAppointments(){
        return executeCountQuery("SELECT COUNT(*) FROM appointments");
    }
    public static int getTotalPatients() {
        return executeCountQuery("SELECT COUNT(*) FROM patients");
    }

    public static int getPatientsWaiting() {
        return executeCountQuery("""
            SELECT COUNT(*) 
            FROM appointments
            WHERE appointment_date = CURDATE()
              AND status = 'Scheduled'
              AND appointment_time <= CURTIME()
        """);
    }

    private static int getTotalDoctors() {
        return executeCountQuery("SELECT COUNT(*) FROM doctors");
    }

    public static int getAvailableDoctors() {
        return executeCountQuery("""
            SELECT COUNT(*) 
            FROM doctors 
            WHERE availability = 'Available'
        """);
    }

    private static int getTodayAppointmentsCount() {
        return executeCountQuery("""
            SELECT COUNT(*) 
            FROM appointments 
            WHERE appointment_date = CURDATE()
        """);
    }

    private static int getTodayCompletedAppointments() {
        return executeCountQuery("""
            SELECT COUNT(*) 
            FROM appointments
            WHERE appointment_date = CURDATE()
              AND status = 'Completed'
        """);
    }

    /* ================= DOCTOR LIST ================= */

    public static List<Map<String, Object>> getAllDoctors() {
        List<Map<String, Object>> list = new ArrayList<>();

        String sql = """
            SELECT doctor_id,
                   first_name,
                   middle_name,
                   last_name,
                   specialization,
                   contact_number,
                   email,
                   availability
            FROM doctors
            ORDER BY first_name
        """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("doctor_id", rs.getInt("doctor_id"));
                row.put("first_name", rs.getString("first_name"));
                row.put("middle_name", rs.getString("middle_name"));
                row.put("last_name", rs.getString("last_name"));
                row.put("specialization", rs.getString("specialization"));
                row.put("contact", rs.getString("contact_number"));
                row.put("email", rs.getString("email"));
                row.put("status", rs.getString("availability"));
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /* ================= DOCTOR TODAY SCHEDULE ================= */

    public static List<Map<String, Object>> getDoctorSchedule(int doctorId) {
        List<Map<String, Object>> list = new ArrayList<>();

        String sql = """
            SELECT a.appointment_time,
                   CONCAT(p.first_name, ' ',
                          COALESCE(p.middle_name,''), ' ',
                          p.last_name) AS patient_name,
                   a.reason,
                   a.status
            FROM appointments a
            JOIN patients p ON a.patient_id = p.patient_id
            WHERE a.doctor_id = ?
            ORDER BY a.appointment_time
        """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, doctorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("time", rs.getTime("appointment_time").toString());
                    row.put("patient", rs.getString("patient_name"));
                    row.put("reason", rs.getString("reason"));
                    row.put("status", rs.getString("status"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /* ================= HELPER ================= */

    private static int executeCountQuery(String query) {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Count query error: " + e.getMessage());
        }
        return 0;
    }

    public static String getDoctorName(int docId) {
        String docName=null;
        String sql="select first_name from doctors where doctor_id=?";
        try {
            Connection con=DatabaseConnection.getConnection();
            Statement st=con.createStatement();
            ResultSet rs=st.executeQuery(sql);
            if (rs.next()){
                 docName=rs.getString(3);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return docName;
    }
}
