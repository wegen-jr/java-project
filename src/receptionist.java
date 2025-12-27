import Database.DatabaseConnection;
import Database.PatientDAO;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.time.*;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.*;
import java.util.*;
import javax.swing.text.*;
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

    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.ENGLISH);
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    String date = now.format(dateFormatter).toLowerCase();
    String time = now.format(timeFormatter).toLowerCase();

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
                    case "dashboard":
                        showDashboard();
                        break;
                    case "patients":
                        patientsDashboard();
                        break;
                    case "appointment":
                        // Show appointment panel
                        break;
                    case "doctors":
                        // Show doctors panel
                        break;
                    case "bills":
                        // Show bills panel
                        break;
                    case "check_in":
                        break;
                    case "new_appointment":
                        break;
                    case "search_records":
                        break;
                    case "update_info":
                        break;
                    case "view_today":
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

    private JPanel createNorthPanel() {
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 200, 10));
        northPanel.setBackground(new Color(2, 48, 71));
        northPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.white));

        JPanel searchWrapper = new JPanel(new FlowLayout());
        JTextField searchBar = new JTextField();
        searchBar.setPreferredSize(new Dimension(500, 30));
        searchBar.setMaximumSize(new Dimension(500, 30));
        JLabel searchLabel = new JLabel("search patient by Id: ");
        searchLabel.setForeground(Color.white);
        searchWrapper.add(searchLabel);
        searchWrapper.add(searchBar);
        searchWrapper.setOpaque(false);

        JPanel userWrapper = new JPanel(new FlowLayout());
        JLabel username = new JLabel("username ke makbel");
        username.setForeground(Color.white);
        username.setFont(new Font("SansSerif", Font.BOLD, 12));
        JButton logout = new JButton("Logout");
        logout.setForeground(Color.white);
        logout.setBackground(new Color(2, 48, 71));
        logout.setFont(new Font("SansSerif", Font.BOLD, 12));
        logout.addActionListener(e -> logout());
        userWrapper.add(username);
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

        addNavClickListener(dashboardWrapper, "dashboard");
        addNavClickListener(patientWrapper, "patients");
        addNavClickListener(appointmentWrapper, "appointment");
        addNavClickListener(doctorWrapper, "doctors");
        addNavClickListener(billWrapper, "bills");

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
        navContainer.add(Box.createVerticalGlue());

        leftPanel.add(navContainer);
        leftPanel.add(Box.createVerticalGlue());

        return leftPanel;
    }

    private JPanel createCentralDashboardPanel(ImageIcon[] actionIcons) {
        JPanel centralPanel = new JPanel();
        centralPanel.setLayout(new BoxLayout(centralPanel, BoxLayout.Y_AXIS));
        centralPanel.setOpaque(false);

        Font title = new Font("Serif", Font.BOLD, 24);
        Font subtitle = new Font("SansSerif", Font.BOLD, 12);
        Color color = new Color(2, 48, 71);

        // Greeting panel
        JPanel greetingPanel = new JPanel();
        greetingPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 5));
        greetingPanel.setOpaque(true);
        greetingPanel.setBackground(new Color(255, 255, 255, 150));
        greetingPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel greetingLabel = new JLabel(getGreeting() + " username kemakbel");
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
        JLabel wifi = new JLabel(new ImageIcon(actionIcons[5].getImage())); // wifi icon
        JLabel timeText = new JLabel(time);
        timeText.setForeground(color);
        timeWrapper.add(wifi);
        timeWrapper.add(timeText);
        dateTimePanel.add(dateText);
        dateTimePanel.add(timeWrapper);

        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        statusPanel.setBackground(new Color(255, 255, 255, 150));
        statusPanel.setOpaque(true);

        JPanel patientStat = createStatPanel(actionIcons[6], "patient waiting", "4", color);
        JPanel doctorStat = createStatPanel(actionIcons[7], "available doctors", "4", color);
        JPanel appointStat = createStatPanel(actionIcons[8], "total appointments", "4", color);

        statusPanel.add(patientStat);
        statusPanel.add(doctorStat);
        statusPanel.add(appointStat);

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
        actionsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 25, 10));
        actionsPanel.setBackground(new Color(255, 255, 255, 150));
        actionsPanel.setOpaque(true);

        // Create action panels
        JPanel checkPanel = createActionPanel(actionIcons[9], "Check in patient", color, subtitle, Color.WHITE);
        JPanel addPanel = createActionPanel(actionIcons[10], "New appointment", color, subtitle, Color.WHITE);
        JPanel searchPanel = createActionPanel(actionIcons[11], "Search records", color, subtitle, Color.WHITE);
        JPanel updatePanel = createActionPanel(actionIcons[12], "Update information", color, subtitle, Color.WHITE);
        JPanel showPanel = createActionPanel(actionIcons[13], "View today", color, subtitle, Color.WHITE);

        addNavClickListener(checkPanel, "check_in");
        addNavClickListener(addPanel, "new_appointment");
        addNavClickListener(searchPanel, "search_records");
        addNavClickListener(updatePanel, "update_info");
        addNavClickListener(showPanel, "view_today");

        actionsPanel.add(checkPanel);
        actionsPanel.add(addPanel);
        actionsPanel.add(searchPanel);
        actionsPanel.add(updatePanel);
        actionsPanel.add(showPanel);

        // Add to central panel
        centralPanel.add(greetingPanel);
        centralPanel.add(dateTimePanel);
        centralPanel.add(statusPanel);
        centralPanel.add(quickActionPanel);
        centralPanel.add(actionsPanel);

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

        ImageIcon[] icons = new ImageIcon[5];
        icons[0] = scaleIcon(dashboardIcon, 20, 20);
        icons[1] = scaleIcon(patientIcon, 20, 20);
        icons[2] = scaleIcon(appointmentIcon, 20, 20);
        icons[3] = scaleIcon(doctorIcon, 20, 20);
        icons[4] = scaleIcon(portalIcon, 30, 30);

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

        ImageIcon[] icons = new ImageIcon[14];
        icons[5] = scaleIcon(wifiIcon, 25, 25);
        icons[6] = scaleIcon(patients, 25, 25);
        icons[7] = scaleIcon(bag, 25, 25);
        icons[8] = scaleIcon(clock, 25, 25);
        icons[9] = scaleIcon(checkIn, 25, 25);
        icons[10] = scaleIcon(add, 25, 25);
        icons[11] = scaleIcon(searchIcon, 25, 25);
        icons[12] = scaleIcon(updateIcon, 25, 25);
        icons[13] = scaleIcon(showIcon, 25, 25);

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
        String[] columnName={"Card_ID","Full Name","Birt Date","Gender","Contact","Email","Adress","Emergency_Contact","Blood_type","Registration Date"};
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

    private void loadPatientData() {
        patientTableModel.setRowCount(0); // clear existing rows
        java.util.List<Map<String, Object>> patients = PatientDAO.getAllPatients();

        for (Map<String, Object> patient : patients) {
            patientTableModel.addRow(new Object[]{
                    patient.get("patient_id"),
                    patient.get("full_name"),
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
        SpinnerDateModel dobModel = new SpinnerDateModel();
        JSpinner txtDob = new JSpinner(dobModel);
        JSpinner.DateEditor dobEditor = new JSpinner.DateEditor(txtDob, "yyyy-MM-dd");
        txtDob.setEditor(dobEditor);
        txtDob.setValue(new Date());
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
        addField(panel, gbc, row++, "Full Name:", txtName);
        addField(panel, gbc, row++, "Date of Birth:", txtDob);
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
            txtContact.setText("");
            txtEmail.setText("");
            txtAddress.setText("");
            txtEmergency.setText("");
            cbGender.setSelectedIndex(0);
            cbBlood.setSelectedIndex(0);
        });
        btnSave.addActionListener(e->{

            String patientId = txtId.getText();
            String fullName = txtName.getText();
            String dateOfBirth = ((JSpinner.DateEditor) txtDob.getEditor()).getFormat().format(txtDob.getValue());
            String gender = cbGender.getSelectedItem().toString();
            String contact = txtContact.getText();
            String email = txtEmail.getText();
            String address = txtAddress.getText();
            String emergencyContact = txtEmergency.getText();
            String bloodType = cbBlood.getSelectedItem().toString();

            if (patientId.isEmpty() || fullName.isEmpty() || dateOfBirth.isEmpty() || gender.isEmpty() || contact.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please fill in all required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Check length restrictions (matching SQL schema)
            if (patientId.length() > 15) {
                JOptionPane.showMessageDialog(panel, "Patient ID cannot exceed 20 characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (fullName.length() > 100) {
                JOptionPane.showMessageDialog(panel, "Full Name cannot exceed 100 characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
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

            boolean Success=PatientDAO.addPatient(patientId,fullName,dateOfBirth,gender,contact,email,address,emergencyContact,bloodType);
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

    private JPanel createPatientListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 255, 255, 200));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));



        return panel;
    }

    private JPanel createAddPatientPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 255, 255, 200));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        return panel;
    }

    private JPanel createSearchPatientPanel() {

        return null;
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
        if (frame != null) {
            frame.dispose();
        }
        new LoginPage().setVisible(true);
    }
}