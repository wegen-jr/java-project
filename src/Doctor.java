// Doctor.java - COMPLETE FIXED VERSION
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Doctor extends staffUser {

    // Doctor attributes
    private String doctorId;
    private String fullName;
    private String specialization;
    private String department;
    private String contactNumber;
    private String email;
    private String qualification;
    private double consultationFee;
    private String availability;

    // Database connection
    private static final String URL = "jdbc:mysql://localhost:3306/HMS";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "abrsh123";

    // Reference to main frame for closing
    private JFrame mainFrame;

    public Doctor() {
        this.role = "DOCTOR";
        // Initialize with default values to avoid null
        this.fullName = "Doctor";
        this.specialization = "General Physician";
        this.department = "General Medicine";
        this.availability = "Available";
        this.consultationFee = 500.00;
    }

    @Override
    Boolean login(String password) {
        String query = "SELECT * FROM authentication WHERE Username = ? AND Passworrd = ? AND Role = 'DOCTOR'";

        try (Connection con = DriverManager.getConnection(URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setString(1, this.usename);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                boolean loaded = loadDoctorFromDatabase(this.usename);
                if (!loaded) {
                    JOptionPane.showMessageDialog(null,
                            "Doctor profile not found in database!\nUsing default values.");
                }
                return true;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Login error: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    private boolean loadDoctorFromDatabase(String username) {
        String query = "SELECT * FROM doctors WHERE username = ?";

        try (Connection con = DriverManager.getConnection(URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                // Load ALL fields from database with null checks
                this.doctorId = getStringSafe(rs, "doctor_id");
                this.fullName = getStringSafe(rs, "full_name", "Doctor");
                this.specialization = getStringSafe(rs, "specialization", "General Physician");
                this.department = getStringSafe(rs, "department", "General Medicine");
                this.contactNumber = getStringSafe(rs, "contact_number", "Not available");
                this.email = getStringSafe(rs, "email", "Not available");
                this.qualification = getStringSafe(rs, "qualification", "Not specified");
                this.consultationFee = getDoubleSafe(rs, "consultation_fee", 500.00);
                this.availability = getStringSafe(rs, "availability", "Available");

                System.out.println("✓ Doctor loaded: " + this.fullName);
                return true;
            } else {
                System.out.println("⚠ Doctor not found in doctors table: " + username);
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Error loading doctor: " + e.getMessage());
            return false;
        }
    }

    // Helper methods for safe database value retrieval
    private String getStringSafe(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return rs.wasNull() ? null : value;
    }

    private String getStringSafe(ResultSet rs, String columnName, String defaultValue) {
        try {
            String value = rs.getString(columnName);
            return (value == null || rs.wasNull()) ? defaultValue : value;
        } catch (SQLException e) {
            return defaultValue;
        }
    }

    private double getDoubleSafe(ResultSet rs, String columnName, double defaultValue) {
        try {
            double value = rs.getDouble(columnName);
            return rs.wasNull() ? defaultValue : value;
        } catch (SQLException e) {
            return defaultValue;
        }
    }

    private List<Appointment> getAppointmentsFromDB() {
        List<Appointment> appointments = new ArrayList<>();

        if (this.doctorId == null) return appointments;

        String query = "SELECT * FROM appointments WHERE doctor_id = ? AND appointment_date = CURDATE() " +
                "ORDER BY appointment_time";

        try (Connection con = DriverManager.getConnection(URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setString(1, this.doctorId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Appointment app = new Appointment();
                app.setAppointmentId(rs.getInt("appointment_id"));
                app.setPatientName(getStringSafe(rs, "patient_name", "Unknown"));
                app.setPatientContact(getStringSafe(rs, "patient_contact", "N/A"));
                app.setDoctorId(getStringSafe(rs, "doctor_id"));
                app.setDate(rs.getDate("appointment_date").toLocalDate());
                app.setTime(rs.getTime("appointment_time").toLocalTime());
                app.setReason(getStringSafe(rs, "reason", "No reason specified"));
                app.setStatus(getStringSafe(rs, "status", "Scheduled"));
                appointments.add(app);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching appointments: " + e.getMessage());
        }

        return appointments;
    }

    @Override
    void showDashboard() {
        // First check if doctor data was loaded
        if (this.fullName == null || this.fullName.equals("Doctor")) {
            int choice = JOptionPane.showConfirmDialog(null,
                    "Doctor profile data not fully loaded.\n" +
                            "Would you like to continue with limited functionality?",
                    "Profile Warning",
                    JOptionPane.YES_NO_OPTION);

            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        mainFrame = new JFrame("HMS - Doctor Portal" +
                (this.fullName != null ? " - Dr. " + this.fullName : ""));
        mainFrame.setSize(1100, 750);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 248, 255));

        // 1. TOP: Header with logout button
        mainPanel.add(createHeaderWithLogout(), BorderLayout.NORTH);

        // 2. CENTER: Tabbed content
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.PLAIN, 14));

        tabs.addTab("🏠 Dashboard", createDashboardTab());
        tabs.addTab("📅 Appointments", createAppointmentsTab());
        tabs.addTab("⏰ Schedule", createScheduleTab());
        tabs.addTab("👤 Profile", createProfileTab());

        mainPanel.add(tabs, BorderLayout.CENTER);

        // 3. BOTTOM: Status bar with logout button
        mainPanel.add(createStatusBar(), BorderLayout.SOUTH);

        mainFrame.add(mainPanel);
        mainFrame.setVisible(true);
    }

    private JPanel createHeaderWithLogout() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(2, 48, 71));
        header.setPreferredSize(new Dimension(100, 100));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Left: Doctor info
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);

        // SAFE: Use non-null values with defaults
        String displayName = (this.fullName != null && !this.fullName.equals("Doctor")) ?
                "Dr. " + this.fullName : "Doctor";
        String displaySpecialization = (this.specialization != null) ?
                this.specialization : "General Physician";
        String displayDepartment = (this.department != null) ?
                this.department : "General Medicine";

        JLabel nameLabel = new JLabel("👨‍⚕️ " + displayName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        nameLabel.setForeground(Color.WHITE);

        JLabel specializationLabel = new JLabel(displaySpecialization + " | " + displayDepartment);
        specializationLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        specializationLabel.setForeground(new Color(200, 220, 255));

        infoPanel.add(nameLabel);
        infoPanel.add(specializationLabel);

        // Right: Logout button and quick info
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        rightPanel.setOpaque(false);

        // Current time
        JLabel timeLabel = new JLabel(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
        timeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timeLabel.setForeground(Color.WHITE);

        // Availability indicator - FIXED NULL CHECK
        String availabilityText = (this.availability != null) ? this.availability : "Unknown";
        Color availabilityColor = "Available".equals(this.availability) ? Color.GREEN :
                (this.availability != null && !this.availability.isEmpty()) ?
                        Color.YELLOW : Color.GRAY;

        JLabel availabilityLabel = new JLabel("● " + availabilityText);
        availabilityLabel.setFont(new Font("Arial", Font.BOLD, 14));
        availabilityLabel.setForeground(availabilityColor);

        // LOGOUT BUTTON
        JButton logoutButton = new JButton("🚪 Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        // Add hover effect
        logoutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(new Color(200, 35, 51));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(new Color(220, 53, 69));
            }
        });

        // Logout action
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(mainFrame,
                    "Are you sure you want to logout?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                performLogout();
            }
        });

        rightPanel.add(timeLabel);
        rightPanel.add(Box.createHorizontalStrut(10));
        rightPanel.add(availabilityLabel);
        rightPanel.add(Box.createHorizontalStrut(20));
        rightPanel.add(logoutButton);

        header.add(infoPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createDashboardTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Get real data
        List<Appointment> appointments = getAppointmentsFromDB();
        int totalAppointments = appointments.size();
        int completed = (int) appointments.stream()
                .filter(a -> a.getStatus() != null && a.getStatus().equals("Completed"))
                .count();
        int pending = totalAppointments - completed;

        // Top: Stats panel
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        statsPanel.setBackground(Color.WHITE);

        // SAFE: Check for null values
        String feeText = (this.consultationFee > 0) ? "₹" + this.consultationFee : "Not set";
        String availabilityText = (this.availability != null) ? this.availability : "Unknown";
        Color availabilityColor = "Available".equals(this.availability) ?
                new Color(46, 204, 113) : new Color(241, 196, 15);
        String departmentText = (this.department != null && !this.department.isEmpty()) ?
                this.department : "Not set";

        statsPanel.add(createDashboardCard("📅 Today's Appointments",
                String.valueOf(totalAppointments),
                new Color(41, 128, 185)));

        statsPanel.add(createDashboardCard("✅ Completed",
                String.valueOf(completed),
                new Color(39, 174, 96)));

        statsPanel.add(createDashboardCard("⏳ Pending",
                String.valueOf(pending),
                new Color(230, 126, 34)));

        statsPanel.add(createDashboardCard("💰 Consultation Fee",
                feeText,
                new Color(155, 89, 182)));

        statsPanel.add(createDashboardCard("📊 Availability",
                availabilityText,
                availabilityColor));

        statsPanel.add(createDashboardCard("🏥 Department",
                departmentText,
                new Color(52, 152, 219)));

        panel.add(statsPanel, BorderLayout.NORTH);

        // Center: Quick actions
        JPanel actionsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        actionsPanel.setBorder(BorderFactory.createTitledBorder("Quick Actions"));
        actionsPanel.setBackground(Color.WHITE);
        actionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Action buttons
        JButton viewAppointmentsBtn = createActionButton("📋 View All Appointments",
                new Color(52, 152, 219));
        JButton addAppointmentBtn = createActionButton("➕ Add New Appointment",
                new Color(46, 204, 113));
        JButton manageScheduleBtn = createActionButton("⏰ Manage Schedule",
                new Color(155, 89, 182));
        JButton quickLogoutBtn = createActionButton("🚪 Quick Logout",
                new Color(231, 76, 60));

        viewAppointmentsBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(mainFrame,
                    "Switch to 'Appointments' tab for details");
        });

        addAppointmentBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(mainFrame,
                    "Add appointment feature coming soon!");
        });

        manageScheduleBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(mainFrame,
                    "Switch to 'Schedule' tab");
        });

        quickLogoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(mainFrame,
                    "Logout immediately?",
                    "Quick Logout",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                performLogout();
            }
        });

        actionsPanel.add(viewAppointmentsBtn);
        actionsPanel.add(addAppointmentBtn);
        actionsPanel.add(manageScheduleBtn);
        actionsPanel.add(quickLogoutBtn);

        panel.add(actionsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    // FIXED METHOD: Changed createEmptyPadding to createEmptyBorder
    private JPanel createDashboardCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)  // FIXED HERE
        ));
        card.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createAppointmentsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Get appointments
        List<Appointment> appointments = getAppointmentsFromDB();

        // Table
        String[] columns = {"Time", "Patient Name", "Contact", "Reason", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        for (Appointment app : appointments) {
            model.addRow(new Object[]{
                    app.getTime() != null ? app.getTime().toString() : "N/A",
                    app.getPatientName() != null ? app.getPatientName() : "Unknown",
                    app.getPatientContact() != null ? app.getPatientContact() : "N/A",
                    app.getReason() != null ? app.getReason() : "No reason",
                    app.getStatus() != null ? app.getStatus() : "Scheduled"
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                "Today's Appointments (" + appointments.size() + ")"
        ));

        // Bottom panel with logout option
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton refreshBtn = new JButton("🔄 Refresh");
        JButton addBtn = new JButton("➕ Add Appointment");
        JButton logoutBtn = new JButton("🚪 Logout from Here");

        logoutBtn.setBackground(new Color(231, 76, 60));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.addActionListener(e -> performLogout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(addBtn);
        buttonPanel.add(logoutBtn);

        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createScheduleTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Create schedule management UI
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel title = new JLabel("⏰ Schedule Management");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(2, 48, 71));
        contentPanel.add(title, gbc);

        gbc.gridy = 1;
        JLabel desc = new JLabel("Manage your working hours and availability");
        desc.setFont(new Font("Arial", Font.PLAIN, 16));
        contentPanel.add(desc, gbc);

        gbc.gridy = 2;
        JButton setScheduleBtn = new JButton("📅 Set Weekly Schedule");
        setScheduleBtn.setFont(new Font("Arial", Font.BOLD, 14));
        setScheduleBtn.setBackground(new Color(52, 152, 219));
        setScheduleBtn.setForeground(Color.WHITE);
        setScheduleBtn.setPreferredSize(new Dimension(250, 50));
        contentPanel.add(setScheduleBtn, gbc);

        gbc.gridy = 3;
        JButton setLeaveBtn = new JButton("🏖️ Mark Leave");
        setLeaveBtn.setFont(new Font("Arial", Font.BOLD, 14));
        setLeaveBtn.setBackground(new Color(241, 196, 15));
        setLeaveBtn.setForeground(Color.BLACK);
        setLeaveBtn.setPreferredSize(new Dimension(250, 50));
        contentPanel.add(setLeaveBtn, gbc);

        // Logout button at bottom
        gbc.gridy = 4;
        JButton scheduleLogoutBtn = new JButton("🚪 Logout");
        scheduleLogoutBtn.setFont(new Font("Arial", Font.BOLD, 14));
        scheduleLogoutBtn.setBackground(new Color(231, 76, 60));
        scheduleLogoutBtn.setForeground(Color.WHITE);
        scheduleLogoutBtn.setPreferredSize(new Dimension(200, 40));
        scheduleLogoutBtn.addActionListener(e -> performLogout());
        contentPanel.add(scheduleLogoutBtn, gbc);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createProfileTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Profile content
        JPanel profilePanel = new JPanel(new GridBagLayout());
        profilePanel.setBackground(Color.WHITE);
        profilePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Title
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel title = new JLabel("👤 Doctor Profile");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(2, 48, 71));
        profilePanel.add(title, gbc);

        // Profile fields
        gbc.gridwidth = 1;
        addProfileField(profilePanel, gbc, 1, "Doctor ID:", this.doctorId);
        addProfileField(profilePanel, gbc, 2, "Full Name:", this.fullName);
        addProfileField(profilePanel, gbc, 3, "Username:", this.usename);
        addProfileField(profilePanel, gbc, 4, "Specialization:", this.specialization);
        addProfileField(profilePanel, gbc, 5, "Department:", this.department);
        addProfileField(profilePanel, gbc, 6, "Contact:", this.contactNumber);
        addProfileField(profilePanel, gbc, 7, "Email:", this.email);
        addProfileField(profilePanel, gbc, 8, "Qualification:", this.qualification);
        addProfileField(profilePanel, gbc, 9, "Consultation Fee:", "₹" + this.consultationFee);
        addProfileField(profilePanel, gbc, 10, "Availability:", this.availability);

        // Logout button at bottom
        gbc.gridy = 11;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton editProfileBtn = new JButton("✏️ Edit Profile");
        editProfileBtn.setBackground(new Color(52, 152, 219));
        editProfileBtn.setForeground(Color.WHITE);

        JButton profileLogoutBtn = new JButton("🚪 Logout from Profile");
        profileLogoutBtn.setBackground(new Color(231, 76, 60));
        profileLogoutBtn.setForeground(Color.WHITE);
        profileLogoutBtn.addActionListener(e -> performLogout());

        buttonPanel.add(editProfileBtn);
        buttonPanel.add(profileLogoutBtn);

        profilePanel.add(buttonPanel, gbc);

        JScrollPane scrollPane = new JScrollPane(profilePanel);
        scrollPane.setBorder(null);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void addProfileField(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        lbl.setForeground(Color.DARK_GRAY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        String displayValue = (value != null && !value.isEmpty()) ? value : "Not set";
        JTextField field = new JTextField(displayValue);
        field.setEditable(false);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        field.setPreferredSize(new Dimension(300, 25));
        panel.add(field, gbc);
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(240, 240, 240));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        statusBar.setPreferredSize(new Dimension(100, 40));

        // Left: Status info
        String doctorName = (this.fullName != null && !this.fullName.equals("Doctor")) ?
                this.fullName : "Doctor";
        String doctorId = (this.doctorId != null) ? this.doctorId : "N/A";

        JLabel statusInfo = new JLabel(" Doctor: " + doctorName +
                " | ID: " + doctorId +
                " | " + LocalDate.now());
        statusInfo.setFont(new Font("Arial", Font.PLAIN, 12));

        // Right: Small logout button
        JButton miniLogoutBtn = new JButton("Logout");
        miniLogoutBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        miniLogoutBtn.setBackground(new Color(231, 76, 60));
        miniLogoutBtn.setForeground(Color.WHITE);
        miniLogoutBtn.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        miniLogoutBtn.addActionListener(e -> performLogout());

        statusBar.add(statusInfo, BorderLayout.WEST);
        statusBar.add(miniLogoutBtn, BorderLayout.EAST);

        return statusBar;
    }

    // Centralized logout method
    private void performLogout() {
        // Show confirmation dialog
        String doctorName = (this.fullName != null && !this.fullName.equals("Doctor")) ?
                this.fullName : "Doctor";
        String doctorId = (this.doctorId != null) ? this.doctorId : "N/A";

        int choice = JOptionPane.showConfirmDialog(mainFrame,
                "Are you sure you want to logout?\n\n" +
                        "Doctor: " + doctorName + "\n" +
                        "ID: " + doctorId,
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            // Show logout message
            JOptionPane.showMessageDialog(mainFrame,
                    "Logout successful!\n\n" +
                            "Thank you for using HMS.\n" +
                            "Goodbye, Dr. " + doctorName + "!",
                    "Logged Out",
                    JOptionPane.INFORMATION_MESSAGE);

            // Close doctor dashboard
            mainFrame.dispose();

            // Return to login page
            SwingUtilities.invokeLater(() -> {
                try {
                    new LoginPage().setVisible(true);
                } catch (Exception e) {
                    System.out.println("Error returning to login: " + e.getMessage());
                }
            });
        }
    }

    @Override
    void logout() {
        performLogout();
    }
}

// Appointment model class
class Appointment {
    private int appointmentId;
    private String patientName;
    private String patientContact;
    private String doctorId;
    private LocalDate date;
    private LocalTime time;
    private String reason;
    private String status;

    // Getters and setters
    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientContact() { return patientContact; }
    public void setPatientContact(String patientContact) { this.patientContact = patientContact; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}