import Database.AppointmentDAO;
import Database.BillingDAO;
import Database.PatientDAO;
import Database.StatisticsDAO;
import Model.DoctorItem;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
public class receptionist extends staffUser {
    private JFrame frame;
    private JPanel mainBackgroundPanel;
    private JPanel northPanel;
    private JPanel leftPanel;
    private JPanel navContainer;
    private ImageIcon backgroundIcon = new ImageIcon("assets/receptionBg.jpg");
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private DefaultTableModel patientTableModel;
    private JTable patientTable;
    private DefaultTableModel model;
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.ENGLISH);
    String date = now.format(dateFormatter).toLowerCase();
    private String userFullName;
    private int currentAuthId;
    private JLabel lblNameVal;
    private JLabel lblPhoneVal;
    private JLabel lblAgeVal;
    private JLabel lblGenderVal;
    private JTextArea txtAddressVal;

    public receptionist(int authId) {
        this.currentAuthId = authId; // Save the ID (e.g., 5)

        // 3. Fetch Name immediately for the Greeting
        Map<String, String> profile = Database.ReceptionistDAO.getReceptionistProfile(currentAuthId);

        if (profile.containsKey("name")) {
            this.userFullName = profile.get("name"); // "Anna Smith"
        } else {
            this.userFullName = "Receptionist"; // Fallback if profile not created yet
        }
    }
    // ========== Helper methods ==========
    private JPanel createNavItem(String text, ImageIcon icon, Color textColor) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(180, 50));
        wrapper.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton iconBtn = new JButton(new ImageIcon(icon.getImage()));
        iconBtn.setOpaque(false);
        iconBtn.setContentAreaFilled(false);
        iconBtn.setBorderPainted(false);
        iconBtn.setFocusPainted(false);

        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(textColor);

        wrapper.add(iconBtn);
        wrapper.add(label);

        return wrapper;
    }
    private void switchCenterPanel(JPanel panel) {
        if (mainBackgroundPanel == null) return;

        BorderLayout layout = (BorderLayout) mainBackgroundPanel.getLayout();
        Component center = layout.getLayoutComponent(BorderLayout.CENTER);

        if (center != null) {
            mainBackgroundPanel.remove(center);
        }

        mainBackgroundPanel.add(panel, BorderLayout.CENTER);
        mainBackgroundPanel.revalidate();
        mainBackgroundPanel.repaint();
    }


    private void addNavClickListener(JPanel panel, String panelName) {
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (panel.getBorder() instanceof LineBorder) {
                    panel.setBorder(new LineBorder(new Color(255, 255, 255, 200), 2, true));
                } else {
                    panel.setBackground(new Color(255, 255, 255, 30));
                    panel.setOpaque(true);
                }
                panel.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (panel.getBorder() instanceof LineBorder) {
                    panel.setBorder(new LineBorder(Color.white, 2, true));
                } else {
                    panel.setBackground(null);
                    panel.setOpaque(false);
                }
                panel.repaint();
            }

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                switch (panelName) {
                    // ===== MAIN NAV =====
                    case "dashboard":
                        switchCenterPanel(createCentralDashboardPanel(loadActionIcons()));
                        break;

                    case "patients":
                        switchCenterPanel(createPatientDashboardPanel());
                        break;

                    case "appointment":
                        switchCenterPanel(createAppointmentDashboardPanel());
                        break;

                    case "doctors":
                        switchCenterPanel(createDoctorDashboardPanel()); // receptionist view
                        break;

                    case "bills":
                        switchCenterPanel(createBillingDashboardPanel());
                        break;
                    case "Profile":
                        switchCenterPanel(createUserProfilePanel());
                        break;
                    // ===== QUICK ACTIONS =====
                    case "check_in":
                        switchCenterPanel(createCheckInPanel());
                        break;

                    case "new_appointment":
                        switchCenterPanel( createAddAppointmentFormPanel(model));
                        break;

                    case "search_records":
                        switchCenterPanel(createSearchRecordsPanel());
                        break;

                    case "update_info":
                        switchCenterPanel(createUpdatePatientPanel(model));
                        break;

                    case "view_today":
                        switchCenterPanel(createCentralDashboardPanel(loadActionIcons()));
                        break;
                }
            }
        });
    }

    private JPanel createActionPanel(ImageIcon icon, String text, Color bgColor, Font textFont, Color textColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(bgColor);
        panel.setBorder(new LineBorder(Color.white, 2, true));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.setPreferredSize(new Dimension(200, 100));
        panel.setMaximumSize(new Dimension(200, 100));

        JLabel imageLabel = new JLabel(new ImageIcon(icon.getImage()));
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel textLabel = new JLabel(text);
        textLabel.setForeground(textColor);
        textLabel.setFont(textFont);
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalStrut(10));
        panel.add(imageLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(textLabel);
        panel.add(Box.createVerticalStrut(10));

        return panel;
    }
    private void searchPatientById(String patientId) {
        if (patientId.isEmpty()) {
            loadPatientData(); // if empty search, reload full table
            return;
        }

        Map<String, Object> patient = PatientDAO.getPatientById(patientId);
        DefaultTableModel model = (DefaultTableModel) patientTable.getModel();
        model.setRowCount(0); // clear table

        if (patient != null) {
            model.addRow(new Object[]{
                    patient.get("patient_id"),
                    patient.get("first_name"),
                    patient.get("middle_name"),
                    patient.get("last_name"),
                    patient.get("date_of_birth"),
                    patient.get("gender"),
                    patient.get("contact_number"),
                    patient.get("email"),
                    patient.get("address"),
                    patient.get("emergency_contact"),
                    patient.get("blood_type"),
                    patient.get("registration_date")
            });
        } else {
            JOptionPane.showMessageDialog(frame, "No patient found with ID: " + patientId);
            loadPatientData(); // reload full table if nothing found
        }
    }

    private JPanel createNorthPanel() {
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 200, 10));
        northPanel.setBackground(new Color(2, 48, 71));
        northPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.white));

        // Search Wrapper
        JPanel searchWrapper = new JPanel(new FlowLayout());
        searchWrapper.setOpaque(false);

        JLabel searchLabel = new JLabel("Search patient by Id: ");
        searchLabel.setForeground(Color.white);

        JTextField searchBar = new JTextField();
        searchBar.setPreferredSize(new Dimension(400, 30));

        // Add search icon button
        ImageIcon[] icons=loadActionIcons();
        ImageIcon searchIcon=icons[11];
        JButton searchButton = new JButton(new ImageIcon(searchIcon.getImage().getScaledInstance(25,25, Image.SCALE_SMOOTH)));
        searchButton.setBorderPainted(false);
        searchButton.setContentAreaFilled(false);
        searchButton.setFocusPainted(false);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add action listener to search button
        searchButton.addActionListener(e -> {
            patientsDashboard();
            String searchText = searchBar.getText().trim();
            if (!searchText.isEmpty()) {
                searchPatientById(searchText);
            }
        });

        searchWrapper.add(searchLabel);
        searchWrapper.add(searchBar);
        searchWrapper.add(searchButton);

        // User wrapper
        JPanel userWrapper = new JPanel(new FlowLayout());
        JLabel usernameLabel = new JLabel(userFullName);
        usernameLabel.setForeground(Color.white);
        usernameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        JButton logout = new JButton("Logout");
        logout.setForeground(Color.white);
        logout.setBackground(new Color(2, 48, 71));
        logout.setFont(new Font("SansSerif", Font.BOLD, 12));
        logout.addActionListener(e -> logout());
        userWrapper.add(usernameLabel);
        userWrapper.add(logout);
        userWrapper.setOpaque(false);

        northPanel.add(searchWrapper);
        northPanel.add(userWrapper);

        return northPanel;
    }


    private JPanel createLeftPanel(ImageIcon[] navIcons) {
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));
        leftPanel.setBackground(new Color(2, 48, 71));
        leftPanel.setOpaque(true);

        navContainer = new JPanel();
        navContainer.setLayout(new BoxLayout(navContainer, BoxLayout.Y_AXIS));
        navContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        navContainer.setOpaque(true);
        navContainer.setBackground(new Color(2, 48, 71));
        navContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        navContainer.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        // Portal section
        JPanel portalImagePanel = new JPanel();
        portalImagePanel.setPreferredSize(new Dimension(180, 40));
        portalImagePanel.setMaximumSize(new Dimension(180, 40));
        portalImagePanel.setOpaque(false);
        JLabel portalImage = new JLabel(new ImageIcon(navIcons[4].getImage())); // portal icon
        portalImagePanel.add(portalImage);

        JPanel portalNamePanel = new JPanel();
        portalNamePanel.setPreferredSize(new Dimension(180, 40));
        portalNamePanel.setMaximumSize(new Dimension(180, 40));
        portalNamePanel.setOpaque(false);
        JLabel portalName = new JLabel("Receptionist portal");
        portalName.setForeground(Color.white);
        portalName.setFont(new Font("Serif", Font.BOLD, 18));
        portalNamePanel.add(portalName);

        // Navigation items
        JPanel dashboardWrapper = createNavItem("Dashboard", navIcons[0], Color.WHITE);
        JPanel patientWrapper = createNavItem("Patients", navIcons[1], Color.WHITE);
        JPanel appointmentWrapper = createNavItem("Appointment", navIcons[2], Color.WHITE);
        JPanel doctorWrapper = createNavItem("Doctors", navIcons[3], Color.WHITE);
        JPanel billWrapper = createNavItem("Bills", navIcons[4], Color.WHITE);
        JPanel profileWrapper=createNavItem("Profile",navIcons[5],Color.WHITE);

        addNavClickListener(dashboardWrapper, "dashboard");
        addNavClickListener(patientWrapper, "patients");
        addNavClickListener(appointmentWrapper, "appointment");
        addNavClickListener(doctorWrapper, "doctors");
        addNavClickListener(billWrapper, "bills");
        addNavClickListener(profileWrapper, "Profile");

        JSeparator separator1 = new JSeparator();
        separator1.setForeground(new Color(255, 255, 255, 150));
        separator1.setMaximumSize(new Dimension(200, 1));

        // Add to navContainer
        navContainer.add(portalImagePanel);
        navContainer.add(Box.createVerticalStrut(10));
        navContainer.add(portalNamePanel);
        navContainer.add(Box.createVerticalStrut(15));
        navContainer.add(separator1);
        navContainer.add(Box.createVerticalStrut(20));
        navContainer.add(dashboardWrapper);
        navContainer.add(Box.createVerticalStrut(12));
        navContainer.add(patientWrapper);
        navContainer.add(Box.createVerticalStrut(12));
        navContainer.add(appointmentWrapper);
        navContainer.add(Box.createVerticalStrut(12));
        navContainer.add(doctorWrapper);
        navContainer.add(Box.createVerticalStrut(12));
        navContainer.add(billWrapper);
        navContainer.add(Box.createVerticalStrut(12));
        navContainer.add(profileWrapper);
        navContainer.add(Box.createVerticalGlue());

        leftPanel.add(navContainer);
        leftPanel.add(Box.createVerticalGlue());

        return leftPanel;
    }
    private JPanel createTodayAppointmentsTablePanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(true);
        wrapper.setBackground(new Color(255, 255, 255, 150));
        wrapper.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Title
        JLabel title = new JLabel("Today's Appointments");
        title.setFont(new Font("Serif", Font.BOLD, 20));
        title.setForeground(new Color(2, 48, 71));
        title.setBorder(new EmptyBorder(0, 0, 8, 0));
        wrapper.add(title, BorderLayout.NORTH);

        // Table
        String[] columns = {"Time", "Patient", "Doctor", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // dashboard table should be read-only
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(22);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setForeground(new Color(2,48,71));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(2,48,71));
        table.setOpaque(false);

        table.getTableHeader().setOpaque(true);
        table.getTableHeader().setBackground(new Color(2,48,71));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setPreferredSize(new Dimension(900, 160)); // compact height

        wrapper.add(scrollPane, BorderLayout.CENTER);

        // Load data
        AppointmentDAO.loadTodayAppointmentsForDashboard(model);

        return wrapper;
    }

    private JPanel createCentralDashboardPanel(ImageIcon[] actionIcons) {
        JPanel centralPanel = new JPanel();
        centralPanel.setLayout(new BoxLayout(centralPanel, BoxLayout.Y_AXIS));
        centralPanel.setOpaque(false);

        Font title = new Font("Serif", Font.BOLD, 24);
        Font subtitle = new Font("SansSerif", Font.BOLD, 12);
        Color color = new Color(2, 48, 71);

        String totalPatientsNumber= String.valueOf(StatisticsDAO.getTotalPatients());
        String totalAppointmentNumber=String.valueOf(StatisticsDAO.getTotalAppointments());
        String availableDoctors= String.valueOf(StatisticsDAO.getAvailableDoctors());
        String waittingPatients=String.valueOf(StatisticsDAO.getPatientsWaiting());

        // Greeting panel
        JPanel greetingPanel = new JPanel();
        greetingPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 5));
        greetingPanel.setOpaque(true);
        greetingPanel.setBackground(new Color(255, 255, 255, 150));
        greetingPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel greetingLabel = new JLabel(getGreeting() +" "+ userFullName);
        greetingLabel.setFont(title);
        greetingLabel.setForeground(color);
        greetingPanel.add(greetingLabel);

        // Date & time panel
        JPanel dateTimePanel = new JPanel();
        dateTimePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
        dateTimePanel.setOpaque(true);
        dateTimePanel.setBackground(new Color(255, 255, 255, 150));
        dateTimePanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel dateText = new JLabel("Here is the hospital overview for " + date);
        dateText.setForeground(Color.gray);
        dateText.setFont(new Font("Arial", Font.PLAIN, 14));
        JPanel timeWrapper = new JPanel(new FlowLayout());
        timeWrapper.setBorder(new EmptyBorder(0, 600, 0, 5));
        timeWrapper.setOpaque(false);

        JLabel wifi = new JLabel(new ImageIcon(actionIcons[6].getImage())); // wifi icon

        JLabel timeText = new JLabel();
        timeText.setForeground(color);

