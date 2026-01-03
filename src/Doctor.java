import Database.DoctorDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.*;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.table.TableRowSorter;
import java.util.List;
import java.util.Map;

public class Doctor extends staffUser {
    private int authId;
    public JFrame Wframe;
    private int doctorId;
    private String fullName, specialization, contactNumber, email;
    private HashMap<Integer, Long> completionTimers = new HashMap<>();


    private JPanel mainBackgroundPanel;
    private JPanel leftPanel;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private Timer liveSyncTimer;

    public Doctor(int authId) {
        this.authId = authId;
        loadDoctorData(this.authId); // Load data immediately using the passed ID
        showDashboard();
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
        int count = new DoctorDAO().getPendingLabsCount(this.doctorId);
        return String.valueOf(count);
    }
    private String fetchLiveAppointmentCount() {
        int count = new DoctorDAO().getTodayAppointmentCount(this.doctorId);
        return String.valueOf(count);
    }
    private String fetchLivePatientCount() {
        int count = new DoctorDAO().getTotalPatientCount(this.doctorId);
        return String.valueOf(count);
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
        // Prevent multiple dashboard windows from opening
        for (Frame f : Frame.getFrames()) {
            if (f instanceof JFrame && f.getTitle().contains("HMS - Hospital Management System (Doctor)")) {
                f.dispose();
            }
        }

        Wframe = new JFrame("HMS - Hospital Management System (Doctor)");
        Wframe.setExtendedState(Frame.MAXIMIZED_BOTH);
        Wframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Font navFont = new Font("SansSerif", Font.BOLD, 12);

        // Main background with image support
        mainBackgroundPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon bgIcon = new ImageIcon("assets/DoctorsHomePage.jpg");
                if (bgIcon.getImage() != null) {
                    g.drawImage(bgIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
                }
            }
        };

        // --- Sidebar ---
        leftPanel = new JPanel();
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

        // Navigation Items
        leftPanel.add(createNavItem("Dashboard", "assets/dashboard.png", navFont, () -> switchToPanel("dashboard")));
        leftPanel.add(createNavItem("My Patients", "assets/hospitalisation.png", navFont, () -> switchToPanel("patients")));
        leftPanel.add(createNavItem("Appointments", "assets/medical-appointment.png", navFont, () -> switchToPanel("appointments")));
        leftPanel.add(createNavItem("Send Lab Requests", "assets/observation.png", navFont, () -> switchToPanel("labRequests")));
        leftPanel.add(createNavItem("Lab Results", "assets/Lab Response.png", navFont, () -> switchToPanel("labResults")));
        leftPanel.add(createNavItem("My Schedule", "assets/calendar.png", navFont, () -> switchToPanel("schedule")));
        leftPanel.add(createNavItem("Prescriptions", "assets/medical-prescription.png", navFont, () -> switchToPanel("prescriptions")));
        leftPanel.add(createNavItem("My Profile", "assets/user.png", navFont, () -> switchToPanel("profile")));

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(createNavItem("Logout", "assets/logout.png", navFont, () -> logout()));

        mainBackgroundPanel.add(leftPanel, BorderLayout.WEST);

        // --- Content Area with CardLayout ---
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        // Create and add all panels
        contentPanel.add(createDashboardPanel(), "dashboard");
        contentPanel.add(createPatientsDashboardPanel(), "patients");
        contentPanel.add(createAppointmentsDashboardPanel(), "appointments");
        contentPanel.add(createLabRequestsDashboardPanel(), "labRequests");
        contentPanel.add(createLabResultsDashboardPanel(), "labResults");
        contentPanel.add(createScheduleDashboardPanel(), "schedule");
        contentPanel.add(createPrescriptionsDashboardPanel(), "prescriptions");
        contentPanel.add(createDoctorProfilePanel(), "profile");

        mainBackgroundPanel.add(contentPanel, BorderLayout.CENTER);
        Wframe.add(mainBackgroundPanel);

