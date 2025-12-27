import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.*;
import javax.swing.border.*;

public class Doctor extends staffUser {


    private static final String URL = "jdbc:mysql://localhost:3306/HMS";
    private static final String DB_USERNAME = "phpmyadmin";
    private static final String DB_PASSWORD = "wegen@1996";


    private String doctorId;
    private String fullName;
    private String specialization;
    private String contactNumber;
    private String email;


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


    private String getAppointmentCount() {
        int count = 0;
        try (Connection con = DriverManager.getConnection(URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement pst = con.prepareStatement("SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_date = CURDATE()")) {
            pst.setString(1, this.doctorId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) count = rs.getInt(1);
        } catch (Exception e) { return "0"; }
        return String.valueOf(count);
    }

    private String getPendingLabsCount() {
        int count = 0;
        try (Connection con = DriverManager.getConnection(URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement pst = con.prepareStatement("SELECT COUNT(*) FROM lab_requests WHERE doctor_id = ? AND (status = 'Pending' OR result_details IS NULL OR result_details = '')")) {
            pst.setString(1, this.doctorId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) count = rs.getInt(1);
        } catch (Exception e) { return "0"; }
        return String.valueOf(count);
    }
    private void updateCardValue(JPanel card, String newValue) {

        BorderLayout layout = (BorderLayout) card.getLayout();
        JPanel content = (JPanel) layout.getLayoutComponent(BorderLayout.CENTER);


        for (Component comp : content.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (label.getIcon() == null) {
                    label.setText(newValue);
                    card.repaint();
                    break;
                }
            }
        }
    }

    private JPanel createSummaryCard(String title, String value, Color accentColor, String iconPath) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


                g2.setColor(new Color(255, 255, 255, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);


                g2.setColor(new Color(255, 255, 255, 230));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 30, 30);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(280, 160));


