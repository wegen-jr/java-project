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

            // Create database if not exists
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS HMS");
            stmt.executeUpdate("USE HMS");
            System.out.println("✅ Database HMS ready");


            // Create tables
            createAuthenticationTable(stmt);
            createPatientsTable(stmt);
            createDoctorsTable(stmt);
            createReceptionistsTable(stmt);
            createAdminsTable(stmt);
            createAppointmentsTable(stmt);
            createMedicalRecordsTable(stmt);
            createBillingTable(stmt);
            createReceptionLogsTable(stmt);

            // Insert default data safely
            insertDefaultData(conn);

            System.out.println("🎉 Database initialization complete!");

        } catch (Exception e) {
            System.err.println("❌ Database setup failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(stmt, conn);
        }
    }

    // Drop all tables in proper order to handle foreign keys
    private static void dropTables(Statement stmt) throws SQLException {
        String[] tables = {
                "reception_logs",
                "billing",
                "medical_records",
                "appointments",
                "admins",
                "receptionists",
                "doctors",
                "patients",
                "authentication"
        };

        for (String table : tables) {
            try {
                stmt.executeUpdate("DROP TABLE IF EXISTS " + table);
                System.out.println("🗑 Dropped table: " + table);
            } catch (SQLException e) {
                System.out.println("⚠ Could not drop table " + table + ": " + e.getMessage());
            }
        }
    }


    private static void createAuthenticationTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS authentication (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) UNIQUE NOT NULL, " +
                "password VARCHAR(255) NOT NULL, " +
                "role VARCHAR(50) NOT NULL" +
                ") ENGINE=InnoDB";
        stmt.executeUpdate(sql);
        System.out.println("✅ Authentication table created");
    }

    private static void createPatientsTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS patients (" +
                "patient_id VARCHAR(20) PRIMARY KEY, " +
                "first_name VARCHAR(100) NOT NULL, " +
                "middle_name VARCHAR(100) NOT NULL," +
                "last_name VARCHAR(100) NOT NULL," +
                "date_of_birth DATE NOT NULL, " +
                "gender ENUM('Male','Female','Other') NOT NULL, " +
                "contact_number VARCHAR(15) NOT NULL, " +
                "email VARCHAR(100) DEFAULT NULL, " +
                "address TEXT DEFAULT NULL, " +
                "emergency_contact VARCHAR(15) DEFAULT NULL, " +
                "blood_type ENUM('A+','A-','B+','B-','AB+','AB-','O+','O-') DEFAULT NULL, " +
                "registration_date DATE NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        stmt.executeUpdate(sql);
        System.out.println("✅ Patients table created");
    }

    private static void createDoctorsTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS doctors (" +
                "doctor_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "auth_id INT NOT NULL, " +
                "first_name VARCHAR(100) NOT NULL, " +
                "middle_name VARCHAR(100) DEFAULT NULL, " +
                "last_name VARCHAR(100) NOT NULL, " +
                "specialization VARCHAR(100) DEFAULT NULL, " +
                "department VARCHAR(100) DEFAULT NULL, " +
                "qualification TEXT DEFAULT NULL, " +
                "license_number VARCHAR(50) DEFAULT NULL, " +
                "contact_number VARCHAR(15) DEFAULT NULL, " +
                "email VARCHAR(100) DEFAULT NULL, " +
                "working_hours VARCHAR(100) DEFAULT NULL, " +
                "availability ENUM('Available','Busy','On Leave') DEFAULT 'Available', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "CONSTRAINT fk_doctor_auth FOREIGN KEY (auth_id) REFERENCES authentication(id) " +
                "ON DELETE CASCADE ON UPDATE CASCADE" +
                ") ENGINE=InnoDB";
        stmt.executeUpdate(sql);
        System.out.println("✅ Doctors table created");
    }

    private static void createReceptionistsTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS receptionists (" +
                "receptionist_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "auth_id INT NOT NULL, " +
                "first_name VARCHAR(100) NOT NULL, " +
                "last_name VARCHAR(100) NOT NULL, " +
                "contact_number VARCHAR(15) DEFAULT NULL, " +
                "email VARCHAR(100) DEFAULT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "CONSTRAINT fk_reception_auth FOREIGN KEY (auth_id) REFERENCES authentication(id) " +
                "ON DELETE CASCADE ON UPDATE CASCADE" +
                ") ENGINE=InnoDB";
        stmt.executeUpdate(sql);
        System.out.println("✅ Receptionists table created");
    }

    private static void createAdminsTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS admins (" +
                "admin_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "auth_id INT NOT NULL, " +
                "first_name VARCHAR(100) NOT NULL, " +
                "last_name VARCHAR(100) NOT NULL, " +
                "contact_number VARCHAR(15) DEFAULT NULL, " +
                "email VARCHAR(100) DEFAULT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "CONSTRAINT fk_admin_auth FOREIGN KEY (auth_id) REFERENCES authentication(id) " +
                "ON DELETE CASCADE ON UPDATE CASCADE" +
                ") ENGINE=InnoDB";
        stmt.executeUpdate(sql);
        System.out.println("✅ Admins table created");
    }

    private static void createAppointmentsTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS appointments (" +
                "appointment_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "patient_id VARCHAR(20) NOT NULL, " +
                "doctor_id INT NOT NULL, " +
                "appointment_date DATE NOT NULL, " +
                "appointment_time TIME NOT NULL, " +
                "reason TEXT DEFAULT NULL, " +
                "status ENUM('Scheduled','Confirmed','Completed','Cancelled','No-show') DEFAULT 'Scheduled', " +
                "created_by VARCHAR(50) DEFAULT NULL, " +
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
                "doctor_id INT NOT NULL, " +
                "visit_date DATE NOT NULL, " +
                "diagnosis TEXT DEFAULT NULL, " +
                "prescription TEXT DEFAULT NULL, " +
                "lab_tests TEXT DEFAULT NULL, " +
                "notes TEXT DEFAULT NULL, " +
                "next_visit_date DATE DEFAULT NULL, " +
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
                "consultation_fee DECIMAL(10,2) DEFAULT 0.00, " +
                "medication_fee DECIMAL(10,2) DEFAULT 0.00, " +
                "lab_fee DECIMAL(10,2) DEFAULT 0.00, " +
                "other_fee DECIMAL(10,2) DEFAULT 0.00, " +
                "total_amount DECIMAL(10,2) NOT NULL, " +
                "payment_status ENUM('Paid','Pending','Partial') DEFAULT 'Pending', " +
                "payment_method VARCHAR(50) DEFAULT NULL, " +
                "created_by VARCHAR(50) DEFAULT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE" +
                ")";
        stmt.executeUpdate(sql);
        System.out.println("✅ Billing table created");
    }

    private static void createReceptionLogsTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS reception_logs (" +
                "log_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "auth_id INT NOT NULL, " +
                "receptionist_username VARCHAR(50) NOT NULL, " +
                "activity VARCHAR(100) NOT NULL, " +
                "patient_id VARCHAR(20) DEFAULT NULL, " +
                "details TEXT DEFAULT NULL, " +
                "ip_address VARCHAR(45) DEFAULT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "CONSTRAINT fk_reception_log_auth FOREIGN KEY (auth_id) REFERENCES authentication(id) " +
                "ON DELETE CASCADE ON UPDATE CASCADE" +
                ")";
        stmt.executeUpdate(sql);
        System.out.println("✅ Reception logs table created");
    }

    private static void insertDefaultData(Connection conn) throws SQLException {
        insertSampleDoctors(conn);
        insertSampleReceptionists(conn);
        insertSampleAdmins(conn);
        insertSamplePatients(conn);
    }

    private static boolean usernameExists(Connection conn, String username) throws SQLException {
        String sql = "SELECT id FROM authentication WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // true if username exists
            }
        }
    }

    private static boolean patientExists(Connection conn, String patientId) throws SQLException {
        String sql = "SELECT patient_id FROM patients WHERE patient_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void insertSampleDoctors(Connection conn) throws SQLException {
        String authSql = "INSERT INTO authentication (username,password,role) VALUES(?,?,?)";
        String doctorSql = "INSERT INTO doctors(auth_id,first_name,last_name,specialization,contact_number) VALUES(?,?,?,?,?)";

        String[][] doctors = {
                {"drsarah","Sarah","Wilson","Cardiologist","111-222-3333"},
                {"drmichael","Michael","Brown","Neurologist","444-555-6666"},
                {"dremily","Emily","Davis","Pediatrician","777-888-9999"}
        };

        for (String[] d : doctors) {
            if (!usernameExists(conn, d[0])) {
                try (PreparedStatement authPs = conn.prepareStatement(authSql, Statement.RETURN_GENERATED_KEYS)) {
                    authPs.setString(1, d[0]);
                    authPs.setString(2, "password123");
                    authPs.setString(3, "DOCTOR");
                    authPs.executeUpdate();

                    try (ResultSet rs = authPs.getGeneratedKeys()) {
                        if (rs.next()) {
                            int authId = rs.getInt(1);
                            try (PreparedStatement docPs = conn.prepareStatement(doctorSql)) {
                                docPs.setInt(1, authId);
                                docPs.setString(2, d[1]);
                                docPs.setString(3, d[2]);
                                docPs.setString(4, d[3]);
                                docPs.setString(5, d[4]);
                                docPs.executeUpdate();
                            }
                            System.out.println("✅ Doctor added: " + d[1] + " " + d[2]);
                        }
                    }
                }
            } else {
                System.out.println("⚠ Doctor username already exists: " + d[0]);
            }
        }
    }

    // Receptionists
    private static void insertSampleReceptionists(Connection conn) throws SQLException {
        String authSql = "INSERT INTO authentication(username,password,role) VALUES(?,?,?)";
        String recSql = "INSERT INTO receptionists(auth_id,first_name,last_name,contact_number) VALUES(?,?,?,?)";

        String[][] receptionists = {
                {"reception1","Anna","Smith","123-111-2222"},
                {"reception2","Tom","Johnson","333-444-5555"}
        };

        for (String[] r : receptionists) {
            if (!usernameExists(conn, r[0])) {
                try (PreparedStatement authPs = conn.prepareStatement(authSql, Statement.RETURN_GENERATED_KEYS)) {
                    authPs.setString(1, r[0]);
                    authPs.setString(2, "password123");
                    authPs.setString(3, "RECEPTIONIST");
                    authPs.executeUpdate();

                    try (ResultSet rs = authPs.getGeneratedKeys()) {
                        if (rs.next()) {
                            int authId = rs.getInt(1);
                            try (PreparedStatement recPs = conn.prepareStatement(recSql)) {
                                recPs.setInt(1, authId);
                                recPs.setString(2, r[1]);
                                recPs.setString(3, r[2]);
                                recPs.setString(4, r[3]);
                                recPs.executeUpdate();
                            }
                            System.out.println("✅ Receptionist added: " + r[1] + " " + r[2]);
                        }
                    }
                }
            } else {
                System.out.println("⚠ Receptionist username already exists: " + r[0]);
            }
        }
    }

    // Admins
    private static void insertSampleAdmins(Connection conn) throws SQLException {
        String authSql = "INSERT INTO authentication(username,password,role) VALUES(?,?,?)";
        String adminSql = "INSERT INTO admins(auth_id,first_name,last_name,contact_number) VALUES(?,?,?,?)";

        String[][] admins = {
                {"admin1","Alice","King","999-888-7777"},
                {"admin2","Bob","White","666-555-4444"}
        };

        for (String[] a : admins) {
            if (!usernameExists(conn, a[0])) {
                try (PreparedStatement authPs = conn.prepareStatement(authSql, Statement.RETURN_GENERATED_KEYS)) {
                    authPs.setString(1, a[0]);
                    authPs.setString(2, "password123");
                    authPs.setString(3, "ADMIN");
                    authPs.executeUpdate();

                    try (ResultSet rs = authPs.getGeneratedKeys()) {
                        if (rs.next()) {
                            int authId = rs.getInt(1);
                            try (PreparedStatement adminPs = conn.prepareStatement(adminSql)) {
                                adminPs.setInt(1, authId);
                                adminPs.setString(2, a[1]);
                                adminPs.setString(3, a[2]);
                                adminPs.setString(4, a[3]);
                                adminPs.executeUpdate();
                            }
                            System.out.println("✅ Admin added: " + a[1] + " " + a[2]);
                        }
                    }
                }
            } else {
                System.out.println("⚠ Admin username already exists: " + a[0]);
            }
        }
    }

    // Patients
    private static void insertSamplePatients(Connection conn) throws SQLException {
        String[][] patients = {
                {"P001","John"," Doe","doa","1988-05-15","Male","123-456-7890"},
                {"P002","Jane","nani"," Smith","1995-08-22","Female","987-654-3210"},
                {"P003","Robert","sanchez"," Johnson","1975-12-10","Male","555-123-4567"}
        };

        String sql = "INSERT INTO patients(patient_id,first_name,middle_name,last_name,date_of_birth,gender,contact_number,registration_date) " +
                "VALUES(?,?,?,?,?,?,?,CURDATE())";

        for (String[] p : patients) {
            if (!patientExists(conn, p[0])) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, p[0]);
                    ps.setString(2, p[1]);
                    ps.setString(3, p[2]);
                    ps.setString(4, p[3]);
                    ps.setDate(5, Date.valueOf(p[4]));
                    ps.setString(6, p[5]);
                    ps.setString(7, p[6]);
                    ps.executeUpdate();
                    System.out.println("✅ Patient added: " + p[1]);
                }
            } else {
                System.out.println("⚠ Patient already exists: " + p[1]);
            }
        }
    }
    private static void closeResources(Statement stmt, Connection conn){
        try{
            if(stmt != null) stmt.close();
            if(conn != null) conn.close();
        } catch(SQLException e){
            e.printStackTrace();
        }
    }


}
