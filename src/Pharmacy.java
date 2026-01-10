import Database.PharmacyDAO;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import javax.swing.Timer;


public class Pharmacy extends staffUser {
    // Styling Colors
    private final Color NAVY = new Color(2, 48, 71);
    private final Color TEAL = new Color(42, 157, 143);
    private final Color GLASS_TINT = new Color(255, 255, 255, 140);
    private final Color SIDEBAR_TEXT = new Color(229, 229, 229);

    private JFrame mainFrame;
    private JPanel contentPanel;
    private Timer autoRefreshTimer;
    private String userId;
    private String usename;
    private String role;
    private String licenseNumber;
    private String shiftType;
    private String contactNumber;
    public Pharmacy(int authId) {
        this.userId = String.valueOf(authId);
       // this.role = "PHARMACY";
        loadPharmacistData(authId);
    }
    private void loadPharmacistData(int loggedInAuthId) {
        PharmacyDAO dao = new PharmacyDAO();
        try (ResultSet rs = dao.getPharmacistRecord(loggedInAuthId)) {
            if (rs.next()) {
                String fName = rs.getString("first_name");
                String lName = rs.getString("last_name");
                this.usename = fName + " " + lName;
                this.userId = String.valueOf(rs.getInt("pharmacist_id"));

                this.licenseNumber = rs.getString("license_number");
                this.shiftType = rs.getString("shift_type");
                this.contactNumber = rs.getString("contact_number");

                System.out.println("✅ Full Profile Loaded for: " + this.usename);
            }
        } catch (SQLException e) {
            // Fallback design
            this.usename = "Pharmacist";
            System.err.println("❌ Error loading pharmacist data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @Override
    void showDashboard() {
        mainFrame = new JFrame("HMS - Pharmacy Division | " + usename);
        mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());

        mainFrame.add(createSidebar(), BorderLayout.WEST);

        contentPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                String path = "assets/pharmacy.jpg";
                File imgFile = new File(path);
                if (!imgFile.exists()) imgFile = new File("src/" + path);

                if (imgFile.exists()) {
                    g2d.drawImage(new ImageIcon(imgFile.getAbsolutePath()).getImage(), 0, 0, getWidth(), getHeight(), this);
                } else {
                    g2d.setPaint(new GradientPaint(0, 0, new Color(245, 247, 250), 0, getHeight(), new Color(180, 190, 210)));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        mainFrame.add(contentPanel, BorderLayout.CENTER);
        showOverview();
        mainFrame.setVisible(true);
    }
    private JPanel createSidebar() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(NAVY);
        p.setPreferredSize(new Dimension(260, 0));
        p.setBorder(new MatteBorder(0, 0, 0, 1, new Color(255, 255, 255, 30)));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        titlePanel.setOpaque(false);
        titlePanel.setMaximumSize(new Dimension(260, 80)); // Lock header height

        JLabel portalHeader = new JLabel("PHARMA-SYNC");
        File portalIcon = new File("assets/logo.jpg");
        if (portalIcon.exists()) {
            portalHeader.setIcon(new ImageIcon(new ImageIcon(portalIcon.getPath()).getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH)));
            portalHeader.setIconTextGap(12);
        }
        portalHeader.setForeground(TEAL);
        portalHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titlePanel.add(portalHeader);
        p.add(titlePanel);

        // Spacing before buttons
        p.add(Box.createVerticalStrut(15));

        // Navigation items with Struts to prevent overlap doubling
        p.add(createNavItem("Dashboard", "assets/dashboard.png", e -> showOverview()));
        p.add(Box.createVerticalStrut(10)); // Spacer fix

        p.add(createNavItem("Prescription Queue", "assets/medical-prescription.png", e -> showPrescriptionQueue()));
        p.add(Box.createVerticalStrut(10)); // Spacer fix

        p.add(createNavItem("Profile", "assets/user.png", e -> showInventoryManager()));

        p.add(Box.createVerticalGlue());

        JPanel logoutWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoutWrapper.setOpaque(false);
        logoutWrapper.setMaximumSize(new Dimension(260, 80));

// Updated action listener with Confirmation Dialog
        JButton logoutBtn = createNavItem("Log Out", "assets/logout.png", e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    mainFrame,
                    "Are you sure you want to log out?",
                    "Logout Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                logout();
            }
        });

