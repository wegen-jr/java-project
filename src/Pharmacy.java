import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;

public class Pharmacy extends staffUser {
    // Styling Colors
    private final Color NAVY = new Color(2, 48, 71);
    private final Color TEAL = new Color(42, 157, 143);
    private final Color GLASS_TINT = new Color(255, 255, 255, 140);
    private final Color SIDEBAR_TEXT = new Color(229, 229, 229);

    private JFrame mainFrame;
    private JPanel contentPanel;

    public Pharmacy(String userId, String username) {
        this.userId = userId;
        this.usename = username;
        this.role = "Pharmacist";
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

                String path = "assets/pharmacy2.jpg";
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
        File portalIcon = new File("assets/overviewicon.jpg");
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
        p.add(createNavItem("Overview", "assets/overviewicon.jpg", e -> showOverview()));
        p.add(Box.createVerticalStrut(10)); // Spacer fix

        p.add(createNavItem("Prescription Queue", "assets/prescriptionqueueicon.jpg", e -> showPrescriptionQueue()));
        p.add(Box.createVerticalStrut(10)); // Spacer fix

        p.add(createNavItem("Profile", "assets/user.png", e -> showInventoryManager()));

        p.add(Box.createVerticalGlue());

        JPanel logoutWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoutWrapper.setOpaque(false);
        logoutWrapper.setMaximumSize(new Dimension(260, 80));

        JButton logoutBtn = createNavItem("Log Out", "assets/logouticon.jpg", e -> logout());
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

    // --- OTHER METHODS (Untouched Design) ---
    private void showOverview() {
        contentPanel.removeAll();
        JPanel overlay = new JPanel(new BorderLayout());
        overlay.setOpaque(false);
        overlay.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel header = new JPanel(new GridLayout(1, 3, 25, 0));
        header.setOpaque(false);
        header.add(createStatCard("Pending Requests", "12", TEAL));
        header.add(createStatCard("Low Stock", "4", new Color(230, 57, 70)));
        header.add(createStatCard("Dispensed Today", "48", NAVY));

        overlay.add(header, BorderLayout.NORTH);

        // Example Table Logic
        String[] cols = {"Activity", "Time", "Status"};
        Object[][] data = {{"Aspirin Dispensed", "10:30 AM", "Completed"},{"Stock Update", "09:15 AM", "Verified"}};
        JTable table = new JTable(new DefaultTableModel(data, cols));
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        overlay.add(scroll, BorderLayout.CENTER);

        contentPanel.add(overlay);
        refreshPanel();
    }

    private void styleTable(JTable table) {
        table.setRowHeight(45);
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
    }

    private JPanel createStatCard(String title, String value, Color accent) {
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
        JLabel valLbl = new JLabel(value, SwingConstants.CENTER);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 42));
        valLbl.setForeground(accent);
        card.add(valLbl, BorderLayout.CENTER);
        return card;
    }

    private void showInventoryManager() {
        contentPanel.removeAll();

        // Using GridBagLayout to keep the ID card centered regardless of window size
        JPanel overlay = new JPanel(new GridBagLayout());
        overlay.setOpaque(false);

        // --- THE DIGITAL SMART CARD ---
        JPanel idCard = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                // 1. Card Body Shadow & Base
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(5, 5, 395, 595, 35, 35); // Soft shadow
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, 400, 600, 30, 30);

                // 2. Modern Header
                g2.setColor(NAVY);
                g2.fillRoundRect(0, 0, 400, 140, 30, 30);
                g2.fillRect(0, 100, 400, 40); // Flattened bottom for header

                // 3. Gold Chip Graphic
                g2.setColor(new Color(212, 175, 55));
                g2.fillRoundRect(320, 160, 45, 35, 8, 8);
                g2.setColor(new Color(0, 0, 0, 40));
                g2.drawRoundRect(320, 160, 45, 35, 8, 8);

                // 4. Photo Circle with Image Clipping
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
                    g2.setClip(null); // Remove mask
                } else {
                    g2.setColor(new Color(180, 180, 180));
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 80));
                    g2.drawString("👤", cx + 30, cy + 100);
                }

                // 5. Teal Ring Around Photo
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

        // Database Population
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/hms", "root", "eyob4791")) {
            String query = "SELECT * FROM pharmacists WHERE auth_id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, userId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                // Personnel Name
                String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
                JLabel nameLabel = new JLabel(fullName.toUpperCase(), SwingConstants.CENTER);
                nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
                nameLabel.setForeground(NAVY);
                nameLabel.setBounds(0, 220, 400, 40);
                idCard.add(nameLabel);

                JLabel rank = new JLabel("LICENSED PHARMACIST", SwingConstants.CENTER);
                rank.setFont(new Font("Segoe UI", Font.BOLD, 13));
                rank.setForeground(TEAL);
                rank.setBounds(0, 255, 400, 20);
                idCard.add(rank);

                // Details Section
                int y = 310;
                addDetail(idCard, "LICENSE NUMBER", rs.getString("license_number"), y);
                addDetail(idCard, "EMPLOYEE SERIAL", "PHARM-" + rs.getString("pharmacist_id"), y + 60);
                addDetail(idCard, "CURRENT SHIFT", rs.getString("shift_type").toUpperCase(), y + 120);
                addDetail(idCard, "EMERGENCY CONTACT", rs.getString("contact_number"), y + 180);


            }
        } catch (SQLException e) {
            JLabel err = new JLabel("OFFLINE MODE - DATA SYNC FAILED", SwingConstants.CENTER);
            err.setBounds(0, 300, 400, 30);
            err.setForeground(Color.RED);
            idCard.add(err);
        }

        overlay.add(idCard);
        contentPanel.add(overlay);
        refreshPanel();
    }

    // Helper method for the ID Card fields
    private void addDetail(JPanel card, String title, String val, int yPos) {
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 10));
        t.setForeground(new Color(160, 160, 160));
        t.setBounds(60, yPos, 300, 15);

        JLabel v = new JLabel(val != null ? val : "NOT ASSIGNED");
        v.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 18));
        v.setForeground(new Color(40, 40, 40));
        v.setBounds(60, yPos + 18, 300, 25);

        card.add(t);
        card.add(v);
    }




     private void showPrescriptionQueue() {
        contentPanel.removeAll();
        JPanel overlay = new JPanel(new BorderLayout());
        overlay.setOpaque(false);
        overlay.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Prescription Management Queue");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(NAVY);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));

        // Columns updated to include Names instead of just IDs
        String[] cols = {"ID", "Patient Name", "Doctor Name", "Medication", "Dosage", "Issued At", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        styleTable(table);

        try {
            // Ensure your DB credentials are correct
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/hms", "root", "eyob4791");

            // SQL query using JOINs and CONCAT for full names
            String query = "SELECT " +
                    "p.prescription_id, " +
                    "CONCAT(pat.first_name, ' ', pat.last_name) AS patient_full_name, " +
                    "CONCAT(doc.first_name, ' ', doc.last_name) AS doctor_full_name, " +
                    "p.medication_name, p.dosage, p.issued_at, p.status " +
                    "FROM prescriptions p " +
                    "JOIN patients pat ON p.patient_id COLLATE utf8mb4_general_ci = pat.patient_id COLLATE utf8mb4_general_ci " +
                    "JOIN doctors doc ON p.doctor_id = doc.doctor_id " +
                    "WHERE p.status = 'Pending' " +
                    "ORDER BY p.issued_at DESC";

            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);

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
            conn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Database Error: " + e.getMessage(),
                    "Query Failed", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        // ... (Keep existing Table and DB loading code)

        JScrollPane scroll = new JScrollPane(table);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(new LineBorder(new Color(255, 255, 255, 80)));

        // Create a container for the buttons to sit side-by-side
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0)); // 1 row, 2 columns, 15px gap
        buttonPanel.setOpaque(false);
        buttonPanel.setPreferredSize(new Dimension(0, 55)); // Fixed height for the bottom bar

        // --- BUTTON 1: MARK AS DISPENSED ---
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
                    Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/hms", "root", "eyob4791");
                    String updateQuery = "UPDATE prescriptions SET status = 'Done' WHERE prescription_id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(updateQuery);
                    pstmt.setInt(1, prescriptionId);

                    if (pstmt.executeUpdate() > 0) {
                        model.removeRow(selectedRow); // Remove from UI
                        JOptionPane.showMessageDialog(mainFrame, "Status updated to Done.");
                    }
                    conn.close();
                } catch (SQLException ex) { ex.printStackTrace(); }
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Select a prescription first!");
            }
        });

        // --- BUTTON 2: ADD BILL ---
        // --- BUTTON 2: ADD BILL ---
        JButton billBtn = new JButton("Add Bill");
        billBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        billBtn.setBackground(NAVY);
        billBtn.setForeground(Color.WHITE);
        billBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        billBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                // 1. Get data from the table
                int prescriptionId = (int) model.getValueAt(selectedRow, 0);
                String patientName = (String) model.getValueAt(selectedRow, 1);

                // 2. Ask for the Medication Fee
                String feeStr = JOptionPane.showInputDialog(mainFrame,
                        "Enter Drug Fee for " + patientName + ":", "Billing - Prescription #" + prescriptionId,
                        JOptionPane.QUESTION_MESSAGE);

                if (feeStr != null && !feeStr.isEmpty()) {
                    try {
                        double medFee = Double.parseDouble(feeStr);

                        // 3. Connect and Insert into billing table
                        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/hms", "root", "eyob4791");

                        // First, we need the patient_id (since we only have the name in the table)
                        String getPatientId = "SELECT patient_id FROM prescriptions WHERE prescription_id = ?";
                        PreparedStatement pst1 = conn.prepareStatement(getPatientId);
                        pst1.setInt(1, prescriptionId);
                        ResultSet rs = pst1.executeQuery();

                        if (rs.next()) {
                            String patientId = rs.getString("patient_id");

                            // SQL to insert into billing table
                            String billQuery = "INSERT INTO billing (patient_id, bill_date, other_fee, total_amount, payment_status, created_by) " +
                                    "VALUES (?, CURDATE(), ?, ?, 'Pending', ?)";

                            PreparedStatement pst2 = conn.prepareStatement(billQuery);
                            pst2.setString(1, patientId);
                            pst2.setDouble(2, medFee);
                            pst2.setDouble(3, medFee); // Total = medFee (assuming other fees are 0 for now)
                            pst2.setString(4, usename); // From your class variable

                            int result = pst2.executeUpdate();
                            if (result > 0) {
                                JOptionPane.showMessageDialog(mainFrame, "Bill Created Successfully for " + patientName);
                            }
                        }
                        conn.close();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(mainFrame, "Invalid Amount! Please enter a number.");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(mainFrame, "DB Error: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Select a row to generate a bill!");
            }
        });

        // Add buttons to the sub-panel
        buttonPanel.add(billBtn);
        buttonPanel.add(processBtn);

        overlay.add(title, BorderLayout.NORTH);
        overlay.add(scroll, BorderLayout.CENTER);
        overlay.add(buttonPanel, BorderLayout.SOUTH); // Add the panel containing both buttons

        contentPanel.add(overlay);
        refreshPanel();
    }

    private void refreshPanel() {
        contentPanel.revalidate();
        contentPanel.repaint(); }
    @Override void logout() { new LoginPage().setVisible(true); }

//    public static void main(String args[]){
//        SwingUtilities.invokeLater(() -> new Pharmacy("9","eyob").showDashboard());
//    }
}