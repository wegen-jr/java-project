package Database;
import java.sql.*;

public class DatabaseSetup {

    public static void initializeDatabase() {
        Connection conn = null;
        Statement stmt = null;

        try {
            // Connect to MySQL server
            String baseUrl = "jdbc:mysql://localhost:3306/";
            String username = "abrshiz";
            String password = "abrsh123";

            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(baseUrl, username, password);
            stmt = conn.createStatement();

            System.out.println("🔧 Initializing database...");

            // Create database
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS HMS");
            stmt.executeUpdate("USE HMS");
            System.out.println("✅ Database HMS ready");

            // Create tables
            createAuthenticationTable(stmt);
            createPatientsTable(stmt);
            createDoctorsTable(stmt);
            createAppointmentsTable(stmt);
            createMedicalRecordsTable(stmt);
            createBillingTable(stmt);
            createReceptionLogsTable(stmt);

            // Insert default data
            insertDefaultData(stmt);

            System.out.println("🎉 Database initialization complete!");

        } catch (Exception e) {
            System.err.println("❌ Database setup failed: " + e.getMessage());
        } finally {
            closeResources(stmt, conn);
        }
    }

    private static void createAuthenticationTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS authentication (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "Username VARCHAR(50) UNIQUE NOT NULL, " +
                "password VARCHAR(255) NOT NULL, " +
                "Role VARCHAR(50) NOT NULL )";
        stmt.executeUpdate(sql);
        System.out.println("✅ Authentication table created");
    }

    private static void createPatientsTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS patients (" +
                "patient_id VARCHAR(20) PRIMARY KEY, " +
                "full_name VARCHAR(100) NOT NULL, " +
                "date_of_birth DATE NOT NULL, " +
                "gender ENUM('Male', 'Female', 'Other') NOT NULL, " +
                "contact_number VARCHAR(15) NOT NULL, " +
                "email VARCHAR(100), " +
                "address TEXT, " +
                "emergency_contact VARCHAR(15), " +
                "blood_type ENUM('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'), " +
                "registration_date DATE NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        stmt.executeUpdate(sql);
        System.out.println("✅ Patients table created");
    }

    private static void createDoctorsTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS doctors (" +
                "doctor_id VARCHAR(20) PRIMARY KEY, " +
                "full_name VARCHAR(100) NOT NULL, " +
                "specialization VARCHAR(100), " +
                "department VARCHAR(100), " +
                "qualification TEXT, " +
                "license_number VARCHAR(50), " +
                "contact_number VARCHAR(15), " +
                "email VARCHAR(100), " +
                "working_hours VARCHAR(100), " +
                "availability ENUM('Available', 'Busy', 'On Leave') DEFAULT 'Available', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        stmt.executeUpdate(sql);
        System.out.println("✅ Doctors table created");
    }

    private static void createAppointmentsTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS appointments (" +
                "appointment_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "patient_id VARCHAR(20) NOT NULL, " +
                "doctor_id VARCHAR(20) NOT NULL, " +
                "appointment_date DATE NOT NULL, " +
                "appointment_time TIME NOT NULL, " +
                "reason TEXT, " +
                "status ENUM('Scheduled', 'Confirmed', 'Completed', 'Cancelled', 'No-show') DEFAULT 'Scheduled', " +
                "created_by VARCHAR(50), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE" +
                ")";
        stmt.executeUpdate(sql);
        System.out.println("✅ Appointments table created");
    }

    private static void createMedicalRecordsTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS medical_records (" +
                "record_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "patient_id VARCHAR(20) NOT NULL, " +
                "doctor_id VARCHAR(20) NOT NULL, " +
                "visit_date DATE NOT NULL, " +
                "diagnosis TEXT, " +
                "prescription TEXT, " +
                "lab_tests TEXT, " +
                "notes TEXT, " +
                "next_visit_date DATE, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE" +
                ")";
        stmt.executeUpdate(sql);
        System.out.println("✅ Medical records table created");
    }

    private static void createBillingTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS billing (" +
                "bill_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "patient_id VARCHAR(20) NOT NULL, " +
                "bill_date DATE NOT NULL, " +
                "consultation_fee DECIMAL(10, 2) DEFAULT 0.00, " +
                "medication_fee DECIMAL(10, 2) DEFAULT 0.00, " +
                "lab_fee DECIMAL(10, 2) DEFAULT 0.00, " +
                "other_fee DECIMAL(10, 2) DEFAULT 0.00, " +
                "total_amount DECIMAL(10, 2) NOT NULL, " +
                "payment_status ENUM('Paid', 'Pending', 'Partial') DEFAULT 'Pending', " +
                "payment_method VARCHAR(50), " +
                "created_by VARCHAR(50), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE" +
                ")";
        stmt.executeUpdate(sql);
        System.out.println("✅ Billing table created");
    }

    private static void createReceptionLogsTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS reception_logs (" +
                "log_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "receptionist_username VARCHAR(50) NOT NULL, " +
                "activity VARCHAR(100) NOT NULL, " +
                "patient_id VARCHAR(20), " +
                "details TEXT, " +
                "ip_address VARCHAR(45), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        stmt.executeUpdate(sql);
        System.out.println("✅ Reception logs table created");
    }

    private static void insertDefaultData(Statement stmt) throws SQLException {
        insertSampleDoctors(stmt);
        insertSamplePatients(stmt);
    }



    private static void insertSampleDoctors(Statement stmt) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM doctors");
        rs.next();
        if (rs.getInt("count") == 0) {
            String[] doctors = {
                    "INSERT INTO doctors (doctor_id, full_name, specialization, contact_number) VALUES " +
                            "('D001', 'Dr. Sarah Wilson', 'Cardiologist', '111-222-3333')",

                    "INSERT INTO doctors (doctor_id, full_name, specialization, contact_number) VALUES " +
                            "('D002', 'Dr. Michael Brown', 'Neurologist', '444-555-6666')",

                    "INSERT INTO doctors (doctor_id, full_name, specialization, contact_number) VALUES " +
                            "('D003', 'Dr. Emily Davis', 'Pediatrician', '777-888-9999')"
            };

            for (String sql : doctors) {
                stmt.executeUpdate(sql);
            }
            System.out.println("✅ Sample doctors added");
        }
    }

    private static void insertSamplePatients(Statement stmt) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM patients");
        rs.next();
        if (rs.getInt("count") == 0) {
            String[] patients = {
                    "INSERT INTO patients (patient_id, full_name, date_of_birth, gender, contact_number, registration_date) VALUES " +
                            "('P001', 'John Doe', '1988-05-15', 'Male', '123-456-7890', CURDATE())",

                    "INSERT INTO patients (patient_id, full_name, date_of_birth, gender, contact_number, registration_date) VALUES " +
                            "('P002', 'Jane Smith', '1995-08-22', 'Female', '987-654-3210', CURDATE())",

                    "INSERT INTO patients (patient_id, full_name, date_of_birth, gender, contact_number, registration_date) VALUES " +
                            "('P003', 'Robert Johnson', '1975-12-10', 'Male', '555-123-4567', CURDATE())"
            };

            for (String sql : patients) {
                stmt.executeUpdate(sql);
            }
            System.out.println("✅ Sample patients added");
        }
    }

    private static void closeResources(Statement stmt, Connection conn) {
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}