        logoutBtn.setPreferredSize(new Dimension(220, 45));
        logoutBtn.setOpaque(true);
        logoutBtn.setBackground(new Color(230, 57, 70, 180));
        logoutBtn.setForeground(Color.WHITE);
        logoutWrapper.add(logoutBtn);

        p.add(logoutWrapper);
        p.add(Box.createVerticalStrut(20));
        return p;
    }
    private JButton createNavItem(String text, String iconPath, ActionListener action) {
        JButton btn = new JButton(text);

        // FIX: Enforce exact sizing to prevent the layout from shifting on hover
        Dimension size = new Dimension(240, 45);
        btn.setPreferredSize(size);
        btn.setMaximumSize(size);
        btn.setMinimumSize(size);

        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setForeground(SIDEBAR_TEXT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        File iconFile = new File(iconPath);
        if (iconFile.exists()) {
            btn.setIcon(new ImageIcon(new ImageIcon(iconPath).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
            btn.setIconTextGap(15);
        }

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setContentAreaFilled(true);
                btn.setBackground(new Color(255, 255, 255, 20));
                btn.setForeground(TEAL);
                btn.getParent().repaint(); // Redraw parent to clear any ghosting
            }
            public void mouseExited(MouseEvent e) {
                btn.setContentAreaFilled(false);
                btn.setForeground(SIDEBAR_TEXT);
                btn.getParent().repaint();
            }
        });
        btn.addActionListener(action);
        return btn;
    }
    private void showOverview() {
        contentPanel.removeAll();
        PharmacyDAO dao = new PharmacyDAO(); // Initialize DAO

        if (autoRefreshTimer != null) autoRefreshTimer.stop();

        JLabel pendingValLbl = new JLabel("0", SwingConstants.CENTER);
        JLabel dispensedValLbl = new JLabel("0", SwingConstants.CENTER);
        DefaultTableModel tableModel = new DefaultTableModel(new String[]{"Patient ID", "Medication", "Time", "Status"}, 0){
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        // 2. Updated Refresh Logic using DAO and DatabaseConnection
        ActionListener refreshAction = e -> {
            try {
                // Get Counts using DAO
                pendingValLbl.setText(String.valueOf(dao.getCountByStatus("Pending", false)));
                dispensedValLbl.setText(String.valueOf(dao.getCountByStatus("Done", true)));

                // Get Table Data using DAO
                try (ResultSet rs3 = dao.getRecentActivity()) {
                    tableModel.setRowCount(0);
                    while(rs3.next()) {
                        tableModel.addRow(new Object[]{
                                rs3.getString("patient_id"),
                                rs3.getString("medication_name"),
                                rs3.getTimestamp("issued_at").toString().substring(11, 16),
                                rs3.getString("status")
                        });
                    }
                }
            } catch (SQLException ex) {
                System.err.println("Live Update Error: " + ex.getMessage());
            }
        };

        // 3. Start Timer (Logic remains same)
        autoRefreshTimer = new Timer(5000, refreshAction);
        autoRefreshTimer.start();
        refreshAction.actionPerformed(null);

        // 4. UI LOGIC (Kept exactly as your design)
        JPanel overlay = new JPanel(new BorderLayout(0, 20));
        overlay.setOpaque(false);
        overlay.setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel welcomeWrapper = new JPanel(new BorderLayout());
        welcomeWrapper.setOpaque(false);

        int hour = java.time.LocalTime.now().getHour();
        String greeting = (hour < 12) ? "Good Morning, Pharmacist " : (hour < 17) ? "Good Afternoon, Pharmacist " : "Good Evening, Pharmacist ";

        JLabel welcomeLbl = new JLabel(greeting + usename);
        welcomeLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLbl.setForeground(Color.BLACK);

        String dateString = new SimpleDateFormat("EEEE, MMMM dd, yyyy").format(new java.util.Date());
        JLabel dateLbl = new JLabel(dateString);
        dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        dateLbl.setForeground(new Color(0, 0, 0, 180));

        welcomeWrapper.add(welcomeLbl, BorderLayout.NORTH);
        welcomeWrapper.add(dateLbl, BorderLayout.SOUTH);

        JPanel header = new JPanel(new GridLayout(1, 2, 25, 0));
        header.setOpaque(false);
        header.add(createLiveStatCard("Pending Requests", pendingValLbl, TEAL));
        header.add(createLiveStatCard("Dispensed Today", dispensedValLbl, NAVY));

        JPanel northPanel = new JPanel(new BorderLayout(0, 25));
        northPanel.setOpaque(false);
        northPanel.add(welcomeWrapper, BorderLayout.NORTH);
        northPanel.add(header, BorderLayout.CENTER);

        JTable table = new JTable(tableModel);
        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(new Color(0, 0, 0, 100)), " Live Pharmacy Activity Feed ",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), Color.BLACK));

        overlay.add(northPanel, BorderLayout.NORTH);
        overlay.add(scroll, BorderLayout.CENTER);

        contentPanel.add(overlay);
        refreshPanel();
    }
    private void styleTable(JTable table) {
        table.setRowHeight(52); // Taller rows are more modern and readable
        table.setShowGrid(false); // Remove old-fashioned grid lines
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(42, 157, 143, 40)); // Subtle Teal highlight
        table.setSelectionForeground(NAVY);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setBackground(new Color(255, 255, 255, 230)); // Glass effect
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 48));
        header.setBackground(NAVY);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setReorderingAllowed(false); // Professional look
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        int statusIdx = table.getColumnCount() - 1;
        table.getColumnModel().getColumn(statusIdx).setCellRenderer(new StatusBadgeRenderer());
    }
    private class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            String status = (value != null) ? value.toString() : "";

            // Create a custom JPanel to draw the "Pill" badge
            return new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    Color bg, fg;
                    if (status.equalsIgnoreCase("Pending")) {
                        bg = new Color(255, 243, 224); // Light Orange
                        fg = new Color(230, 126, 34);  // Dark Orange
                    } else if (status.equalsIgnoreCase("Done") || status.equalsIgnoreCase("Completed")) {
                        bg = new Color(232, 245, 233); // Light Green
                        fg = new Color(46, 125, 50);   // Dark Green
                    } else {
                        bg = new Color(245, 245, 245);
                        fg = Color.GRAY;
                    }

                    // Draw Background Pill
                    g2.setColor(bg);
                    g2.fillRoundRect(getWidth()/2 - 45, getHeight()/2 - 12, 90, 24, 15, 15);

                    // Draw Text
                    g2.setColor(fg);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    FontMetrics fm = g2.getFontMetrics();
                    int textX = (getWidth() - fm.stringWidth(status.toUpperCase())) / 2;
                    int textY = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g2.drawString(status.toUpperCase(), textX, textY);
                    g2.dispose();
                }
            };
        }
    }
    private JPanel createLiveStatCard(String title, JLabel valLbl, Color accent) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(GLASS_TINT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(255, 255, 255, 100));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 42));
        valLbl.setForeground(accent);

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(new Color(50, 50, 50));

        card.add(valLbl, BorderLayout.CENTER);
        card.add(titleLbl, BorderLayout.SOUTH);
        return card;
    }
    private void showInventoryManager() {
        contentPanel.removeAll();

        // Using GridBagLayout to keep the ID card centered
        JPanel overlay = new JPanel(new GridBagLayout());
        overlay.setOpaque(false);

        // --- THE DIGITAL SMART CARD (Design kept exactly the same) ---
        JPanel idCard = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                // 1. Card Body Shadow & Base
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(5, 5, 395, 595, 35, 35);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, 400, 600, 30, 30);

                // 2. Modern Header
                g2.setColor(NAVY);
                g2.fillRoundRect(0, 0, 400, 140, 30, 30);
                g2.fillRect(0, 100, 400, 40);

                // 3. Gold Chip Graphic
                g2.setColor(new Color(212, 175, 55));
                g2.fillRoundRect(320, 160, 45, 35, 8, 8);
                g2.setColor(new Color(0, 0, 0, 40));
                g2.drawRoundRect(320, 160, 45, 35, 8, 8);

                // 4. Photo Circle
                int cx = 130, cy = 70, size = 140;
                g2.setColor(new Color(240, 240, 240));
                g2.fillOval(cx, cy, size, size);

                String photoPath = "assets/user.png";
                File imgFile = new File(photoPath);
                if (!imgFile.exists()) imgFile = new File("src/" + photoPath);

                if (imgFile.exists()) {
                    Image img = new ImageIcon(imgFile.getAbsolutePath()).getImage();
                    g2.setClip(new java.awt.geom.Ellipse2D.Float(cx, cy, size, size));
                    g2.drawImage(img, cx, cy, size, size, null);
                    g2.setClip(null);
                } else {
                    g2.setColor(new Color(180, 180, 180));
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 80));
                    g2.drawString("👤", cx + 30, cy + 100);
                }

                // 5. Teal Ring
                g2.setColor(TEAL);
                g2.setStroke(new BasicStroke(4));
                g2.drawOval(cx + 2, cy + 2, size - 4, size - 4);

                // 6. Card Border
                g2.setColor(new Color(0, 0, 0, 50));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, 399, 599, 30, 30);
                g2.dispose();
            }
        };
        idCard.setPreferredSize(new Dimension(410, 610));
        idCard.setOpaque(false);

        // Static Header Text
        JLabel hospitalName = new JLabel("HMS NETWORK PORTAL");
        hospitalName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        hospitalName.setForeground(new Color(255, 255, 255, 200));
        hospitalName.setBounds(30, 25, 200, 20);
        idCard.add(hospitalName);

        JLabel systemTitle = new JLabel("STAFF DIGITAL ID");
        systemTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        systemTitle.setForeground(Color.WHITE);
        systemTitle.setBounds(30, 45, 300, 35);
        idCard.add(systemTitle);

        // --- FIX: USE PRE-LOADED DATA INSTEAD OF SQL ---
        // Personnel Name (Already loaded in constructor)
        String displayName = (usename != null) ? usename.toUpperCase() : "PHARMACIST";
        JLabel nameLabel = new JLabel(displayName, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        nameLabel.setForeground(NAVY);
        nameLabel.setBounds(0, 220, 400, 40);
        idCard.add(nameLabel);

        JLabel rank = new JLabel("LICENSED PHARMACIST", SwingConstants.CENTER);
        rank.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rank.setForeground(TEAL);
        rank.setBounds(0, 255, 400, 20);
        idCard.add(rank);

        // Details Section (Using variables assigned in loadPharmacistData)
        int y = 310;
        addDetail(idCard, "LICENSE NUMBER", (licenseNumber != null ? licenseNumber : "N/A"), y);
        addDetail(idCard, "EMPLOYEE SERIAL", "PHARM-" + userId, y + 60);
        addDetail(idCard, "CURRENT SHIFT", (shiftType != null ? shiftType.toUpperCase() : "N/A"), y + 120);
        addDetail(idCard, "EMERGENCY CONTACT", (contactNumber != null ? contactNumber : "N/A"), y + 180);

        overlay.add(idCard);
        contentPanel.add(overlay);
        refreshPanel();
    }
    private void addDetail(JPanel card, String label, String value, int y) {
        // Label (e.g., "LICENSE NUMBER")
        JLabel title = new JLabel(label);
        title.setFont(new Font("Segoe UI", Font.BOLD, 10));
        title.setForeground(Color.GRAY);
        title.setBounds(50, y, 300, 15);

        // Value (e.g., "PHARM-12345")
        JLabel content = new JLabel(value);
        content.setFont(new Font("Segoe UI", Font.BOLD, 16));
        content.setForeground(Color.BLACK);
        content.setBounds(50, y + 18, 300, 25);

        card.add(title);
        card.add(content);
    }
    private void showPrescriptionQueue() {
        contentPanel.removeAll();
        PharmacyDAO dao = new PharmacyDAO(); // Initialize DAO

        JPanel overlay = new JPanel(new BorderLayout());
        overlay.setOpaque(false);
        overlay.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Prescription Management Queue");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(NAVY);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));

        String[] cols = {"ID", "Patient Name", "Doctor Name", "Medication", "Dosage", "Issued At", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        styleTable(table);

        // --- DATA LOADING LOGIC ---
        try (ResultSet rs = dao.getPrescriptionQueue()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("prescription_id"),
                        rs.getString("patient_full_name"),
                        rs.getString("doctor_full_name"),
                        rs.getString("medication_name"),
                        rs.getString("dosage"),
                        rs.getTimestamp("issued_at"),
                        rs.getString("status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Database Error: " + e.getMessage());
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(new LineBorder(new Color(255, 255, 255, 80)));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setPreferredSize(new Dimension(0, 55));

        // --- BUTTON 1: MARK AS DONE ---
        JButton processBtn = new JButton("Mark as Done");
        processBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        processBtn.setBackground(TEAL);
        processBtn.setForeground(Color.WHITE);
        processBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        processBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int prescriptionId = (int) model.getValueAt(selectedRow, 0);
                try {
                    if (dao.updatePrescriptionStatus(prescriptionId, "Done")) {
                        model.removeRow(selectedRow);
                        JOptionPane.showMessageDialog(mainFrame, "Status updated to Done.");
                    }
                } catch (SQLException ex) { ex.printStackTrace(); }
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Select a prescription first!");
            }
        });

        // --- BUTTON 2: ADD BILL ---
        JButton billBtn = new JButton("Add Bill");
        billBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        billBtn.setBackground(NAVY);
        billBtn.setForeground(Color.WHITE);
        billBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        billBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int prescriptionId = (int) model.getValueAt(selectedRow, 0);
                String patientName = (String) model.getValueAt(selectedRow, 1);

                String feeStr = JOptionPane.showInputDialog(mainFrame,
                        "Enter Drug Fee for " + patientName + ":", "Billing - Prescription #" + prescriptionId,
                        JOptionPane.QUESTION_MESSAGE);

                if (feeStr != null && !feeStr.isEmpty()) {
                    try {
                        double medFee = Double.parseDouble(feeStr);
                        if (dao.createPrescriptionBill(prescriptionId, medFee, usename)) {
                            JOptionPane.showMessageDialog(mainFrame, "Bill Created Successfully for " + patientName);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(mainFrame, "Invalid Amount!");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(mainFrame, "DB Error: " + ex.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Select a row to generate a bill!");
            }
        });

        buttonPanel.add(billBtn);
        buttonPanel.add(processBtn);

        overlay.add(title, BorderLayout.NORTH);
        overlay.add(scroll, BorderLayout.CENTER);
        overlay.add(buttonPanel, BorderLayout.SOUTH);

        contentPanel.add(overlay);
        refreshPanel();
    }
    private void refreshPanel() {
        contentPanel.revalidate();
        contentPanel.repaint(); }
    @Override void logout() { new LoginPage().setVisible(true); }

}