import Database.LabratoryDAO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Labratory extends staffUser {
    private int authId;
    private JFrame frame;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JLabel lblPendingCount, lblCompletedCount, lblTotalWork;
    private JLabel animatedGreeting, animatedTimer;
    private String fullName;
    private Font navFont = new Font("SansSerif", Font.BOLD, 13);

    private final Color MATCHED_HOVER = new Color(12, 58, 81);
    private final Color NAVY = new Color(2, 48, 71);
    private final Color HOVER_BLUE = new Color(33, 158, 188);
    private final Color SIDEBAR_BG = new Color(2, 48, 71);
    private final Color SUCCESS_GREEN = new Color(42, 157, 143);
    private final Color GLASS_WHITE = new Color(255, 255, 255, 235);
    private final Color ACCENT_GOLD = new Color(255, 183, 3);

    // FIXED CONSTRUCTOR
    public Labratory(int authId) {
        this.authId = authId;
        updateWorkCounts();
    }

    @Override
    public void showDashboard() {
        // FETCH DATA FIRST SO VARIABLES ARE READY FOR YOUR DESIGN
        Map<String, String> data = LabratoryDAO.getTechnicianProfile(this.authId);
        if (data != null) {
            this.fullName = data.getOrDefault("full_name", "Staff Member");
            this.usename = data.getOrDefault("username", "User");
        }

        frame = new JFrame("HMS - Clinical Laboratory Portal");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        cardPanel.add(createDashboardBody(), "DASHBOARD");
        cardPanel.add(createProfileBody(), "PROFILE");
        cardPanel.add(createActiveRequestsBody(), "ACTIVE_REQUESTS");
        cardPanel.add(createLabResponseBody(), "LAB_RESPONSE");
        cardPanel.add(createHistoryBody(), "HISTORY");

        JPanel mainContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                String path = "assets/Lab-home.png";
                if (new File(path).exists()) {
                    g2d.drawImage(new ImageIcon(path).getImage(), 0, 0, getWidth(), getHeight(), this);
                } else {
                    g2d.setPaint(new GradientPaint(0, 0, new Color(245, 247, 250), 0, getHeight(), new Color(200, 210, 225)));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        mainContainer.add(createSidebar(), BorderLayout.WEST);
        mainContainer.add(cardPanel, BorderLayout.CENTER);
        frame.add(mainContainer);

        startAnimations();
        frame.setVisible(true);
    }

    private JPanel createSidebar() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(SIDEBAR_BG);
        p.setPreferredSize(new Dimension(260, 0));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        titlePanel.setOpaque(false);
        JLabel portalHeader = new JLabel("LAB SYSTEM");
        File portalIcon = new File("assets/portal.png");
        if (portalIcon.exists()) {
            portalHeader.setIcon(new ImageIcon(new ImageIcon("assets/portal.png").getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH)));
            portalHeader.setIconTextGap(12);
        }
        portalHeader.setForeground(Color.WHITE);
        portalHeader.setFont(new Font("SansSerif", Font.BOLD, 18));
        titlePanel.add(portalHeader);
        p.add(titlePanel);
        p.add(Box.createVerticalStrut(10));

        p.add(createNavItem("Dashboard", "assets/dashboard.png", e -> cardLayout.show(cardPanel, "DASHBOARD")));
        p.add(createNavItem("Active Requests", "assets/observation.png", e -> cardLayout.show(cardPanel, "ACTIVE_REQUESTS")));
        p.add(createNavItem("Lab Response", "assets/update.png", e -> cardLayout.show(cardPanel, "LAB_RESPONSE")));
        p.add(createNavItem("My Profile", "assets/user.png", e -> cardLayout.show(cardPanel, "PROFILE")));
        p.add(createNavItem("Request History", "assets/history.jpg", e -> cardLayout.show(cardPanel, "HISTORY")));

        p.add(Box.createVerticalGlue());
        JPanel logoutWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoutWrapper.setOpaque(false);
        JPanel logoutBtn = createNavItem("Log Out", "assets/logout.png", e -> logout());
        logoutBtn.setPreferredSize(new Dimension(200, 40));
        logoutBtn.setOpaque(true);
        logoutBtn.setBackground(new Color(230, 57, 70, 150));
        logoutWrapper.add(logoutBtn);
        p.add(logoutWrapper);
        p.add(Box.createVerticalStrut(20));
        return p;
    }

    // 1. ADD THIS FIELD TO YOUR CLASS
    // Ensure this is declared at the top of your class, not inside the method
    private Timer autoRefreshTimer;

    private JPanel createActiveRequestsBody() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(30, 40, 40, 40));

        // 1. Updated Column Names to include Doctor's Notes
        String[] colNames = {"ACCESS-ID", "PATIENT NAME", "REQUESTED ANALYSIS", "DOCTOR'S NOTES", "PRIORITY LEVEL", "STATUS"};

        DefaultTableModel model = new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        // 2. LIVE REFRESH LOGIC
        refreshQueueData(model);

        if (autoRefreshTimer != null && autoRefreshTimer.isRunning()) {
            autoRefreshTimer.stop();
        }

        autoRefreshTimer = new Timer(5000, e -> {
            if (container.isShowing()) {
                refreshQueueData(model);
                updateWorkCounts();
            }
        });
        autoRefreshTimer.start();

        // 3. TABLE SETUP (Design untouched)
        JTable table = new JTable(model);
        table.setRowHeight(52);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setReorderingAllowed(false);

        applyProfessionalQueueUI(table);

        // Set specific width for Notes if needed (optional, design-safe)
        table.getColumnModel().getColumn(3).setPreferredWidth(200);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));
        scroll.getViewport().setBackground(Color.WHITE);

        // 4. HEADER SECTION (Design untouched)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel title = new JLabel("Live Diagnostic Queue");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(NAVY);

        JLabel subtitle = new JLabel("● LIVE • Monitoring incoming laboratory requests");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(42, 157, 143));

        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(subtitle, BorderLayout.SOUTH);

        container.add(topPanel, BorderLayout.NORTH);
        container.add(scroll, BorderLayout.CENTER);

        return container;
    }

    /**
     * Syncs the table with the database including the new Notes column
     */
    private void refreshQueueData(DefaultTableModel model) {
        // 1. Fetch data using the corrected method above
        List<Object[]> requests = LabratoryDAO.getPendingRequests();

        // 2. Clear table to prevent duplicates
        model.setRowCount(0);

        if (requests != null && !requests.isEmpty()) {
            for (Object[] r : requests) {
                // Add the row directly - it already contains the 6 columns
                model.addRow(r);
            }
        } else {
            // Optional: Debugging line to see if data is actually coming through
            System.out.println("No 'Pending' requests found in database.");
        }
    }
    private void applyProfessionalQueueUI(JTable table) {
        // Modern Navy Header
        table.getTableHeader().setBackground(new Color(2, 48, 71));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));
        table.getTableHeader().setBorder(null);

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                label.setBorder(new EmptyBorder(0, 15, 0, 15)); // Padding
                label.setOpaque(true);

                // Zebra Striping
                if (!s) {
                    label.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                } else {
                    label.setBackground(new Color(33, 158, 188, 40)); // Highlight blue
                }

                String val = (v != null) ? v.toString() : "";

                // 1. Column: PATIENT NAME (Bold for realism)
                if (c == 1) {
                    label.setFont(new Font("SansSerif", Font.BOLD, 13));
                    label.setForeground(new Color(2, 48, 71));
                }
                // 2. Column: DOCTOR'S NOTES (Italicized, gray)
                else if (c == 3) {
                    label.setFont(new Font("SansSerif", Font.ITALIC, 12));
                    label.setForeground(Color.GRAY);
                }
                // 3. Column: PRIORITY (Realistic color alerts)
                else if (c == 4) {
                    label.setFont(new Font("SansSerif", Font.BOLD, 12));
                    String p = val.toUpperCase();
                    if (p.contains("EMERGENCY")) {
                        label.setForeground(new Color(230, 57, 70)); // Deep Red
                        label.setText("🚨 " + p);
                    } else if (p.contains("URGENT")) {
                        label.setForeground(new Color(255, 183, 3)); // Orange/Gold
                        label.setText("⚠ " + p);
                    } else {
                        label.setForeground(new Color(42, 157, 143)); // Calm Teal
                        label.setText("✓ " + p);
                    }
                }
                // 4. Column: STATUS (Badge style)
                else if (c == 5) {
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    label.setFont(new Font("SansSerif", Font.BOLD, 11));
                    label.setForeground(new Color(33, 158, 188)); // Theme Blue
                    label.setText("◌ " + val.toUpperCase());
                } else {
                    label.setForeground(new Color(60, 60, 60));
                    label.setFont(new Font("SansSerif", Font.PLAIN, 13));
                }

                return label;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }



    private JPanel createLabResponseBody() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);

        // --- LEFT NAVIGATION: THE WORK QUEUE ---
        JPanel leftNav = new JPanel(new BorderLayout());
        leftNav.setPreferredSize(new Dimension(400, 0));
        leftNav.setBackground(Color.WHITE);
        leftNav.setBorder(new MatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));

        JPanel navHeader = new JPanel(new BorderLayout());
        navHeader.setBackground(new Color(248, 249, 252));
        navHeader.setBorder(new EmptyBorder(20, 25, 20, 25));
        JLabel navTitle = new JLabel("INCOMING SAMPLES");
        navTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        navTitle.setForeground(new Color(120, 130, 140));
        navHeader.add(navTitle, BorderLayout.WEST);
        leftNav.add(navHeader, BorderLayout.NORTH);

        String[] cols = {"ID", "PATIENT", "ANALYSIS TYPE"};
        DefaultTableModel navModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable navTable = new JTable(navModel);
        navTable.setRowHeight(60);
        navTable.setShowGrid(false);
        navTable.setSelectionBackground(new Color(235, 245, 255));
        navTable.setSelectionForeground(new Color(2, 48, 71));
        navTable.setFocusable(false);

        upgradeTableUI(navTable);

        // Initial Data Fetch
        List<Object[]> pendingList = LabratoryDAO.getPendingRequests();
        if (pendingList != null) for(Object[] r : pendingList) navModel.addRow(new Object[]{r[0], r[1], r[2]});

        JScrollPane navScroll = new JScrollPane(navTable);
        navScroll.setBorder(null);
        navScroll.getViewport().setBackground(Color.WHITE);
        leftNav.add(navScroll, BorderLayout.CENTER);

        // --- RIGHT PANEL: THE DIAGNOSTIC WORKBENCH ---
        JPanel entryPanel = new JPanel(new GridBagLayout());
        entryPanel.setBackground(new Color(242, 244, 248));

        JPanel entryCard = new JPanel(new BorderLayout());
        entryCard.setBackground(Color.WHITE);
        entryCard.setPreferredSize(new Dimension(650, 650));
        entryCard.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));

        JPanel cardHead = new JPanel(new GridBagLayout());
        cardHead.setBackground(new Color(2, 48, 71));
        cardHead.setBorder(new EmptyBorder(25, 35, 25, 35));
        GridBagConstraints hg = new GridBagConstraints();
        hg.fill = GridBagConstraints.HORIZONTAL; hg.weightx = 1.0;

        JLabel cardTitle = new JLabel("SELECT A SAMPLE TO START");
        cardTitle.setForeground(Color.WHITE);
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel cardSubtitle = new JLabel("Current Procedure: Waiting for selection...");
        cardSubtitle.setForeground(new Color(180, 190, 200));
        cardSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));

        hg.gridy=0; cardHead.add(cardTitle, hg);
        hg.gridy=1; cardHead.add(cardSubtitle, hg);
        entryCard.add(cardHead, BorderLayout.NORTH);

        JPanel fieldBody = new JPanel(new GridBagLayout());
        fieldBody.setBackground(Color.WHITE);
        fieldBody.setBorder(new EmptyBorder(40, 60, 40, 60));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1.0; g.insets = new Insets(10,0,10,0);

        JTextField valField = new JTextField();
        valField.setPreferredSize(new Dimension(0, 45));
        JTextArea noteArea = new JTextArea(5, 20);
        noteArea.setLineWrap(true);
        JScrollPane noteScroll = new JScrollPane(noteArea);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Normal", "Abnormal", "Critical / Urgent"});
        statusCombo.setPreferredSize(new Dimension(0, 45));

        g.gridy=0; fieldBody.add(createStyledLabel("QUANTITATIVE VALUE (e.g., 14.2 g/dL)"), g);
        g.gridy=1; fieldBody.add(valField, g);
        g.gridy=2; fieldBody.add(createStyledLabel("PATHOLOGIST'S OBSERVATIONS / REMARKS"), g);
        g.gridy=3; fieldBody.add(noteScroll, g);
        g.gridy=4; fieldBody.add(createStyledLabel("CLINICAL CONCLUSION"), g);
        g.gridy=5; fieldBody.add(statusCombo, g);
        entryCard.add(fieldBody, BorderLayout.CENTER);

        JButton submitBtn = new JButton("FINALIZE & VALIDATE RESULT");
        submitBtn.setFocusPainted(false);
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.setBackground(new Color(42, 157, 143));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        submitBtn.setPreferredSize(new Dimension(0, 70));
        submitBtn.setBorder(null);

        // --- BUTTON LOGIC ---
        submitBtn.addActionListener(e -> {
            int selectedRow = navTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "Please select a sample from the list first.");
                return;
            }

            String requestId = navModel.getValueAt(selectedRow, 0).toString();
            String resultDetails = String.format("Value: %s | Remarks: %s | Conclusion: %s",
                    valField.getText(), noteArea.getText(), statusCombo.getSelectedItem());

            // Update Database via DAO
            if (LabratoryDAO.submitLabResult(requestId, resultDetails)) {
                JOptionPane.showMessageDialog(null, "Diagnostic verified and sent to doctor.");
                updateWorkCounts();
                // UI Refresh
                navModel.removeRow(selectedRow); // Remove from queue
                valField.setText("");
                noteArea.setText("");
                cardTitle.setText("SELECT A SAMPLE TO START");
                cardSubtitle.setText("Current Procedure: Waiting...");

                // Update Stat Cards (SYSTEM LOGS)
                updateWorkCounts();
            }
        });

        submitBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { submitBtn.setBackground(new Color(36, 135, 123)); }
            public void mouseExited(java.awt.event.MouseEvent evt) { submitBtn.setBackground(new Color(42, 157, 143)); }
        });

        entryCard.add(submitBtn, BorderLayout.SOUTH);
        entryPanel.add(entryCard);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftNav, entryPanel);
        split.setDividerLocation(400);
        split.setDividerSize(1);
        split.setBorder(null);
        mainPanel.add(split, BorderLayout.CENTER);

        navTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && navTable.getSelectedRow() != -1) {
                cardTitle.setText(navTable.getValueAt(navTable.getSelectedRow(), 1).toString().toUpperCase());
                cardSubtitle.setText("Current Procedure: " + navTable.getValueAt(navTable.getSelectedRow(), 2).toString());
            }
        });

        return mainPanel;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(new Color(100, 110, 120));
        return label;
    }
    // Add this field to your class if not already present
    private Timer historyRefreshTimer;

    private JPanel createHistoryBody() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(30, 40, 40, 40));

        // Realistic Medical Headers
        String[] cols = {"REFERENCE ID", "PATIENT IDENTITY", "LABORATORY TEST", "FINAL STATUS", "VERIFIED DATE"};

        // 1. STRICTLY UNEDITABLE MODEL
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // 2. LIVE REFRESH LOGIC
        // Initial data load
        refreshHistoryData(model);

        // Stop any existing timer to prevent duplicates
        if (historyRefreshTimer != null && historyRefreshTimer.isRunning()) {
            historyRefreshTimer.stop();
        }

        // Set to 5 or 10 seconds for a "Live" feel
        historyRefreshTimer = new Timer(5000, e -> {
            if (container.isShowing()) {
                refreshHistoryData(model);
            }
        });
        historyRefreshTimer.start();

        // 3. TABLE SETUP (Design untouched)
        JTable table = new JTable(model);
        table.setRowHeight(50);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        applyRealisticTableUI(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230), 1, true));
        scroll.getViewport().setBackground(Color.WHITE);

        // 4. HEADER SECTION (Design untouched)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Completed Lab Archives");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(NAVY);

        // Visual "Live" indicator in the subtitle
        JLabel subtitle = new JLabel("● LIVE • Read-only records of verified diagnostic results");
        subtitle.setFont(new Font("SansSerif", Font.ITALIC, 13));
        subtitle.setForeground(new Color(42, 157, 143)); // Professional green

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);

        container.add(headerPanel, BorderLayout.NORTH);
        container.add(scroll, BorderLayout.CENTER);

        return container;
    }


    private void refreshHistoryData(DefaultTableModel model) {
        List<Object[]> history = LabratoryDAO.getFullLabArchive();

        // Wipe and reload to ensure the UI stays in sync with DB
        model.setRowCount(0);

        if (history != null) {
            for (Object[] r : history) {
                // FILTER: Only add rows where status matches 'Completed'
                if (r.length >= 4 && r[3] != null && r[3].toString().equalsIgnoreCase("Completed")) {
                    model.addRow(r);
                }
            }
        }
    }

    // Specialized realistic UI method
    private void applyRealisticTableUI(JTable table) {
        // Header Styling
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        // Cell Styling
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);

                // Professional padding
                label.setBorder(new EmptyBorder(0, 20, 0, 20));

                // Zebra Striping
                if (!s) {
                    label.setBackground(r % 2 == 0 ? Color.WHITE : new Color(242, 245, 248));
                } else {
                    label.setBackground(new Color(33, 158, 188, 30));
                    label.setForeground(NAVY);
                }

                // Realistic Status Styling
                if (c == 3) { // STATUS COLUMN
                    label.setText(" ● " + v.toString().toUpperCase());
                    label.setForeground(SUCCESS_GREEN);
                    label.setFont(new Font("SansSerif", Font.BOLD, 11));
                } else {
                    label.setForeground(new Color(70, 70, 70));
                    label.setFont(new Font("SansSerif", Font.PLAIN, 13));
                }

                return label;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private JPanel createDashboardBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.anchor = GridBagConstraints.CENTER;
        animatedGreeting = new JLabel("Welcome, " + fullName );
        animatedGreeting.setFont(new Font("Serif", Font.BOLD, 52));
        animatedGreeting.setForeground(NAVY);
        animatedTimer = new JLabel("");
        animatedTimer.setFont(new Font("Monospaced", Font.BOLD, 20));
        JPanel statsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        statsRow.setOpaque(false);
        lblPendingCount = createStatCard(statsRow, "PENDING TESTS", new Color(230, 57, 70));
        lblCompletedCount = createStatCard(statsRow, "VERIFIED RESULTS", SUCCESS_GREEN);
        lblTotalWork = createStatCard(statsRow, "SYSTEM LOGS", NAVY);
        gbc.gridy = 0; body.add(animatedGreeting, gbc);
        gbc.gridy = 1; body.add(animatedTimer, gbc);
        gbc.gridy = 2; gbc.insets = new Insets(50, 0, 0, 0); body.add(statsRow, gbc);
        updateWorkCounts();
        return body;
    }

    private JLabel createStatCard(JPanel parent, String title, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(250, 130));
        card.setBackground(new Color(255, 255, 255, 215));
        JLabel val = new JLabel("0", SwingConstants.CENTER);
        val.setFont(new Font("Arial", Font.BOLD, 48));
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblTitle.setForeground(Color.GRAY);
        card.add(val, BorderLayout.CENTER);
        card.add(lblTitle, BorderLayout.SOUTH);
        parent.add(card);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(0, 0, 0, 20), 1, true),
                new CompoundBorder(
                        new MatteBorder(0, 6, 0, 0, accent),
                        new EmptyBorder(10, 15, 10, 15)
                )
        ));
        return val;
    }

    private JPanel createProfileBody() {
        Map<String, String> data = LabratoryDAO.getTechnicianProfile(this.authId);
        if (data == null) data = new HashMap<>();

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        // Main Card
        JPanel masterCard = new JPanel(new BorderLayout());
        masterCard.setPreferredSize(new Dimension(950, 600)); // Increased width to prevent text clipping
        masterCard.setBackground(new Color(255, 255, 255, 240));
        masterCard.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));

        // --- LEFT PANEL (NAVY) ---
        JPanel identityPanel = new JPanel();
        identityPanel.setLayout(new BoxLayout(identityPanel, BoxLayout.Y_AXIS));
        identityPanel.setBackground(new Color(2, 48, 71)); // Your exact Navy
        identityPanel.setPreferredSize(new Dimension(320, 600));
        identityPanel.add(Box.createVerticalStrut(60));

        JLabel avatar = new JLabel();
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        try {
            ImageIcon icon = new ImageIcon("assets/user.png");
            Image img = icon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);
            avatar.setIcon(new ImageIcon(img));
            avatar.setBorder(new LineBorder(Color.WHITE, 2, true));
        } catch(Exception e) { avatar.setText("USER"); }

        identityPanel.add(avatar);
        identityPanel.add(Box.createVerticalStrut(25));

        JLabel nameLbl = new JLabel(this.fullName != null ? this.fullName : "Yeabsira Abebe");
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 26));
        nameLbl.setForeground(Color.WHITE);
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        identityPanel.add(nameLbl);

        JLabel posLbl = new JLabel(data.getOrDefault("job_title", "Senior Laboratory Technician"));
        posLbl.setFont(new Font("SansSerif", Font.PLAIN, 15));
        posLbl.setForeground(new Color(33, 158, 188)); // HOVER_BLUE
        posLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        identityPanel.add(posLbl);

        identityPanel.add(Box.createVerticalStrut(40));
        identityPanel.add(createBadge("CERTIFIED", new Color(42, 157, 143)));
        identityPanel.add(Box.createVerticalStrut(10));
        identityPanel.add(createBadge("ACTIVE DUTY", new Color(255, 183, 3)));

        identityPanel.add(Box.createVerticalGlue());

        // --- RIGHT PANEL (CONTENT) ---
        JPanel dataPanel = new JPanel();
        dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
        dataPanel.setOpaque(false);
        dataPanel.setBorder(new EmptyBorder(50, 60, 50, 60));

        // Header section with proper alignment to prevent subtitle cut-off
        JPanel headerArea = new JPanel(new GridLayout(2, 1, 0, 5));
        headerArea.setOpaque(false);
        headerArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        headerArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Personal Information");
        title.setFont(new Font("SansSerif", Font.BOLD, 36));
        title.setForeground(new Color(2, 48, 71));

        JLabel subtitle = new JLabel("Official personnel record and verification details");
        subtitle.setFont(new Font("SansSerif", Font.ITALIC, 14));
        subtitle.setForeground(Color.GRAY);

        headerArea.add(title);
        headerArea.add(subtitle);
        dataPanel.add(headerArea);

        dataPanel.add(Box.createVerticalStrut(50));

        // Info Grid
        JPanel grid = new JPanel(new GridLayout(3, 2, 40, 45));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        grid.add(createDetailBox("STAFF IDENTIFICATION", data.getOrDefault("id", "1")));
        grid.add(createDetailBox("MEDICAL LICENSE", data.getOrDefault("license", "Masters")));
        grid.add(createDetailBox("SYSTEM USERNAME", this.usename != null ? this.usename : "yabu"));
        grid.add(createDetailBox("CONTACT EXTENSION", data.getOrDefault("phone", "+251969631190")));
        grid.add(createDetailBox("DEPARTMENT", data.getOrDefault("department","Hematology")));
        grid.add(createDetailBox("INSTITUTIONAL EMAIL", data.getOrDefault("email", "yabu@hospital.com")));

        dataPanel.add(grid);
        dataPanel.add(Box.createVerticalGlue());

        masterCard.add(identityPanel, BorderLayout.WEST);
        masterCard.add(dataPanel, BorderLayout.CENTER);
        centerWrapper.add(masterCard);

        return centerWrapper;
    }

    private JPanel createDetailBox(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(Color.GRAY);
        JLabel v = new JLabel(value);
        v.setFont(new Font("SansSerif", Font.PLAIN, 15));
        v.setForeground(Color.BLACK);
        p.add(l, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        p.setBorder(new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        return p;
    }

    private JLabel createBadge(String text, Color color) {
        JLabel b = new JLabel(text, SwingConstants.CENTER);
        b.setFont(new Font("SansSerif", Font.BOLD, 10));
        b.setOpaque(true);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setMaximumSize(new Dimension(100, 25));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setBorder(new EmptyBorder(5, 10, 5, 10));
        return b;
    }

    private JPanel createNavItem(String text, String iconPath, ActionListener action) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 12));
        item.setOpaque(false);
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(200, 200, 200));
        lbl.setFont(navFont);
        File f = new File(iconPath);
        if (f.exists()) {
            lbl.setIcon(new ImageIcon(new ImageIcon(iconPath).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH)));
            lbl.setIconTextGap(15);
        }
        item.add(lbl);
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setOpaque(true);
                item.setBackground(MATCHED_HOVER);
                lbl.setForeground(Color.WHITE);
                item.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (text.equalsIgnoreCase("Log Out")) {
                    item.setOpaque(true);
                    item.setBackground(new Color(230, 57, 70, 150));
                } else {
                    item.setOpaque(false);
                    lbl.setForeground(new Color(200, 200, 200));
                }
                item.repaint();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                action.actionPerformed(null);
            }
        });
        return item;
    }

    private void startAnimations() {
        new Timer(1000, e -> {
            if (animatedTimer != null) animatedTimer.setText(new SimpleDateFormat("EEEE, MMMM dd | HH:mm:ss").format(new Date()));
        }).start();
    }

    private void updateWorkCounts() {
        // PREVENT CRASH: Check if UI labels are initialized
        if (lblPendingCount == null || lblCompletedCount == null || lblTotalWork == null) {
            return;
        }

        Map<String, Integer> stats = LabratoryDAO.getWorkStats();
        if (stats != null) {
            int pending = stats.getOrDefault("pending", 0);
            int completed = stats.getOrDefault("completed", 0);

            lblPendingCount.setText(String.valueOf(pending));
            lblCompletedCount.setText(String.valueOf(completed));

            // SYSTEM LOGS: Should show the total history (Sum of all)
            lblTotalWork.setText(String.valueOf(pending + completed));
        }
    }

    private void upgradeTableUI(JTable table) {
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                label.setBorder(new EmptyBorder(0, 15, 0, 15));
                if (s) {
                    label.setBackground(new Color(33, 158, 188, 50));
                    label.setForeground(NAVY);
                } else {
                    label.setBackground(r % 2 == 0 ? Color.WHITE : new Color(245, 248, 250));
                    label.setForeground(new Color(60, 60, 60));
                }
                return label;
            }
        });
    }

    @Override public void logout() {
        if (JOptionPane.showConfirmDialog(frame, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION) == 0) {
            frame.dispose(); new LoginPage().setVisible(true);
        }
    }
}