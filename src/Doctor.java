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

import static Database.DatabaseConnection.*;

public class Doctor extends staffUser {
    private String doctorId, fullName, specialization, contactNumber, email;
    private String currentUsername; // To store the login username

    // The Constructor MUST accept the username string from LoginPage
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


    private String getAppointmentCount() {
        int count = 0;
        // Uses his 'appointments' table and 'doctor_id'
        String sql = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_date = CURDATE()";
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/HMS", "abrshiz", "abrsh123");
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, this.doctorId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) count = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return String.valueOf(count);
    }

    private String getPendingLabsCount() {
        int count = 0;
        // Since he put lab_tests in medical_records, we count records where lab_tests exist
        // but might not have results yet. (Adjust this logic based on how you save results)
        String sql = "SELECT COUNT(*) FROM medical_records WHERE doctor_id = ? AND lab_tests IS NOT NULL AND lab_tests != ''";
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/HMS", "abrshiz", "abrsh123");
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, this.doctorId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) count = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return String.valueOf(count);
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

    void showDashboard() {
        // --- NEW LOGIC: Prevent Window Duplication ---
        // Closes any existing Doctor dashboard before opening a new one
        for (Frame f : Frame.getFrames()) {
            if (f instanceof JFrame && f.getTitle().contains("HMS - Hospital Management System (Doctor)")) {
                f.dispose();
            }
        }

        JFrame frame = new JFrame("HMS - Hospital Management System (Doctor)");
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Font navFont = new Font("SansSerif", Font.BOLD, 12);

        // Main background with the stethoscope image
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

        // --- Left Sidebar (Your Original Design) ---
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

        // Nav Items (Calling your existing methods)
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

        // --- Right Content Area ---
        loadDoctorData(); // This should now query 'full_name' from doctors table
        JPanel rightPanel = new JPanel(null);
        rightPanel.setOpaque(false);

        // --- NEW LOGIC: Points to friend's database structure ---
        String greeting = getTimeGreeting();
        // Using fullName (queried from 'full_name' column)
        JLabel welcomeLabel = new JLabel(greeting + ", Dr. " + (fullName != null ? fullName : "Doctor") + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 42));
        welcomeLabel.setForeground(new Color(2, 48, 71));
        welcomeLabel.setBounds(200, 260, 1000, 60);
        rightPanel.add(welcomeLabel);

        // Summary Cards Container
        JPanel cardContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 35, 0));
        cardContainer.setOpaque(false);
        cardContainer.setBounds(150, 380, 1050, 200);

        // --- NEW LOGIC: Statistics updated to match friend's HMS schema ---
        cardContainer.add(createSummaryCard("TODAY'S APPOINTMENTS", getAppointmentCount(),
                new Color(2, 48, 71), "assets/medical-appointment.png"));

        cardContainer.add(createSummaryCard("PENDING LABS", getPendingLabsCount(),
                new Color(230, 57, 70), "assets/observation.png"));

        cardContainer.add(createSummaryCard("TOTAL PATIENTS", String.valueOf(getMyPatients().size()),
                new Color(42, 157, 143), "assets/hospitalisation.png"));

        rightPanel.add(cardContainer);

        mainBackgroundPanel.add(rightPanel, BorderLayout.CENTER);
        frame.add(mainBackgroundPanel);
        frame.setVisible(true);
    }
    private void loadDoctorData() {
        if (this.currentUsername == null) return;

        String query = "SELECT * FROM doctors WHERE username = ?";

        // IMPORTANT: Call your helper class instead of DriverManager
        try (Connection con = Database.DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setString(1, this.currentUsername);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    this.doctorId = rs.getString("doctor_id");
                    this.fullName = rs.getString("full_name");
                    this.specialization = rs.getString("specialization");
                    this.contactNumber = rs.getString("contact_number");
                    this.email = rs.getString("email");
                    System.out.println("✅ Portal context set to: " + this.fullName);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Profile Load Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    void showProfileWindow() {
        // Refresh data from the new HMS database before showing the profile
        loadDoctorData();

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

        // Using the updated 'fullName' variable from friend's database
        JLabel nameLabel = new JLabel("Dr. " + (fullName != null ? fullName : "Practitioner"), SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setBounds(0, 125, 450, 30);
        headerCard.add(nameLabel);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(20, 40, 20, 40));

        // These values are now populated from the new loadDoctorData() logic
        addProfileRow(body, "STAFF ID", (doctorId != null ? doctorId : "N/A"));
        addProfileRow(body, "SPECIALIZATION", (specialization != null ? specialization : "General Medicine"));
        addProfileRow(body, "CONTACT NUMBER", (contactNumber != null ? contactNumber : "Not Set"));
        addProfileRow(body, "EMAIL ADDRESS", (email != null ? email : "Not Set"));

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
        closeLabel.addMouseListener(btnListener);

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
        // Increased height slightly to 65 to prevent text clipping on high-res screens
        row.setMaximumSize(new Dimension(450, 65));
        row.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        // A slightly darker grey for better readability against white backgrounds
        lbl.setForeground(new Color(100, 110, 120));

        // Logic Fix: Ensure we handle empty strings ("") the same as nulls
        String displayValue = (value == null || value.trim().isEmpty()) ? "Not Specified" : value;

        JLabel val = new JLabel(displayValue);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 15)); // Slightly larger for better UX
        val.setForeground(new Color(2, 48, 71)); // Using your primary dark blue for the actual data

        row.add(lbl, BorderLayout.NORTH);
        row.add(val, BorderLayout.CENTER);

        // Modern separator color
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(230, 230, 230));
        row.add(sep, BorderLayout.SOUTH);

        parent.add(row);
        parent.add(Box.createVerticalStrut(5)); // Adds consistent spacing between rows
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

        JLabel titleLabel = new JLabel("Medical Records & Lab History");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel infoLabel = new JLabel("Data synchronized with medical_records table");
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(200, 200, 200));
        headerPanel.add(infoLabel, BorderLayout.SOUTH);

        // --- Table Design ---
        // Mapping: Record ID, Patient Name, Test Type (lab_tests), Notes, Date
        String[] columns = {"Record ID", "Patient Name", "Test Type", "Visit Date", "Doctor's Notes"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        // --- Data Loading Logic adapted for HMS Schema ---
        Runnable loadData = () -> {
            model.setRowCount(0);
            // UPDATED SQL: Joins medical_records with patients to get the full_name
            String selectSql = "SELECT mr.record_id, p.full_name, mr.lab_tests, mr.visit_date, mr.notes " +
                    "FROM medical_records mr " +
                    "JOIN patients p ON mr.patient_id = p.patient_id " +
                    "WHERE mr.doctor_id = ? ORDER BY mr.visit_date DESC";

            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/HMS", "abrshiz", "abrsh123");
                 PreparedStatement selectPst = con.prepareStatement(selectSql)) {

                selectPst.setString(1, this.doctorId);
                ResultSet rs = selectPst.executeQuery();

                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("record_id"),
                            rs.getString("full_name"),
                            rs.getString("lab_tests"),
                            rs.getDate("visit_date"),
                            rs.getString("notes") == null ? "No notes added" : rs.getString("notes")
                    });
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(resultsFrame, "Database Error: " + e.getMessage());
            }
        };

        loadData.run();

        // ... [The rest of your table styling remains the same] ...
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

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        scrollPane.getViewport().setBackground(Color.WHITE);

        resultsFrame.add(headerPanel, BorderLayout.NORTH);
        resultsFrame.add(scrollPane, BorderLayout.CENTER);

        // --- Footer with Refresh and Dismiss ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        footer.setBackground(Color.WHITE);

        // [Buttons use your original design]
        Color navNormal = new Color(2, 48, 71);
        Color refreshColor = new Color(42, 157, 143);

        JPanel refreshBtnNav = createModernBtn("REFRESH", refreshColor);
        JPanel dismissBtnNav = createModernBtn("DISMISS", navNormal);

        refreshBtnNav.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { loadData.run(); }
        });
        dismissBtnNav.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { resultsFrame.dispose(); }
        });

        footer.add(refreshBtnNav);
        footer.add(dismissBtnNav);
        resultsFrame.add(footer, BorderLayout.SOUTH);

        resultsFrame.setVisible(true);
    }

    // Helper to keep the code clean
    private JPanel createModernBtn(String text, Color bg) {
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        btn.setBackground(bg);
        btn.setPreferredSize(new Dimension(150, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.add(lbl);
        return btn;
    }

    void showLabRequestsDashboard() {
        JFrame labFrame = new JFrame("Laboratory Test Request");
        labFrame.setSize(500, 650);
        labFrame.setLocationRelativeTo(null);

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(240, 244, 247));
        container.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(25, 30, 25, 30)
        ));

        JLabel title = new JLabel("Lab Order Form");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(2, 48, 71));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        Font labelFont = new Font("SansSerif", Font.BOLD, 12);
        Color labelColor = new Color(100, 100, 100);

        // --- UPDATED: Fetching from friend's patient table ---
        JLabel l1 = new JLabel("PATIENT NAME");
        l1.setFont(labelFont); l1.setForeground(labelColor);
        JComboBox<String> pCombo = new JComboBox<>();

        // Fill combo box from the database
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/HMS", "abrshiz", "abrsh123");
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT patient_id, full_name FROM patients")) {
            while (rs.next()) {
                pCombo.addItem(rs.getString("patient_id") + " - " + rs.getString("full_name"));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        pCombo.setMaximumSize(new Dimension(400, 35));

        JLabel l2 = new JLabel("TEST CATEGORY");
        l2.setFont(labelFont); l2.setForeground(labelColor);
        String[] tests = {"Complete Blood Count", "X-Ray Chest", "MRI Scan", "Urinalysis", "Glucose Test"};
        JComboBox<String> testCombo = new JComboBox<>(tests);
        testCombo.setMaximumSize(new Dimension(400, 35));

        JLabel l3 = new JLabel("PRIORITY (Saved to Notes)");
        l3.setFont(labelFont); l3.setForeground(labelColor);
        String[] priorities = {"Normal", "Urgent", "Emergency"};
        JComboBox<String> priorityCombo = new JComboBox<>(priorities);
        priorityCombo.setMaximumSize(new Dimension(400, 35));

        JLabel l4 = new JLabel("INSTRUCTIONS / NOTES");
        l4.setFont(labelFont); l4.setForeground(labelColor);
        JTextArea notesArea = new JTextArea(4, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBackground(new Color(250, 250, 250));
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setMaximumSize(new Dimension(400, 80));

        card.add(title); card.add(Box.createVerticalStrut(20));
        card.add(l1); card.add(Box.createVerticalStrut(5));
        card.add(pCombo); card.add(Box.createVerticalStrut(15));
        card.add(l2); card.add(Box.createVerticalStrut(5));
        card.add(testCombo); card.add(Box.createVerticalStrut(15));
        card.add(l3); card.add(Box.createVerticalStrut(5));
        card.add(priorityCombo); card.add(Box.createVerticalStrut(15));
        card.add(l4); card.add(Box.createVerticalStrut(5));
        card.add(notesScroll); card.add(Box.createVerticalStrut(30));

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

        MouseAdapter navAction = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String selected = (String) pCombo.getSelectedItem();
                if (selected == null) return;
                String pId = selected.split(" - ")[0];

                // --- UPDATED SQL: Inserting into friend's medical_records table ---
                String sql = "INSERT INTO medical_records (patient_id, doctor_id, visit_date, lab_tests, notes) VALUES (?, ?, CURDATE(), ?, ?)";

                try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/HMS", "abrshiz", "abrsh123");
                     PreparedStatement pst = con.prepareStatement(sql)) {

                    pst.setString(1, pId);
                    pst.setString(2, doctorId);
                    pst.setString(3, testCombo.getSelectedItem().toString());
                    // Combining Priority and Instructions since friend has one 'notes' column
                    String combinedNotes = "Priority: " + priorityCombo.getSelectedItem().toString() + "\n" + notesArea.getText();
                    pst.setString(4, combinedNotes);

                    pst.executeUpdate();
                    JOptionPane.showMessageDialog(labFrame, "Lab Order Successfully Recorded in Medical Records.");
                    labFrame.dispose();

                    // Trigger dashboard refresh
                    showDashboard();

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(labFrame, "DB Error: " + ex.getMessage());
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) { sendBtnNav.setBackground(navHover); }
            @Override
            public void mouseExited(MouseEvent e) { sendBtnNav.setBackground(navNormal); }
        };

        sendBtnNav.addMouseListener(navAction);
        btnLabel.addMouseListener(navAction);

        card.add(sendBtnNav);
        container.add(card, BorderLayout.CENTER);
        labFrame.add(container);
        labFrame.setVisible(true);
    }

    void showPatientsDashboard() {
        JFrame patientsFrame = new JFrame("Patient Directory - Dr. " + (fullName != null ? fullName : ""));
        patientsFrame.setSize(1000, 650);
        patientsFrame.setLocationRelativeTo(null);
        patientsFrame.getContentPane().setBackground(Color.WHITE);
        patientsFrame.setLayout(new BorderLayout());

        // --- Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(2, 48, 71)); // Navy Blue
        headerPanel.setPreferredSize(new Dimension(1000, 80));
        headerPanel.setBorder(new javax.swing.border.EmptyBorder(15, 25, 15, 25));

        JLabel titleLabel = new JLabel("My Assigned Patients");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // --- Table ---
        String[] columns = {"Patient ID", "Full Name", "Age", "Gender", "Contact", "Registration Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        // --- Data Loading ---
        String sql = "SELECT DISTINCT p.patient_id, p.full_name, " +
                "TIMESTAMPDIFF(YEAR, p.date_of_birth, CURDATE()) AS age, " +
                "p.gender, p.contact_number, p.registration_date " +
                "FROM patients p " +
                "INNER JOIN appointments a ON p.patient_id = a.patient_id " +
                "WHERE a.doctor_id = ?";

        try (Connection con = Database.DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, this.doctorId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("patient_id"),
                        rs.getString("full_name"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("contact_number"),
                        rs.getDate("registration_date")
                });
            }
        } catch (SQLException e) {
            System.err.println("❌ Patient Load Error: " + e.getMessage());
        }

        JTable table = new JTable(model);
        table.setRowHeight(45);
        table.setBackground(Color.WHITE);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230, 230, 230));

        table.getTableHeader().setBackground(new Color(245, 248, 250));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE); // Fixes the "hole"
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // --- Footer ---
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        footerPanel.setBackground(Color.WHITE);
        JButton closeBtn = new JButton("CLOSE");
        closeBtn.addActionListener(e -> patientsFrame.dispose());
        footerPanel.add(closeBtn);

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

        // --- 1. Header Panel (Navy Blue) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(2, 48, 71));
        headerPanel.setPreferredSize(new Dimension(1000, 80));
        headerPanel.setBorder(new javax.swing.border.EmptyBorder(15, 25, 15, 25));

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

        // --- DATABASE LOGIC: Sync with DatabaseConnection manager ---
        // This query joins appointments with patients to get the full_name
        String sql = "SELECT a.appointment_time, p.full_name, a.reason, a.status " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "WHERE a.doctor_id = ? AND a.appointment_date = CURDATE() " +
                "ORDER BY a.appointment_time ASC";

        // IMPORTANT: Using your DatabaseConnection.getConnection()
        try (Connection con = Database.DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            // Ensure doctorId was loaded in loadDoctorData()
            pst.setString(1, this.doctorId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getTime("appointment_time"),
                        rs.getString("full_name"),
                        rs.getString("reason"),
                        rs.getString("status")
                });
            }
        } catch (SQLException e) {
            System.err.println("❌ Error loading appointments: " + e.getMessage());
        }

        JTable table = new JTable(model);
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setGridColor(new Color(235, 235, 235));
        table.setSelectionBackground(new Color(230, 240, 250));
        table.setSelectionForeground(new Color(2, 48, 71));
        table.setShowVerticalLines(false);
        table.setBackground(Color.WHITE);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(245, 248, 250));
        table.getTableHeader().setForeground(new Color(2, 48, 71));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        // --- FIX: Filling the "White Hole" below the table ---
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        scrollPane.getViewport().setBackground(Color.WHITE); // Keeps the empty space white
        scrollPane.setBackground(Color.WHITE);

        // --- 3. Footer Action Panel ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 15));
        footer.setBackground(Color.WHITE);

        Color normalColor = new Color(2, 48, 71);
        Color hoverColor = new Color(6, 75, 110);

        JPanel closeBtnNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        closeBtnNav.setBackground(normalColor);
        closeBtnNav.setPreferredSize(new Dimension(180, 40));
        closeBtnNav.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel closeLabel = new JLabel("CLOSE SCHEDULE");
        closeLabel.setForeground(Color.WHITE);
        closeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        closeBtnNav.add(closeLabel);

        MouseAdapter navStyleAction = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { appFrame.dispose(); }
            @Override public void mouseEntered(MouseEvent e) { closeBtnNav.setBackground(hoverColor); }
            @Override public void mouseExited(MouseEvent e) { closeBtnNav.setBackground(normalColor); }
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

        // --- UPDATED LOGIC: Using DatabaseConnection and HMS Schema ---
        // Using a JOIN to ensure we only get patients assigned to THIS doctor
        String query = "SELECT a.appointment_time, p.full_name, a.status, a.reason " +
                "FROM appointments a JOIN patients p ON a.patient_id = p.patient_id " +
                "WHERE a.doctor_id = ? ORDER BY a.appointment_time ASC";

        try (Connection con = Database.DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setString(1, this.doctorId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                // Note: I used 'reason' for Clinical Notes as per your previous schema
                String notes = rs.getString(4);
                model.addRow(new Object[]{
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        (notes == null || notes.isEmpty()) ? "No notes added" : notes
                });
            }
        } catch (SQLException e) {
            System.err.println("❌ Schedule Load Error: " + e.getMessage());
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

        // Column Width Adjustments
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(350);

        // --- FIX: Filling the Viewport to avoid the "white hole" ---
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBackground(Color.WHITE);

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

    // RESTORED PRESCRIPTION LOGIC MATCHING YOUR TABLE
    void showPrescriptionsDashboard() {
        JFrame prescFrame = new JFrame("Clinical Prescription - HMS");
        prescFrame.setSize(650, 750);
        prescFrame.setLocationRelativeTo(null);
        prescFrame.getContentPane().setBackground(Color.WHITE);
        prescFrame.setLayout(new BorderLayout());

        // Theme Color
        Color navyBlue = new Color(2, 48, 71);

        // --- 1. Header Section ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(navyBlue);
        headerPanel.setPreferredSize(new Dimension(650, 90));
        headerPanel.setBorder(new javax.swing.border.EmptyBorder(15, 25, 15, 25));

        JLabel titleLabel = new JLabel("Issue New Prescription");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        JLabel docLabel = new JLabel("Provider: Dr. " + (fullName != null ? fullName : ""));
        docLabel.setForeground(new Color(200, 215, 225));

        JPanel titleGrp = new JPanel(new GridLayout(2, 1));
        titleGrp.setOpaque(false);
        titleGrp.add(titleLabel);
        titleGrp.add(docLabel);
        headerPanel.add(titleGrp, BorderLayout.WEST);

        // --- 2. Form Area ---
        JPanel formPanel = new JPanel(new GridLayout(0, 1, 5, 5)); // Reduced vertical gap
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new javax.swing.border.EmptyBorder(20, 40, 20, 40));

        // Helper method to create visible labels
        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);

        // Patient Selection
        JLabel lbl1 = new JLabel("SELECT PATIENT");
        lbl1.setForeground(navyBlue); // FIXED: Explicitly set color
        lbl1.setFont(labelFont);
        formPanel.add(lbl1);

        JComboBox<String> pDropdown = new JComboBox<>();
        pDropdown.setPreferredSize(new Dimension(0, 35));
        // (Loading logic remains the same...)
        try (Connection con = Database.DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT DISTINCT p.patient_id, p.full_name FROM patients p JOIN appointments a ON p.patient_id = a.patient_id WHERE a.doctor_id = ?")) {
            pst.setString(1, this.doctorId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) pDropdown.addItem(rs.getString(1) + " - " + rs.getString(2));
        } catch (SQLException e) { e.printStackTrace(); }
        formPanel.add(pDropdown);

        // Medication Name
        JLabel lbl2 = new JLabel("MEDICATION NAME");
        lbl2.setForeground(navyBlue); // FIXED
        lbl2.setFont(labelFont);
        formPanel.add(lbl2);
        JTextField medField = new JTextField();
        formPanel.add(medField);

        // Dosage
        JLabel lbl3 = new JLabel("DOSAGE (e.g., 500mg)");
        lbl3.setForeground(navyBlue); // FIXED
        lbl3.setFont(labelFont);
        formPanel.add(lbl3);
        JTextField doseField = new JTextField();
        formPanel.add(doseField);

        // Frequency
        JLabel lbl4 = new JLabel("FREQUENCY (e.g., Twice Daily)");
        lbl4.setForeground(navyBlue); // FIXED
        lbl4.setFont(labelFont);
        formPanel.add(lbl4);
        JTextField freqField = new JTextField();
        formPanel.add(freqField);

        // Instructions
        JLabel lbl5 = new JLabel("ADDITIONAL INSTRUCTIONS");
        lbl5.setForeground(navyBlue); // FIXED
        lbl5.setFont(labelFont);
        formPanel.add(lbl5);
        JTextArea instrArea = new JTextArea(4, 20);
        instrArea.setLineWrap(true);
        instrArea.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        formPanel.add(new JScrollPane(instrArea));

        // --- 3. Footer ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        footer.setBackground(Color.WHITE);

        JButton saveBtn = new JButton("SAVE PRESCRIPTION");
        saveBtn.setBackground(navyBlue);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setPreferredSize(new Dimension(220, 45));
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));

        saveBtn.addActionListener(e -> {
            // ... (Save logic remains exactly as before)
            String selected = (String) pDropdown.getSelectedItem();
            if (selected == null || medField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(prescFrame, "Patient and Medication Name are required.");
                return;
            }
            String pId = selected.split(" - ")[0];
            String sql = "INSERT INTO prescriptions (patient_id, doctor_id, medication_name, dosage, frequency, instructions, prescribed_at) VALUES (?, ?, ?, ?, ?, ?, NOW())";

            try (Connection con = Database.DatabaseConnection.getConnection()) {
                con.setAutoCommit(true);
                try (PreparedStatement pst = con.prepareStatement(sql)) {
                    pst.setString(1, pId);
                    pst.setString(2, doctorId);
                    pst.setString(3, medField.getText());
                    pst.setString(4, doseField.getText());
                    pst.setString(5, freqField.getText());
                    pst.setString(6, instrArea.getText());
                    if (pst.executeUpdate() > 0) {
                        JOptionPane.showMessageDialog(prescFrame, "Prescription successfully recorded!");
                        prescFrame.dispose();
                    }
                }
            } catch (SQLException ex) { JOptionPane.showMessageDialog(prescFrame, "Error: " + ex.getMessage()); }
        });

        footer.add(saveBtn);

        prescFrame.add(headerPanel, BorderLayout.NORTH);
        prescFrame.add(formPanel, BorderLayout.CENTER);
        prescFrame.add(footer, BorderLayout.SOUTH);
        prescFrame.setVisible(true);
    }

    private List<Patient> getMyPatients() {
        List<Patient> list = new ArrayList<>();

        // We select from patients but filter through the appointments table
        // to ensure ONLY patients assigned to THIS doctor are returned.
        String query = "SELECT DISTINCT p.patient_id, p.full_name, " +
                "TIMESTAMPDIFF(YEAR, p.date_of_birth, CURDATE()) AS calculated_age, " +
                "p.gender, a.appointment_date as last_visit, a.status " +
                "FROM patients p " +
                "INNER JOIN appointments a ON p.patient_id = a.patient_id " +
                "WHERE a.doctor_id = ? " +
                "AND a.appointment_date = (SELECT MAX(appointment_date) " +
                "FROM appointments " +
                "WHERE patient_id = p.patient_id " +
                "AND doctor_id = ?)";

        try (Connection con = Database.DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            // We set the doctorId twice: once for the main join and once for the subquery
            pst.setString(1, this.doctorId);
            pst.setString(2, this.doctorId);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Patient p = new Patient();
                p.setPatientId(rs.getString("patient_id"));
                p.setName(rs.getString("full_name"));
                p.setAge(rs.getInt("calculated_age"));
                p.setGender(rs.getString("gender"));
                p.setLastVisit(rs.getString("last_visit"));
                p.setStatus(rs.getString("status"));
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error in getMyPatients: " + e.getMessage());
        }
        return list;
    }

    private List<Appointment> getTodaysAppointments() {
        List<Appointment> list = new ArrayList<>();

        // The query is perfect: it joins to get the name and filters for today's date
        String query = "SELECT a.appointment_time, p.full_name, a.reason, a.status " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "WHERE a.doctor_id = ? AND a.appointment_date = CURDATE() " +
                "ORDER BY a.appointment_time ASC";

        // Use your friend's database details
        String dbUrl = "jdbc:mysql://localhost:3306/HMS";
        String dbUser = "abrshiz";
        String dbPass = "abrsh123";

        try (Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPass);
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setString(1, doctorId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Appointment app = new Appointment();
                // Time is stored as Time in MySQL, getString works fine for display
                app.setTime(rs.getString("appointment_time"));
                app.setPatientName(rs.getString("full_name"));
                app.setReason(rs.getString("reason"));
                app.setStatus(rs.getString("status"));
                list.add(app);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching today's appointments: " + e.getMessage());
        }
        return list;
    }

    @Override void logout() { new LoginPage().setVisible(true); }
//    public static void main (String args[]){
//        new Doctor("abrshiz").showDashboard();
//    }
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