        // Show dashboard initially
        cardLayout.show(contentPanel, "dashboard");
        Wframe.setVisible(true);
    }
    private void switchToPanel(String panelName) {
        cardLayout.show(contentPanel, panelName);
    }
    private JPanel createDashboardPanel() {
        // Make sure data is loaded using the new Auth Logic
        loadDoctorData(this.authId);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);

        // Welcome Header
        JPanel welcomeBox = new JPanel();
        welcomeBox.setLayout(new BoxLayout(welcomeBox, BoxLayout.Y_AXIS));
        welcomeBox.setOpaque(false);
        welcomeBox.setBorder(BorderFactory.createEmptyBorder(150, 100, 0, 0));

        // Uses your correctly reconstructed name
        JLabel welcomeLabel = new JLabel(getTimeGreeting() + ", Dr. " + (fullName != null ? fullName : "Doctor") + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 48));
        welcomeLabel.setForeground(new Color(2, 48, 71));

        String pendingInitial = fetchLivePendingLabsCount();
        String statusNote = "<html><i>System status: Secure & Synchronized</i><br>You have <font color='#e63946'>" +
                pendingInitial + "</font> pending lab reports to review.</html>";
        JLabel statusLabel = new JLabel(statusNote);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        statusLabel.setForeground(new Color(50, 70, 90));

        welcomeBox.add(welcomeLabel);
        welcomeBox.add(Box.createVerticalStrut(10));
        welcomeBox.add(statusLabel);

        rightPanel.add(welcomeBox, BorderLayout.NORTH);

        // --- Summary Cards ---
        JPanel cardContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        cardContainer.setOpaque(false);

        // Initial data fetch using the integer doctorId class variable
        JPanel cardAppt = createSummaryCard("TODAY'S APPOINTMENTS", fetchLiveAppointmentCount(), new Color(2, 48, 71), "assets/medical-appointment.png");
        JPanel cardLab = createSummaryCard("PENDING LABS", pendingInitial, new Color(230, 57, 70), "assets/observation.png");
        JPanel cardPat = createSummaryCard("TOTAL PATIENTS", fetchLivePatientCount(), new Color(42, 157, 143), "assets/hospitalisation.png");

        cardContainer.add(cardAppt);
        cardContainer.add(cardLab);
        cardContainer.add(cardPat);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(cardContainer);

        rightPanel.add(centerWrapper, BorderLayout.CENTER);

        // --- THE LIVE SYNC HEARTBEAT (5 Second Refresh) ---
        if (liveSyncTimer != null) {
            liveSyncTimer.stop();
        }
        liveSyncTimer = new Timer(5000, e -> {
            if (!Wframe.isShowing()) {
                ((Timer)e.getSource()).stop();
                return;
            }

            // Fetching fresh data via the updated fetch methods
            String pending = fetchLivePendingLabsCount();
            String appointments = fetchLiveAppointmentCount();
            String totalPatients = fetchLivePatientCount();

            // Update card values
            updateCardContent(cardAppt, appointments);
            updateCardContent(cardLab, pending);
            updateCardContent(cardPat, totalPatients);

            // Update the dynamic greeting subtitle
            statusLabel.setText("<html><i>System status: Secure & Synchronized</i><br>You have <font color='#e63946'>" +
                    pending + "</font> pending lab reports to review.</html>");
        });
        liveSyncTimer.start();

        return rightPanel;
    }
    private JPanel createPatientsDashboardPanel() {
        JPanel patientsPanel = new JPanel(new BorderLayout());
        patientsPanel.setBackground(new Color(248, 250, 252));
        patientsPanel.setOpaque(false);

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

        // Displays the doctor's name from the class variable
        JLabel subTitle = new JLabel("Authorized Provider: Dr. " + (fullName != null ? fullName.toUpperCase() : "Clinical Staff"));
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTitle.setForeground(new Color(156, 163, 175));

        titleGroup.add(title);
        titleGroup.add(subTitle);
        navHeader.add(titleGroup, BorderLayout.WEST);

        // --- SEARCH SECTION ---
        JPanel searchWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 25));
        searchWrapper.setOpaque(false);

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        searchIcon.setForeground(new Color(156, 163, 175));

        JTextField searchField = new JTextField("Search by ID, Name, or Contact...");
        searchField.setPreferredSize(new Dimension(300, 35));
        searchField.setBackground(new Color(55, 65, 81));
        searchField.setForeground(new Color(156, 163, 175));
        searchField.setCaretColor(Color.WHITE);
        searchField.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Search by ID, Name, or Contact...")) {
                    searchField.setText("");
                    searchField.setForeground(Color.WHITE);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setForeground(new Color(156, 163, 175));
                    searchField.setText("Search by ID, Name, or Contact...");
                }
            }
        });

        searchWrapper.add(searchIcon);
        searchWrapper.add(searchField);
        navHeader.add(searchWrapper, BorderLayout.EAST);

        patientsPanel.add(navHeader, BorderLayout.NORTH);

        // --- 2. STYLIZED TABLE WITH ALL PATIENT INFORMATION ---
        String[] columns = {
                "Patient ID",
                "First Name",
                "Middle Name",
                "Last Name",
                "Date of Birth",
                "Gender",
                "Contact",
                "Email",
                "Address",
                "Emergency Contact",
                "Blood Type",
                "Registration Date",
                "Age"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };

        // --- DATA FETCHING WITH CALCULATED AGE ---
        DoctorDAO doctorDAO = new DoctorDAO();
        List<Object[]> patients = doctorDAO.getAllPatientsForDoctor(this.doctorId);

        if (patients != null) {
            for (Object[] row : patients) {
                // Calculate age from date of birth
                String dobString = row[4].toString(); // Assuming date_of_birth is at index 4
                int age = calculateAgeFromDOB(dobString);

                // Create new row with all data + calculated age
                Object[] fullRow = new Object[]{
                        row[0], // patient_id
                        row[1], // first_name
                        row[2], // middle_name
                        row[3], // last_name
                        row[4], // date_of_birth
                        row[5], // gender
                        row[6], // contact_number
                        row[7], // email
                        row[8], // address
                        row[9], // emergency_contact
                        row[10], // blood_type
                        row[11], // registration_date
                        age + " years" // Calculated age
                };
                model.addRow(fullRow);
            }
        }

        JTable table = new JTable(model);
        table.setRowHeight(40); // Slightly smaller to fit more data
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(241, 245, 249));
        table.setSelectionForeground(Color.BLACK);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Smaller font for more columns

        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(80);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(80);  // First Name
        table.getColumnModel().getColumn(2).setPreferredWidth(80);  // Middle Name
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Last Name
        table.getColumnModel().getColumn(4).setPreferredWidth(90);  // DOB
        table.getColumnModel().getColumn(5).setPreferredWidth(60);  // Gender
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // Contact
        table.getColumnModel().getColumn(7).setPreferredWidth(120); // Email
        table.getColumnModel().getColumn(8).setPreferredWidth(150); // Address
        table.getColumnModel().getColumn(9).setPreferredWidth(100); // Emergency Contact
        table.getColumnModel().getColumn(10).setPreferredWidth(70); // Blood Type
        table.getColumnModel().getColumn(11).setPreferredWidth(100); // Reg Date
        table.getColumnModel().getColumn(12).setPreferredWidth(70); // Age

        // Table Sorter for Search
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Enhanced search to search across multiple columns
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void filter() {
                String text = searchField.getText().trim().toLowerCase();
                if (text.isEmpty() || text.equals("search by id, name, or contact...")) {
                    sorter.setRowFilter(null);
                } else {
                    // Search across multiple columns: ID, names, contact, email
                    RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + text,
                            0, 1, 2, 3, 6, 7); // Search in ID, first name, middle name, last name, contact, email
                    sorter.setRowFilter(rf);
                }
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        // Custom header renderer
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setForeground(new Color(100, 116, 139));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Add mouse listener for double-click to view full details
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        int modelRow = table.convertRowIndexToModel(row);
                        showPatientDetailsDialog(
                                model.getValueAt(modelRow, 0).toString(),  // ID
                                model.getValueAt(modelRow, 1).toString(),  // First Name
                                model.getValueAt(modelRow, 2).toString(),  // Middle Name
                                model.getValueAt(modelRow, 3).toString(),  // Last Name
                                model.getValueAt(modelRow, 4).toString(),  // DOB
                                model.getValueAt(modelRow, 5).toString(),  // Gender
                                model.getValueAt(modelRow, 6).toString(),  // Contact
                                model.getValueAt(modelRow, 7).toString(),  // Email
                                model.getValueAt(modelRow, 8).toString(),  // Address
                                model.getValueAt(modelRow, 9).toString(),  // Emergency Contact
                                model.getValueAt(modelRow, 10).toString(), // Blood Type
                                model.getValueAt(modelRow, 11).toString(), // Registration Date
                                model.getValueAt(modelRow, 12).toString()  // Age
                        );
                    }
                }
            }
        });

        patientsPanel.add(scrollPane, BorderLayout.CENTER);

        // --- 3. ENHANCED COMMAND FOOTER ---
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setPreferredSize(new Dimension(0, 80));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        // Left side: Summary info
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        summaryPanel.setOpaque(false);

        int totalPatients = model.getRowCount();
        JLabel summaryLabel = new JLabel("Total Patients: " + totalPatients);
        summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        summaryLabel.setForeground(new Color(31, 41, 55));

        summaryPanel.add(summaryLabel);
        footer.add(summaryPanel, BorderLayout.WEST);

        // Right side: Action buttons
        JPanel btnContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        btnContainer.setOpaque(false);

        JButton refreshBtn = new JButton("🔄 REFRESH RECORDS");
        styleNavButton(refreshBtn, new Color(59, 130, 246), Color.WHITE);
        refreshBtn.addActionListener(e -> {
            // Refresh the panel
            contentPanel.remove(patientsPanel);
            contentPanel.add(createPatientsDashboardPanel(), "patients");
            cardLayout.show(contentPanel, "patients");

            // Show notification
            JOptionPane.showMessageDialog(Wframe,
                    "Patient records refreshed successfully!",
                    "Refresh Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        });



        JButton closeBtn = new JButton("← BACK TO DASHBOARD");
        styleNavButton(closeBtn, new Color(31, 41, 55), Color.WHITE);
        closeBtn.addActionListener(e -> switchToPanel("dashboard"));
        btnContainer.add(refreshBtn);
        btnContainer.add(closeBtn);
        footer.add(btnContainer, BorderLayout.EAST);

        patientsPanel.add(footer, BorderLayout.SOUTH);

        return patientsPanel;
    }
    private int calculateAgeFromDOB(String dobString) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate dob = LocalDate.parse(dobString, formatter);
            LocalDate now = LocalDate.now();
            return Period.between(dob, now).getYears();
        } catch (Exception e) {
            return 0; // Return 0 if calculation fails
        }
    }
    private void showPatientDetailsDialog(String id, String firstName, String middleName, String lastName,
                                          String dob, String gender, String contact, String email,
                                          String address, String emergencyContact, String bloodType,
                                          String regDate, String age) {
        JDialog detailsDialog = new JDialog(Wframe, "Patient Details - " + id, true);
        detailsDialog.setSize(500, 600);
        detailsDialog.setLocationRelativeTo(Wframe);
        detailsDialog.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(31, 41, 55));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("PATIENT DETAILS - " + id);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        detailsDialog.add(headerPanel, BorderLayout.NORTH);

        // Details Panel
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Create detail rows
        detailsPanel.add(createDetailRow("Full Name:", firstName + " " + middleName + " " + lastName));
        detailsPanel.add(createDetailRow("Date of Birth:", dob + " (" + age + ")"));
        detailsPanel.add(createDetailRow("Gender:", gender));
        detailsPanel.add(createDetailRow("Contact Number:", contact));
        detailsPanel.add(createDetailRow("Email:", email.isEmpty() ? "Not Provided" : email));
        detailsPanel.add(createDetailRow("Address:", address.isEmpty() ? "Not Provided" : address));
        detailsPanel.add(createDetailRow("Emergency Contact:", emergencyContact.isEmpty() ? "Not Provided" : emergencyContact));
        detailsPanel.add(createDetailRow("Blood Type:", bloodType.isEmpty() ? "Not Provided" : bloodType));
        detailsPanel.add(createDetailRow("Registration Date:", regDate));

        JScrollPane scrollPane = new JScrollPane(detailsPanel);
        scrollPane.setBorder(null);
        detailsDialog.add(scrollPane, BorderLayout.CENTER);

        // Footer with close button
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> detailsDialog.dispose());
        styleNavButton(closeBtn, new Color(31, 41, 55), Color.WHITE);

        footerPanel.add(closeBtn);
        detailsDialog.add(footerPanel, BorderLayout.SOUTH);

        detailsDialog.setVisible(true);
    }
    private JPanel createDetailRow(String label, String value) {
        JPanel rowPanel = new JPanel(new BorderLayout());
        rowPanel.setOpaque(false);
        rowPanel.setMaximumSize(new Dimension(450, 40));
        rowPanel.setBorder(new EmptyBorder(5, 0, 5, 0));

        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelLabel.setForeground(new Color(75, 85, 99));
        labelLabel.setPreferredSize(new Dimension(150, 25));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        valueLabel.setForeground(new Color(31, 41, 55));

        rowPanel.add(labelLabel, BorderLayout.WEST);
        rowPanel.add(valueLabel, BorderLayout.CENTER);

        // Add separator
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(229, 231, 235));
        rowPanel.add(separator, BorderLayout.SOUTH);

        return rowPanel;
    }
    private JPanel createAppointmentsDashboardPanel() {
        JPanel appPanel = new JPanel(new BorderLayout());
        appPanel.setBackground(new Color(245, 247, 250));

        Color navDark = new Color(23, 32, 42);
        Color accentBlue = new Color(52, 152, 219);
        DoctorDAO doctorDAO = new DoctorDAO();

        // --- 1. TOP NAVIGATION WITH LIVE INDICATOR ---
        JPanel topNav = new JPanel(new BorderLayout());
        topNav.setBackground(navDark);
        topNav.setPreferredSize(new Dimension(0, 100)); // Slightly taller for Nav
        topNav.setBorder(new javax.swing.border.EmptyBorder(10, 30, 10, 30));

        // Nav Bar (Top Right)
        JPanel navLinks = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navLinks.setOpaque(false);
        JLabel backNav = new JLabel("RETURN TO DASHBOARD");
        backNav.setFont(new Font("Segoe UI", Font.BOLD, 12));
        backNav.setForeground(new Color(189, 195, 199));
        backNav.setCursor(new Cursor(Cursor.HAND_CURSOR));
        navLinks.add(backNav);

        JPanel titleBox = new JPanel(new GridLayout(2, 1));
        titleBox.setOpaque(false);
        JLabel mainTitle = new JLabel("DAILY CLINICAL SCHEDULE");
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        mainTitle.setForeground(Color.WHITE);

        JLabel liveStatus = new JLabel("● LIVE SYNC ACTIVE");
        liveStatus.setFont(new Font("Monospaced", Font.BOLD, 12));
        liveStatus.setForeground(new Color(46, 204, 113)); // Green status

        titleBox.add(mainTitle);
        titleBox.add(liveStatus);

        topNav.add(navLinks, BorderLayout.NORTH);
        topNav.add(titleBox, BorderLayout.WEST);
        appPanel.add(topNav, BorderLayout.NORTH);

        // --- 2. TABLE DESIGN ---
        String[] columns = {"ID", "TIME SLOT", "PATIENT NAME", "REASON FOR VISIT", "STATUS"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        // Load Data Helper
        Runnable loadData = () -> {
            // We only clear if the data has actually changed to prevent flickering
            List<Object[]> appointments = doctorDAO.getTodayAppointments(this.doctorId);
            model.setRowCount(0);
            if (appointments != null) {
                for (Object[] rowData : appointments) {
                    model.addRow(rowData);
                }
            }
        };

        // --- LIVE TIMER (REFRESH EVERY 5 SECONDS) ---
        Timer liveSyncTimer = new Timer(5000, e -> loadData.run());
        liveSyncTimer.start();

        // Navigation Click Logic
        backNav.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                liveSyncTimer.stop(); // Stop the thread when leaving
                switchToPanel("dashboard");
            }
            @Override
            public void mouseEntered(MouseEvent e) { backNav.setForeground(Color.WHITE); }
            @Override
            public void mouseExited(MouseEvent e) { backNav.setForeground(new Color(189, 195, 199)); }
        });

        loadData.run(); // Initial manual load

        JTable table = new JTable(model);
        table.setRowHeight(55);
        table.setSelectionBackground(new Color(235, 245, 251));
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        // --- DOUBLE-CLICK LOGIC ---
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        int apptId = Integer.parseInt(table.getValueAt(row, 0).toString());
                        Object statusValue = table.getValueAt(row, 4);
                        String currentStatus = (statusValue != null) ? statusValue.toString().trim() : "";

                        String nextStatus = "";
                        // Matches your current DB casing/logic
                        if (currentStatus.equalsIgnoreCase("SCHEDULED") || currentStatus.isEmpty()) {
                            nextStatus = "Confirmed";
                        } else if (currentStatus.equalsIgnoreCase("CONFIRMED")) {
                            nextStatus = "Completed";
                        }

                        if (!nextStatus.isEmpty()) {
                            if (doctorDAO.updateAppointmentStatus(apptId, nextStatus)) {
                                loadData.run(); // Immediate UI feedback
                            }
                        }
                    }
                }
            }
        });

        // --- STATUS COLOR RENDERER ---
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                String status = (v != null) ? v.toString().toUpperCase().trim() : "";
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));

                if (status.equals("CONFIRMED")) {
                    lbl.setForeground(accentBlue);
                } else if (status.equals("COMPLETED")) {
                    lbl.setForeground(new Color(46, 204, 113));
                } else {
                    lbl.setForeground(new Color(231, 76, 60));
                }
                return lbl;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        scrollPane.getViewport().setBackground(Color.WHITE);
        appPanel.add(scrollPane, BorderLayout.CENTER);

        return appPanel;
    }
    private JPanel createLabRequestsDashboardPanel() {
        JPanel labPanel = new JPanel(new BorderLayout());
        labPanel.setOpaque(false);

        DoctorDAO doctorDAO = new DoctorDAO();
        Color darkHeader = new Color(23, 32, 42);
        Color electricBlue = new Color(52, 152, 219);

        // --- 1. TOP DIGITAL HEADER ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(darkHeader);
        topBar.setPreferredSize(new Dimension(0, 80));
        topBar.setBorder(new javax.swing.border.EmptyBorder(5, 20, 5, 20));

        // Nav Bar for Dismissal
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navBar.setOpaque(false);
        JLabel dismissNav = new JLabel("DISMISS REQUEST");
        dismissNav.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dismissNav.setForeground(new Color(189, 195, 199));
        dismissNav.setCursor(new Cursor(Cursor.HAND_CURSOR));

        String realId = doctorDAO.getNextLabRequestId();
        JLabel idDisplay = new JLabel("LAB REQ: #" + (realId != null ? realId : "NEW"));
        idDisplay.setFont(new Font("Monospaced", Font.BOLD, 22));
        idDisplay.setForeground(electricBlue);

        JPanel leftHeader = new JPanel(new GridLayout(2, 1));
        leftHeader.setOpaque(false);
        leftHeader.add(idDisplay);

        JLabel syncLabel = new JLabel("● AUTO-SYNCING CONFIRMED PATIENTS");
        syncLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        syncLabel.setForeground(new Color(46, 204, 113)); // Live Green
        leftHeader.add(syncLabel);

        topBar.add(navBar, BorderLayout.NORTH);
        topBar.add(leftHeader, BorderLayout.WEST);
        labPanel.add(topBar, BorderLayout.NORTH);

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

        card.add(new JLabel("ACTIVE CONFIRMED PATIENT") {{
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setForeground(new Color(100, 100, 100));
        }});

        JComboBox<String> pCombo = new JComboBox<>();
        pCombo.setMaximumSize(new Dimension(450, 35));
        card.add(pCombo); card.add(Box.createVerticalStrut(15));

        Runnable refreshPatients = () -> {
            String currentSelection = (String) pCombo.getSelectedItem();
            List<Object[]> appointments = doctorDAO.getTodayAppointments(this.doctorId);

            DefaultComboBoxModel<String> newModel = new DefaultComboBoxModel<>();
            boolean foundConfirmed = false;

            if (appointments != null) {
                for (Object[] appt : appointments) {
                    // Index 4 is Status (must be "CONFIRMED")
                    // Note: Since DAO uses .toUpperCase(), we check against "CONFIRMED"
                    if (appt[4] != null && appt[4].toString().equalsIgnoreCase("CONFIRMED")) {

                        // THE FIX: Pull Patient ID from Index 5 and Name from Index 2
                        String patientId = appt[5].toString();
                        String patientName = appt[2].toString();

                        newModel.addElement(patientId + " - " + patientName);
                        foundConfirmed = true;
                    }
                }
            }

            if (!foundConfirmed) {
                newModel.addElement("No Confirmed Patients Available");
                pCombo.setEnabled(false);
            } else {
                pCombo.setEnabled(true);
                // Prevent constant flickering by only updating if items changed
                if (pCombo.getModel().getSize() != newModel.getSize()) {
                    pCombo.setModel(newModel);
                }
                if (currentSelection != null) pCombo.setSelectedItem(currentSelection);
            }
        };


        refreshPatients.run();
        Timer liveLabTimer = new Timer(2000, e -> refreshPatients.run());
        liveLabTimer.start();


        dismissNav.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                liveLabTimer.stop();
                switchToPanel("dashboard");
            }
            @Override public void mouseEntered(MouseEvent e) { dismissNav.setForeground(Color.WHITE); }
            @Override public void mouseExited(MouseEvent e) { dismissNav.setForeground(new Color(189, 195, 199)); }
        });


        card.add(new JLabel("TEST CATEGORY") {{ setFont(new Font("Segoe UI", Font.BOLD, 11)); setForeground(new Color(100, 100, 100)); }});
        String[] tests = {"Complete Blood Count", "X-Ray Chest", "MRI Scan", "Urinalysis", "Glucose Test"};
        JComboBox<String> testCombo = new JComboBox<>(tests);
        testCombo.setMaximumSize(new Dimension(450, 35));
        card.add(testCombo); card.add(Box.createVerticalStrut(15));

        card.add(new JLabel("CLINICAL NOTES") {{ setFont(new Font("Segoe UI", Font.BOLD, 11)); setForeground(new Color(100, 100, 100)); }});
        JTextArea notesArea = new JTextArea(5, 20);
        notesArea.setLineWrap(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setMaximumSize(new Dimension(450, 100));
        card.add(notesScroll); card.add(Box.createVerticalStrut(30));

        // Submit Button
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
                if (!pCombo.isEnabled() || pCombo.getSelectedItem().toString().contains("No Confirmed")) return;

                String selected = (String) pCombo.getSelectedItem();
                String pId = selected.split(" - ")[0];

                boolean success = doctorDAO.submitLabRequest(doctorId, pId, testCombo.getSelectedItem().toString(), "Normal", notesArea.getText().trim());
                if (success) {
                    liveLabTimer.stop();
                    JOptionPane.showMessageDialog(null, "Request Sent.");
                    switchToPanel("dashboard");
                }
            }
            @Override public void mouseEntered(MouseEvent e) { navBtn.setBackground(electricBlue); }
            @Override public void mouseExited(MouseEvent e) { navBtn.setBackground(new Color(44, 62, 80)); }
        });

        card.add(navBtn);
        container.add(card, BorderLayout.CENTER);
        labPanel.add(container);

        return labPanel;
    }
    private JPanel createLabResultsDashboardPanel() {
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBackground(new Color(245, 247, 250)); // Your light background

        DoctorDAO doctorDAO = new DoctorDAO();
        Color navDark = new Color(23, 32, 42);
        Color electricBlue = new Color(52, 152, 219);

        // --- 1. TOP DIGITAL HEADER ---
        JPanel topNav = new JPanel(new BorderLayout());
        topNav.setBackground(navDark);
        topNav.setPreferredSize(new Dimension(0, 100));
        topNav.setBorder(new javax.swing.border.EmptyBorder(10, 30, 10, 30));

        // Nav Bar for Dismissal
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navBar.setOpaque(false);
        JLabel backNav = new JLabel("RETURN TO DASHBOARD");
        backNav.setFont(new Font("Segoe UI", Font.BOLD, 12));
        backNav.setForeground(new Color(189, 195, 199));
        backNav.setCursor(new Cursor(Cursor.HAND_CURSOR));
        navBar.add(backNav);

        JPanel titleBox = new JPanel(new GridLayout(2, 1));
        titleBox.setOpaque(false);
        JLabel mainTitle = new JLabel("DIAGNOSTIC ARCHIVE");
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        mainTitle.setForeground(Color.WHITE);

        JLabel syncTag = new JLabel("● LIVE LAB FEED ACTIVE (AUTO-REFRESH)");
        syncTag.setFont(new Font("Monospaced", Font.BOLD, 11));
        syncTag.setForeground(new Color(46, 204, 113)); // Pulsing Green

        titleBox.add(mainTitle);
        titleBox.add(syncTag);
        topNav.add(navBar, BorderLayout.NORTH);
        topNav.add(titleBox, BorderLayout.WEST);
        resultsPanel.add(topNav, BorderLayout.NORTH);

        // --- 2. TABLE DATA LOGIC ---
        String[] columns = {"ORDER ID", "PATIENT", "TEST TYPE", "PRIORITY", "STATUS", "LAB RESULTS (DOUBLE-CLICK)"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        Runnable loadData = () -> {
            model.setRowCount(0);
            long now = System.currentTimeMillis();
            List<Object[]> rawData = doctorDAO.getLabResultsForDoctor(this.doctorId);

            if (rawData != null) {
                for (Object[] row : rawData) {
                    if (row == null || row[0] == null) continue;

                    int reqId = (int) row[0];
                    String pName = (row[1] != null) ? row[1].toString().toUpperCase() : "UNKNOWN";
                    String tType = (row[2] != null) ? row[2].toString() : "N/A";
                    String priority = (row[3] != null) ? row[3].toString().toUpperCase() : "NORMAL";

                    // Live Status Logic: Pending if results are null
                    Object resultObj = row[5];
                    String statusStr;
                    String resultDisplay;

                    if (resultObj == null || resultObj.toString().trim().isEmpty()) {
                        statusStr = "PENDING";
                        resultDisplay = "Awaiting Results...";
                    } else {
                        statusStr = "COMPLETED";
                        resultDisplay = resultObj.toString();
                    }

                    // 10-minute hide rule for completed
                    if (statusStr.equals("COMPLETED")) {
                        if (!completionTimers.containsKey(reqId)) completionTimers.put(reqId, now);
                        if ((now - completionTimers.get(reqId)) > 600000) continue;
                    }

                    String statusIcon = (statusStr.equals("COMPLETED") ? "🟢 " : "🟡 ") + statusStr;

                    model.addRow(new Object[]{
                            "L-ORD#" + reqId,
                            pName,
                            tType,
                            priority,
                            statusIcon,
                            resultDisplay
                    });
                }
            }
        };

        // Timer & Nav Logic
        javax.swing.Timer liveTimer = new javax.swing.Timer(5000, e -> loadData.run());
        liveTimer.start();
        loadData.run();

        backNav.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { liveTimer.stop(); switchToPanel("dashboard"); }
            @Override public void mouseEntered(MouseEvent e) { backNav.setForeground(Color.WHITE); }
            @Override public void mouseExited(MouseEvent e) { backNav.setForeground(new Color(189, 195, 199)); }
        });

        // --- 3. THE TABLE DESIGN (YOUR DESIGN RESTORED) ---
        JTable table = new JTable(model);
        table.setRowHeight(55);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(232, 244, 253));

        // Custom Header
        table.getTableHeader().setPreferredSize(new Dimension(0, 50));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setForeground(new Color(120, 130, 140));

        // Restoring Cursor Interaction for column 5
        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                table.setCursor(col == 5 ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        // Double-click viewer
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
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
            }
        });

        // Custom Cell Renderer (Restore Blue Clickable Look)
        // --- STATUS COLOR & DESIGN RENDERER ---
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasF, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasF, r, c);

                // Base Styling
                lbl.setBackground(isSel ? new Color(232, 244, 253) : Color.WHITE);
                lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(235, 238, 242)));
                lbl.setHorizontalAlignment(JLabel.LEFT);

                String val = (v != null) ? v.toString() : "";

                // 1. STYLE THE STATUS COLUMN (Index 4)
                if (c == 4) {
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    lbl.setHorizontalAlignment(JLabel.CENTER);

                    if (val.contains("COMPLETED")) {
                        lbl.setForeground(new Color(46, 204, 113)); // Digital Green
                    } else if (val.contains("PENDING")) {
                        lbl.setForeground(new Color(231, 76, 60));  // Vivid Red
                    }
                }
                // 2. STYLE THE RESULTS COLUMN (Index 5)
                else if (c == 5) {
                    lbl.setForeground(new Color(52, 152, 219)); // Electric Blue
                    lbl.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                }
                // 3. STYLE ALL OTHER COLUMNS
                else {
                    lbl.setForeground(new Color(51, 51, 51));
                    lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                }

                return lbl;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        resultsPanel.add(scrollPane, BorderLayout.CENTER);

        return resultsPanel;
    }
    private JPanel createScheduleDashboardPanel() {
        JPanel schedulePanel = new JPanel(new BorderLayout(0, 0));
        schedulePanel.setBackground(new Color(248, 250, 252));

        DoctorDAO doctorDAO = new DoctorDAO();

        // --- 1. MODERN HEADER WITH TOP NAV ---
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(2, 48, 71), getWidth(), 0, new Color(13, 71, 100)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 100));
        headerPanel.setBorder(new javax.swing.border.EmptyBorder(10, 30, 10, 30));

        // LEFT: Title and Status
        JLabel titleLabel = new JLabel("CLINICAL TIMELINE");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        JLabel liveIndicator = new JLabel("● LIVE SYNC ACTIVE");
        liveIndicator.setFont(new Font("Monospaced", Font.BOLD, 11));
        liveIndicator.setForeground(new Color(46, 204, 113));

        JPanel leftHeader = new JPanel(new GridLayout(2, 1));
        leftHeader.setOpaque(false);
        leftHeader.add(titleLabel);
        leftHeader.add(liveIndicator);

        // --- NEW: TOP NAVIGATION BAR ---
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
        navBar.setOpaque(false);

        JLabel returnNav = new JLabel("RETURN TO DASHBOARD");
        returnNav.setFont(new Font("Segoe UI", Font.BOLD, 12));
        returnNav.setForeground(new Color(200, 215, 225));
        returnNav.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // --- LIVE REFRESH TIMER ---
        Timer liveTimer = new Timer(5000, null); // Initialized but listener added later

        returnNav.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                liveTimer.stop(); // Stop sync when leaving
                switchToPanel("dashboard");
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                returnNav.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                returnNav.setForeground(new Color(200, 215, 225));
            }
        });
        navBar.add(returnNav);

        headerPanel.add(navBar, BorderLayout.NORTH);
        headerPanel.add(leftHeader, BorderLayout.WEST);
        schedulePanel.add(headerPanel, BorderLayout.NORTH);

        // --- 2. THE DIGITAL TABLE ---
        String[] columns = {"TIME SLOT", "PATIENT IDENTITY", "STATUS", "CLINICAL OBSERVATIONS"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(50);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(232, 244, 253));

        // Status Renderer
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                String status = (v != null) ? v.toString().toUpperCase() : "";
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));

                if (status.contains("PENDING")) lbl.setForeground(new Color(231, 76, 60));
                else if (status.contains("CONFIRMED")) lbl.setForeground(new Color(52, 152, 219));
                else lbl.setForeground(new Color(100, 116, 139));

                return lbl;
            }
        });

        // --- REFRESH LOGIC ---
        Runnable refreshData = () -> {
            List<Object[]> newData = doctorDAO.getFullClinicalSchedule(this.doctorId);
            if (newData != null) {
                model.setRowCount(0);
                for (Object[] row : newData) {
                    model.addRow(row);
                }
            }
        };

        refreshData.run(); // Initial load
        liveTimer.addActionListener(e -> refreshData.run());
        liveTimer.start();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        scrollPane.getViewport().setBackground(new Color(248, 250, 252));
        schedulePanel.add(scrollPane, BorderLayout.CENTER);

        return schedulePanel;
    }
    private JPanel createPrescriptionsDashboardPanel() {
        JPanel prescPanel = new JPanel(new BorderLayout());
        prescPanel.setBackground(new Color(242, 243, 244));

        DoctorDAO doctorDAO = new DoctorDAO();
        Color electricBlue = new Color(52, 152, 219);
        Color navDark = new Color(23, 32, 42);

        // --- 1. DIGITAL HEADER WITH NAV ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(navDark);
        topBar.setPreferredSize(new Dimension(0, 100));
        topBar.setBorder(new EmptyBorder(10, 30, 10, 30));

        // Top Right Nav Link
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navBar.setOpaque(false);
        JLabel backNav = new JLabel("RETURN TO DASHBOARD");
        backNav.setFont(new Font("Segoe UI", Font.BOLD, 12));
        backNav.setForeground(new Color(189, 195, 199));
        backNav.setCursor(new Cursor(Cursor.HAND_CURSOR));
        navBar.add(backNav);

        String realId = doctorDAO.getNextPrescriptionId();
        JLabel idDisplay = new JLabel("ID: PRESC" + (realId != null ? realId : "NEW"));
        idDisplay.setFont(new Font("Monospaced", Font.BOLD, 22));
        idDisplay.setForeground(electricBlue);

        JLabel liveIndicator = new JLabel("● LIVE APPOINTMENT SYNC (CONFIRMED ONLY)");
        liveIndicator.setFont(new Font("Segoe UI", Font.BOLD, 10));
        liveIndicator.setForeground(new Color(46, 204, 113));

        JPanel leftHeader = new JPanel(new GridLayout(2, 1));
        leftHeader.setOpaque(false);
        leftHeader.add(idDisplay);
        leftHeader.add(liveIndicator);

        topBar.add(navBar, BorderLayout.NORTH);
        topBar.add(leftHeader, BorderLayout.WEST);
        prescPanel.add(topBar, BorderLayout.NORTH);

        // --- 2. MAIN CONTENT AREA ---
        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        // Form fields
        JComboBox<String> pDropdown = createDigitalDropdown();
        JTextField diagField = createDigitalTextField("Enter clinical diagnosis");
        JTextField medField = createDigitalTextField("Enter medication name");
        JTextField doseField = createDigitalTextField("Enter dosage (e.g. 500mg - 2x daily)");

        // --- LIVE FILTER LOGIC (CONFIRMED ONLY) ---
        // --- UPDATED LIVE FILTER (Fixing ID Mismatch) ---
        Runnable refreshPatientList = () -> {
            String currentSelection = (String) pDropdown.getSelectedItem();
            List<Object[]> todayAppts = doctorDAO.getTodayAppointments(this.doctorId);

            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            boolean foundAny = false;

            if (todayAppts != null) {
                for (Object[] appt : todayAppts) {
                    // Index 4 is the Status (Confirmed)
                    if (appt[4] != null && appt[4].toString().equalsIgnoreCase("CONFIRMED")) {

                        // USE INDEX 5: This is the actual Patient ID we just added to the DAO
                        String patientId = appt[5].toString();
                        String patientName = appt[2].toString();

                        model.addElement(patientId + " - " + patientName);
                        foundAny = true;
                    }
                }
            }

            if (!foundAny) {
                model.addElement("No Confirmed Patients Found");
                pDropdown.setEnabled(false);
            } else {
                pDropdown.setEnabled(true);
                pDropdown.setModel(model);
                if (currentSelection != null) pDropdown.setSelectedItem(currentSelection);
            }
        };

        // Initial load and Timer (2s)
        refreshPatientList.run();
        javax.swing.Timer syncTimer = new javax.swing.Timer(2000, e -> refreshPatientList.run());
        syncTimer.start();

        // Back Nav Listener
        backNav.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { syncTimer.stop(); switchToPanel("dashboard"); }
            @Override public void mouseEntered(MouseEvent e) { backNav.setForeground(Color.WHITE); }
            @Override public void mouseExited(MouseEvent e) { backNav.setForeground(new Color(189, 195, 199)); }
        });

        // Layout left/right columns (keeping your original design)
        JPanel leftCol = new JPanel(new GridLayout(4, 1, 0, 20));
        leftCol.setOpaque(false);
        leftCol.add(createDigitalFieldPanel("PATIENT NAME *", pDropdown));
        leftCol.add(createDigitalFieldPanel("CLINICAL DIAGNOSIS *", diagField));
        leftCol.add(createDigitalFieldPanel("MEDICATION NAME *", medField));
        leftCol.add(createDigitalFieldPanel("DOSAGE *", doseField));

        gbc.gridx = 0; gbc.weightx = 0.4; gbc.weighty = 1.0;
        mainContent.add(leftCol, gbc);

        // Right Column (Instructions)
        JTextArea instrArea = new JTextArea();
        instrArea.setLineWrap(true);
        instrArea.setWrapStyleWord(true);
        JScrollPane instrScroll = new JScrollPane(instrArea);

        JPanel instructionsPanel = new JPanel(new BorderLayout());
        instructionsPanel.setBackground(Color.WHITE);
        instructionsPanel.setBorder(BorderFactory.createTitledBorder("ADDITIONAL INSTRUCTIONS"));
        instructionsPanel.add(instrScroll, BorderLayout.CENTER);

        gbc.gridx = 1; gbc.weightx = 0.6;
        mainContent.add(instructionsPanel, gbc);
        prescPanel.add(mainContent, BorderLayout.CENTER);

        // --- 3. ACTION FOOTER ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        actionPanel.setBackground(Color.WHITE);
        JButton saveBtn = createDigitalButton("SAVE PRESCRIPTION", electricBlue);

        saveBtn.addActionListener(e -> {
            if (!pDropdown.isEnabled()) return;

            String selected = pDropdown.getSelectedItem().toString();
            if (selected.contains("No Confirmed")) return;

            // This correctly grabs the Patient ID (the part before the " - ")
            String pId = selected.split(" - ")[0].trim();

            boolean success = doctorDAO.submitPrescriptionAndRecord(
                    pId, // Now sending actual Patient ID
                    this.doctorId,
                    medField.getText(),
                    doseField.getText(),
                    instrArea.getText(),
                    diagField.getText()
            );
            // ... rest of your success logic
                if (success) {
                    JOptionPane.showMessageDialog(null, "Prescription Saved Successfully.");
                    syncTimer.stop();
                    switchToPanel("dashboard");
            }
        });

        actionPanel.add(saveBtn);
        prescPanel.add(actionPanel, BorderLayout.SOUTH);

        return prescPanel;
    }
    private JComboBox<String> createDigitalDropdown() {
        JComboBox<String> combo = new JComboBox<>();
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(new Color(245, 247, 250));
        combo.setForeground(new Color(50, 50, 50));
        combo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return combo;
    }
    private JTextField createDigitalTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBackground(new Color(245, 247, 250));
        field.setForeground(new Color(150, 150, 150));
        field.setCaretColor(new Color(52, 152, 219));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 220, 240), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Set placeholder text
        field.setText(placeholder);
        field.putClientProperty("placeholder", placeholder);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(new Color(50, 50, 50));
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(new Color(150, 150, 150));
                }
            }
        });

        return field;
    }
    private JPanel createDigitalFieldPanel(String label, Component field) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 5, 0));

        JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(new Color(100, 100, 100));
        titleLabel.setBorder(new EmptyBorder(0, 0, 8, 0));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }
    private JButton createDigitalButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 38));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }
    private JPanel createDoctorProfilePanel() {
        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setBackground(new Color(242, 245, 248));

        DoctorDAO doctorDAO = new DoctorDAO();
        Map<String, String> doctorData = doctorDAO.getDoctorProfileData(this.doctorId);

        // --- 1. DIGITAL IDENTITY HEADER (DARK MODE) ---
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, new Color(20, 30, 48), getWidth(), 0, new Color(36, 59, 85)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, 160));
        header.setBorder(new EmptyBorder(10, 50, 30, 50)); // Adjusted top border for nav space

        // Profile Image Placeholder (Digital Circle)
        JLabel profilePic = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(52, 152, 219));
                g2.fillOval(0, 0, 90, 90);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 36));
                String initial = (fullName != null && !fullName.isEmpty()) ? fullName.substring(0, 1) : "D";
                g2.drawString(initial, 33, 58);
                g2.dispose();
            }
        };
        profilePic.setPreferredSize(new Dimension(100, 100));

        JPanel infoText = new JPanel(new GridLayout(3, 1));
        infoText.setOpaque(false);

        JLabel idTag = new JLabel("SECURE STAFF ACCESS ID: #" + this.doctorId);
        idTag.setFont(new Font("Monospaced", Font.BOLD, 12));
        idTag.setForeground(new Color(52, 152, 219));

        JLabel nameLabel = new JLabel("DR. " + (fullName != null ? fullName.toUpperCase() : "PHYSICIAN"));
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        nameLabel.setForeground(Color.WHITE);

        JLabel specLabel = new JLabel(doctorData.getOrDefault("specialization", "General Staff").toUpperCase());
        specLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        specLabel.setForeground(new Color(189, 195, 199));

        infoText.add(idTag);
        infoText.add(nameLabel);
        infoText.add(specLabel);

        JPanel headerContent = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 0));
        headerContent.setOpaque(false);
        headerContent.add(profilePic);
        headerContent.add(infoText);

        // --- NAVIGATION BAR (Replacing Footer Button) ---
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        navBar.setOpaque(false);

        JLabel dismissNav = new JLabel("DISMISS PROFILE");
        dismissNav.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dismissNav.setForeground(new Color(189, 195, 199));
        dismissNav.setCursor(new Cursor(Cursor.HAND_CURSOR));

        dismissNav.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                switchToPanel("dashboard");
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                dismissNav.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                dismissNav.setForeground(new Color(189, 195, 199));
            }
        });
        navBar.add(dismissNav);

        header.add(navBar, BorderLayout.NORTH); // Added Dismiss to the top nav position
        header.add(headerContent, BorderLayout.WEST);
        profilePanel.add(header, BorderLayout.NORTH);

        // --- 2. THE DIGITAL DATA GRID ---
        JPanel gridContainer = new JPanel(new GridLayout(0, 2, 25, 25));
        gridContainer.setBackground(new Color(242, 245, 248));
        gridContainer.setBorder(new EmptyBorder(40, 50, 40, 50));

        if (doctorData != null) {
            addDigitalDataCard(gridContainer, "ASSIGNED DEPARTMENT", doctorData.getOrDefault("department", "N/A"), "🏢");
            addDigitalDataCard(gridContainer, "QUALIFICATIONS", doctorData.getOrDefault("qualification", "M.D."), "🎓");
            addDigitalDataCard(gridContainer, "PRACTICE LICENSE", doctorData.getOrDefault("license_number", "VERIFIED"), "📜");
            addDigitalDataCard(gridContainer, "SYSTEM EMAIL", doctorData.getOrDefault("email", "N/A"), "📧");
            addDigitalDataCard(gridContainer, "OFFICIAL CONTACT", doctorData.getOrDefault("contact_number", "N/A"), "📱");
            addDigitalDataCard(gridContainer, "WORK SCHEDULE", doctorData.getOrDefault("working_hours", "09:00 - 17:00"), "🕒");
        }

        JScrollPane scroll = new JScrollPane(gridContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        profilePanel.add(scroll, BorderLayout.CENTER);

        // --- FOOTER REMOVED (Button moved to Nav) ---

        return profilePanel;
    }
    private void addDigitalDataCard(JPanel parent, String label, String value, String icon) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 235, 240), 1),
                new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(149, 165, 166));

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        val.setForeground(new Color(44, 62, 80));

        textPanel.add(lbl);
        textPanel.add(val);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        parent.add(card);
    }
    private void updateCardContent(JPanel card, String newValue) {
        try {
            String displayValue = (newValue != null) ? newValue : "0";

            if (card.getComponentCount() > 1 && card.getComponent(1) instanceof JPanel) {
                JPanel contentPanel = (JPanel) card.getComponent(1);

                for (Component c : contentPanel.getComponents()) {
                    if (c instanceof JLabel) {
                        JLabel label = (JLabel) c;

                        if (label.getIcon() == null) {
                            if (!label.getText().equals(displayValue)) {
                                label.setText(displayValue);
                                contentPanel.revalidate();
                                card.revalidate();
                                card.repaint();
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Dashboard Heartbeat Sync Error: " + e.getMessage());
        }
    }
    private void loadDoctorData(int loggedInAuthId) {
        // Initialize DAO
        DoctorDAO doctorDAO = new DoctorDAO();

        // Fetch data using the AUTH_ID from the authentication table
        Map<String, String> data = doctorDAO.getDoctorByAuthId(loggedInAuthId);

        if (data != null && !data.isEmpty()) {
            try {
                // 1. Set the primary Doctor ID (int)
                this.doctorId = Integer.parseInt(data.get("doctor_id"));

                // 2. Reconstruct Full Name from the three columns
                String fName = data.get("first_name");
                String mName = data.get("middle_name");
                String lName = data.get("last_name");

                this.fullName = fName + (mName != null && !mName.isEmpty() ? " " + mName : "") + " " + lName;

                // 3. Map the rest of the clinical fields
                this.specialization = data.get("specialization");
                this.contactNumber = data.get("contact_number");
                this.email = data.get("email");

                System.out.println("✅ Portal context set for: Dr. " + this.fullName + " (ID: " + this.doctorId + ")");
            } catch (Exception e) {
                System.err.println("❌ Data Mapping Error: " + e.getMessage());
            }
        } else {
            System.err.println("❌ Profile Load Error: No doctor linked to Auth ID #" + loggedInAuthId);
        }
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
    private void openFullResultViewer(String orderId, String patient, String content) {
        JFrame detailFrame = new JFrame("Laboratory EHR - Diagnostic Report");
        detailFrame.setSize(600, 550); // Slightly taller for the footer
        detailFrame.setLocationRelativeTo(null);
        detailFrame.setLayout(new BorderLayout());

        // --- 1. HEADER (STAYS DARK) ---
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(new Color(23, 32, 42));
        header.setBorder(new javax.swing.border.EmptyBorder(15, 25, 15, 25));

        JLabel l1 = new JLabel("PATIENT: " + patient.toUpperCase());
        l1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l1.setForeground(Color.WHITE);

        JLabel l2 = new JLabel("ORDER REF: " + orderId);
        l2.setFont(new Font("Monospaced", Font.BOLD, 12));
        l2.setForeground(new Color(52, 152, 219));

        header.add(l1);
        header.add(l2);

        // --- 2. REPORT CONTENT ---
        JTextArea textArea = new JTextArea(content);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setMargin(new Insets(25, 25, 25, 25));
        textArea.setBackground(new Color(250, 251, 252));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // --- 3. FOOTER (NEW: ACTIONS) ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JButton closeBtn = new JButton("CLOSE REPORT");
        closeBtn.setFocusPainted(false);
        closeBtn.setBackground(new Color(23, 32, 42));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        closeBtn.addActionListener(e -> detailFrame.dispose());

        footer.add(closeBtn);

        detailFrame.add(header, BorderLayout.NORTH);
        detailFrame.add(scrollPane, BorderLayout.CENTER);
        detailFrame.add(footer, BorderLayout.SOUTH);

        detailFrame.setVisible(true);
    }
    @Override
    void logout() {
        int choice = JOptionPane.showConfirmDialog(
                Wframe,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            if (liveSyncTimer != null) {
                liveSyncTimer.stop();
            }
            if (Wframe != null) {
                Wframe.dispose();
            }
            new LoginPage().setVisible(true);
        }
    }

}