        JPanel accent = new JPanel();
        accent.setPreferredSize(new Dimension(0, 5));
        accent.setBackground(accentColor);
        card.add(accent, BorderLayout.NORTH);


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

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(100, 110, 120));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        card.add(content, BorderLayout.CENTER);
        card.add(lblTitle, BorderLayout.SOUTH);
        return card;
    }

    @Override
    void showDashboard() {
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


        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(new Color(2, 48, 71));
        leftPanel.setPreferredSize(new Dimension(220, 0));
        leftPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1, true));

        JPanel portalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        portalPanel.setOpaque(false);
        portalPanel.setMaximumSize(new Dimension(250, 60));
        ImageIcon portalIcon = new ImageIcon(new ImageIcon("assets/portal.png").getImage()
                .getScaledInstance(25, 25, Image.SCALE_SMOOTH));
        JLabel portalHeader = new JLabel("Doctor Portal", portalIcon, JLabel.LEFT);
        portalHeader.setForeground(Color.WHITE);
        portalHeader.setFont(new Font("Arial", Font.BOLD, 18));
        portalPanel.add(portalHeader);


        JPanel navDash = createNavItem("Dashboard", "assets/dashboard.png", navFont, this::showDashboard);
        JPanel navPat  = createNavItem("My Patients", "assets/hospitalisation.png", navFont, this::showPatientsDashboard);
        JPanel navApp  = createNavItem("Appointments", "assets/medical-appointment.png", navFont, this::showAppointmentsDashboard);
        JPanel navLab  = createNavItem("Send Lab Requests", "assets/observation.png", navFont, this::showLabRequestsDashboard);
        JPanel navLabRes = createNavItem("Lab Results", "assets/Lab Response.png", navFont, this::showLabResultsWindow);
        JPanel navSch  = createNavItem("My Schedule", "assets/calendar.png", navFont, this::showScheduleDashboard);
        JPanel navPres = createNavItem("Prescriptions", "assets/medical-prescription.png", navFont, this::showPrescriptionsDashboard);
        JPanel navProf = createNavItem("My Profile", "assets/user.png", navFont, this::showProfileWindow);

        JPanel navLogout = createNavItem("Logout", "assets/logout.png", navFont, () -> {
            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) { frame.dispose(); logout(); }
        });

        leftPanel.add(portalPanel);
        leftPanel.add(new JSeparator());
        leftPanel.add(Box.createVerticalStrut(2));
        leftPanel.add(navDash); leftPanel.add(navPat); leftPanel.add(navApp);
        leftPanel.add(navLab); leftPanel.add(navLabRes); leftPanel.add(navSch);
        leftPanel.add(navPres); leftPanel.add(navProf);
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(new JSeparator());
        leftPanel.add(navLogout);
        leftPanel.add(Box.createVerticalStrut(10));

        mainBackgroundPanel.add(leftPanel, BorderLayout.WEST);


        loadDoctorData();
        JPanel rightPanel = new JPanel(null);
        rightPanel.setOpaque(false);


        JPanel greetingContainer = new JPanel(new BorderLayout(20, 0));
        greetingContainer.setOpaque(false);
        greetingContainer.setBounds(60, 220, 1000, 100);


        JPanel accentLine = new JPanel();
        accentLine.setBackground(new Color(2, 48, 71));
        accentLine.setPreferredSize(new Dimension(8, 0));
        greetingContainer.add(accentLine, BorderLayout.WEST);


        JPanel textSection = new JPanel(new GridLayout(2, 1, 0, 5));
        textSection.setOpaque(false);

        String greeting = getTimeGreeting();
        JLabel welcomeLabel = new JLabel(greeting + ", Dr. " + (fullName != null ? fullName : "Doctor") + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        welcomeLabel.setForeground(new Color(2, 48, 71));

        JLabel subtitleLabel = new JLabel("Your hospital overview for today is ready.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(80, 100, 110));

        textSection.add(welcomeLabel);
        textSection.add(subtitleLabel);
        greetingContainer.add(textSection, BorderLayout.CENTER);

        rightPanel.add(greetingContainer);


        JPanel cardContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        cardContainer.setOpaque(false);
        cardContainer.setBounds(60, 360, 1100, 200);


        cardContainer.add(createSummaryCard("TODAY'S APPOINTMENTS", getAppointmentCount(),
                new Color(2, 48, 71), "assets/medical-appointment.png"));

        cardContainer.add(createSummaryCard("PENDING LABS", getPendingLabsCount(),
                new Color(230, 57, 70), "assets/observation.png"));

        cardContainer.add(createSummaryCard("TOTAL PATIENTS", String.valueOf(getMyPatients().size()),
                new Color(42, 157, 143), "assets/hospitalisation.png"));

        rightPanel.add(cardContainer);

        mainBackgroundPanel.add(rightPanel, BorderLayout.CENTER);
        frame.add(mainBackgroundPanel);

        Timer refreshTimer = new Timer(5000, e -> {

            String appCount = getAppointmentCount();
            String labCount = getPendingLabsCount();
            String patCount = String.valueOf(getMyPatients().size());

            Component[] cards = cardContainer.getComponents();
            if (cards.length >= 3) {
                updateCardValue((JPanel)cards[0], appCount);
                updateCardValue((JPanel)cards[1], labCount);
                updateCardValue((JPanel)cards[2], patCount);
            }
        });
        refreshTimer.start();


        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                refreshTimer.stop();
            }
        });
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

    void showProfileWindow() {
        JFrame profFrame = new JFrame("Medical Practitioner Profile");
        profFrame.setSize(450, 600);
        profFrame.setLocationRelativeTo(null);
        profFrame.setLayout(new BorderLayout());


        JPanel headerCard = new JPanel();
        headerCard.setBackground(new Color(2, 48, 71));
        headerCard.setPreferredSize(new Dimension(450, 190));
        headerCard.setLayout(null);

        JLabel photoLabel = new JLabel("", SwingConstants.CENTER);
        photoLabel.setBounds(180, 25, 90, 90);

        try {
            ImageIcon rawIcon = new ImageIcon("assets/user.png");
            Image img = rawIcon.getImage();
            java.awt.image.BufferedImage masked = new java.awt.image.BufferedImage(90, 90, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = masked.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.fillOval(0, 0, 90, 90);
            g2d.setComposite(AlphaComposite.SrcIn);
            g2d.drawImage(img, 0, 0, 90, 90, null);
            g2d.dispose();
            photoLabel.setIcon(new ImageIcon(masked));
        } catch (Exception e) {
            photoLabel.setIcon(new ImageIcon(new java.awt.image.BufferedImage(90, 90, java.awt.image.BufferedImage.TYPE_INT_ARGB) {{
                Graphics2D g = createGraphics();
                g.setColor(Color.LIGHT_GRAY);
                g.fillOval(0, 0, 90, 90);
                g.dispose();
            }}));
        }
        headerCard.add(photoLabel);

        JLabel nameLabel = new JLabel("Dr. " + (fullName != null ? fullName : "Practitioner"), SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setBounds(0, 125, 450, 30);
        headerCard.add(nameLabel);


        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(20, 40, 20, 40));

        addProfileRow(body, "STAFF ID", doctorId);
        addProfileRow(body, "SPECIALIZATION", specialization);
        addProfileRow(body, "CONTACT NUMBER", contactNumber);
        addProfileRow(body, "EMAIL ADDRESS", email);

        JScrollPane scrollPane = new JScrollPane(body);
        scrollPane.setBorder(null);


        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        footer.setBackground(Color.WHITE);

        
        Color normalColor = new Color(2, 48, 71);
        Color hoverColor = new Color(6, 75, 110);

        JPanel closeBtnNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        closeBtnNav.setBackground(normalColor);
        closeBtnNav.setPreferredSize(new Dimension(200, 40));
        closeBtnNav.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel closeLabel = new JLabel("CLOSE PROFILE");
        closeLabel.setForeground(Color.WHITE);
        closeLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        closeBtnNav.add(closeLabel);

        // Apply the same MouseListener logic you used in createNavItem
        MouseAdapter btnListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                profFrame.dispose();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                closeBtnNav.setBackground(hoverColor);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                closeBtnNav.setBackground(normalColor);
            }
        };

        closeBtnNav.addMouseListener(btnListener);
        closeLabel.addMouseListener(btnListener); // Ensure clicking text also works

        footer.add(closeBtnNav);

        profFrame.add(headerCard, BorderLayout.NORTH);
        profFrame.add(scrollPane, BorderLayout.CENTER);
        profFrame.add(footer, BorderLayout.SOUTH);

        profFrame.setVisible(true);
    }

    // Keep the helper method the same
    private void addProfileRow(JPanel parent, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(400, 60));
        row.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(new Color(120, 130, 140));

        JLabel val = new JLabel(value != null ? value : "Not Set");
        val.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        val.setForeground(new Color(50, 50, 50));

        row.add(lbl, BorderLayout.NORTH);
        row.add(val, BorderLayout.CENTER);
        row.add(new JSeparator(), BorderLayout.SOUTH);

        parent.add(row);
    }

    void showLabResultsWindow() {
        JFrame resultsFrame = new JFrame("Lab Test Results - Dr. " + (fullName != null ? fullName : ""));
        resultsFrame.setSize(1000, 600);
        resultsFrame.setLocationRelativeTo(null);
        resultsFrame.getContentPane().setBackground(Color.WHITE);
        resultsFrame.setLayout(new BorderLayout(0, 0));

        // --- Header Panel ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(2, 48, 71));
        headerPanel.setPreferredSize(new Dimension(1000, 70));
        headerPanel.setBorder(new javax.swing.border.EmptyBorder(0, 20, 0, 20));

        JLabel titleLabel = new JLabel("Laboratory Reports History");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel infoLabel = new JLabel("Database automatically synchronizes 'Completed' status upon refresh");
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(200, 200, 200));
        headerPanel.add(infoLabel, BorderLayout.SOUTH);

        // --- Table Design ---
        String[] columns = {"Request ID", "Patient Name", "Test Type", "Status", "Lab Response", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        // --- Data Loading Logic with DATABASE SYNC ---
        Runnable loadData = () -> {
            model.setRowCount(0);
            String selectSql = "SELECT lr.request_id, p.full_name, lr.test_type, lr.status, lr.result_details, lr.request_date " +
                    "FROM lab_requests lr JOIN patients p ON lr.patient_id = p.patient_id WHERE lr.doctor_id = ?";

            // This SQL will update the database status if a result exists but status is still 'Pending'
            String updateSql = "UPDATE lab_requests SET status = 'Completed' WHERE request_id = ? AND result_details IS NOT NULL AND status != 'Completed'";

            try (Connection con = DriverManager.getConnection(URL, DB_USERNAME, DB_PASSWORD);
                 PreparedStatement selectPst = con.prepareStatement(selectSql);
                 PreparedStatement updatePst = con.prepareStatement(updateSql)) {

                selectPst.setString(1, this.doctorId);
                ResultSet rs = selectPst.executeQuery();

                while (rs.next()) {
                    int reqId = rs.getInt(1);
                    String currentStatus = rs.getString(4);
                    String labResponse = rs.getString(5);

                    // --- DATABASE SYNC LOGIC ---
                    // If there is a response but the DB still says 'Pending' or something else
                    if (labResponse != null && !labResponse.trim().isEmpty() && !"Completed".equalsIgnoreCase(currentStatus)) {
                        updatePst.setInt(1, reqId);
                        updatePst.executeUpdate(); // This actually changes the text in your MySQL table
                        currentStatus = "Completed"; // Update local variable for the table display
                    }

                    model.addRow(new Object[]{
                            reqId,
                            rs.getString(2),
                            rs.getString(3),
                            currentStatus,
                            labResponse == null ? "Pending..." : labResponse,
                            rs.getTimestamp(6)
                    });
                }
            } catch (SQLException e) { e.printStackTrace(); }
        };

        loadData.run();

        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(240, 244, 247));
        table.setShowVerticalLines(false);

        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(245, 247, 249));
        table.getTableHeader().setForeground(new Color(2, 48, 71));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(300);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        scrollPane.getViewport().setBackground(Color.WHITE);

        resultsFrame.add(headerPanel, BorderLayout.NORTH);
        resultsFrame.add(scrollPane, BorderLayout.CENTER);

        // --- Footer with Refresh and Dismiss ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        footer.setBackground(Color.WHITE);

        Color navNormal = new Color(2, 48, 71);
        Color navHover = new Color(6, 75, 110);
        Color refreshColor = new Color(42, 157, 143);

        JPanel refreshBtnNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        refreshBtnNav.setBackground(refreshColor);
        refreshBtnNav.setPreferredSize(new Dimension(150, 40));
        refreshBtnNav.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JLabel refreshLabel = new JLabel("REFRESH");
        refreshLabel.setForeground(Color.WHITE);
        refreshLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        refreshBtnNav.add(refreshLabel);

        JPanel dismissBtnNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        dismissBtnNav.setBackground(navNormal);
        dismissBtnNav.setPreferredSize(new Dimension(150, 40));
        dismissBtnNav.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JLabel dismissLabel = new JLabel("DISMISS");
        dismissLabel.setForeground(Color.WHITE);
        dismissLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        dismissBtnNav.add(dismissLabel);

        MouseAdapter refreshAction = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { loadData.run(); }
            @Override public void mouseEntered(MouseEvent e) { refreshBtnNav.setBackground(new Color(50, 180, 165)); }
            @Override public void mouseExited(MouseEvent e) { refreshBtnNav.setBackground(refreshColor); }
        };
        refreshBtnNav.addMouseListener(refreshAction);
        refreshLabel.addMouseListener(refreshAction);

        MouseAdapter dismissAction = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { resultsFrame.dispose(); }
            @Override public void mouseEntered(MouseEvent e) { dismissBtnNav.setBackground(navHover); }
            @Override public void mouseExited(MouseEvent e) { dismissBtnNav.setBackground(navNormal); }
        };
        dismissBtnNav.addMouseListener(dismissAction);
        dismissLabel.addMouseListener(dismissAction);

        footer.add(refreshBtnNav);
        footer.add(dismissBtnNav);
        resultsFrame.add(footer, BorderLayout.SOUTH);

        resultsFrame.setVisible(true);
    }

    void showLabRequestsDashboard() {
        JFrame labFrame = new JFrame("Laboratory Test Request");
        labFrame.setSize(500, 650);
        labFrame.setLocationRelativeTo(null);

        // Main container with a light grey/blue background
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(240, 244, 247));
        container.setBorder(new EmptyBorder(20, 20, 20, 20));

        // The "Card"
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(25, 30, 25, 30)
        ));

        // Heading
        JLabel title = new JLabel("Lab Order Form");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(2, 48, 71));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        Font labelFont = new Font("SansSerif", Font.BOLD, 12);
        Color labelColor = new Color(100, 100, 100);

        // --- Form Fields ---
        JLabel l1 = new JLabel("PATIENT NAME");
        l1.setFont(labelFont); l1.setForeground(labelColor);
        JComboBox<String> pCombo = new JComboBox<>();
        for (Patient p : getMyPatients()) pCombo.addItem(p.getPatientId() + " - " + p.getName());
        pCombo.setMaximumSize(new Dimension(400, 35));

        JLabel l2 = new JLabel("TEST CATEGORY");
        l2.setFont(labelFont); l2.setForeground(labelColor);
        String[] tests = {"Complete Blood Count", "X-Ray Chest", "MRI Scan", "Urinalysis", "Glucose Test"};
        JComboBox<String> testCombo = new JComboBox<>(tests);
        testCombo.setMaximumSize(new Dimension(400, 35));

        JLabel l3 = new JLabel("PRIORITY");
        l3.setFont(labelFont); l3.setForeground(labelColor);
        String[] priorities = {"Normal", "Urgent", "Emergency"};
        JComboBox<String> priorityCombo = new JComboBox<>(priorities);
        priorityCombo.setMaximumSize(new Dimension(400, 35));

        JLabel l4 = new JLabel("INSTRUCTIONS");
        l4.setFont(labelFont); l4.setForeground(labelColor);
        JTextArea notesArea = new JTextArea(4, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBackground(new Color(250, 250, 250));
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setMaximumSize(new Dimension(400, 80));

        // Adding components to card
        card.add(title); card.add(Box.createVerticalStrut(20));
        card.add(l1); card.add(Box.createVerticalStrut(5));
        card.add(pCombo); card.add(Box.createVerticalStrut(15));
        card.add(l2); card.add(Box.createVerticalStrut(5));
        card.add(testCombo); card.add(Box.createVerticalStrut(15));
        card.add(l3); card.add(Box.createVerticalStrut(5));
        card.add(priorityCombo); card.add(Box.createVerticalStrut(15));
        card.add(l4); card.add(Box.createVerticalStrut(5));
        card.add(notesScroll); card.add(Box.createVerticalStrut(30));

        // --- NAV-STYLE SUBMIT BUTTON ---
        Color navNormal = new Color(2, 48, 71);
        Color navHover = new Color(6, 75, 110);

        JPanel sendBtnNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        sendBtnNav.setBackground(navNormal);
        sendBtnNav.setMaximumSize(new Dimension(400, 45));
        sendBtnNav.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel btnLabel = new JLabel("PLACE ORDER");
        btnLabel.setForeground(Color.WHITE);
        btnLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        sendBtnNav.add(btnLabel);

        // Button Interaction & SQL logic
        MouseAdapter navAction = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String selected = (String) pCombo.getSelectedItem();
                if (selected == null) return;
                String pId = selected.split(" - ")[0];

                String sql = "INSERT INTO lab_requests (doctor_id, patient_id, test_type, priority, notes_from_doctor, status) VALUES (?, ?, ?, ?, ?, 'Pending')";
                try (Connection con = DriverManager.getConnection(URL, DB_USERNAME, DB_PASSWORD);
                     PreparedStatement pst = con.prepareStatement(sql)) {
                    pst.setString(1, doctorId);
                    pst.setString(2, pId);
                    pst.setString(3, testCombo.getSelectedItem().toString());
                    pst.setString(4, priorityCombo.getSelectedItem().toString());
                    pst.setString(5, notesArea.getText());
                    pst.executeUpdate();
                    JOptionPane.showMessageDialog(labFrame, "Lab Order Successfully Placed.");
                    labFrame.dispose();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(labFrame, "Error: " + ex.getMessage());
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) { sendBtnNav.setBackground(navHover); }
            @Override
            public void mouseExited(MouseEvent e) { sendBtnNav.setBackground(navNormal); }
        };

        sendBtnNav.addMouseListener(navAction);
        btnLabel.addMouseListener(navAction); // Ensure clicking label also works

        card.add(sendBtnNav);
        container.add(card, BorderLayout.CENTER);
        labFrame.add(container);
        labFrame.setVisible(true);
    }

    void showPatientsDashboard() {
        JFrame patientsFrame = new JFrame("Patient Directory - HMS");
        patientsFrame.setSize(1000, 650);
        patientsFrame.setLocationRelativeTo(null);
        patientsFrame.getContentPane().setBackground(Color.WHITE);
        patientsFrame.setLayout(new BorderLayout(0, 0));

        // --- Header Panel ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(2, 48, 71));
        headerPanel.setPreferredSize(new Dimension(1000, 80));
        headerPanel.setBorder(new javax.swing.border.EmptyBorder(15, 25, 15, 25));

        JLabel titleLabel = new JLabel("My Assigned Patients");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Total Patients: " + getMyPatients().size());
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(200, 215, 225));

        JPanel textContainer = new JPanel(new GridLayout(2, 1));
        textContainer.setOpaque(false);
        textContainer.add(titleLabel);
        textContainer.add(subtitleLabel);
        headerPanel.add(textContainer, BorderLayout.WEST);

        // --- Table Design ---
        String[] columns = {"Patient ID", "Full Name", "Age", "Gender", "Last Visit", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        List<Patient> patients = getMyPatients();
        for (Patient p : patients) {
            model.addRow(new Object[]{p.getPatientId(), p.getName(), p.getAge(), p.getGender(), p.getLastVisit(), p.getStatus()});
        }

        JTable table = new JTable(model);
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setGridColor(new Color(235, 235, 235));
        table.setSelectionBackground(new Color(230, 240, 250));
        table.setSelectionForeground(new Color(2, 48, 71));
        table.setShowVerticalLines(false);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(245, 248, 250));
        table.getTableHeader().setForeground(new Color(2, 48, 71));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // --- Footer Action Bar (NAV-STYLE BUTTON) ---
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        footerPanel.setBackground(Color.WHITE);

        Color navNormal = new Color(2, 48, 71);
        Color navHover = new Color(6, 75, 110);

        // Close Directory Button Only
        JPanel closeBtnNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        closeBtnNav.setBackground(navNormal);
        closeBtnNav.setPreferredSize(new Dimension(160, 40));
        closeBtnNav.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel closeLabel = new JLabel("CLOSE DIRECTORY");
        closeLabel.setForeground(Color.WHITE);
        closeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeBtnNav.add(closeLabel);

        // Mouse Logic for Close
        MouseAdapter closeAction = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                patientsFrame.dispose();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                closeBtnNav.setBackground(navHover);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                closeBtnNav.setBackground(navNormal);
            }
        };
        closeBtnNav.addMouseListener(closeAction);
        closeLabel.addMouseListener(closeAction);

        footerPanel.add(closeBtnNav);

        patientsFrame.add(headerPanel, BorderLayout.NORTH);
        patientsFrame.add(scrollPane, BorderLayout.CENTER);
        patientsFrame.add(footerPanel, BorderLayout.SOUTH);

        patientsFrame.setVisible(true);
    }

    void showAppointmentsDashboard() {
        JFrame appFrame = new JFrame("Daily Schedule - Today's Appointments");
        appFrame.setSize(1000, 650);
        appFrame.setLocationRelativeTo(null);
        appFrame.getContentPane().setBackground(Color.WHITE);
        appFrame.setLayout(new BorderLayout(0, 0));

        // --- 1. Header Panel ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(2, 48, 71));
        headerPanel.setPreferredSize(new Dimension(1000, 80));
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel titleLabel = new JLabel("Appointment Schedule");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        JLabel dateLabel = new JLabel("Today: " + java.time.LocalDate.now());
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateLabel.setForeground(new Color(200, 215, 225));

        JPanel textContainer = new JPanel(new GridLayout(2, 1));
        textContainer.setOpaque(false);
        textContainer.add(titleLabel);
        textContainer.add(dateLabel);
        headerPanel.add(textContainer, BorderLayout.WEST);

        // --- 2. Table Design ---
        String[] columns = {"Time Slot", "Patient Name", "Reason for Visit", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        // Keep your exact logic
        List<Appointment> appointments = getTodaysAppointments();
        for (Appointment app : appointments) {
            model.addRow(new Object[]{app.getTime(), app.getPatientName(), app.getReason(), app.getStatus()});
        }

        JTable table = new JTable(model);
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setGridColor(new Color(235, 235, 235));
        table.setSelectionBackground(new Color(230, 240, 250));
        table.setSelectionForeground(new Color(2, 48, 71));
        table.setShowVerticalLines(false);

        // Header Styling
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(245, 248, 250));
        table.getTableHeader().setForeground(new Color(2, 48, 71));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // --- 3. Footer Action Panel (Nav-Click Style Button) ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 15));
        footer.setBackground(Color.WHITE);

        // Sidebar Style Button
        Color normalColor = new Color(2, 48, 71);
        Color hoverColor = new Color(6, 75, 110);

        JPanel closeBtnNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        closeBtnNav.setBackground(normalColor);
        closeBtnNav.setPreferredSize(new Dimension(180, 40));
        closeBtnNav.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel closeLabel = new JLabel("CLOSE SCHEDULE");
        closeLabel.setForeground(Color.WHITE);
        closeLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        closeBtnNav.add(closeLabel);

        MouseAdapter navStyleAction = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { appFrame.dispose(); }
            @Override
            public void mouseEntered(MouseEvent e) { closeBtnNav.setBackground(hoverColor); }
            @Override
            public void mouseExited(MouseEvent e) { closeBtnNav.setBackground(normalColor); }
        };

        closeBtnNav.addMouseListener(navStyleAction);
        closeLabel.addMouseListener(navStyleAction);

        footer.add(closeBtnNav);

        appFrame.add(headerPanel, BorderLayout.NORTH);
        appFrame.add(scrollPane, BorderLayout.CENTER);
        appFrame.add(footer, BorderLayout.SOUTH);

        appFrame.setVisible(true);
    }

    void showScheduleDashboard() {
        JFrame scheduleFrame = new JFrame("Clinical Schedule - Dr. " + (fullName != null ? fullName : ""));
        scheduleFrame.setSize(950, 600);
        scheduleFrame.setLocationRelativeTo(null);
        scheduleFrame.getContentPane().setBackground(Color.WHITE);
        scheduleFrame.setLayout(new BorderLayout(0, 0));

        // --- 1. Header Panel ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(2, 48, 71)); // Dark Navy
        headerPanel.setPreferredSize(new Dimension(950, 80));
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

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

        try (Connection con = DriverManager.getConnection(URL, DB_USERNAME, DB_PASSWORD)) {
            String query = "SELECT a.appointment_time, p.full_name, a.status, a.notes " +
                    "FROM appointments a JOIN patients p ON a.patient_id = p.patient_id " +
                    "WHERE a.doctor_id = ? ORDER BY a.appointment_time";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, doctorId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4) == null ? "No notes added" : rs.getString(4)
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
        }

        JTable table = new JTable(model);
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(230, 240, 250));

        // Styling Header
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(245, 248, 250));
        table.getTableHeader().setForeground(new Color(2, 48, 71));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        // Column Width Adjustments
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(350); // Give more space to notes

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // --- 3. Footer Action Panel (Nav-Click Style Button) ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 15));
        footer.setBackground(Color.WHITE);

        // Creating Nav-Style close button
        Color normalColor = new Color(2, 48, 71);
        Color hoverColor = new Color(6, 75, 110);

        JPanel closeBtnNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        closeBtnNav.setBackground(normalColor);
        closeBtnNav.setPreferredSize(new Dimension(180, 40));
        closeBtnNav.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel closeLabel = new JLabel("BACK TO PORTAL");
        closeLabel.setForeground(Color.WHITE);
        closeLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        closeBtnNav.add(closeLabel);

        MouseAdapter navAction = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { scheduleFrame.dispose(); }
            @Override
            public void mouseEntered(MouseEvent e) { closeBtnNav.setBackground(hoverColor); }
            @Override
            public void mouseExited(MouseEvent e) { closeBtnNav.setBackground(normalColor); }
        };

        closeBtnNav.addMouseListener(navAction);
        closeLabel.addMouseListener(navAction);

        footer.add(closeBtnNav);

        scheduleFrame.add(headerPanel, BorderLayout.NORTH);
        scheduleFrame.add(scrollPane, BorderLayout.CENTER);
        scheduleFrame.add(footer, BorderLayout.SOUTH);

        scheduleFrame.setVisible(true);
    }

    // RESTORED PRESCRIPTION LOGIC MATCHING YOUR TABLE
    void showPrescriptionsDashboard() {
        JFrame prescFrame = new JFrame("Medical Prescription Pad");
        prescFrame.setSize(600, 700);
        prescFrame.setLocationRelativeTo(null);
        prescFrame.getContentPane().setBackground(new Color(240, 244, 247));
        prescFrame.setLayout(new BorderLayout());

        // --- 1. Header Section ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(2, 48, 71));
        headerPanel.setPreferredSize(new Dimension(600, 80));
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel titleLabel = new JLabel("New Prescription");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JLabel docLabel = new JLabel("Prescribing Physician: Dr. " + (fullName != null ? fullName : ""));
        docLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        docLabel.setForeground(new Color(200, 215, 225));

        JPanel textContainer = new JPanel(new GridLayout(2, 1));
        textContainer.setOpaque(false);
        textContainer.add(titleLabel);
        textContainer.add(docLabel);
        headerPanel.add(textContainer, BorderLayout.WEST);

        // --- 2. Patient Selector Bar ---
        JPanel selectorPanel = new JPanel(new BorderLayout());
        selectorPanel.setBackground(Color.WHITE);
        selectorPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel pLabel = new JLabel("Select Patient:  ");
        pLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JComboBox<String> pDropdown = new JComboBox<>();
        pDropdown.setPreferredSize(new Dimension(300, 35));
        for (Patient p : getMyPatients()) {
            pDropdown.addItem(p.getPatientId() + " - " + p.getName());
        }
        selectorPanel.add(pLabel, BorderLayout.WEST);
        selectorPanel.add(pDropdown, BorderLayout.CENTER);

        // --- 3. Prescription Writing Area (The "Pad") ---
        JPanel padContainer = new JPanel(new BorderLayout());
        padContainer.setOpaque(false);
        padContainer.setBorder(new EmptyBorder(10, 25, 10, 25));

        JTextArea area = new JTextArea("DATE: " + LocalDate.now() + "\n" +
                "--------------------------------------------------\n" +
                "Rx:\n\n" +
                "1. \n" +
                "2. \n\n" +
                "Instructions:");
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(20, 20, 20, 20));

        JScrollPane scrollArea = new JScrollPane(area);
        scrollArea.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        padContainer.add(scrollArea, BorderLayout.CENTER);

        // --- 4. Footer with Nav-Style Buttons ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        footer.setBackground(new Color(240, 244, 247));

        // Nav-Style Save Button
        Color navNormal = new Color(2, 48, 71);
        Color navHover = new Color(6, 75, 110);

        JPanel saveBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        saveBtn.setBackground(navNormal);
        saveBtn.setPreferredSize(new Dimension(180, 45));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel saveLabel = new JLabel("SAVE & RECORD");
        saveLabel.setForeground(Color.WHITE);
        saveLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        saveBtn.add(saveLabel);

        // Nav-Style Cancel Button
        JPanel cancelBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        cancelBtn.setBackground(new Color(150, 150, 150));
        cancelBtn.setPreferredSize(new Dimension(120, 45));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel cancelLabel = new JLabel("CANCEL");
        cancelLabel.setForeground(Color.WHITE);
        cancelLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        cancelBtn.add(cancelLabel);

        // Logic for Save
        saveBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String selectedItem = (String) pDropdown.getSelectedItem();
                if (selectedItem == null) return;
                String pId = selectedItem.split(" - ")[0];
                String pText = area.getText();

                String sql = "INSERT INTO prescriptions (doctor_id, patient_id, prescription_text, prescribed_date) VALUES (?, ?, ?, ?)";
                try (Connection con = DriverManager.getConnection(URL, DB_USERNAME, DB_PASSWORD);
                     PreparedStatement pst = con.prepareStatement(sql)) {
                    pst.setString(1, doctorId);
                    pst.setString(2, pId);
                    pst.setString(3, pText);
                    pst.setDate(4, java.sql.Date.valueOf(LocalDate.now()));

                    if (pst.executeUpdate() > 0) {
                        JOptionPane.showMessageDialog(prescFrame, "Prescription saved to medical history.");
                        prescFrame.dispose();
                    }
                } catch (SQLException ex) { JOptionPane.showMessageDialog(prescFrame, "Error: " + ex.getMessage()); }
            }
            @Override public void mouseEntered(MouseEvent e) { saveBtn.setBackground(navHover); }
            @Override public void mouseExited(MouseEvent e) { saveBtn.setBackground(navNormal); }
        });

        cancelBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { prescFrame.dispose(); }
            @Override public void mouseEntered(MouseEvent e) { cancelBtn.setBackground(new Color(100, 100, 100)); }
            @Override public void mouseExited(MouseEvent e) { cancelBtn.setBackground(new Color(150, 150, 150)); }
        });

        footer.add(cancelBtn);
        footer.add(saveBtn);

        // Combine Everything
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(selectorPanel, BorderLayout.SOUTH);

        prescFrame.add(topPanel, BorderLayout.NORTH);
        prescFrame.add(padContainer, BorderLayout.CENTER);
        prescFrame.add(footer, BorderLayout.SOUTH);

        prescFrame.setVisible(true);
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
    @Override void logout() { new LoginPage().setVisible(true); }

}

// Data Helper Classes
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
