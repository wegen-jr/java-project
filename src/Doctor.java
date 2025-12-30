import Database.DoctorDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalTime;
import java.util.HashMap;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.table.TableRowSorter;
import java.util.List;
import java.util.Map;

public class Doctor extends staffUser {
    private String doctorId, fullName, specialization, contactNumber, email;
    private String currentUsername; // To store the login username

    public Doctor(String loginUsername) {
        this.currentUsername = loginUsername;
        loadDoctorData(); // Pulls the specific profile (Zebiba or Abrham)
    }

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
    private String getTimeGreeting() {
        int hour = LocalTime.now().getHour();
        if (hour >= 5 && hour < 12) return "Good Morning";
        if (hour >= 12 && hour < 17) return "Good Afternoon";
        return "Good Evening";
    }
    private String fetchLivePendingLabsCount() {
        // Only counts labs that are NOT completed
        String sql = "SELECT COUNT(*) FROM lab_requests WHERE doctor_id = ? AND status = 'Pending'";
        try (Connection con = Database.DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, this.doctorId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return String.valueOf(rs.getInt(1));
        } catch (SQLException e) { e.printStackTrace(); }
        return "0";
    }
    private String fetchLiveAppointmentCount() {
        // We only count today's appointments that are NOT 'Completed' or 'Cancelled'
        String sql = "SELECT COUNT(*) FROM appointments " +
                "WHERE doctor_id = ? " +
                "AND DATE(appointment_date) = CURDATE() " +
                "AND status NOT IN ('Completed', 'Cancelled')";

        try (Connection con = Database.DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, this.doctorId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return String.valueOf(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Appointment Count Error: " + e.getMessage());
        }
        return "0";
    }
    private String fetchLivePatientCount() {
        String sql = "SELECT COUNT(DISTINCT patient_id) FROM appointments WHERE doctor_id = ?";
        try (Connection con = Database.DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, this.doctorId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return String.valueOf(rs.getInt(1));
        } catch (SQLException e) { e.printStackTrace(); }
        return "0";
    }
    private JPanel createSummaryCard(String title, String value, Color accentColor, String iconPath) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Glass effect
                g2.setColor(new Color(255, 255, 255, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                // Border
                g2.setColor(new Color(255, 255, 255, 230));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 30, 30);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(280, 160));

        // Top Accent Strip
        JPanel accent = new JPanel();
        accent.setPreferredSize(new Dimension(0, 5));
        accent.setBackground(accentColor);
        card.add(accent, BorderLayout.NORTH);

        // Content (Icon and Value)
        JPanel content = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 30));
        content.setOpaque(false);

        try {
            ImageIcon icon = new ImageIcon(new ImageIcon(iconPath).getImage()
                    .getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            content.add(new JLabel(icon));
        } catch (Exception e) {}

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 46));
        lblValue.setForeground(new Color(30, 40, 50));
        content.add(lblValue);

        // Title at the bottom
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(100, 110, 120));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        card.add(content, BorderLayout.CENTER);
        card.add(lblTitle, BorderLayout.SOUTH);
        return card;
    }

    void showDashboard() {
        for (Frame f : Frame.getFrames()) {
            if (f instanceof JFrame && f.getTitle().contains("HMS - Hospital Management System (Doctor)")) {
                f.dispose();
            }
        }

        JFrame frame = new JFrame("HMS - Hospital Management System (Doctor)");
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Font navFont = new Font("SansSerif", Font.BOLD, 12);

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

        // --- Sidebar (Exact Original Design) ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(new Color(2, 48, 71));
        leftPanel.setPreferredSize(new Dimension(220, 0));
        leftPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1, true));

        JPanel portalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        portalPanel.setOpaque(false);
        portalPanel.setMaximumSize(new Dimension(250, 60));
        JLabel portalHeader = new JLabel("Doctor Portal", new ImageIcon(new ImageIcon("assets/portal.png").getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH)), JLabel.LEFT);
        portalHeader.setForeground(Color.WHITE);
        portalHeader.setFont(new Font("Arial", Font.BOLD, 18));
        portalPanel.add(portalHeader);

        leftPanel.add(portalPanel);
        leftPanel.add(new JSeparator());
        leftPanel.add(createNavItem("Dashboard", "assets/dashboard.png", navFont, this::showDashboard));
        leftPanel.add(createNavItem("My Patients", "assets/hospitalisation.png", navFont, this::showPatientsDashboard));
        leftPanel.add(createNavItem("Appointments", "assets/medical-appointment.png", navFont, this::showAppointmentsDashboard));
        leftPanel.add(createNavItem("Send Lab Requests", "assets/observation.png", navFont, this::showLabRequestsDashboard));
        leftPanel.add(createNavItem("Lab Results", "assets/Lab Response.png", navFont, this::showLabResultsDashboard));
        leftPanel.add(createNavItem("My Schedule", "assets/calendar.png", navFont, this::showScheduleDashboard));
        leftPanel.add(createNavItem("Prescriptions", "assets/medical-prescription.png", navFont, this::showPrescriptionsDashboard));
        leftPanel.add(createNavItem("My Profile", "assets/user.png", navFont, this::showDoctorProfile));
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(createNavItem("Logout", "assets/logout.png", navFont, () -> { frame.dispose(); logout(); }));

        mainBackgroundPanel.add(leftPanel, BorderLayout.WEST);

        // --- Right Content Area (Layout Fix) ---
        loadDoctorData();
        // Using BorderLayout for the right side to handle centering better
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);

        // Welcoming Text Header
        JPanel welcomeBox = new JPanel();
        welcomeBox.setLayout(new BoxLayout(welcomeBox, BoxLayout.Y_AXIS));
        welcomeBox.setOpaque(false);
        welcomeBox.setBorder(BorderFactory.createEmptyBorder(150, 100, 0, 0));

        JLabel welcomeLabel = new JLabel(getTimeGreeting() + ", Dr. " + (fullName != null ? fullName : "Doctor") + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 48)); // Increased size
        welcomeLabel.setForeground(new Color(2, 48, 71));

        // NEW FEATURE: Dynamic status line
        String statusNote = "<html><i>System status: Secure & Synchronized</i><br>You have <font color='#e63946'>" +
                fetchLivePendingLabsCount() + "</font> pending lab reports to review.</html>";
        JLabel statusLabel = new JLabel(statusNote);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        statusLabel.setForeground(new Color(50, 70, 90));

        welcomeBox.add(welcomeLabel);
        welcomeBox.add(Box.createVerticalStrut(10));
        welcomeBox.add(statusLabel);

        rightPanel.add(welcomeBox, BorderLayout.NORTH);

        // --- Summary Cards (Position Fix) ---
        JPanel cardContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        cardContainer.setOpaque(false);

        JPanel cardAppt = createSummaryCard("TODAY'S APPOINTMENTS", fetchLiveAppointmentCount(), new Color(2, 48, 71), "assets/medical-appointment.png");
        JPanel cardLab = createSummaryCard("PENDING LABS", fetchLivePendingLabsCount(), new Color(230, 57, 70), "assets/observation.png");
        JPanel cardPat = createSummaryCard("TOTAL PATIENTS", fetchLivePatientCount(), new Color(42, 157, 143), "assets/hospitalisation.png");

        cardContainer.add(cardAppt);
        cardContainer.add(cardLab);
        cardContainer.add(cardPat);

        // Putting cards in a wrapper to push them to the center of the screen
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(cardContainer);

        rightPanel.add(centerWrapper, BorderLayout.CENTER);

        mainBackgroundPanel.add(rightPanel, BorderLayout.CENTER);
        frame.add(mainBackgroundPanel);

        // --- THE LIVE SYNC HEARTBEAT ---
        Timer liveSyncTimer = new Timer(5000, e -> {
            if (!frame.isShowing()) { ((Timer)e.getSource()).stop(); return; }

            String pending = fetchLivePendingLabsCount();
            updateCardContent(cardAppt, fetchLiveAppointmentCount());
            updateCardContent(cardLab, pending);
            updateCardContent(cardPat, fetchLivePatientCount());

            // Update the welcoming subtitle live too!
            statusLabel.setText("<html><i>System status: Secure & Synchronized</i><br>You have <font color='#e63946'>" +
                    pending + "</font> pending lab reports to review.</html>");
        });
        liveSyncTimer.start();

        frame.setVisible(true);
    }
    void showPatientsDashboard() {
        JFrame patientsFrame = new JFrame("HMS - Patient Directory");
        patientsFrame.setSize(1100, 700);
        patientsFrame.setLocationRelativeTo(null);
        patientsFrame.getContentPane().setBackground(new Color(248, 250, 252));
        patientsFrame.setLayout(new BorderLayout());

        // --- 1. MODERN NAVIGATION HEADER ---
        JPanel navHeader = new JPanel(new BorderLayout());
        navHeader.setBackground(new Color(31, 41, 55));
        navHeader.setPreferredSize(new Dimension(0, 90));
        navHeader.setBorder(new javax.swing.border.EmptyBorder(0, 30, 0, 30));

        JPanel titleGroup = new JPanel(new GridLayout(2, 1));
        titleGroup.setOpaque(false);
        JLabel title = new JLabel("PATIENT MASTER DIRECTORY");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JLabel subTitle = new JLabel("Authorized Provider: Dr. " + (fullName != null ? fullName.toUpperCase() : "Clinical Staff"));
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTitle.setForeground(new Color(156, 163, 175));

        titleGroup.add(title);
        titleGroup.add(subTitle);
        navHeader.add(titleGroup, BorderLayout.WEST);

        // --- SEARCH SECTION ---
        JPanel searchWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 25));
        searchWrapper.setOpaque(false);

        // Search Icon (Replace "🔍" with your ImageIcon if needed)
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        searchIcon.setForeground(new Color(156, 163, 175));

        JTextField searchField = new JTextField("Search by ID or Name...");
        searchField.setPreferredSize(new Dimension(250, 35));
        searchField.setBackground(new Color(55, 65, 81));
        searchField.setForeground(new Color(156, 163, 175)); // Placeholder color
        searchField.setCaretColor(Color.WHITE);
        searchField.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        // Placeholder Logic: Make text removable on click/focus
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Search by ID or Name...")) {
                    searchField.setText("");
                    searchField.setForeground(Color.WHITE);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setForeground(new Color(156, 163, 175));
                    searchField.setText("Search by ID or Name...");
                }
            }
        });

        searchWrapper.add(searchIcon);
        searchWrapper.add(searchField);
        navHeader.add(searchWrapper, BorderLayout.EAST);

        // --- 2. STYLIZED TABLE ---
        String[] columns = {"ID", "FULL NAME", "AGE", "GENDER", "CONTACT", "REG. DATE"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        // Load Data using DAO
        DoctorDAO doctorDAO = new DoctorDAO();
        List<Object[]> patients = doctorDAO.getAllPatientsForDoctor(this.doctorId);
        for (Object[] row : patients) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        table.setRowHeight(55);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(241, 245, 249));
        table.setSelectionForeground(Color.BLACK);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Table Sorter
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // STRICT SEARCH LOGIC: Only ID (0) and Name (1)
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void filter() {
                String text = searchField.getText().trim();
                if (text.isEmpty() || text.equals("Search by ID or Name...")) {
                    sorter.setRowFilter(null);
                } else {
                    // Restricted to column index 0 and 1
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0, 1));
                }
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setForeground(new Color(100, 116, 139));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setPreferredSize(new Dimension(0, 50));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // --- 3. COMMAND FOOTER ---
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setPreferredSize(new Dimension(0, 80));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JPanel btnContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        btnContainer.setOpaque(false);

        JButton refreshBtn = new JButton("REFRESH RECORDS");
        styleButton(refreshBtn, new Color(241, 245, 249), Color.DARK_GRAY);
        refreshBtn.addActionListener(e -> { patientsFrame.dispose(); showPatientsDashboard(); });

        JButton closeBtn = new JButton("EXIT DIRECTORY");
        styleButton(closeBtn, new Color(31, 41, 55), Color.WHITE);
        closeBtn.addActionListener(e -> patientsFrame.dispose());

        btnContainer.add(refreshBtn);
        btnContainer.add(closeBtn);
        footer.add(btnContainer, BorderLayout.EAST);

        patientsFrame.add(navHeader, BorderLayout.NORTH);
        patientsFrame.add(scrollPane, BorderLayout.CENTER);
        patientsFrame.add(footer, BorderLayout.SOUTH);

        patientsFrame.setVisible(true);
    }
    void showAppointmentsDashboard() {
        JFrame appFrame = new JFrame("EHR System - Appointment Scheduler");
        appFrame.setSize(1100, 700);
        appFrame.setLocationRelativeTo(null);
        appFrame.getContentPane().setBackground(new Color(245, 247, 250));
        appFrame.setLayout(new BorderLayout());

        Color navDark = new Color(23, 32, 42);
        Color accentBlue = new Color(52, 152, 219);

        // Initialize DAO
        DoctorDAO doctorDAO = new DoctorDAO();

        // --- 1. TOP NAVIGATION ---
        JPanel topNav = new JPanel(new BorderLayout());
        topNav.setBackground(navDark);
        topNav.setPreferredSize(new Dimension(0, 80));
        topNav.setBorder(new javax.swing.border.EmptyBorder(0, 30, 0, 30));

        JPanel titleBox = new JPanel(new GridLayout(2, 1));
        titleBox.setOpaque(false);
        JLabel mainTitle = new JLabel("DAILY CLINICAL SCHEDULE");
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        mainTitle.setForeground(Color.WHITE);

        JLabel dateTag = new JLabel("DOUBLE-CLICK A ROW TO MARK AS COMPLETED");
        dateTag.setFont(new Font("Monospaced", Font.BOLD, 12));
        dateTag.setForeground(accentBlue);

        titleBox.add(mainTitle);
        titleBox.add(dateTag);
        topNav.add(titleBox, BorderLayout.WEST);
        appFrame.add(topNav, BorderLayout.NORTH);

        // --- 2. TABLE DESIGN ---
        String[] columns = {"ID", "TIME SLOT", "PATIENT NAME", "REASON FOR VISIT", "STATUS"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        // --- UPDATED LOAD LOGIC (SQL MOVED TO DAO) ---
        Runnable loadData = () -> {
            model.setRowCount(0);
            List<Object[]> appointments = doctorDAO.getTodayAppointments(this.doctorId);
            for (Object[] rowData : appointments) {
                model.addRow(rowData);
            }
        };

        loadData.run();

        JTable table = new JTable(model);
        table.setRowHeight(55);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        // --- UPDATED DOUBLE-CLICK (SQL MOVED TO DAO) ---
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    int apptId = (int) table.getValueAt(row, 0);
                    String currentStatus = (String) table.getValueAt(row, 4);

                    if (!currentStatus.equals("COMPLETED")) {
                        // Call DAO instead of local helper
                        doctorDAO.updateAppointmentStatus(apptId, "Completed");
                        loadData.run();
                    }
                }
            }
        });

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                if ("COMPLETED".equals(v)) lbl.setForeground(new Color(46, 204, 113));
                else lbl.setForeground(new Color(231, 76, 60));
                return lbl;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));
        appFrame.add(scrollPane, BorderLayout.CENTER);

        // --- 3. FOOTER ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        footer.setBackground(Color.WHITE);
        JButton closeBtn = new JButton("EXIT MODULE");
        styleNavButton(closeBtn, navDark, Color.WHITE);
        closeBtn.addActionListener(e -> appFrame.dispose());
        footer.add(closeBtn);
        appFrame.add(footer, BorderLayout.SOUTH);

        appFrame.setVisible(true);
    }
    void showLabRequestsDashboard() {
        JFrame labFrame = new JFrame("Laboratory EHR - Digital Order System");
        labFrame.setSize(550, 750);
        labFrame.setLocationRelativeTo(null);
        labFrame.setLayout(new BorderLayout());

        // Initialize DAO
        DoctorDAO doctorDAO = new DoctorDAO();

        // --- 1. TOP DIGITAL HEADER ---
        Color darkHeader = new Color(23, 32, 42);
        Color electricBlue = new Color(52, 152, 219);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(darkHeader);
        topBar.setPreferredSize(new Dimension(0, 70));
        topBar.setBorder(new javax.swing.border.EmptyBorder(0, 20, 0, 20));

        // Fetch the REAL ID from DAO
        String realId = doctorDAO.getNextLabRequestId();

        JLabel idDisplay = new JLabel("LAB REQ: #" + realId);
        idDisplay.setFont(new Font("Monospaced", Font.BOLD, 22));
        idDisplay.setForeground(electricBlue);

        JLabel doctorInfo = new JLabel("ORDERING DOCTOR ID: " + this.doctorId);
        doctorInfo.setForeground(Color.LIGHT_GRAY);
        doctorInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JPanel leftHeader = new JPanel(new GridLayout(2, 1));
        leftHeader.setOpaque(false);
        leftHeader.add(idDisplay);
        leftHeader.add(doctorInfo);

        topBar.add(leftHeader, BorderLayout.WEST);
        labFrame.add(topBar, BorderLayout.NORTH);

        // --- 2. THE FORM AREA ---
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(242, 243, 244));
        container.setBorder(new javax.swing.border.EmptyBorder(25, 25, 25, 25));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new javax.swing.border.EmptyBorder(25, 30, 25, 30)
        ));

        Font labelFont = new Font("Segoe UI", Font.BOLD, 11);
        Color labelColor = new Color(100, 100, 100);

        // Patient Dropdown (Loaded via DAO)
        card.add(new JLabel("PATIENT IDENTITY") {{ setFont(labelFont); setForeground(labelColor); }});
        JComboBox<String> pCombo = new JComboBox<>();

        List<String> patientList = doctorDAO.getDoctorPatientList(this.doctorId);
        for (String patient : patientList) {
            pCombo.addItem(patient);
        }

        pCombo.setMaximumSize(new Dimension(450, 35));
        card.add(pCombo); card.add(Box.createVerticalStrut(15));

        // Test Type
        card.add(new JLabel("TEST CATEGORY") {{ setFont(labelFont); setForeground(labelColor); }});
        String[] tests = {"Complete Blood Count", "X-Ray Chest", "MRI Scan", "Urinalysis", "Glucose Test", "Lipid Profile"};
        JComboBox<String> testCombo = new JComboBox<>(tests);
        testCombo.setMaximumSize(new Dimension(450, 35));
        card.add(testCombo); card.add(Box.createVerticalStrut(15));

        // Priority
        card.add(new JLabel("PRIORITY LEVEL") {{ setFont(labelFont); setForeground(labelColor); }});
        String[] priorities = {"Normal", "Urgent", "Emergency"};
        JComboBox<String> priorityCombo = new JComboBox<>(priorities);
        priorityCombo.setMaximumSize(new Dimension(450, 35));
        card.add(priorityCombo); card.add(Box.createVerticalStrut(15));

        // Notes
        card.add(new JLabel("DOCTOR'S CLINICAL NOTES") {{ setFont(labelFont); setForeground(labelColor); }});
        JTextArea notesArea = new JTextArea(5, 20);
        notesArea.setLineWrap(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setMaximumSize(new Dimension(450, 100));
        card.add(notesScroll); card.add(Box.createVerticalStrut(30));

        // --- 3. THE NAV ACTION BUTTON ---
        JPanel navBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        navBtn.setBackground(new Color(44, 62, 80));
        navBtn.setMaximumSize(new Dimension(450, 45));
        navBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel btnLabel = new JLabel("COMMIT LAB REQUEST");
        btnLabel.setForeground(Color.WHITE);
        btnLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        navBtn.add(btnLabel);

        navBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String selected = (String) pCombo.getSelectedItem();
                if (selected == null || notesArea.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(labFrame, "Incomplete Order: Please select patient and add notes.");
                    return;
                }

                String pId = selected.split(" - ")[0];

                // Execute Insertion via DAO
                boolean success = doctorDAO.submitLabRequest(
                        doctorId, pId,
                        testCombo.getSelectedItem().toString(),
                        priorityCombo.getSelectedItem().toString(),
                        notesArea.getText()
                );

                if (success) {
                    JOptionPane.showMessageDialog(labFrame, "Lab Request #" + realId + " Sent to Laboratory.");
                    labFrame.dispose();
                } else {
                    JOptionPane.showMessageDialog(labFrame, "Failed to submit request.");
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) { navBtn.setBackground(electricBlue); }
            @Override
            public void mouseExited(MouseEvent e) { navBtn.setBackground(new Color(44, 62, 80)); }
        });

        card.add(navBtn);
        container.add(card, BorderLayout.CENTER);
        labFrame.add(container);
        labFrame.setVisible(true);
    }
    void showLabResultsDashboard() {
        JFrame resultsFrame = new JFrame("Laboratory EHR - Results Viewer");
        resultsFrame.setSize(1150, 750);
        resultsFrame.setLocationRelativeTo(null);
        resultsFrame.getContentPane().setBackground(new Color(245, 247, 250));
        resultsFrame.setLayout(new BorderLayout());

        DoctorDAO doctorDAO = new DoctorDAO();

        // --- 1. THE HEADER ---
        Color navDark = new Color(23, 32, 42);
        Color electricBlue = new Color(52, 152, 219);

        JPanel topNav = new JPanel(new BorderLayout());
        topNav.setBackground(navDark);
        topNav.setPreferredSize(new Dimension(0, 85));
        topNav.setBorder(new javax.swing.border.EmptyBorder(0, 30, 0, 30));

        JPanel titleBox = new JPanel(new GridLayout(2, 1));
        titleBox.setOpaque(false);
        JLabel mainTitle = new JLabel("DIAGNOSTIC ARCHIVE");
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        mainTitle.setForeground(Color.WHITE);

        JLabel syncTag = new JLabel("Double-click any result to view full clinical details");
        syncTag.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        syncTag.setForeground(electricBlue);

        titleBox.add(mainTitle);
        titleBox.add(syncTag);
        topNav.add(titleBox, BorderLayout.WEST);
        resultsFrame.add(topNav, BorderLayout.NORTH);

        // --- 2. TABLE SETUP ---
        String[] columns = {"ORDER ID", "PATIENT", "TEST TYPE", "PRIORITY", "STATUS", "LAB RESULTS (DOUBLE-CLICK)"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        // --- 3. DATA LOADING ---
        Runnable loadData = () -> {
            model.setRowCount(0);
            long now = System.currentTimeMillis();
            List<Object[]> rawData = doctorDAO.getLabResultsForDoctor(this.doctorId);

            for (Object[] row : rawData) {
                int reqId = (int) row[0];
                String status = (String) row[4];
                String fullResultText = (row[5] == null) ? "Awaiting Results..." : row[5].toString();

                if (status.equalsIgnoreCase("Completed")) {
                    if (!completionTimers.containsKey(reqId)) completionTimers.put(reqId, now);
                    if ((now - completionTimers.get(reqId)) > 600000) continue;
                }

                String statusDisplay = (status.equalsIgnoreCase("Completed") ? "🟢 " : "🟡 ") + status.toUpperCase();
                model.addRow(new Object[]{
                        "L-ORD#" + reqId,
                        row[1].toString().toUpperCase(),
                        row[2],
                        row[3].toString().toUpperCase(),
                        statusDisplay,
                        fullResultText
                });
            }
        };
        loadData.run();

        // --- 4. TABLE DESIGN & INTERACTION ---
        JTable table = new JTable(model);
        table.setRowHeight(55);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        // Interaction 1: Change cursor to Hand on the Results Column
        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                table.setCursor(col == 5 ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        // Interaction 2: Double Click to Open Full Viewer
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    String content = table.getValueAt(row, 5).toString();
                    if (!content.equals("Awaiting Results...")) {
                        openFullResultViewer(
                                table.getValueAt(row, 0).toString(),
                                table.getValueAt(row, 1).toString(),
                                content
                        );
                    }
                }
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasF, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isSel, hasF, r, c);
                comp.setBackground(isSel ? new Color(220, 235, 250) : Color.WHITE);

                // Stylize the Results column to look clickable
                if (c == 5) {
                    comp.setForeground(new Color(52, 152, 219));
                    setFont(new Font("Segoe UI", Font.ITALIC, 13));
                } else {
                    comp.setForeground(Color.BLACK);
                }

                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(235, 238, 242)));
                return comp;
            }
        });

        table.getTableHeader().setPreferredSize(new Dimension(0, 50));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));
        resultsFrame.add(scrollPane, BorderLayout.CENTER);

        // --- 5. FOOTER ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        javax.swing.Timer autoRefresh = new javax.swing.Timer(30000, e -> loadData.run());
        autoRefresh.start();

        JButton refreshBtn = new JButton("REFRESH NOW");
        styleNavButton(refreshBtn, new Color(240, 242, 245), Color.DARK_GRAY);
        refreshBtn.addActionListener(e -> loadData.run());

        JButton closeBtn = new JButton("CLOSE MONITOR");
        styleNavButton(closeBtn, navDark, Color.WHITE);
        closeBtn.addActionListener(e -> { autoRefresh.stop(); resultsFrame.dispose(); });

        footer.add(refreshBtn); footer.add(closeBtn);
        resultsFrame.add(footer, BorderLayout.SOUTH);

        resultsFrame.setVisible(true);
    }
    private void openFullResultViewer(String orderId, String patient, String content) {
        JFrame detailFrame = new JFrame("Laboratory EHR - Diagnostic Report");
        detailFrame.setSize(600, 500);
        detailFrame.setLocationRelativeTo(null);

        JTextArea textArea = new JTextArea(content);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setMargin(new Insets(20, 20, 20, 20));

        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(new Color(23, 32, 42));
        header.setBorder(new javax.swing.border.EmptyBorder(10, 20, 10, 20));
        JLabel l1 = new JLabel("PATIENT: " + patient); l1.setForeground(Color.WHITE);
        JLabel l2 = new JLabel("ORDER: " + orderId); l2.setForeground(new Color(52, 152, 219));
        header.add(l1); header.add(l2);

        detailFrame.add(header, BorderLayout.NORTH);
        detailFrame.add(new JScrollPane(textArea), BorderLayout.CENTER);
        detailFrame.setVisible(true);
    }
    void showScheduleDashboard() {
        JFrame scheduleFrame = new JFrame("Clinical Schedule - Dr. " + (fullName != null ? fullName : ""));
        scheduleFrame.setSize(950, 600);
        scheduleFrame.setLocationRelativeTo(null);
        scheduleFrame.getContentPane().setBackground(Color.WHITE);
        scheduleFrame.setLayout(new BorderLayout(0, 0));

        // Initialize DAO
        DoctorDAO doctorDAO = new DoctorDAO();

        // --- 1. Header Panel (Navy Blue) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(2, 48, 71));
        headerPanel.setPreferredSize(new Dimension(950, 80));
        headerPanel.setBorder(new javax.swing.border.EmptyBorder(15, 25, 15, 25));

        JLabel titleLabel = new JLabel("Full Clinical Schedule");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Manage your patient timeline and clinical notes");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(200, 215, 225));

        JPanel textContainer = new JPanel(new GridLayout(2, 1));
        textContainer.setOpaque(false);
        textContainer.add(titleLabel);
        textContainer.add(subtitleLabel);
        headerPanel.add(textContainer, BorderLayout.WEST);

        // --- 2. Table and Data ---
        String[] columns = {"Time Slot", "Patient Name", "Status", "Clinical Notes"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        // --- DATA LOADING REDIRECTED TO DAO ---
        List<Object[]> scheduleData = doctorDAO.getFullClinicalSchedule(this.doctorId);
        for (Object[] row : scheduleData) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(230, 240, 250));
        table.setBackground(Color.WHITE);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(245, 248, 250));
        table.getTableHeader().setForeground(new Color(2, 48, 71));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(350);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // --- 3. Footer Action Panel ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 15));
        footer.setBackground(Color.WHITE);

        Color normalColor = new Color(2, 48, 71);
        Color hoverColor = new Color(6, 75, 110);

        JPanel closeBtnNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        closeBtnNav.setBackground(normalColor);
        closeBtnNav.setPreferredSize(new Dimension(180, 40));
        closeBtnNav.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel closeLabel = new JLabel("BACK TO PORTAL");
        closeLabel.setForeground(Color.WHITE);
        closeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        closeBtnNav.add(closeLabel);

        MouseAdapter navAction = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { scheduleFrame.dispose(); }
            @Override public void mouseEntered(MouseEvent e) { closeBtnNav.setBackground(hoverColor); }
            @Override public void mouseExited(MouseEvent e) { closeBtnNav.setBackground(normalColor); }
        };

        closeBtnNav.addMouseListener(navAction);
        closeLabel.addMouseListener(navAction);

        footer.add(closeBtnNav);
        scheduleFrame.add(headerPanel, BorderLayout.NORTH);
        scheduleFrame.add(scrollPane, BorderLayout.CENTER);
        scheduleFrame.add(footer, BorderLayout.SOUTH);

        scheduleFrame.setVisible(true);
    }
    void showPrescriptionsDashboard() {
        JFrame prescFrame = new JFrame("Clinical EHR - Digital Prescription System");
        prescFrame.setSize(800, 650);
        prescFrame.setLocationRelativeTo(null);
        prescFrame.setLayout(new BorderLayout());

        // Initialize DAO
        DoctorDAO doctorDAO = new DoctorDAO();

        // Modern Digital Palette
        Color darkHeader = new Color(23, 32, 42);
        Color electricBlue = new Color(52, 152, 219);
        Color bgSoft = new Color(242, 243, 244);
        Color navGray = new Color(248, 249, 250);

        // --- 1. TOP DIGITAL HEADER ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(darkHeader);
        topBar.setPreferredSize(new Dimension(0, 70));
        topBar.setBorder(new javax.swing.border.EmptyBorder(0, 20, 0, 20));

        // Fetch REAL ID from DAO
        String realId = doctorDAO.getNextPrescriptionId();

        JLabel idDisplay = new JLabel("ID: #" + realId);
        idDisplay.setFont(new Font("Monospaced", Font.BOLD, 22));
        idDisplay.setForeground(electricBlue);

        JLabel doctorInfo = new JLabel("LOGGED IN: DR. " + (fullName != null ? fullName.toUpperCase() : "ADMIN"));
        doctorInfo.setForeground(Color.LIGHT_GRAY);
        doctorInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel leftHeader = new JPanel(new GridLayout(2, 1));
        leftHeader.setOpaque(false);
        leftHeader.add(idDisplay);
        leftHeader.add(doctorInfo);

        topBar.add(leftHeader, BorderLayout.WEST);
        topBar.add(new JLabel("SYSTEM STATUS: ONLINE") {{ setForeground(new Color(46, 204, 113)); }}, BorderLayout.EAST);
        prescFrame.add(topBar, BorderLayout.NORTH);

        // --- 2. THE DIGITAL WORKSPACE ---
        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setBackground(bgSoft);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        JPanel leftCol = new JPanel(new GridLayout(4, 1, 0, 10));
        leftCol.setOpaque(false);

        // Patient Dropdown (Loaded via DAO)
        JComboBox<String> pDropdown = new JComboBox<>();
        pDropdown.setBorder(BorderFactory.createTitledBorder("PATIENT SELECTOR"));
        List<String> patients = doctorDAO.getDoctorPatientList(this.doctorId);
        for (String p : patients) pDropdown.addItem(p);

        JTextField diagField = new JTextField();
        diagField.setBorder(BorderFactory.createTitledBorder("CLINICAL DIAGNOSIS"));

        JTextField medField = new JTextField();
        medField.setBorder(BorderFactory.createTitledBorder("MEDICATION NAME"));

        JTextField doseField = new JTextField();
        doseField.setBorder(BorderFactory.createTitledBorder("DOSAGE (e.g. 500mg - 2x daily)"));

        leftCol.add(pDropdown);
        leftCol.add(diagField);
        leftCol.add(medField);
        leftCol.add(doseField);

        JTextArea instrArea = new JTextArea();
        instrArea.setBorder(BorderFactory.createTitledBorder("ADDITIONAL INSTRUCTIONS"));
        instrArea.setLineWrap(true);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.4; gbc.weighty = 1.0;
        mainContent.add(leftCol, gbc);
        gbc.gridx = 1; gbc.weightx = 0.6;
        mainContent.add(new JScrollPane(instrArea), gbc);

        prescFrame.add(mainContent, BorderLayout.CENTER);

        // --- 3. THE NAV ACTION BAR ---
        JPanel navActionPanel = new JPanel(new BorderLayout());
        navActionPanel.setBackground(navGray);
        navActionPanel.setPreferredSize(new Dimension(0, 65));
        navActionPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        rightNav.setOpaque(false);

        JButton cancelBtn = new JButton("DISCARD");
        styleNavButton(cancelBtn, new Color(149, 165, 166));

        JButton saveBtn = new JButton("COMMIT RECORD");
        styleNavButton(saveBtn, electricBlue);

        rightNav.add(cancelBtn);
        rightNav.add(saveBtn);
        navActionPanel.add(rightNav, BorderLayout.EAST);
        prescFrame.add(navActionPanel, BorderLayout.SOUTH);

        // --- Logic ---
        saveBtn.addActionListener(e -> {
            String selected = (String) pDropdown.getSelectedItem();
            if (selected == null || diagField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(prescFrame, "Error: Missing Mandatory Fields.");
                return;
            }

            String pId = selected.split(" - ")[0];

            // Execute Transaction via DAO
            boolean success = doctorDAO.submitPrescriptionAndRecord(
                    pId, this.doctorId,
                    medField.getText(),
                    doseField.getText(),
                    instrArea.getText(),
                    diagField.getText()
            );

            if (success) {
                JOptionPane.showMessageDialog(prescFrame, "Record Successfully Synced.");
                prescFrame.dispose();
            } else {
                JOptionPane.showMessageDialog(prescFrame, "System Error: Transaction Failed.");
            }
        });

        cancelBtn.addActionListener(e -> prescFrame.dispose());
        prescFrame.setVisible(true);
    }
    void showDoctorProfile() {
        JFrame profileFrame = new JFrame("EHR System - Staff Credentials");
        profileFrame.setSize(550, 800);
        profileFrame.setLocationRelativeTo(null);
        profileFrame.getContentPane().setBackground(new Color(242, 245, 248));
        profileFrame.setLayout(new BorderLayout());

        // Initialize DAO
        DoctorDAO doctorDAO = new DoctorDAO();

        // --- 1. DIGITAL IDENTITY HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(23, 32, 42));
        header.setPreferredSize(new Dimension(0, 110));
        header.setBorder(new javax.swing.border.EmptyBorder(25, 35, 20, 35));

        JLabel idTag = new JLabel("AUTHENTICATED STAFF ID: #" + this.doctorId);
        idTag.setFont(new Font("Monospaced", Font.BOLD, 13));
        idTag.setForeground(new Color(52, 152, 219));

        JLabel nameLabel = new JLabel("DR. " + (fullName != null ? fullName.toUpperCase() : "PHYSICIAN"));
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        nameLabel.setForeground(Color.WHITE);

        JPanel headerText = new JPanel(new GridLayout(2, 1));
        headerText.setOpaque(false);
        headerText.add(idTag);
        headerText.add(nameLabel);
        header.add(headerText, BorderLayout.WEST);

        profileFrame.add(header, BorderLayout.NORTH);

        // --- 2. THE DIGITAL DATA CONTAINER ---
        JPanel contentContainer = new JPanel();
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
        contentContainer.setBackground(Color.WHITE);
        contentContainer.setBorder(new javax.swing.border.EmptyBorder(30, 40, 30, 40));

        // FETCH DATA FROM DAO
        Map<String, String> doctorData = doctorDAO.getDoctorProfileData(this.doctorId);

        if (!doctorData.isEmpty()) {
            addProfileRow(contentContainer, "MEDICAL SPECIALIZATION", doctorData.get("specialization"));
            addProfileRow(contentContainer, "ASSIGNED DEPARTMENT", doctorData.get("department"));
            addProfileRow(contentContainer, "QUALIFICATIONS & DEGREES", doctorData.get("qualification"));
            addProfileRow(contentContainer, "PRACTICE LICENSE NUMBER", doctorData.get("license_number"));
            addProfileRow(contentContainer, "OFFICIAL CONTACT", doctorData.get("contact_number"));
            addProfileRow(contentContainer, "SYSTEM EMAIL", doctorData.get("email"));
            addProfileRow(contentContainer, "CLINICAL WORKING HOURS", doctorData.get("working_hours"));

            // Availability Badge
            JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            statusPanel.setOpaque(false);
            String status = doctorData.get("availability");
            JLabel statusIcon = new JLabel("● SYSTEM STATUS: " + status.toUpperCase());
            statusIcon.setFont(new Font("Segoe UI", Font.BOLD, 11));
            statusIcon.setForeground(status.equalsIgnoreCase("Available") ? new Color(46, 204, 113) : Color.ORANGE);
            statusPanel.add(statusIcon);
            contentContainer.add(statusPanel);
        }

        JScrollPane scroll = new JScrollPane(contentContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        profileFrame.add(scroll, BorderLayout.CENTER);

        // --- 3. NAV ACTION FOOTER ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 15));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(235, 235, 235)));

        JButton closeBtn = new JButton("DISMISS PROFILE");
        styleNavButton(closeBtn, new Color(23, 32, 42));
        closeBtn.addActionListener(e -> profileFrame.dispose());

        footer.add(closeBtn);
        profileFrame.add(footer, BorderLayout.SOUTH);

        profileFrame.setVisible(true);
    }
    private void updateCardContent(JPanel card, String newValue) {
        try {
            // card (BorderLayout) -> component at index 1 is the 'content' panel (FlowLayout)
            if (card.getComponentCount() > 1 && card.getComponent(1) instanceof JPanel) {
                JPanel content = (JPanel) card.getComponent(1);

                for (Component c : content.getComponents()) {
                    if (c instanceof JLabel) {
                        JLabel label = (JLabel) c;
                        // We only want to update the label that has text numbers, not the icon
                        if (label.getIcon() == null) {
                            if (!label.getText().equals(newValue)) {
                                label.setText(newValue);
                                card.revalidate();
                                card.repaint();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // This prevents the "rise error" from crashing the app
            System.err.println("Live update failed: " + e.getMessage());
        }
    }
    private void loadDoctorData() {
        if (this.currentUsername == null) return;

        // Initialize DAO
        DoctorDAO doctorDAO = new DoctorDAO();

        // Fetch the specific doctor object/map from DAO
        Map<String, String> data = doctorDAO.getDoctorByUsername(this.currentUsername);

        if (!data.isEmpty()) {
            this.doctorId = data.get("doctor_id");
            this.fullName = data.get("full_name");
            this.specialization = data.get("specialization");
            this.contactNumber = data.get("contact_number");
            this.email = data.get("email");
            System.out.println("✅ Portal context set to: " + this.fullName);
        } else {
            System.err.println("❌ Profile Load Error: No doctor found for username " + this.currentUsername);
        }
    }
    private HashMap<Integer, Long> completionTimers = new HashMap<>();
    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setPreferredSize(new Dimension(180, 40));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    private void styleNavButton(JButton btn, Color bg) {
        btn.setPreferredSize(new Dimension(180, 42));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    private void addProfileRow(JPanel parent, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(500, 65));
        row.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(new Color(100, 110, 120));

        String displayValue = (value == null || value.trim().isEmpty()) ? "Not Specified" : value;

        JLabel val = new JLabel(displayValue);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        val.setForeground(new Color(2, 48, 71));

        row.add(lbl, BorderLayout.NORTH);
        row.add(val, BorderLayout.CENTER);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(235, 235, 235));
        row.add(sep, BorderLayout.SOUTH);

        parent.add(row);
        parent.add(Box.createVerticalStrut(5));
    }
    private void styleNavButton(JButton btn, Color bg, Color fg) {
        btn.setPreferredSize(new Dimension(180, 40));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    @Override void logout() { new LoginPage().setVisible(true); }

    public static void main (String args[]){
        new Doctor("abrshiz").showDashboard();
    }
}
