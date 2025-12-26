import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Doctor extends staffUser {

    // Database connection
    private static final String URL = "jdbc:mysql://localhost:3306/HMS";
    private static final String DB_USERNAME = "abrshiz";
    private static final String DB_PASSWORD = "abrsh123";

    // Doctor data
    private String doctorId;
    private String fullName;
    private String specialization;
    private String department;
    private String contactNumber;
    private String email;
    private String availability;
    private double consultationFee;

    /**
     * ATTACHMENT LOGIC: This method wraps the Icon and Text together into one panel.
     * It scales the image and adds hover/click effects to the whole unit.
     */
    private JPanel createNavItem(String text, String iconPath, Font font, Runnable action) {
        Color bg = new Color(2, 48, 71);
        Color hover = new Color(6, 75, 110);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        panel.setBackground(bg);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.setMaximumSize(new Dimension(250, 50));

        ImageIcon icon = new ImageIcon(new ImageIcon(iconPath).getImage()
                .getScaledInstance(20, 20, Image.SCALE_SMOOTH));

        JLabel iconLabel = new JLabel(icon);
        JLabel textLabel = new JLabel(text);
        textLabel.setForeground(Color.WHITE);
        textLabel.setFont(font);

        panel.add(iconLabel);
        panel.add(textLabel);


        MouseAdapter navListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { action.run(); }
            @Override
            public void mouseEntered(MouseEvent e) { panel.setBackground(hover); }
            @Override
            public void mouseExited(MouseEvent e) { panel.setBackground(bg); }
        };

        panel.addMouseListener(navListener);
        iconLabel.addMouseListener(navListener);
        textLabel.addMouseListener(navListener);

        return panel;
    }

    @Override
    void showDashboard() {
        JFrame frame = new JFrame("HMS - Hospital Management System (Doctor)");
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Font navFont = new Font("SansSerif", Font.BOLD, 12);

        // Main background panel with image painting
        JPanel mainBackgroundPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };

        // Left Sidebar Navigation
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(new Color(2, 48, 71));
        leftPanel.setPreferredSize(new Dimension(220, 0));
        leftPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1, true));

        // Header Section
        JPanel portalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 20));
        portalPanel.setOpaque(false);
        ImageIcon portalIcon = new ImageIcon(new ImageIcon("assets/portal.png").getImage()
                .getScaledInstance(25, 25, Image.SCALE_SMOOTH));
        JLabel portalHeader = new JLabel("Doctor Portal", portalIcon, JLabel.LEFT);
        portalHeader.setForeground(Color.WHITE);
        portalHeader.setFont(new Font("Arial", Font.BOLD, 18));
        portalHeader.setIconTextGap(10);
        portalPanel.add(portalHeader);

        // --- ATTACHED NAVIGATION ITEMS ---
        JPanel navDash = createNavItem("Dashboard", "assets/dashboard.png", navFont, this::showDashboard);
        JPanel navPat  = createNavItem("My Patients", "assets/user.png", navFont, this::showPatientsDashboard);
        JPanel navApp  = createNavItem("Appointments", "assets/appointment.png", navFont, this::showAppointmentsDashboard);
        JPanel navSch  = createNavItem("My Schedule", "assets/calendar.png", navFont, this::showScheduleDashboard);
        JPanel navPres = createNavItem("Prescriptions", "assets/medical-prescription.png", navFont, this::showPrescriptionsDashboard);

        // Add components to the sidebar
        leftPanel.add(portalPanel);
        leftPanel.add(new JSeparator());
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(navDash);
        leftPanel.add(navPat);
        leftPanel.add(navApp);
        leftPanel.add(navSch);
        leftPanel.add(navPres);
        leftPanel.add(Box.createVerticalGlue());

        mainBackgroundPanel.add(leftPanel, BorderLayout.WEST);

        // Right side Welcome area
        loadDoctorData();
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome, Dr. " + (fullName != null ? fullName : "Doctor") + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 36));
        welcomeLabel.setForeground(new Color(2, 48, 71));
        rightPanel.add(welcomeLabel, BorderLayout.CENTER);

        // Logout Section
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.setOpaque(false);
        logoutPanel.setBorder(new EmptyBorder(0, 0, 20, 20));
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(2, 48, 71));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        logoutButton.addActionListener(e -> { frame.dispose(); logout(); });
        logoutPanel.add(logoutButton);
        rightPanel.add(logoutPanel, BorderLayout.SOUTH);

        mainBackgroundPanel.add(rightPanel, BorderLayout.CENTER);
        frame.add(mainBackgroundPanel);
        frame.setVisible(true);
    }

    // This goes inside your Doctor class
    private void loadDoctorData() {
        if (this.usename == null) return;

        // Query the NEW doctors table using the username from authentication
        String query = "SELECT * FROM doctors WHERE username = ?";

        try (Connection con = DriverManager.getConnection(URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setString(1, this.usename);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                // Now we can get the real full name!
                this.doctorId = rs.getString("doctor_id");
                this.fullName = rs.getString("full_name");
                this.specialization = rs.getString("specialization");
            }
        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
        }
    }

    // --- DASHBOARD METHODS ---

    void showPatientsDashboard() {
        JFrame patientsFrame = new JFrame("My Patients");
        patientsFrame.setSize(800, 600);
        patientsFrame.setLocationRelativeTo(null);

        List<Patient> patients = getMyPatients();

        String[] columns = {"Patient ID", "Name", "Age", "Gender", "Last Visit", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        for (Patient p : patients) {
            model.addRow(new Object[]{
                    p.getPatientId(),
                    p.getName(),
                    p.getAge(),
                    p.getGender(),
                    p.getLastVisit(),
                    p.getStatus()
            });
        }

        JTable patientsTable = new JTable(model);
        patientsFrame.add(new JScrollPane(patientsTable));
        patientsFrame.setVisible(true);
    }

    void showAppointmentsDashboard() {
        JFrame appointmentsFrame = new JFrame("Today's Appointments - Dr. " + fullName);
        appointmentsFrame.setSize(900, 600);
        appointmentsFrame.setLocationRelativeTo(null);
        appointmentsFrame.setLayout(new BorderLayout());

        // 1. Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(2, 48, 71));
        JLabel titleLabel = new JLabel("Today's Schedule (" + LocalDate.now() + ")");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titlePanel.add(titleLabel);
        appointmentsFrame.add(titlePanel, BorderLayout.NORTH);

        // 2. Fetch Data
        List<Appointment> appointments = getTodaysAppointments();
        String[] columns = {"Time", "Patient Name", "Reason", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        for (Appointment app : appointments) {
            model.addRow(new Object[]{
                    app.getTime(),
                    app.getPatientName(),
                    app.getReason(),
                    app.getStatus()
            });
        }

        // 3. Table Styling
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.getTableHeader().setBackground(Color.LIGHT_GRAY);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        appointmentsFrame.add(scrollPane, BorderLayout.CENTER);
        appointmentsFrame.setVisible(true);
    }

    void showScheduleDashboard() {
        JFrame scheduleFrame = new JFrame("My Schedule - Dr. " + fullName);
        scheduleFrame.setSize(800, 500);
        scheduleFrame.setLocationRelativeTo(null);
        scheduleFrame.setLayout(new BorderLayout());

        // Column Headers
        String[] columns = {"Time Slot", "Patient", "Status", "Notes"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        // Fetch data from Database
        try (Connection con = DriverManager.getConnection(URL, DB_USERNAME, DB_PASSWORD)) {
            // Query to get upcoming appointments (Today and Future)
            String query = "SELECT a.appointment_time, p.full_name, a.status, a.notes " +
                    "FROM appointments a " +
                    "JOIN patients p ON a.patient_id = p.patient_id " +
                    "WHERE a.doctor_id = ? " +
                    "ORDER BY a.appointment_date, a.appointment_time";

            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, doctorId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("appointment_time"),
                        rs.getString("full_name"),
                        rs.getString("status"),
                        rs.getString("notes")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(scheduleFrame, "Error loading schedule: " + e.getMessage());
        }

        // Table Styling
        JTable table = new JTable(model);
        table.setRowHeight(25);
        table.getTableHeader().setBackground(new Color(2, 48, 71));
        table.getTableHeader().setForeground(Color.WHITE);

        scheduleFrame.add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom Panel for refreshing or closing
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> { scheduleFrame.dispose(); showScheduleDashboard(); });
        bottomPanel.add(refreshBtn);
        scheduleFrame.add(bottomPanel, BorderLayout.SOUTH);

        scheduleFrame.setVisible(true);
    }

    void showPrescriptionsDashboard() {
        JFrame prescriptionsFrame = new JFrame("Write Prescription - Dr. " + fullName);
        prescriptionsFrame.setSize(600, 600);
        prescriptionsFrame.setLocationRelativeTo(null);
        prescriptionsFrame.setLayout(new BorderLayout(10, 10));

        // 1. Top Panel: Patient Selection
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel patientLabel = new JLabel("Select Patient: ");
        JComboBox<String> patientDropdown = new JComboBox<>();

        // Fill dropdown with real patient IDs from DB
        List<Patient> patients = getMyPatients();
        for (Patient p : patients) {
            patientDropdown.addItem(p.getPatientId() + " - " + p.getName());
        }

        topPanel.add(patientLabel);
        topPanel.add(patientDropdown);

        // 2. Center Panel: Text Area
        JTextArea area = new JTextArea();
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));
        area.setText("DATE: " + LocalDate.now() + "\n\nDIAGNOSIS:\n\n\nMEDICATION:\n1. \n2. \n\nINSTRUCTIONS:");

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createTitledBorder("Prescription Details"));

        // 3. Bottom Panel: Save Button
        JButton save = new JButton("Save Prescription");
        save.setBackground(new Color(2, 48, 71));
        save.setForeground(Color.WHITE);
        save.setPreferredSize(new Dimension(150, 40));

        save.addActionListener(e -> {
            String selected = (String) patientDropdown.getSelectedItem();
            if (selected == null) {
                JOptionPane.showMessageDialog(prescriptionsFrame, "Please select a patient!");
                return;
            }

            String pId = selected.split(" - ")[0]; // Extract ID
            String text = area.getText();

            // SAVE TO DATABASE
            try (Connection con = DriverManager.getConnection(URL, DB_USERNAME, DB_PASSWORD)) {
                String sql = "INSERT INTO prescriptions (doctor_id, patient_id, prescription_text, prescribed_date) VALUES (?, ?, ?, ?)";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, doctorId);
                pst.setString(2, pId);
                pst.setString(3, text);
                pst.setDate(4, Date.valueOf(LocalDate.now()));

                pst.executeUpdate();
                JOptionPane.showMessageDialog(prescriptionsFrame, "Prescription Saved Successfully!");
                prescriptionsFrame.dispose();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(prescriptionsFrame, "Error saving: " + ex.getMessage());
            }
        });

        prescriptionsFrame.add(topPanel, BorderLayout.NORTH);
        prescriptionsFrame.add(scroll, BorderLayout.CENTER);
        prescriptionsFrame.add(save, BorderLayout.SOUTH);

        prescriptionsFrame.setVisible(true);
    }

    // --- DATABASE HELPER METHODS ---

    private List<Patient> getMyPatients() {
        List<Patient> patients = new ArrayList<>();
        if (doctorId == null) return patients;

        // This query joins patients with their latest appointment details
        String query = "SELECT p.patient_id, p.full_name, p.age, p.gender, " +
                "MAX(a.appointment_date) as last_visit, a.status " +
                "FROM patients p " +
                "JOIN appointments a ON p.patient_id = a.patient_id " +
                "WHERE a.doctor_id = ? " +
                "GROUP BY p.patient_id";

        try (Connection con = DriverManager.getConnection(URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setString(1, doctorId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Patient p = new Patient();
                p.setPatientId(rs.getString("patient_id"));
                p.setName(rs.getString("full_name"));
                p.setAge(rs.getInt("age"));
                p.setGender(rs.getString("gender"));
                p.setLastVisit(rs.getString("last_visit")); // From the MAX(date)
                p.setStatus(rs.getString("status"));
                patients.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return patients;
    }

    private List<Appointment> getTodaysAppointments() {
        List<Appointment> apps = new ArrayList<>();
        if (doctorId == null) return apps;

        // Query filters by CURRENT DATE and the specific Doctor ID
        String query = "SELECT a.appointment_time, p.full_name, a.reason, a.status " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "WHERE a.doctor_id = ? AND a.appointment_date = CURDATE() " +
                "ORDER BY a.appointment_time ASC";

        try (Connection con = DriverManager.getConnection(URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setString(1, doctorId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Appointment app = new Appointment();
                app.setTime(rs.getTime("appointment_time").toString());
                app.setPatientName(rs.getString("full_name"));
                app.setReason(rs.getString("reason"));
                app.setStatus(rs.getString("status"));
                apps.add(app);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return apps;
    }

    @Override
    Boolean login(String password) {
        String query = "SELECT * FROM authentication WHERE Username = ? AND Passworrd = ? AND Role = 'DOCTOR'";
        try (Connection con = DriverManager.getConnection(URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, this.usename);
            pst.setString(2, password);
            return pst.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    @Override
    void logout() {
        int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) new LoginPage().setVisible(true);
    }

//    public static void main(String[] args) {
//        new Doctor().showDashboard();
//    }
}

// Model Classes
class Patient {
    private String patientId, name, gender, lastVisit, status;
    private int age;
    public String getPatientId() { return patientId; }
    public void setPatientId(String id) { this.patientId = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String g) { this.gender = g; }
    public String getLastVisit() { return lastVisit; }
    public void setLastVisit(String v) { this.lastVisit = v; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
}

class Appointment {
    private String patientName, time, reason, status;
    public String getPatientName() { return patientName; }
    public void setPatientName(String n) { this.patientName = n; }
    public String getTime() { return time; }
    public void setTime(String t) { this.time = t; }
    public String getReason() { return reason; }
    public void setReason(String r) { this.reason = r; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
}