// LIVE TIME TIMER
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        javax.swing.Timer timer = new javax.swing.Timer(1000, e -> {
            timeText.setText(LocalTime.now().format(timeFormatter));
            timeText.setOpaque(false);
        });
        timer.start();

        timeWrapper.add(wifi);
        timeWrapper.add(timeText);

        dateTimePanel.add(dateText);
        dateTimePanel.add(timeWrapper);

        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        statusPanel.setBackground(new Color(255, 255, 255, 150));
        statusPanel.setOpaque(true);

        JPanel patientStat = createStatPanel(actionIcons[6], "patient waiting", waittingPatients, color);
        JPanel doctorStat = createStatPanel(actionIcons[7], "available doctors",availableDoctors, color);
        JPanel appointStat = createStatPanel(actionIcons[8], "total appointments", totalAppointmentNumber, color);
        JPanel totalPatientStat = createStatPanel(actionIcons[6], "total patients", totalPatientsNumber, color);

        statusPanel.add(patientStat);
        statusPanel.add(doctorStat);
        statusPanel.add(appointStat);
        statusPanel.add(totalPatientStat);
        // Quick Actions panel
        JPanel quickActionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quickActionPanel.setBackground(new Color(255, 255, 255, 150));
        quickActionPanel.setOpaque(true);
        JLabel quickActions = new JLabel("Quick Actions");
        quickActions.setForeground(color);
        quickActions.setFont(title);
        quickActionPanel.add(quickActions);
        quickActionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel actionsPanel = new JPanel();
        actionsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 25, 10));
        actionsPanel.setBackground(new Color(255, 255, 255, 150));
        actionsPanel.setOpaque(true);

        // Create action panels
        JPanel checkPanel = createActionPanel(actionIcons[9], "Check in patient", color, subtitle, Color.WHITE);
        JPanel addPanel = createActionPanel(actionIcons[10], "New appointment", color, subtitle, Color.WHITE);
        JPanel searchPanel = createActionPanel(actionIcons[11], "Search records", color, subtitle, Color.WHITE);
        JPanel updatePanel = createActionPanel(actionIcons[12], "Update information", color, subtitle, Color.WHITE);

        addNavClickListener(checkPanel, "check_in");
        addNavClickListener(addPanel, "new_appointment");
        addNavClickListener(searchPanel, "search_records");
        addNavClickListener(updatePanel, "update_info");

        actionsPanel.add(checkPanel);
        actionsPanel.add(addPanel);
        actionsPanel.add(searchPanel);
        actionsPanel.add(updatePanel);

        JPanel todayAppointmentsPanel = createTodayAppointmentsTablePanel();

        // Add to central panel
        centralPanel.add(greetingPanel);
        centralPanel.add(dateTimePanel);
        centralPanel.add(statusPanel);
        centralPanel.add(quickActionPanel);
        centralPanel.add(actionsPanel);
        centralPanel.add(todayAppointmentsPanel);
        return centralPanel;
    }

    private JPanel createStatPanel(ImageIcon icon, String text, String value, Color color) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(color);
        panel.setBorder(new LineBorder(Color.white, 2, true));
        panel.setOpaque(true);

        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel iconLabel = new JLabel(new ImageIcon(icon.getImage()));
        JLabel textLabel = new JLabel(text);
        textLabel.setForeground(Color.white);
        textLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        iconPanel.setOpaque(false);
        iconPanel.add(iconLabel);
        iconPanel.add(textLabel);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Serif", Font.BOLD, 18));
        valueLabel.setForeground(Color.white);

        panel.add(iconPanel);
        panel.add(Box.createVerticalStrut(2));
        panel.add(valueLabel);
        panel.setPreferredSize(new Dimension(200, 100));
        panel.setMaximumSize(new Dimension(200, 100));

        return panel;
    }

    @Override
    void showDashboard() {
        if (frame == null) {
            frame = new JFrame("HMS - Hospital Management System(Receptionist)");
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        // Load all icons
        ImageIcon[] navIcons = loadNavIcons();
        ImageIcon[] actionIcons = loadActionIcons();

        // Create main panel
        mainBackgroundPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundIcon.getImage() != null) {
                    g.drawImage(backgroundIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
                }
            }
        };

        // Create panels
        northPanel = createNorthPanel();
        leftPanel = createLeftPanel(navIcons);
        JPanel centralPanel = createCentralDashboardPanel(actionIcons);

        // Add to main panel
        mainBackgroundPanel.add(centralPanel, BorderLayout.CENTER);
        mainBackgroundPanel.add(northPanel, BorderLayout.NORTH);
        mainBackgroundPanel.add(leftPanel, BorderLayout.WEST);

        // Set frame content
        frame.setContentPane(mainBackgroundPanel);
        frame.setVisible(true);
        mainBackgroundPanel.revalidate();
        mainBackgroundPanel.repaint();
    }

    private ImageIcon[] loadNavIcons() {
        ImageIcon dashboardIcon = new ImageIcon("assets/dashboard.png");
        ImageIcon patientIcon = new ImageIcon("assets/user.png");
        ImageIcon appointmentIcon = new ImageIcon("assets/appointment.png");
        ImageIcon doctorIcon = new ImageIcon("assets/stethoscope.png");
        ImageIcon portalIcon = new ImageIcon("assets/portal.png");
        ImageIcon profileIcon=new ImageIcon("assets/account.png");

        ImageIcon[] icons = new ImageIcon[6];
        icons[0] = scaleIcon(dashboardIcon, 20, 20);
        icons[1] = scaleIcon(patientIcon, 20, 20);
        icons[2] = scaleIcon(appointmentIcon, 20, 20);
        icons[3] = scaleIcon(doctorIcon, 20, 20);
        icons[4] = scaleIcon(portalIcon, 30, 30);
        icons[5]=scaleIcon(profileIcon,25,25);
        return icons;
    }

    private ImageIcon[] loadActionIcons() {
        ImageIcon wifiIcon = new ImageIcon("assets/wifi.png");
        ImageIcon patients = new ImageIcon("assets/group.png");
        ImageIcon bag = new ImageIcon("assets/kit-bag.png");
        ImageIcon clock = new ImageIcon("assets/clock.png");
        ImageIcon checkIn = new ImageIcon("assets/checkIn.png");
        ImageIcon add = new ImageIcon("assets/add.png");
        ImageIcon searchIcon = new ImageIcon("assets/circle.png");
        ImageIcon updateIcon = new ImageIcon("assets/update.png");
        ImageIcon showIcon = new ImageIcon("assets/vision.png");


        ImageIcon[] icons = new ImageIcon[15];
        icons[6] = scaleIcon(wifiIcon, 25, 25);
        icons[7] = scaleIcon(patients, 25, 25);
        icons[8] = scaleIcon(bag, 25, 25);
        icons[9] = scaleIcon(clock, 25, 25);
        icons[10] = scaleIcon(checkIn, 25, 25);
        icons[11] = scaleIcon(add, 25, 25);
        icons[12] = scaleIcon(searchIcon, 25, 25);
        icons[13] = scaleIcon(updateIcon, 25, 25);
        icons[14] = scaleIcon(showIcon, 25, 25);

        return icons;
    }

    private ImageIcon scaleIcon(ImageIcon icon, int width, int height) {
        Image scaledImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    void patientsDashboard() {
        if (mainBackgroundPanel == null) return;

        // Remove only the center component
        BorderLayout layout = (BorderLayout) mainBackgroundPanel.getLayout();
        Component centerComp = layout.getLayoutComponent(BorderLayout.CENTER);
        if (centerComp != null) {
            mainBackgroundPanel.remove(centerComp);
        }

        // Create and add patient dashboard
        JPanel patientDashboardPanel = createPatientDashboardPanel();
        mainBackgroundPanel.add(patientDashboardPanel, BorderLayout.CENTER);

        // Refresh
        mainBackgroundPanel.revalidate();
        mainBackgroundPanel.repaint();
    }

    private JPanel createPatientDashboardPanel() {
        JPanel patientPanel = new JPanel(new BorderLayout());
        patientPanel.setBackground(new Color(255,255,255,150));
        patientPanel.setOpaque(false); // keep container semi-transparent for background

        // Title Panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(255,255,255,150));
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Patient Management", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(2, 48, 71));

        JPanel btnPanel=new JPanel(new FlowLayout(FlowLayout.CENTER,10,0));
        btnPanel.setOpaque(false);

        JButton addPatientBtn = new JButton("+ Add New ");
        addPatientBtn.setBackground(new Color(2, 48, 71));
        addPatientBtn.setPreferredSize(new Dimension(150,40));
        addPatientBtn.setForeground(Color.WHITE);
        addPatientBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        addPatientBtn.setFocusPainted(false);

        JButton listPatientBtn = new JButton("List All");
        listPatientBtn.setBackground(new Color(2, 48, 71));
        listPatientBtn.setPreferredSize(new Dimension(150,40));
        listPatientBtn.setForeground(Color.WHITE);
        listPatientBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        listPatientBtn.setFocusPainted(false);

        btnPanel.add(addPatientBtn);
        btnPanel.add(listPatientBtn);

        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(btnPanel, BorderLayout.EAST);

        // CardLayout container
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false); // keep background visible
        contentPanel.setBackground(new Color(0,0,0,0)); // fully transparent

        // Table panel (opaque so it hides the background properly)
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(true);
        tablePanel.setBackground(new Color(255,255,255,180)); // semi-transparent white for content
        String[] columnName={"Card_ID","First Name","Middle Name","Last Name","Birt Date","Gender","Contact","Email","Adress","Emergency_Contact","Blood_type","Registration Date"};
        patientTableModel=new DefaultTableModel(columnName,0);
        patientTable = new JTable(patientTableModel);
        patientTable.setBackground(new Color(255,255,255,180));
        patientTable.setOpaque(false);
        patientTable.setForeground(new Color(2,48,71));
        patientTable.setRowHeight(20);
        patientTable.setFont(new Font("Arial", Font.PLAIN, 14));
        patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientTable.setGridColor(new Color(2,48,71));
        JScrollPane tableScroller = new JScrollPane(patientTable);
        tableScroller.setOpaque(false);
        tableScroller.getViewport().setOpaque(false);
        patientTable.getTableHeader().setOpaque(true);
        patientTable.getTableHeader().setBackground(new Color(2,48,71));
        patientTable.getTableHeader().setForeground(Color.white);
        patientTableModel.setRowCount(0);

        loadPatientData();
        tablePanel.add(tableScroller, BorderLayout.CENTER);

        // Form panel (opaque so it hides table when shown)
        JPanel formPanel = createAddPatientFormPanel();
        formPanel.setOpaque(true);
        formPanel.setBackground(new Color(255,255,255,180));

        contentPanel.add(tablePanel, "TABLE");
        contentPanel.add(formPanel, "FORM");

        cardLayout.show(contentPanel, "TABLE");

        // Button actions
        addPatientBtn.addActionListener(e -> cardLayout.show(contentPanel, "FORM"));
        listPatientBtn.addActionListener(e -> cardLayout.show(contentPanel, "TABLE"));

        patientPanel.add(titlePanel, BorderLayout.NORTH);
        patientPanel.add(contentPanel, BorderLayout.CENTER);

        return patientPanel;
    }
    private JPanel createUpdatePatientPanel(DefaultTableModel tableModel) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 255, 255, 180));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ===== Fields =====
        JTextField txtPatientId = new JTextField();
        JTextField txtFirstName = new JTextField();
        JTextField txtMiddleName = new JTextField();
        JTextField txtLastName = new JTextField();
        JComboBox<String> cbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});

        // Date spinner for DOB
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner dobSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dobSpinner, "yyyy-MM-dd");
        dobSpinner.setEditor(dateEditor);

        JTextField txtPhone = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtAddress = new JTextField();

        JButton btnLoad = new JButton("Load Patient");
        JButton btnUpdate = new JButton("Update");
        JButton btnClear = new JButton("Clear");

        // ===== Colors =====
        Color btnColor = new Color(2, 48, 71);
        btnLoad.setBackground(btnColor); btnLoad.setForeground(Color.WHITE);
        btnUpdate.setBackground(btnColor); btnUpdate.setForeground(Color.WHITE);
        btnClear.setBackground(btnColor); btnClear.setForeground(Color.WHITE);

        // ===== Layout =====
        int row = 0;
        addField(panel, gbc, row++, "Patient ID:", txtPatientId);
        addField(panel, gbc, row++, "First Name:", txtFirstName);
        addField(panel, gbc, row++, "Middle Name:", txtMiddleName);
        addField(panel, gbc, row++, "Last Name:", txtLastName);
        addField(panel, gbc, row++, "Gender:", cbGender);
        addField(panel, gbc, row++, "Date of Birth:", dobSpinner);
        addField(panel, gbc, row++, "Phone:", txtPhone);
        addField(panel, gbc, row++, "Email:", txtEmail);
        addField(panel, gbc, row++, "Address:", txtAddress);

        gbc.gridx = 0; gbc.gridy = row; panel.add(btnLoad, gbc);
        gbc.gridx = 1; panel.add(btnUpdate, gbc);
        gbc.gridx = 2; panel.add(btnClear, gbc);

        // ===== Button Actions =====
        // Load patient data into fields
        btnLoad.addActionListener(e -> {
            String patientId = txtPatientId.getText().trim();
            if(patientId.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Enter Patient ID", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Map<String, Object> patient = PatientDAO.getPatientById(patientId);
            if(patient != null) {
                txtFirstName.setText((String) patient.get("first_name"));
                txtMiddleName.setText((String) patient.get("middle_name"));
                txtLastName.setText((String) patient.get("last_name"));
                cbGender.setSelectedItem(patient.get("gender"));

                // Set DOB in spinner
                Object dobObj = patient.get("dob");
                if(dobObj instanceof java.sql.Date) {
                    dobSpinner.setValue(new java.util.Date(((java.sql.Date) dobObj).getTime()));
                } else if(dobObj instanceof LocalDate) {
                    dobSpinner.setValue(Date.from(((LocalDate) dobObj).atStartOfDay(ZoneId.systemDefault()).toInstant()));
                }

                txtPhone.setText((String) patient.get("phone"));
                txtEmail.setText((String) patient.get("email"));
                txtAddress.setText((String) patient.get("address"));
            } else {
                JOptionPane.showMessageDialog(panel, "Patient not found", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Update patient
        btnUpdate.addActionListener(e -> {
            String patientId = txtPatientId.getText().trim();
            String firstName = txtFirstName.getText().trim();
            String middleName = txtMiddleName.getText().trim();
            String lastName = txtLastName.getText().trim();
            String phone = txtPhone.getText().trim();
            String email = txtEmail.getText().trim();
            String address = txtAddress.getText().trim();
            String gender = cbGender.getSelectedItem().toString();

            // Validation
            if(patientId.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please fill all required fields", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Get DOB from spinner
            java.util.Date selectedDate = (java.util.Date) dobSpinner.getValue();
            LocalDate dob = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            // Prepare map for DAO
            Map<String, Object> patientData = new HashMap<>();
            patientData.put("patient_id", patientId);
            patientData.put("first_name", firstName);
            patientData.put("middle_name", middleName);
            patientData.put("last_name", lastName);
            patientData.put("gender", gender);
            patientData.put("date_of_birth", dob); // LocalDate from spinner
            patientData.put("contact_number", phone);
            patientData.put("email", email);
            patientData.put("address", address);

            boolean success = PatientDAO.updatePatient(patientData);
            if(success) {
                JOptionPane.showMessageDialog(panel, "Patient updated successfully!");
                loadPatientData(); // reload table
                cardLayout.show(contentPanel, "TABLE"); // back to table view
            } else {
                JOptionPane.showMessageDialog(panel, "Failed to update patient", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Clear fields
        btnClear.addActionListener(e -> {
            txtPatientId.setText("");
            txtFirstName.setText("");
            txtMiddleName.setText("");
            txtLastName.setText("");
            cbGender.setSelectedIndex(0);
            dobSpinner.setValue(new java.util.Date()); // reset to today
            txtPhone.setText("");
            txtEmail.setText("");
            txtAddress.setText("");
        });

        return panel;
    }




    private void loadPatientData() {
        patientTableModel.setRowCount(0); // clear existing rows
        java.util.List<Map<String, Object>> patients = PatientDAO.getAllPatients();

        for (Map<String, Object> patient : patients) {
            patientTableModel.addRow(new Object[]{
                    patient.get("patient_id"),
                    patient.get("first_name"),
                    patient.get("middle_name"),
                    patient.get("last_name"),
                    patient.get("date_of_birth"),
                    patient.get("gender"),
                    patient.get("contact_number"),
                    patient.get("email"),
                    patient.get("address"),
                    patient.get("emergency_contact"),
                    patient.get("blood_type"),
                    patient.get("registration_date")
            });
        }
    }
    private JPanel showDoctorSchedulePanel(String doctorId) {
        JPanel schedulePanel = new JPanel(new BorderLayout());
        schedulePanel.setBackground(new Color(255, 255, 255, 180));
        schedulePanel.setOpaque(true);

        // ===== Top Panel =====
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);
        String docName=StatisticsDAO.getDoctorName(Integer.parseInt(doctorId));
        JLabel titleLabel = new JLabel("DR "+docName+"'s Schedule");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(2, 48, 71));

        JButton backBtn = new JButton("Back");
        backBtn.setBackground(new Color(2, 48, 71));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setPreferredSize(new Dimension(100, 30));
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 12));

        topPanel.add(titleLabel);
        topPanel.add(backBtn);

        schedulePanel.add(topPanel, BorderLayout.NORTH);

        // ===== Table =====
        String[] cols = {"Time", "Patient", "Reason", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);

        table.setBackground(new Color(255, 255, 255, 180));
        table.setForeground(new Color(2, 48, 71));
        table.setRowHeight(20);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(2, 48, 71));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        table.getTableHeader().setOpaque(true);
        table.getTableHeader().setBackground(new Color(2, 48, 71));
        table.getTableHeader().setForeground(Color.WHITE);

        // Load schedule
        StatisticsDAO statsDAO = new StatisticsDAO();
        for (Map<String, Object> a : statsDAO.getDoctorSchedule(Integer.parseInt(doctorId))) {
            model.addRow(new Object[]{
                    a.get("time"),
                    a.get("patient"),
                    a.get("reason"),
                    a.get("status")
            });
        }

        schedulePanel.add(scrollPane, BorderLayout.CENTER);

        // ===== Back Button Action =====
        backBtn.addActionListener(e -> cardLayout.show(contentPanel, "TABLE")); // go back to main table

        return schedulePanel;
    }

    private JPanel createDoctorDashboardPanel() {
        JPanel doctorPanel = new JPanel(new BorderLayout());
        doctorPanel.setBackground(new Color(255, 255, 255, 150));
        doctorPanel.setOpaque(false);

        // ===== Title Panel =====
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(255, 255, 255, 150));
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Doctor Management", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(2, 48, 71));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setOpaque(false);

        JButton viewScheduleBtn = new JButton("View Schedule");
        viewScheduleBtn.setBackground(new Color(2, 48, 71));
        viewScheduleBtn.setPreferredSize(new Dimension(150, 40));
        viewScheduleBtn.setForeground(Color.WHITE);
        viewScheduleBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        viewScheduleBtn.setFocusPainted(false);

        JButton listDoctorBtn = new JButton("List All");
        listDoctorBtn.setBackground(new Color(2, 48, 71));
        listDoctorBtn.setPreferredSize(new Dimension(150, 40));
        listDoctorBtn.setForeground(Color.WHITE);
        listDoctorBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        listDoctorBtn.setFocusPainted(false);

        btnPanel.add(viewScheduleBtn);
        btnPanel.add(listDoctorBtn);

        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(btnPanel, BorderLayout.EAST);

        // ===== CardLayout Container =====
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);
        contentPanel.setBackground(new Color(0, 0, 0, 0));

        // ===== Table Panel =====
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(true);
        tablePanel.setBackground(new Color(255, 255, 255, 180));

        String[] columnNames = {"Doctor ID", "First Name","Middle Name","Last Name", "Specialization", "Contact", "Email", "Status"};
        DefaultTableModel  doctorTableModel = new DefaultTableModel(columnNames, 0);
        JTable doctorTable = new JTable(doctorTableModel);
        doctorTable.setBackground(new Color(255, 255, 255, 180));
        doctorTable.setOpaque(false);
        doctorTable.setForeground(new Color(2, 48, 71));
        doctorTable.setRowHeight(20);
        doctorTable.setFont(new Font("Arial", Font.PLAIN, 14));
        doctorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        doctorTable.setGridColor(new Color(2, 48, 71));

        JScrollPane tableScroller = new JScrollPane(doctorTable);
        tableScroller.setOpaque(false);
        tableScroller.getViewport().setOpaque(false);

        doctorTable.getTableHeader().setOpaque(true);
        doctorTable.getTableHeader().setBackground(new Color(2, 48, 71));
        doctorTable.getTableHeader().setForeground(Color.white);

        // Load data
        doctorTableModel.setRowCount(0);
        StatisticsDAO statsDAO = new StatisticsDAO();
        for (Map<String, Object> d : statsDAO.getAllDoctors()) {
            doctorTableModel.addRow(new Object[]{
                    d.get("doctor_id"),
                    d.get("first_name"),
                    d.get("name_name"),
                    d.get("last_name"),
                    d.get("specialization"),
                    d.get("contact"),
                    d.get("email"),
                    d.get("availability")
            });
        }

        tablePanel.add(tableScroller, BorderLayout.CENTER);

        // ===== Form Panel Placeholder (Optional) =====
        JPanel formPanel = new JPanel(); // If you have a form for adding doctors later
        formPanel.setOpaque(true);
        formPanel.setBackground(new Color(255, 255, 255, 180));

        contentPanel.add(tablePanel, "TABLE");
        contentPanel.add(formPanel, "FORM");

        cardLayout.show(contentPanel, "TABLE");

        // ===== Button Actions =====
        viewScheduleBtn.addActionListener(e -> {
            int row = doctorTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(doctorPanel, "Select a doctor first");
                return;
            }
            String doctorId = doctorTableModel.getValueAt(row, 0).toString();

            // Remove the previous schedule panel if it exists (optional, to avoid duplicates)
            for (Component comp : contentPanel.getComponents()) {
                if ("SCHEDULE".equals(comp.getName())) {
                    contentPanel.remove(comp);
                    break;
                }
            }

            // Create new schedule panel and set its name for identification
            JPanel schedulePanel = showDoctorSchedulePanel(doctorId);
            schedulePanel.setName("SCHEDULE");
            contentPanel.add(schedulePanel, "SCHEDULE");

            // Switch to schedule card
            cardLayout.show(contentPanel, "SCHEDULE");

            // Refresh the content panel
            contentPanel.revalidate();
            contentPanel.repaint();
        });
        listDoctorBtn.addActionListener(e -> cardLayout.show(contentPanel, "TABLE"));

        doctorPanel.add(titlePanel, BorderLayout.NORTH);
        doctorPanel.add(contentPanel, BorderLayout.CENTER);

        return doctorPanel;
    }



    public class PrefixFilter extends DocumentFilter {

        private final String prefix;

        public PrefixFilter(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length)
                throws BadLocationException {

            // Prevent deleting the prefix
            if (offset < prefix.length()) {
                return;
            }
            super.remove(fb, offset, length);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length,
                            String text, AttributeSet attrs)
                throws BadLocationException {

            // Prevent editing inside the prefix
            if (offset < prefix.length()) {
                return;
            }
            super.replace(fb, offset, length, text, attrs);
        }
    }

    private JPanel createAddPatientFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255,255,255));
        panel.setOpaque(false);
        Color color=new Color(2,48,71);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String prefix = "HMS-";
        // fields (same as your code)
        JTextField txtId = new JTextField(prefix);
        AbstractDocument doc = (AbstractDocument) txtId.getDocument();
        doc.setDocumentFilter(new PrefixFilter(prefix));
        txtId.setCaretPosition(prefix.length());
        JTextField txtName = new JTextField();
        JTextField txtMName = new JTextField();
        JTextField txtLName = new JTextField();
        JDateChooser dateChooser=new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setPreferredSize(new Dimension(150,30));
        dateChooser.setDate(new Date());
        JComboBox<String> cbGender = new JComboBox<>(new String[]{"Other", "Female", "Male"});
        JTextField txtContact = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextArea txtAddress = new JTextArea(3, 20);
        JScrollPane addressScroll = new JScrollPane(txtAddress);
        JTextField txtEmergency = new JTextField();
        JComboBox<String> cbBlood = new JComboBox<>(
                new String[]{"Unknown","A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"}
        );
        JTextField txtRegDate = new JTextField(date);
        txtRegDate.setEditable(false);
        cbGender.setBackground(color);
        cbGender.setForeground(Color.white);
        cbBlood.setBackground(color);
        cbBlood.setForeground(Color.white);
        JButton btnSave = new JButton("Save");
        JButton btnClear = new JButton("Clear");
        btnSave.setBackground(color);
        btnSave.setForeground(Color.white);
        btnClear.setBackground(color);
        btnClear.setForeground(Color.white);

        int row = 0;
        addField(panel, gbc, row++, "Patient ID:", txtId);
        addField(panel, gbc, row++, "First Name:", txtName);
        addField(panel, gbc, row++, "Middle Name:", txtMName);
        addField(panel, gbc, row++, "Last Name:", txtLName);
        addField(panel, gbc, row++, "Date of Birth:", dateChooser);
        addField(panel, gbc, row++, "Gender:", cbGender);
        addField(panel, gbc, row++, "Contact Number:", txtContact);
        addField(panel, gbc, row++, "Email:", txtEmail);
        addField(panel, gbc, row++, "Address:", addressScroll);
        addField(panel, gbc, row++, "Emergency Contact:", txtEmergency);
        addField(panel, gbc, row++, "Blood Type:", cbBlood);
        addField(panel, gbc, row++, "Registration Date:", txtRegDate);

        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(btnSave, gbc);

        gbc.gridx = 1;
        panel.add(btnClear, gbc);

        btnClear.addActionListener(e -> {
            txtId.setText("HMS-");
            txtId.setCaretPosition(4);
            txtName.setText("");
            txtMName.setText("");
            txtLName.setText("");
            txtContact.setText("");
            txtEmail.setText("");
            txtAddress.setText("");
            txtEmergency.setText("");
            cbGender.setSelectedIndex(0);
            cbBlood.setSelectedIndex(0);
        });
        btnSave.addActionListener(e->{
            Date selectedDate=dateChooser.getDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            String patientId = txtId.getText();
            String firstName = txtName.getText();
            String middleName = txtName.getText();
            String lastName = txtName.getText();
            String dateOfBirth = sdf.format(selectedDate);
            String gender = cbGender.getSelectedItem().toString();
            String contact = txtContact.getText();
            String email = txtEmail.getText();
            String address = txtAddress.getText();
            String emergencyContact = txtEmergency.getText();
            String bloodType = cbBlood.getSelectedItem().toString();

            if (patientId.isEmpty() || firstName.isEmpty() || middleName.isEmpty() || lastName.isEmpty() || dateOfBirth.isEmpty() ||email.isEmpty() || gender.isEmpty() || contact.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please fill in all required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Check length restrictions (matching SQL schema)
            if (patientId.length() > 15) {
                JOptionPane.showMessageDialog(panel, "Patient ID cannot exceed 20 characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (firstName.length() > 100) {
                JOptionPane.showMessageDialog(panel, "First Name cannot exceed 100 characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (middleName.length() > 100) {
                JOptionPane.showMessageDialog(panel, "Middle Name cannot exceed 100 characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (lastName.length() > 100) {
                JOptionPane.showMessageDialog(panel, "Last Name cannot exceed 100 characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (contact.length() != 10 && !contact.matches("\\d{1,10}") ) {
                JOptionPane.showMessageDialog(panel, "Contact number cannot exceed or less than 10 digits.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (email.length() > 30) {
                JOptionPane.showMessageDialog(panel, "Email cannot exceed 100 characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Optional: validate email format
            if (!email.isEmpty() && !email.matches("^[\\w\\.-]+@[\\w\\.-]+\\.\\w{2,}$")) {
                JOptionPane.showMessageDialog(panel, "Invalid email format.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if(emergencyContact.length()!=10 && !emergencyContact.matches("\\d{1,10}")){
                JOptionPane.showMessageDialog(panel, "Emergency number cannot exceed or less than 10 digits.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean Success=PatientDAO.addPatient(patientId,firstName,middleName,lastName,dateOfBirth,gender,contact,email,address,emergencyContact,bloodType);
            if(Success) {
                JOptionPane.showMessageDialog(panel, "Recored add successfully");
                loadPatientData();
            }else
                JOptionPane.showMessageDialog(panel, "Failed to add patient.", "Error", JOptionPane.ERROR_MESSAGE);
        });
        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int y,
                          String label, Component field) {
        Color fgColor=new Color(2,48,71);
        gbc.gridx = 0;
        gbc.gridy = y;
        JLabel coloredLabel=new JLabel(label);
        coloredLabel.setForeground(fgColor);
        panel.add(coloredLabel, gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private JPanel createAddAppointmentFormPanel(DefaultTableModel tableModel) {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 255, 255, 180));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtPatient = new JTextField();
        JComboBox<DoctorItem> cmbDoctor = new JComboBox<>();

        JDateChooser dateChooser=new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setPreferredSize(new Dimension(150,30));
        dateChooser.setDate(new Date());

        JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm"));

        JTextArea txtReason = new JTextArea(3, 20);
        JScrollPane reasonScroll = new JScrollPane(txtReason);

        JButton btnSave = new JButton("Save");
        JButton btnClear = new JButton("Clear");

        Color btnColor = new Color(2, 48, 71);
        btnSave.setBackground(btnColor);
        btnSave.setForeground(Color.WHITE);
        btnClear.setBackground(btnColor);
        btnClear.setForeground(Color.WHITE);

        // Load doctors into combo box
        AppointmentDAO.loadDoctors(cmbDoctor);

        int row = 0;
        addField(panel, gbc, row++, "Patient ID:", txtPatient);
        addField(panel, gbc, row++, "Doctor:", cmbDoctor);
        addField(panel, gbc,  row++, "Appointment date:",dateChooser);
        addField(panel, gbc, row++, "Appointment Time:", timeSpinner);
        addField(panel, gbc, row++, "Reason:", reasonScroll);

        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(btnSave, gbc);

        gbc.gridx = 1;
        panel.add(btnClear, gbc);

        // Clear button logic
        btnClear.addActionListener(e -> {
            txtPatient.setText("");
            cmbDoctor.setSelectedIndex(-1);
            txtReason.setText("");
            timeSpinner.setValue(new Date());
            dateChooser.setDate(new Date());
        });

        // Save button logic
        btnSave.addActionListener(e -> {

            String patientId = txtPatient.getText().trim();
            DoctorItem selectedDoctor = (DoctorItem) cmbDoctor.getSelectedItem();
            String reason = txtReason.getText().trim();
            Date selectedDate = dateChooser.getDate();

            if (patientId.isEmpty() || selectedDoctor == null || reason.isEmpty() || selectedDate == null) {
                JOptionPane.showMessageDialog(
                        panel,
                        "Please fill all fields",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = sdf.format(selectedDate);

            LocalTime time = ((Date) timeSpinner.getValue())
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime();

            boolean success = AppointmentDAO.addAppointment(
                    patientId,
                    selectedDoctor.getDoctorId(),
                    formattedDate,
                    time,
                    reason,
                    userFullName
            );

            if (success) {
                loadAppointments(tableModel);
                JOptionPane.showMessageDialog(panel, "Appointment scheduled successfully!");
                cardLayout.show(contentPanel, "TABLE");
            } else {
                JOptionPane.showMessageDialog(
                        panel,
                        "Failed to schedule appointment",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
        return panel;
    }
    public JPanel createCheckInPanel() {
        // 1. MAIN CONTAINER (Matches your AppointmentPanel style)
        JPanel checkInPanel = new JPanel(new BorderLayout());
        checkInPanel.setBackground(new Color(255, 255, 255, 150));


        // ================= TITLE & CONTROLS PANEL =================
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(255, 255, 255, 150));
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // -- Left Side: Title --
        JLabel titleLabel = new JLabel("Patient Check-In", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(2, 48, 71));

        // -- Right Side: Input & Buttons (FlowLayout) --
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlsPanel.setOpaque(false);

        // Input Field
        JLabel lblId = new JLabel("Appt ID:");
        lblId.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblId.setForeground(new Color(2, 48, 71));

        JTextField txtApptId = new JTextField(10); // Width 10
        txtApptId.setPreferredSize(new Dimension(100, 35));
        txtApptId.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Confirm Button
        JButton btnCheckIn = new JButton("Confirm Check-In");
        styleButton(btnCheckIn, new Color(2, 48, 71));

        // Refresh Button
        JButton btnRefresh = new JButton("Refresh");
        styleButton(btnRefresh, new Color(2, 48, 71));

        // Add to Controls Panel
        controlsPanel.add(lblId);
        controlsPanel.add(txtApptId);
        controlsPanel.add(btnCheckIn);
        controlsPanel.add(btnRefresh);

        // Add to Title Panel
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(controlsPanel, BorderLayout.EAST);

        // ================= TABLE SECTION =================
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false); // Transparent to show background
        tablePanel.setBorder(new EmptyBorder(0, 20, 20, 20)); // Padding around table

        String[] columnNames = {"ID", "Patient ID", "Doctor ID", "Time", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Apply your Custom Table Style
        JTable table = new JTable(tableModel);
        table.setBackground(new Color(255, 255, 255, 200)); // Slightly more opaque for readability
        table.setOpaque(true);
        table.setForeground(new Color(2, 48, 71));
        table.setRowHeight(25); // Slightly taller for better look
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(2, 48, 71));

        // Header Styling
        table.getTableHeader().setOpaque(true);
        table.getTableHeader().setBackground(new Color(2, 48, 71));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));

        // ScrollPane Styling
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setOpaque(false);
        tableScroll.getViewport().setOpaque(false);

        tablePanel.add(tableScroll, BorderLayout.CENTER);

        // ================= LOGIC & LISTENERS =================

        // 1. Load Data on Startup
        AppointmentDAO.loadTodayAppointments(tableModel);

        // 2. Table Click Listener (Auto-fill ID)
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    String id = table.getValueAt(selectedRow, 0).toString();
                    txtApptId.setText(id);
                }
            }
        });

        // 3. Refresh Action
        btnRefresh.addActionListener(e -> {
            AppointmentDAO.loadTodayAppointments(tableModel);
            txtApptId.setText("");
        });

        // 4. Check-In Action
        btnCheckIn.addActionListener(e -> {
            String idText = txtApptId.getText().trim();

            if (idText.isEmpty()) {
                JOptionPane.showMessageDialog(checkInPanel, "Please select an appointment or enter an ID.");
                return;
            }

            try {
                int apptId = Integer.parseInt(idText);

                // --- DAO VALIDATION ---
                String validationMsg = AppointmentDAO.validateCheckIn(apptId);

                if (!validationMsg.equals("VALID")) {
                    JOptionPane.showMessageDialog(checkInPanel, validationMsg, "Check-In Failed", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // --- DAO EXECUTION ---
                boolean success = AppointmentDAO.performCheckIn(apptId);

                if (success) {
                    JOptionPane.showMessageDialog(checkInPanel, "Success! Patient Checked In.");
                    AppointmentDAO.loadTodayAppointments(tableModel); // Refresh table
                    txtApptId.setText("");
                } else {
                    JOptionPane.showMessageDialog(checkInPanel, "Database Error.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(checkInPanel, "ID must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // ================= FINAL ASSEMBLY =================
        checkInPanel.add(titlePanel, BorderLayout.NORTH);
        checkInPanel.add(tablePanel, BorderLayout.CENTER);

        return checkInPanel;
    }

    // Helper method to keep button styling consistent and clean
    private void styleButton(JButton btn, Color bgColor) {
        btn.setBackground(bgColor);
        btn.setPreferredSize(new Dimension(160, 35)); // Adjusted size
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false); // Makes it look flatter/modern
    }

    public JPanel createSearchRecordsPanel() {

        // --- 1. MAIN CONTAINER (FIXED PAINTING) ---
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(255, 255, 255, 150));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        mainPanel.setOpaque(false);

        // --- 2. HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("Search Medical Records", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(2, 48, 71));

        JPanel searchControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchControls.setOpaque(false);

        JLabel lblSearch = new JLabel("Patient ID:");
        lblSearch.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblSearch.setForeground(new Color(2, 48, 71));

        JTextField txtSearchId = new JTextField(12);
        txtSearchId.setPreferredSize(new Dimension(120, 35));
        txtSearchId.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JButton btnSearch = new JButton("Search");
        styleButton(btnSearch, new Color(2, 48, 71));

        searchControls.add(lblSearch);
        searchControls.add(txtSearchId);
        searchControls.add(btnSearch);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(searchControls, BorderLayout.EAST);

        // --- 3. CONTENT PANEL (FIXED PAINTING) ---
        JPanel contentPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(255, 255, 255, 150));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(0, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();

        JPanel profilePanel = createProfilePanel();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 0);
        contentPanel.add(profilePanel, gbc);

        // --- HISTORY TABLE ---
        String[] columns = {"Visit Date", "Doctor", "Diagnosis/Reason", "Prescription", "Status"};
        DefaultTableModel historyModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable historyTable = new JTable(historyModel);
        styleTable(historyTable);
        historyTable.setFillsViewportHeight(true);
        historyTable.setRowHeight(28);

        JScrollPane tableScroll = new JScrollPane(historyTable);
        tableScroll.setOpaque(false);
        tableScroll.getViewport().setOpaque(false);
        tableScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(2, 48, 71)), "Visit History",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 16),
                new Color(2, 48, 71)));

        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPanel.add(tableScroll, gbc);

        // --- SEARCH LOGIC ---
        btnSearch.addActionListener(e -> {
            String id = txtSearchId.getText().trim();

            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(mainPanel, "Please enter a Patient ID.");
                return;
            }

            clearFields(historyModel); // clear old content first

            String[] data = PatientDAO.getPatientProfile(id);

            if (data != null) {
                lblNameVal.setText(data[0]);
                lblPhoneVal.setText(data[1]);
                lblAgeVal.setText(data[2]);
                lblGenderVal.setText(data[3]);
                txtAddressVal.setText(data[4]);

                PatientDAO.loadPatientHistory(id, historyModel);
            } else {
                JOptionPane.showMessageDialog(mainPanel, "Patient not found.");
            }

            contentPanel.revalidate();
            contentPanel.repaint();
        });

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    // ===== PROFILE PANEL (FIXED PAINTING) =====
    private JPanel createProfilePanel() {

        JPanel p = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(255, 255, 255, 200));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(2, 48, 71)), "Patient Details",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 16),
                new Color(2, 48, 71)));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 15, 5, 15);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 0.5;

        lblNameVal = createValueLabel();
        lblPhoneVal = createValueLabel();
        lblAgeVal = createValueLabel();
        lblGenderVal = createValueLabel();

        txtAddressVal = new JTextArea(2, 20);
        txtAddressVal.setEditable(false);
        txtAddressVal.setOpaque(false);
        txtAddressVal.setFont(new Font("SansSerif", Font.BOLD, 14));
        txtAddressVal.setForeground(new Color(50, 50, 50));
        txtAddressVal.setLineWrap(true);

        addLabelPair(p, g, 0, 0, "Name:", lblNameVal);
        addLabelPair(p, g, 1, 0, "Age:", lblAgeVal);

        addLabelPair(p, g, 0, 1, "Phone:", lblPhoneVal);
        addLabelPair(p, g, 1, 1, "Gender:", lblGenderVal);

        g.gridx = 0;
        g.gridy = 2;
        JLabel lblAddr = new JLabel("Address:");
        lblAddr.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblAddr.setForeground(new Color(2, 48, 71));
        p.add(lblAddr, g);

        g.gridx = 1;
        g.gridwidth = 3;
        p.add(txtAddressVal, g);

        return p;
    }

    // ===== HELPERS =====
    private void addLabelPair(JPanel p, GridBagConstraints g, int col, int row, String title, JLabel valueLabel) {
        g.gridx = col * 2;
        g.gridy = row;
        g.gridwidth = 1;

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblTitle.setForeground(new Color(2, 48, 71));
        p.add(lblTitle, g);

        g.gridx = (col * 2) + 1;
        p.add(valueLabel, g);
    }

    private JLabel createValueLabel() {
        JLabel l = new JLabel("-");
        l.setFont(new Font("SansSerif", Font.BOLD, 14));
        l.setForeground(new Color(50, 50, 50));
        return l;
    }

    private void clearFields(DefaultTableModel model) {
        lblNameVal.setText("-");
        lblPhoneVal.setText("-");
        lblAgeVal.setText("-");
        lblGenderVal.setText("-");
        txtAddressVal.setText("");
        model.setRowCount(0);
    }
    private void styleTable(JTable table) {
        table.setBackground(new Color(255, 255, 255, 180));
        table.setOpaque(true);
        table.setForeground(new Color(2, 48, 71));
        table.setRowHeight(25);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(2, 48, 71));
        table.getTableHeader().setOpaque(true);
        table.getTableHeader().setBackground(new Color(2, 48, 71));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
    }
    // 1. MAIN METHOD: Creates the Profile Card Panel
    private JPanel createUserProfilePanel() {
        // --- 1. Main Container ---
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);

        // --- 2. Fetch Data ---
        Map<String, String> userInfo = Database.ReceptionistDAO.getReceptionistProfile(currentAuthId);

        // Fallback if user not found (prevents crash)
        if (userInfo.isEmpty()) {
            userInfo.put("name", "unknown");
            userInfo.put("email", "N/A");
            userInfo.put("phone", "N/A");
            userInfo.put("id", "N/A");
        }

        // --- 3. The Card Panel ---
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setPreferredSize(new Dimension(420, 500)); // Adjusted size
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(2, 48, 71), 1, true),
                new EmptyBorder(30, 40, 30, 40)
        ));

        // --- HEADER SECTION ---
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);

        // Avatar
        JLabel profileImage = new JLabel(createRoundIcon(userInfo.get("name"), 90));
        profileImage.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Name
        JLabel lblName = new JLabel(userInfo.get("name"));
        lblName.setFont(new Font("Serif", Font.BOLD, 24));
        lblName.setForeground(new Color(2, 48, 71));
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblName.setBorder(new EmptyBorder(10, 0, 5, 0));

        // Role
        JLabel lblRole = new JLabel("Receptionist");
        lblRole.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblRole.setForeground(Color.GRAY);
        lblRole.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(profileImage);
        headerPanel.add(lblName);
        headerPanel.add(lblRole);
        headerPanel.add(Box.createVerticalStrut(25));

        // --- DETAILS SECTION ---
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        // We use a counter to ensure rows don't overlap
        int rowIndex = 0;

        addProfileRow(detailsPanel, gbc, rowIndex, "Employee ID", userInfo.get("id"));
        rowIndex += 2; // Move down 2 spots (1 for data, 1 for line)

        addProfileRow(detailsPanel, gbc, rowIndex, "Email", userInfo.get("email"));
        rowIndex += 2;

        addProfileRow(detailsPanel, gbc, rowIndex, "Phone", userInfo.get("phone"));
        // rowIndex += 2; // If you add Address back later, uncomment this


        // Assemble
        cardPanel.add(headerPanel, BorderLayout.NORTH);
        cardPanel.add(detailsPanel, BorderLayout.CENTER);


        mainPanel.add(cardPanel);
        return mainPanel;
    }

    // --- Helper 1: Add Data Row (Fixed Layout) ---
    private void addProfileRow(JPanel panel, GridBagConstraints gbc, int row, String title, String value) {
        // Label (Left)
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblTitle.setForeground(Color.GRAY);
        panel.add(lblTitle, gbc);

        // Value (Right)
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblValue.setForeground(new Color(50, 50, 50));
        lblValue.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(lblValue, gbc);

        // Line (Below)
        gbc.gridx = 0;
        gbc.gridy = row + 1;
        gbc.gridwidth = 2; // Span full width
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(240, 240, 240));
        panel.add(sep, gbc);

        // Reset
        gbc.gridwidth = 1;
    }

    // --- Helper 2: Create Round Icon ---
    private Icon createRoundIcon(String name, int size) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(2, 48, 71));
        g2.fillOval(0, 0, size, size);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, size / 2));
        FontMetrics fm = g2.getFontMetrics();

        String initials = "";
        if (name != null && !name.trim().isEmpty()) {
            String[] parts = name.split(" ");
            initials += parts[0].charAt(0);
            if (parts.length > 1) initials += parts[1].charAt(0);
        } else {
            initials = "U";
        }

        int x = (size - fm.stringWidth(initials.toUpperCase())) / 2;
        int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(initials.toUpperCase(), x, y);

        g2.dispose();
        return new ImageIcon(img);
    }
    private void loadAppointments(DefaultTableModel model) {
        model.setRowCount(0);

        for (Map<String,Object> row : AppointmentDAO.getAppointments()) {
            model.addRow(new Object[]{
                    row.get("appointment_id"),
                    row.get("patient_name"),
                    row.get("doctor_name"),
                    row.get("time"),
                    row.get("status")
            });
        }
    }

    private JPanel createAppointmentDashboardPanel() {
        JPanel appointmentPanel = new JPanel(new BorderLayout());
        appointmentPanel.setBackground(new Color(255,255,255,150));
        appointmentPanel.setOpaque(false); // keep background semi-transparent

        // ===== Title Panel =====
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(255,255,255,150));
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Appointment Management", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(2, 48, 71));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setOpaque(false);

        JButton addAppointmentBtn = new JButton("+ Add New ");
        addAppointmentBtn.setBackground(new Color(2, 48, 71));
        addAppointmentBtn.setPreferredSize(new Dimension(150, 40));
        addAppointmentBtn.setForeground(Color.WHITE);
        addAppointmentBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        addAppointmentBtn.setFocusPainted(false);

        JButton listAppointmentBtn = new JButton("List All");
        listAppointmentBtn.setBackground(new Color(2, 48, 71));
        listAppointmentBtn.setPreferredSize(new Dimension(150, 40));
        listAppointmentBtn.setForeground(Color.WHITE);
        listAppointmentBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        listAppointmentBtn.setFocusPainted(false);

        btnPanel.add(addAppointmentBtn);
        btnPanel.add(listAppointmentBtn);

        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(btnPanel, BorderLayout.EAST);

        // ===== CardLayout container =====
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);
        contentPanel.setBackground(new Color(0,0,0,0));

        // ===== Table Panel =====
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(true);
        tablePanel.setBackground(new Color(255,255,255,180));

        String[] columnNames = {"ID", "Patient", "Doctor", "Time", "Status"};
        DefaultTableModel appointmentTableModel = new DefaultTableModel(columnNames, 0);
        JTable appointmentTable = new JTable(appointmentTableModel);
        appointmentTable.setBackground(new Color(255,255,255,180));
        appointmentTable.setOpaque(false);
        appointmentTable.setForeground(new Color(2,48,71));
        appointmentTable.setRowHeight(20);
        appointmentTable.setFont(new Font("Arial", Font.PLAIN, 14));
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentTable.setGridColor(new Color(2,48,71));

        JScrollPane tableScroll = new JScrollPane(appointmentTable);
        tableScroll.setOpaque(false);
        tableScroll.getViewport().setOpaque(false);
        appointmentTable.getTableHeader().setOpaque(true);
        appointmentTable.getTableHeader().setBackground(new Color(2,48,71));
        appointmentTable.getTableHeader().setForeground(Color.white);

        tablePanel.add(tableScroll, BorderLayout.CENTER);

        // ===== Form Panel =====
        JPanel formPanel = createAddAppointmentFormPanel(appointmentTableModel); // you need to implement this like patient form
        formPanel.setOpaque(true);
        formPanel.setBackground(new Color(255,255,255,180));

        // ===== Add panels to CardLayout =====
        contentPanel.add(tablePanel, "TABLE");
        contentPanel.add(formPanel, "FORM");

        cardLayout.show(contentPanel, "TABLE"); // show table by default

        // ===== Button Actions =====
        addAppointmentBtn.addActionListener(e -> cardLayout.show(contentPanel, "FORM"));
        listAppointmentBtn.addActionListener(e -> cardLayout.show(contentPanel, "TABLE"));

        // ===== Add to main panel =====
        appointmentPanel.add(titlePanel, BorderLayout.NORTH);
        appointmentPanel.add(contentPanel, BorderLayout.CENTER);

        // ===== Load table data =====
        loadAppointments(appointmentTableModel);

        return appointmentPanel;
    }
    private JPanel createBillingDashboardPanel() {
        JPanel billingPanel = new JPanel(new BorderLayout());
        billingPanel.setBackground(new Color(255, 255, 255, 150));
        billingPanel.setOpaque(false);

        // ===== Title Panel =====
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Billing Management", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(2, 48, 71));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setOpaque(false);

        JButton addBillBtn = new JButton("+ Add New Bill");
        addBillBtn.setBackground(new Color(2, 48, 71));
        addBillBtn.setForeground(Color.WHITE);
        addBillBtn.setPreferredSize(new Dimension(150, 40));
        addBillBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        addBillBtn.setFocusPainted(false);

        JButton listBillBtn = new JButton("List All");
        listBillBtn.setBackground(new Color(2, 48, 71));
        listBillBtn.setForeground(Color.WHITE);
        listBillBtn.setPreferredSize(new Dimension(150, 40));
        listBillBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        listBillBtn.setFocusPainted(false);

        btnPanel.add(addBillBtn);
        btnPanel.add(listBillBtn);

        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(btnPanel, BorderLayout.EAST);

        // ===== CardLayout Container =====
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        // ===== Table Panel =====
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(true);
        tablePanel.setBackground(new Color(255, 255, 255, 180));

        String[] columns = {"Bill ID", "Patient ID", "Patient Name", "Date", "Amount", "Status"};
        DefaultTableModel billingTableModel = new DefaultTableModel(columns, 0);
        JTable billingTable = new JTable(billingTableModel);
        billingTable.setBackground(new Color(255, 255, 255, 180));
        billingTable.setForeground(new Color(2, 48, 71));
        billingTable.setRowHeight(20);
        billingTable.setFont(new Font("Arial", Font.PLAIN, 14));
        billingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        billingTable.setGridColor(new Color(2, 48, 71));

        JScrollPane scrollPane = new JScrollPane(billingTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        billingTable.getTableHeader().setOpaque(true);
        billingTable.getTableHeader().setBackground(new Color(2, 48, 71));
        billingTable.getTableHeader().setForeground(Color.WHITE);

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Load all bills
        loadAllBills(billingTableModel);

        // ===== Add Bill Form Panel =====
        JPanel addBillPanel = createAddBillFormPanel(billingTableModel);
        addBillPanel.setOpaque(true);
        addBillPanel.setBackground(new Color(255, 255, 255, 180));

        // ===== Add panels to CardLayout =====
        contentPanel.add(tablePanel, "TABLE");
        contentPanel.add(addBillPanel, "FORM");

        cardLayout.show(contentPanel, "TABLE"); // show table by default

        // ===== Button Actions =====
        addBillBtn.addActionListener(e -> cardLayout.show(contentPanel, "FORM"));
        listBillBtn.addActionListener(e -> {
            loadAllBills(billingTableModel);
            cardLayout.show(contentPanel, "TABLE");
        });

        billingPanel.add(titlePanel, BorderLayout.NORTH);
        billingPanel.add(contentPanel, BorderLayout.CENTER);

        return billingPanel;
    }

    // ===== Helper method to load all bills from DAO =====
    private void loadAllBills(DefaultTableModel model) {
        model.setRowCount(0);

        for (Map<String, Object> bill : BillingDAO.getAllBills()) {
            model.addRow(new Object[]{
                    bill.get("bill_id"),
                    bill.get("patient_id"),
                    bill.get("patient_name"),
                    bill.get("bill_date"),
                    bill.get("total_amount"),
                    bill.get("payment_status")
            });
        }
    }

    // ===== Create Add Bill Form Panel =====
    private JPanel createAddBillFormPanel(DefaultTableModel tableModel) {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 255, 255, 180));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ===== Fields =====
        JTextField txtPatientId = new JTextField();
        txtPatientId.setEditable(false); // 🔒 auto-filled only

        JTextField txtConsultationFee = new JTextField("0.00");

        JTextField txtLabFee = new JTextField("200.00"); // 💰 constant lab fee
        txtLabFee.setEditable(false);   // 🔒 lock it

        JTextField txtOtherFee = new JTextField("0.00");

        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Paid"});

        JTextField txtDate = new JTextField(LocalDate.now().toString());
        txtDate.setEditable(false);

        JButton btnSave = new JButton("Save");
        JButton btnClear = new JButton("Clear");

        btnSave.setBackground(new Color(2, 48, 71));
        btnSave.setForeground(Color.WHITE);

        btnClear.setBackground(new Color(2, 48, 71));
        btnClear.setForeground(Color.WHITE);

        // ===== Load pending lab request =====
        String pendingPatientId = BillingDAO.getPendingPatientId();
        if (pendingPatientId != null) {
            txtPatientId.setText(pendingPatientId);
        } else {
            JOptionPane.showMessageDialog(panel,
                    "No pending lab requests found.",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        int row = 0;
        addField(panel, gbc, row++, "Patient ID:", txtPatientId);
        addField(panel, gbc, row++, "Consultation Fee:", txtConsultationFee);
        addField(panel, gbc, row++, "Lab Fee:", txtLabFee);
        addField(panel, gbc, row++, "Other Fee:", txtOtherFee);
        addField(panel, gbc, row++, "Status:", cbStatus);
        addField(panel, gbc, row++, "Date:", txtDate);

        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(btnSave, gbc);

        gbc.gridx = 1;
        panel.add(btnClear, gbc);

        // ===== Button Actions =====
        btnClear.addActionListener(e -> {
            txtConsultationFee.setText("0.00");
            txtOtherFee.setText("0.00");
            cbStatus.setSelectedIndex(0);

            // Reload pending patient id
            String pid = BillingDAO.getPendingPatientId();
            txtPatientId.setText(pid != null ? pid : "");
        });

        btnSave.addActionListener(e -> {

            String patientId = txtPatientId.getText().trim();
            String status = cbStatus.getSelectedItem().toString();
            String date = txtDate.getText();

            if (patientId.isEmpty()) {
                JOptionPane.showMessageDialog(panel,
                        "No pending lab request to bill.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Parse fees
            double consultationFee, labFee, otherFee;
            try {
                consultationFee = Double.parseDouble(txtConsultationFee.getText().trim());
                labFee = Double.parseDouble(txtLabFee.getText().trim()); // always 200
                otherFee = Double.parseDouble(txtOtherFee.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel,
                        "Invalid fee input",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            double totalAmount = consultationFee + labFee + otherFee;

            boolean success = BillingDAO.addBill(
                    patientId,
                    LocalDate.parse(date),
                    consultationFee,
                    labFee,
                    otherFee,
                    totalAmount,
                    status,
                    null,
                    userFullName
            );

            if (success) {
                JOptionPane.showMessageDialog(panel, "Bill added successfully!");
                loadAllBills(tableModel);

                cardLayout.show(contentPanel, "TABLE");
            } else {
                JOptionPane.showMessageDialog(panel,
                        "Failed to add bill",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }




    public static String getGreeting() {
        LocalTime now = LocalTime.now();

        if (now.isAfter(LocalTime.of(4, 59)) && now.isBefore(LocalTime.NOON)) {
            return "Good Morning";
        } else if (now.isAfter(LocalTime.of(11, 59)) && now.isBefore(LocalTime.of(17, 0))) {
            return "Good Afternoon";
        } else if (now.isAfter(LocalTime.of(16, 59)) && now.isBefore(LocalTime.of(22, 0))) {
            return "Good Evening";
        } else {
            return "Good Night";
        }
    }

    @Override
    void logout() {
        int choice = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            if (frame != null) {
                frame.dispose();
            }
            new LoginPage().setVisible(true);
        }
    }
}