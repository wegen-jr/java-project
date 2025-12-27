package Database;
import java.sql.*;
import java.util.HashMap;
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

    private static int getTotalPatients() {
        String query = "SELECT COUNT(*) as count FROM patients";
        return executeCountQuery(query);
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