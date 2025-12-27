package Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsDAO {

    public static Map<String, Integer> getDashboardStats() {
        Map<String, Integer> stats = new HashMap<>();

        stats.put("totalPatients", getTotalPatients());
        stats.put("patientsWaiting", getPatientsWaiting());
        stats.put("totalDoctors", getTotalDoctors());
        stats.put("availableDoctors", getAvailableDoctors());
        stats.put("todayAppointments", getTodayAppointmentsCount());
        stats.put("todayCompleted", getTodayCompletedAppointments());

        return stats;
    }

    public static int getTotalPatients() {
        String query = "SELECT COUNT(*) as count FROM patients";
        return executeCountQuery(query);
    }
    public static List<Map<String, Object>> getAllDoctors() {
        List<Map<String, Object>> list = new ArrayList<>();

        String sql = """
        SELECT doctor_id,
               full_name,
               specialization,
               contact_number,
               email,
               availability
        FROM doctors
    """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("doctor_id", rs.getString("doctor_id"));
                row.put("full_name", rs.getString("full_name"));
                row.put("specialization", rs.getString("specialization"));
                row.put("contact", rs.getString("contact_number"));
                row.put("email", rs.getString("email"));
                row.put("status", rs.getString("availability")); // use availability here
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    // Get today's schedule of one doctor
    public static List<Map<String, Object>> getDoctorTodaySchedule(String doctorId) {
        List<Map<String, Object>> list = new ArrayList<>();

        String sql = """
            SELECT a.appointment_time,
                   p.full_name AS patient,
                   a.reason,
                   a.status
            FROM appointments a
            JOIN patients p ON a.patient_id = p.patient_id
            WHERE a.doctor_id = ?
              AND a.appointment_date = CURDATE()
            ORDER BY a.appointment_time
        """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, doctorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("time", rs.getString("appointment_time"));
                row.put("patient", rs.getString("patient"));
                row.put("reason", rs.getString("reason"));
                row.put("status", rs.getString("status"));
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    private static int getPatientsWaiting() {
        String query = "SELECT COUNT(*) as count FROM appointments " +
                "WHERE appointment_date = CURDATE() AND status = 'Scheduled' " +
                "AND appointment_time <= CURTIME()";
        return executeCountQuery(query);
    }

    private static int getTotalDoctors() {
        String query = "SELECT COUNT(*) as count FROM doctors";
        return executeCountQuery(query);
    }

    private static int getAvailableDoctors() {
        String query = "SELECT COUNT(*) as count FROM doctors WHERE availability = 'Available'";
        return executeCountQuery(query);
    }

    private static int getTodayAppointmentsCount() {
        String query = "SELECT COUNT(*) as count FROM appointments WHERE appointment_date = CURDATE()";
        return executeCountQuery(query);
    }

    private static int getTodayCompletedAppointments() {
        String query = "SELECT COUNT(*) as count FROM appointments " +
                "WHERE appointment_date = CURDATE() AND status = 'Completed'";
        return executeCountQuery(query);
    }

    private static int executeCountQuery(String query) {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error executing count query: " + e.getMessage());
        }
        return 0;
    }
}