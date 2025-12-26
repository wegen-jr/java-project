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
    private String contactNumber;
    private String email;

    /**
     * ATTACHMENT LOGIC: This method wraps the Icon and Text together into one panel.
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
            public void mouseClicked(MouseEvent e) {
                action.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(bg);
            }
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

        // Main background panel
        JPanel mainBackgroundPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon bgIcon = new ImageIcon("assets/DoctorsHomePage.jpg");
                if (bgIcon.getImage() != null) {
                    g.drawImage(bgIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
                }
            }
        };

        // Left Sidebar Navigation
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(new Color(2, 48, 71));
        leftPanel.setPreferredSize(new Dimension(220, 0));
        leftPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1, true));

        // Header Section
        JPanel portalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        portalPanel.setOpaque(false);
        portalPanel.setMaximumSize(new Dimension(250, 60));
        ImageIcon portalIcon = new ImageIcon(new ImageIcon("assets/portal.png").getImage()
                .getScaledInstance(25, 25, Image.SCALE_SMOOTH));
        JLabel portalHeader = new JLabel("Doctor Portal", portalIcon, JLabel.LEFT);
        portalHeader.setForeground(Color.WHITE);
        portalHeader.setFont(new Font("Arial", Font.BOLD, 18));
        portalHeader.setIconTextGap(10);
        portalPanel.add(portalHeader);

        // --- NAVIGATION ITEMS ---
        JPanel navDash = createNavItem("Dashboard", "assets/dashboard.png", navFont, this::showDashboard);
        JPanel navProf = createNavItem("My Profile", "assets/user.png", navFont, this::showProfileWindow);
        JPanel navPat  = createNavItem("My Patients", "assets/hospitalisation.png", navFont, this::showPatientsDashboard);
        JPanel navApp  = createNavItem("Appointments", "assets/medical-appointment.png", navFont, this::showAppointmentsDashboard);
        JPanel navLab  = createNavItem("Send Lab Requests", "assets/observation.png", navFont, this::showLabRequestsDashboard);
        JPanel navLabRes = createNavItem("Lab Results", "assets/Lab Response.png", navFont, this::showLabResultsWindow);
        JPanel navSch  = createNavItem("My Schedule", "assets/calendar.png", navFont, this::showScheduleDashboard);
        JPanel navPres = createNavItem("Prescriptions", "assets/medical-prescription.png", navFont, this::showPrescriptionsDashboard);

        // Styled Logout for Sidebar
        JPanel navLogout = createNavItem("Logout", "assets/logout.png", navFont, () -> {
            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                frame.dispose();
                logout();
            }
        });

        // Adding components to the sidebar
        leftPanel.add(portalPanel);
        leftPanel.add(new JSeparator());
        leftPanel.add(Box.createVerticalStrut(2));
        leftPanel.add(navDash);
        leftPanel.add(navPat);
        leftPanel.add(navApp);
        leftPanel.add(navLab);
        leftPanel.add(navLabRes);
        leftPanel.add(navSch);
        leftPanel.add(navPres);
        leftPanel.add(navProf);

        // --- THIS PART FIXES THE PLACEMENT ---
        leftPanel.add(Box.createVerticalGlue()); // This pushes everything above it to the top
        leftPanel.add(new JSeparator());
        leftPanel.add(navLogout);               // This puts Logout at the very bottom
        leftPanel.add(Box.createVerticalStrut(10));

        mainBackgroundPanel.add(leftPanel, BorderLayout.WEST);

        // Right side Welcome area (Cleaner without the floating logout button)
        loadDoctorData();
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome, Dr. " + (fullName != null ? fullName : "Doctor") + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 42)); // Slightly bigger font
        welcomeLabel.setForeground(new Color(2, 48, 71));

        // Add shadow effect or padding if you want, but this keeps it centered perfectly
        rightPanel.add(welcomeLabel, BorderLayout.CENTER);

        mainBackgroundPanel.add(rightPanel, BorderLayout.CENTER);
        frame.add(mainBackgroundPanel);
        frame.setVisible(true);
    }

    private void loadDoctorData() {
        if (this.usename == null) return;
        String query = "SELECT * FROM doctors WHERE username = ?";
        try (Connection con = DriverManager.getConnection(URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, this.usename);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                this.doctorId = rs.getString("doctor_id");
                this.fullName = rs.getString("full_name");
                this.specialization = rs.getString("specialization");
                this.contactNumber = rs.getString("contact_number");
                this.email = rs.getString("email");
            }
        } catch (SQLException e) {
            System.err.println("DB Error: " + e.getMessage());
        }
    }

    // --- NEW PROFILE WINDOW ---
    void showProfileWindow() {
        JFrame profFrame = new JFrame("My Profile");
        profFrame.setSize(400, 350);
        profFrame.setLocationRelativeTo(null);

        JPanel card = new JPanel(new GridLayout(5, 2, 10, 20));
        card.setBorder(new EmptyBorder(30, 30, 30, 30));

        card.add(new JLabel("Doctor ID:"));
        card.add(new JLabel(doctorId));
        card.add(new JLabel("Full Name:"));
        card.add(new JLabel(fullName));
        card.add(new JLabel("Specialty:"));
        card.add(new JLabel(specialization));
        card.add(new JLabel("Contact:"));
        card.add(new JLabel(contactNumber));
        card.add(new JLabel("Email:"));
        card.add(new JLabel(email));

        profFrame.add(card);
        profFrame.setVisible(true);
    }

    void showLabResultsWindow() {
        JFrame resultsFrame = new JFrame("Lab Test Results - Dr. " + fullName);
        resultsFrame.setSize(950, 500);
        resultsFrame.setLocationRelativeTo(null);
        resultsFrame.setLayout(new BorderLayout(10, 10));

        // Table Setup
        // Added "Status" column so you can see if it is still 'Pending'
        String[] columns = {"ID", "Patient Name", "Test Type", "Status", "Lab Response/Notes", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        // SQL Query: Removed the "Completed" filter so you can see EVERYTHING
        String sql = "SELECT lr.request_id, p.full_name, lr.test_type, lr.status, " +
                "lr.result_details, lr.notes_from_doctor, lr.request_date " +
                "FROM lab_requests lr " +
                "JOIN patients p ON lr.patient_id = p.patient_id " +
                "WHERE lr.doctor_id = ? " +
                "ORDER BY lr.request_date DESC";

        try (Connection con = DriverManager.getConnection(URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, this.doctorId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                // Logic: If the lab hasn't replied yet, show the Doctor's original notes
                String response = rs.getString("result_details");
                if (response == null || response.isEmpty()) {
                    response = "WAITING: " + rs.getString("notes_from_doctor");
                }

                model.addRow(new Object[]{
                        rs.getInt("request_id"),
                        rs.getString("full_name"),
                        rs.getString("test_type"),
                        rs.getString("status"),
                        response,
                        rs.getTimestamp("request_date")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(resultsFrame, "Error loading results: " + e.getMessage());
        }

        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(2, 48, 71));
        table.getTableHeader().setForeground(Color.WHITE);

        resultsFrame.add(new JScrollPane(table), BorderLayout.CENTER);

        // --- REFRESH BUTTON AREA ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("Refresh Data");
        JButton closeBtn = new JButton("Close");

        refreshBtn.addActionListener(e -> {
            resultsFrame.dispose();
            showLabResultsWindow();
        });

        closeBtn.addActionListener(e -> resultsFrame.dispose());

        bottomPanel.add(refreshBtn);
        bottomPanel.add(closeBtn);
        resultsFrame.add(bottomPanel, BorderLayout.SOUTH);

        resultsFrame.setVisible(true);
    }

    void showLabRequestsDashboard() {
        JFrame labFrame = new JFrame("New Lab Request");
        labFrame.setSize(500, 450);
        labFrame.setLocationRelativeTo(null);
        labFrame.setLayout(new BorderLayout(15, 15));

        // Form Panel
        JPanel form = new JPanel(new GridLayout(5, 2, 10, 15));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. Patient Selection (Fetches from your existing getMyPatients method)
        JComboBox<String> pCombo = new JComboBox<>();
        for (Patient p : getMyPatients()) {
            pCombo.addItem(p.getPatientId() + " - " + p.getName());
        }

        // 2. Test Selection
        String[] tests = {"Complete Blood Count", "X-Ray Chest", "MRI Scan", "Urinalysis", "Glucose Test"};
        JComboBox<String> testCombo = new JComboBox<>(tests);

        // 3. Priority Selection
        String[] priorities = {"Normal", "Urgent", "Emergency"};
        JComboBox<String> priorityCombo = new JComboBox<>(priorities);

        // 4. Doctor's Notes
        JTextField notesField = new JTextField();

        form.add(new JLabel("Select Patient:")); form.add(pCombo);
        form.add(new JLabel("Test Type:")); form.add(testCombo);
        form.add(new JLabel("Priority Level:")); form.add(priorityCombo);
        form.add(new JLabel("Instructions:")); form.add(notesField);

        // 5. Save Button
        JButton sendBtn = new JButton("Submit Request to Lab");
        sendBtn.setBackground(new Color(2, 48, 71));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFont(new Font("Arial", Font.BOLD, 14));

        sendBtn.addActionListener(e -> {
            if (pCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(labFrame, "No patient selected!");
                return;
            }

            String pId = pCombo.getSelectedItem().toString().split(" - ")[0];
            String test = testCombo.getSelectedItem().toString();
            String priority = priorityCombo.getSelectedItem().toString();
            String notes = notesField.getText();

            // SQL INSERT to lab_requests
            String sql = "INSERT INTO lab_requests (doctor_id, patient_id, test_type, priority, notes_from_doctor, status) VALUES (?, ?, ?, ?, ?, 'Pending')";

            try (Connection con = DriverManager.getConnection(URL, DB_USERNAME, DB_PASSWORD);
                 PreparedStatement pst = con.prepareStatement(sql)) {

                pst.setString(1, this.doctorId);
                pst.setString(2, pId);
                pst.setString(3, test);
                pst.setString(4, priority);
                pst.setString(5, notes);

                pst.executeUpdate();
                JOptionPane.showMessageDialog(labFrame, "Request sent to Lab successfully!");
                labFrame.dispose();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(labFrame, "Error: " + ex.getMessage());
            }
        });

        labFrame.add(form, BorderLayout.CENTER);
        labFrame.add(sendBtn, BorderLayout.SOUTH);
        labFrame.setVisible(true);
    }

    // --- YOUR ORIGINAL DASHBOARD METHODS ---
    void showPatientsDashboard() {
        JFrame patientsFrame = new JFrame("My Patients");
        patientsFrame.setSize(800, 600);
        patientsFrame.setLocationRelativeTo(null);
        List<Patient> patients = getMyPatients();
        String[] columns = {"Patient ID", "Name", "Age", "Gender", "Last Visit", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        for (Patient p : patients) {
            model.addRow(new Object[]{p.getPatientId(), p.getName(), p.getAge(), p.getGender(), p.getLastVisit(), p.getStatus()});
        }
        patientsFrame.add(new JScrollPane(new JTable(model)));
        patientsFrame.setVisible(true);
    }

    void showAppointmentsDashboard() {
        JFrame appointmentsFrame = new JFrame("Today's Appointments");
        appointmentsFrame.setSize(900, 600);
        appointmentsFrame.setLocationRelativeTo(null);
        List<Appointment> appointments = getTodaysAppointments();
        String[] columns = {"Time", "Patient Name", "Reason", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        for (Appointment app : appointments) {
            model.addRow(new Object[]{app.getTime(), app.getPatientName(), app.getReason(), app.getStatus()});
        }
        appointmentsFrame.add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        appointmentsFrame.setVisible(true);
    }

    void showScheduleDashboard() {
        JFrame scheduleFrame = new JFrame("My Schedule");
        scheduleFrame.setSize(800, 500);
        scheduleFrame.setLocationRelativeTo(null);
        String[] columns = {"Time Slot", "Patient", "Status", "Notes"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        try (Connection con = DriverManager.getConnection(URL, DB_USERNAME, DB_PASSWORD)) {
            String query = "SELECT a.appointment_time, p.full_name, a.status, a.notes FROM appointments a JOIN patients p ON a.patient_id = p.patient_id WHERE a.doctor_id = ? ORDER BY a.appointment_time";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, doctorId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("appointment_time"), rs.getString("full_name"), rs.getString("status"), rs.getString("notes")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
        scheduleFrame.add(new JScrollPane(new JTable(model)));
        scheduleFrame.setVisible(true);
    }

    void showPrescriptionsDashboard() {
        JFrame prescriptionsFrame = new JFrame("Write Prescription");
        prescriptionsFrame.setSize(600, 600);
        prescriptionsFrame.setLocationRelativeTo(null);
        prescriptionsFrame.setLayout(new BorderLayout());
        JComboBox<String> pDropdown = new JComboBox<>();
        for (Patient p : getMyPatients()) pDropdown.addItem(p.getPatientId() + " - " + p.getName());
        JTextArea area = new JTextArea("DATE: " + LocalDate.now() + "\n\nMEDICATION:");
        JButton save = new JButton("Save Prescription");
        save.addActionListener(e -> {
            // Save logic here
            JOptionPane.showMessageDialog(prescriptionsFrame, "Saved!");
            prescriptionsFrame.dispose();
        });
        prescriptionsFrame.add(pDropdown, BorderLayout.NORTH);
        prescriptionsFrame.add(new JScrollPane(area), BorderLayout.CENTER);
        prescriptionsFrame.add(save, BorderLayout.SOUTH);
        prescriptionsFrame.setVisible(true);
    }

    private List<Patient> getMyPatients() {
        List<Patient> list = new ArrayList<>();
        String query = "SELECT p.patient_id, p.full_name, p.age, p.gender, MAX(a.appointment_date) as last_visit, a.status FROM patients p JOIN appointments a ON p.patient_id = a.patient_id WHERE a.doctor_id = ? GROUP BY p.patient_id";
        try (Connection con = DriverManager.getConnection(URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, doctorId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Patient p = new Patient();
                p.setPatientId(rs.getString("patient_id")); p.setName(rs.getString("full_name")); p.setAge(rs.getInt("age"));
                p.setGender(rs.getString("gender")); p.setLastVisit(rs.getString("last_visit")); p.setStatus(rs.getString("status"));
                list.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private List<Appointment> getTodaysAppointments() {
        List<Appointment> list = new ArrayList<>();
        String query = "SELECT a.appointment_time, p.full_name, a.reason, a.status FROM appointments a JOIN patients p ON a.patient_id = p.patient_id WHERE a.doctor_id = ? AND a.appointment_date = CURDATE()";
        try (Connection con = DriverManager.getConnection(URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, doctorId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Appointment app = new Appointment();
                app.setTime(rs.getString("appointment_time")); app.setPatientName(rs.getString("full_name"));
                app.setReason(rs.getString("reason")); app.setStatus(rs.getString("status"));
                list.add(app);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override Boolean login(String password) { return true; }
    @Override void logout() { new LoginPage().setVisible(true); }

    public static void main(String args[]){
        Doctor doc = new Doctor();
        doc.usename = "abrshiz"; // Example username
        doc.showDashboard();
    }
}

// Keep your Patient and Appointment classes outside
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