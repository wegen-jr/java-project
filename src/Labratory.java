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

    @Override
    public void showDashboard() {
        try {
            this.fullName = LabratoryDAO.getFullNameByUsername(this.usename);
        } catch (Exception e) { this.fullName = "Staff Member"; }
        if (this.fullName == null) this.fullName = "Staff Member";

        frame = new JFrame("HMS - Clinical Laboratory Portal");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

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

        cardPanel.add(createDashboardBody(), "DASHBOARD");
        cardPanel.add(createProfileBody(), "PROFILE");
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
        p.add(createNavItem("Active Requests", "assets/observation.png", e -> showLabRequestsDashboard()));
        p.add(createNavItem("Lab Response", "assets/update.png", e -> showLabResponseWindow()));
        p.add(createNavItem("My Profile", "assets/user.png", e -> cardLayout.show(cardPanel, "PROFILE")));
        p.add(createNavItem("Request History", "assets/history.jpg", e -> showHistoryWindow()));
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

    private JPanel createProfileBody() {
        Map<String, String> data = LabratoryDAO.getTechnicianProfile(this.usename);
        if (data == null) data = new HashMap<>();
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        JPanel masterCard = new JPanel(new BorderLayout());
        masterCard.setPreferredSize(new Dimension(850, 550));
        masterCard.setBackground(GLASS_WHITE);
        masterCard.setBorder(new LineBorder(new Color(200, 200, 200), 1, true));
        JPanel identityPanel = new JPanel();
        identityPanel.setLayout(new BoxLayout(identityPanel, BoxLayout.Y_AXIS));
        identityPanel.setBackground(NAVY);
        identityPanel.setPreferredSize(new Dimension(300, 550));
        identityPanel.add(Box.createVerticalStrut(50));
        JLabel avatar = new JLabel();
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        try {
            avatar.setIcon(new ImageIcon(new ImageIcon("assets/user.png").getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH)));
            avatar.setBorder(new LineBorder(Color.WHITE, 3, true));
        } catch(Exception e) { avatar.setText("USER"); }
        identityPanel.add(avatar);
        identityPanel.add(Box.createVerticalStrut(20));
        JLabel nameLbl = new JLabel(data.getOrDefault("full_name", fullName));
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        nameLbl.setForeground(Color.WHITE);
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        identityPanel.add(nameLbl);
        JLabel posLbl = new JLabel("Laboratory Technician");
        posLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        posLbl.setForeground(HOVER_BLUE);
        posLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        identityPanel.add(posLbl);
        identityPanel.add(Box.createVerticalStrut(40));
        identityPanel.add(createBadge("CERTIFIED", SUCCESS_GREEN));
        identityPanel.add(Box.createVerticalStrut(10));
        identityPanel.add(createBadge("ACTIVE DUTY", ACCENT_GOLD));
        JPanel dataPanel = new JPanel();
        dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
        dataPanel.setOpaque(false);
        dataPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
        JLabel title = new JLabel("Professional Information");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(NAVY);
        dataPanel.add(title);
        dataPanel.add(Box.createVerticalStrut(30));
        JPanel grid = new JPanel(new GridLayout(3, 2, 20, 30));
        grid.setOpaque(false);
        grid.add(createDetailBox("STAFF ID", data.getOrDefault("id", "LAB-000")));
        grid.add(createDetailBox("CERTIFICATION", data.getOrDefault("license", "Standard")));
        grid.add(createDetailBox("USERNAME", usename));
        grid.add(createDetailBox("CONTACT", data.getOrDefault("phone", "N/A")));
        grid.add(createDetailBox("DEPARTMENT", data.getOrDefault("department","Clinical Pathology")));
        grid.add(createDetailBox("EMAIL", usename + "@hospital.org"));
        dataPanel.add(grid);
        dataPanel.add(Box.createVerticalGlue());
        JButton editBtn = new JButton("Update Information");
        editBtn.setFocusPainted(false);
        editBtn.setBackground(NAVY);
        editBtn.setForeground(Color.WHITE);
        dataPanel.add(editBtn);
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

    void showLabResponseWindow() {
        JFrame win = new JFrame("Laboratory Information System - Result Entry");
        win.setSize(1200, 750);
        win.setLocationRelativeTo(frame);

        // --- LEFT NAVIGATION PANEL (Patient Selection) ---
        JPanel leftNav = new JPanel(new BorderLayout());
        leftNav.setPreferredSize(new Dimension(380, 0));
        leftNav.setBackground(Color.WHITE);
        leftNav.setBorder(new MatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));

        JLabel navTitle = new JLabel("  Select Patient to Analyze", SwingConstants.LEFT);
        navTitle.setPreferredSize(new Dimension(0, 50));
        navTitle.setOpaque(true);
        navTitle.setBackground(new Color(245, 245, 245));
        navTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        leftNav.add(navTitle, BorderLayout.NORTH);

        String[] cols = {"ID", "NAME", "TEST"};
        DefaultTableModel navModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // Table Setup
        JTable navTable = new JTable(navModel);
        navTable.setRowHeight(45);
        navTable.setSelectionBackground(new Color(235, 245, 255));
        navTable.setShowVerticalLines(false);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(navModel);
        navTable.setRowSorter(sorter);

        // Search Logic (Fixed to use existing table sorter)
        JTextField searchBox = new JTextField(" 🔍 Search by name...");
        searchBox.setFont(new Font("SansSerif", Font.ITALIC, 12));
        searchBox.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0, Color.LIGHT_GRAY), new EmptyBorder(10,10,10,10)));
        searchBox.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String query = searchBox.getText().toLowerCase();
                if (query.contains("search by name")) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
            }
        });

        leftNav.add(searchBox, BorderLayout.NORTH);

        // Load Data
        List<Object[]> pendingList = LabratoryDAO.getPendingRequests();
        if (pendingList != null) for(Object[] r : pendingList) navModel.addRow(new Object[]{r[0], r[1], r[2]});
        leftNav.add(new JScrollPane(navTable), BorderLayout.CENTER);

        // --- RIGHT ENTRY PANEL (Results Input) ---
        JPanel entryPanel = new JPanel(new GridBagLayout());
        entryPanel.setBackground(new Color(250, 251, 253));

        JPanel entryCard = new JPanel();
        entryCard.setLayout(new BoxLayout(entryCard, BoxLayout.Y_AXIS));
        entryCard.setBackground(Color.WHITE);
        entryCard.setPreferredSize(new Dimension(600, 580));
        entryCard.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));

        JPanel cardHead = new JPanel(new BorderLayout());
        cardHead.setBackground(NAVY);
        cardHead.setBorder(new EmptyBorder(20, 30, 20, 30));
        JLabel cardTitle = new JLabel("VALIDATE FINDINGS");
        cardTitle.setForeground(Color.WHITE);
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        cardHead.add(cardTitle, BorderLayout.WEST);
        entryCard.add(cardHead);

        JPanel fieldBody = new JPanel(new GridBagLayout());
        fieldBody.setOpaque(false);
        fieldBody.setBorder(new EmptyBorder(40, 50, 40, 50));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1.0; g.insets = new Insets(10,0,10,0);

        JTextField valField = new JTextField();
        valField.setPreferredSize(new Dimension(0, 45));
        valField.setFont(new Font("SansSerif", Font.BOLD, 18));

        JTextArea noteArea = new JTextArea(4, 20);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setBorder(new LineBorder(new Color(200, 200, 200)));

        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Normal", "Abnormal", "Critical / Urgent"});
        statusCombo.setPreferredSize(new Dimension(0, 40));

        g.gridy=0; fieldBody.add(new JLabel("OBSERVED VALUE / RESULT"), g);
        g.gridy=1; fieldBody.add(valField, g);
        g.gridy=2; fieldBody.add(new JLabel("TECHNICIAN REMARKS"), g);
        g.gridy=3; fieldBody.add(new JScrollPane(noteArea), g);
        g.gridy=4; fieldBody.add(new JLabel("ANALYSIS CONCLUSION"), g);
        g.gridy=5; fieldBody.add(statusCombo, g);
        entryCard.add(fieldBody);

        JButton submitBtn = new JButton("FINALIZE & SUBMIT TO DOCTOR");
        submitBtn.setBackground(SUCCESS_GREEN);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        submitBtn.setPreferredSize(new Dimension(0, 60));
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        entryCard.add(submitBtn);

        entryPanel.add(entryCard);

        // Selection Listener
        navTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && navTable.getSelectedRow() != -1) {
                int selectedRow = navTable.getSelectedRow();
                cardTitle.setText("Analysing: " + navTable.getValueAt(selectedRow, 1).toString());
            }
        });

        // --- SUBMIT LOGIC (Mapped to result_details) ---
        submitBtn.addActionListener(e -> {
            int row = navTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(win, "Select a patient first.");
                return;
            }

            // Get ID from model (handle sorting index)
            int modelRow = navTable.convertRowIndexToModel(row);
            String reqID = navModel.getValueAt(modelRow, 0).toString();

            // Professional Formatting for result_details column
            String finalResultDetails = String.format(
                    "VALUE: %s | REMARKS: %s | CONCLUSION: %s",
                    valField.getText().trim(),
                    noteArea.getText().trim(),
                    statusCombo.getSelectedItem().toString()
            );

            // Update Database
            boolean ok = LabratoryDAO.submitLabResult(reqID, finalResultDetails);

            if (ok) {
                JOptionPane.showMessageDialog(win, "Diagnostic Result Released Successfully.");
                updateWorkCounts();

                // Refresh UI
                navModel.removeRow(modelRow);
                valField.setText("");
                noteArea.setText("");
                cardTitle.setText("VALIDATE FINDINGS");
            } else {
                JOptionPane.showMessageDialog(win, "Error updating database. Verify table structure.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftNav, entryPanel);
        split.setDividerLocation(380);
        split.setDividerSize(1);
        win.add(split);
        win.setVisible(true);
    }

    void showLabRequestsDashboard() {
        JFrame win = new JFrame("Active Diagnostics Queue");
        win.setSize(1000, 650);
        win.setLocationRelativeTo(frame);
        win.getContentPane().setBackground(new Color(248, 249, 251));

        // Headers match your requested table structure
        String[] colNames = {"REQUEST ID", "PATIENT NAME", "TEST", "PRIORITY", "STATUS"};

        DefaultTableModel model = new DefaultTableModel(colNames, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // LOGIC: LabratoryDAO.getAllRecentRequests() MUST use a JOIN to get the Patient Name
        List<Object[]> requests = LabratoryDAO.getAllRecentRequests();
        if (requests != null) {
            for (Object[] r : requests) {
                model.addRow(r);
            }
        }

        JTable table = new JTable(model);
        upgradeTableUI(table); // Maintains your custom styling
        table.setRowHeight(50);

        // Dynamic Status Renderer (Pending vs Completed)
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);

                // Handle null values gracefully
                String val = (v != null) ? v.toString().toUpperCase() : "PENDING";

                if (val.contains("COMPLETED")) {
                    l.setForeground(SUCCESS_GREEN);
                    l.setText("✔ COMPLETED");
                } else {
                    l.setForeground(ACCENT_GOLD);
                    l.setText("⏳ PENDING");
                }
                return l;
            }
        });

        // Scannable scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        win.add(scrollPane, BorderLayout.CENTER);

        win.setVisible(true);
    }

    private void startAnimations() {
        new Timer(1000, e -> {
            if (animatedTimer != null) animatedTimer.setText(new SimpleDateFormat("EEEE, MMMM dd | HH:mm:ss").format(new Date()));
        }).start();
    }

    private void updateWorkCounts() {
        Map<String, Integer> stats = LabratoryDAO.getWorkStats();
        if (stats != null) {
            lblPendingCount.setText(String.valueOf(stats.getOrDefault("pending", 0)));
            lblCompletedCount.setText(String.valueOf(stats.getOrDefault("completed", 0)));
            lblTotalWork.setText(String.valueOf(stats.getOrDefault("pending", 0) + stats.getOrDefault("completed", 0)));
        }
    }

    @Override public void logout() {
        if (JOptionPane.showConfirmDialog(frame, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION) == 0) {
            frame.dispose(); new LoginPage().setVisible(true);
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

    void showHistoryWindow() {
        JFrame historyWin = new JFrame("Laboratory Archive");
        historyWin.setSize(1100, 700);
        historyWin.setLocationRelativeTo(frame);
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        String[] cols = {"ID", "PATIENT", "TEST", "STATUS", "DATE"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        List<Object[]> history = LabratoryDAO.getFullLabArchive();
        if (history != null) {
            for (Object[] r : history) model.addRow(r);
        }
        JTable table = new JTable(model);
        upgradeTableUI(table);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        historyWin.add(mainPanel);
        historyWin.setVisible(true);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Labratory lab = new Labratory();
            lab.usename = "yabu";
            lab.showDashboard();
        });
    }
}