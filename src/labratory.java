import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class labratory {
    private static final String URL = "jdbc:mysql://localhost:3306/HMS";
    private static final String DB_USER = "phpmyadmin";
    private static final String DB_PASS = "wegen@1996";

    private JFrame frame;
    public String username;
    private String labTechnicianId, fullName, contactNumber, email;
    private Font navFont = new Font("SansSerif", Font.BOLD, 12);
    private JLabel dateTimeLabel; // Label for the timestamp

    public void showDashboard() {
        loadLabtechnicianData();
        frame = new JFrame("HMS - Laboratory Department");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main background panel with IMAGE BACKGROUND
        JPanel mainBackgroundPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon bgIcon = new ImageIcon("assets/lab1.png");
                if (bgIcon.getImage() != null) {
                    g.drawImage(bgIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(new Color(240, 245, 249));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        // Left Sidebar Navigation
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(new Color(2, 48, 71));
        leftPanel.setPreferredSize(new Dimension(250, 0));
        leftPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1, true));

        // Header Section
        JPanel portalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        portalPanel.setOpaque(false);
        portalPanel.setMaximumSize(new Dimension(250, 60));

        ImageIcon portalIcon = new ImageIcon(new ImageIcon("assets/portal.png").getImage()
                .getScaledInstance(25, 25, Image.SCALE_SMOOTH));
        JLabel portalHeader = new JLabel("Lab Portal", portalIcon, JLabel.LEFT);
        portalHeader.setForeground(Color.WHITE);
        portalHeader.setFont(new Font("Arial", Font.BOLD, 18));
        portalHeader.setIconTextGap(10);
        portalPanel.add(portalHeader);

        // Navigation Items
        JPanel navDash = createNavItem("Dashboard", "assets/dashboard.png", navFont, () -> {});
        JPanel navLab  = createNavItem("Pending Requests", "assets/observation.png", navFont, this::showLabRequestsDashboard);
        JPanel navProf = createNavItem("My Profile", "assets/user.png", navFont, this::showProfileWindow);

        JPanel navLogout = createNavItem("Logout", "assets/logout.png", navFont, () -> {
            int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) frame.dispose();
        });

        leftPanel.add(portalPanel);
        leftPanel.add(new JSeparator());
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(navDash);
        leftPanel.add(Box.createVerticalStrut(2));
        leftPanel.add(navLab);
        leftPanel.add(Box.createVerticalStrut(2));
        leftPanel.add(navProf);
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(new JSeparator());
        leftPanel.add(navLogout);
        leftPanel.add(Box.createVerticalStrut(10));

        mainBackgroundPanel.add(leftPanel, BorderLayout.WEST);

        // Right side content area
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        // Welcome Message
        JLabel welcome = new JLabel("Welcome, " + (fullName != null ? fullName : "Technician"));
        welcome.setFont(new Font("Arial", Font.BOLD, 40));
        welcome.setForeground(new Color(0, 0, 0));
        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(welcome, gbc);

        // --- TIME STAMP LOGIC ---
        dateTimeLabel = new JLabel();
        dateTimeLabel.setFont(new Font("SansSerif", Font.ITALIC, 18));
        dateTimeLabel.setForeground(new Color(50, 50, 50));
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 0, 0);
        centerPanel.add(dateTimeLabel, gbc);

        // Timer to update time every second
        Timer timer = new Timer(1000, e -> updateDateTime());
        timer.start();
        updateDateTime(); // Initial call

        mainBackgroundPanel.add(centerPanel, BorderLayout.CENTER);

        frame.add(mainBackgroundPanel);
        frame.setVisible(true);
    }

    // Helper to update the label text
    private void updateDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy  |  hh:mm:ss a");
        dateTimeLabel.setText(sdf.format(new Date()));
    }

    private JPanel createNavItem(String text, String iconPath, Font font, Runnable action) {
        Color bg = new Color(2, 48, 71);
        Color hover = new Color(6, 75, 110);
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        panel.setBackground(bg);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.setMaximumSize(new Dimension(250, 50));

        JLabel iconLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(new ImageIcon(iconPath).getImage()
                    .getScaledInstance(20, 20, Image.SCALE_SMOOTH));
            iconLabel.setIcon(icon);
        } catch (Exception e) {}

        JLabel textLabel = new JLabel(text);
        textLabel.setForeground(Color.WHITE);
        textLabel.setFont(font);

        panel.add(iconLabel);
        panel.add(textLabel);

        MouseAdapter navListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { action.run(); }
            @Override
            public void mouseEntered(MouseEvent e) { panel.setBackground(hover); }
            @Override
            public void mouseExited(MouseEvent e) { panel.setBackground(bg); }
        };

        panel.addMouseListener(navListener);
        return panel;
    }

    private void loadLabtechnicianData() {
        if (username == null) return;
        try (Connection con = DriverManager.getConnection(URL, DB_USER, DB_PASS)) {
            String query = "SELECT * FROM Lab_technician WHERE username = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                labTechnicianId = rs.getString("Labtechnician_id");
                fullName = rs.getString("full_name");
                contactNumber = rs.getString("contact_number");
                email = rs.getString("email");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    void showLabRequestsDashboard() {
        JFrame reqFrame = new JFrame("Incoming Lab Requests");
        reqFrame.setSize(900, 500);
        reqFrame.setLocationRelativeTo(null);

        String[] cols = {"ID", "Patient", "Test Type", "Priority", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(2, 48, 71));
        table.getTableHeader().setForeground(Color.WHITE);

        try (Connection con = DriverManager.getConnection(URL, DB_USER, DB_PASS)) {
            String query = "SELECT lr.request_id, p.full_name, lr.test_type, lr.priority, lr.status " +
                    "FROM lab_requests lr JOIN patients p ON lr.patient_id = p.patient_id " +
                    "WHERE lr.status = 'Pending'";
            ResultSet rs = con.createStatement().executeQuery(query);
            while(rs.next()) {
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)});
            }
        } catch (SQLException e) { e.printStackTrace(); }

        JButton updateBtn = new JButton("Process Selected Request");
        updateBtn.setBackground(new Color(2, 48, 71));
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setFont(new Font("Arial", Font.BOLD, 14));

        updateBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row != -1) {
                String reqId = table.getValueAt(row, 0).toString();
                String result = JOptionPane.showInputDialog("Enter Lab Results for Request #" + reqId + ":");
                if(result != null && !result.trim().isEmpty()) {
                    submitResult(reqId, result);
                    reqFrame.dispose();
                    showLabRequestsDashboard();
                }
            } else {
                JOptionPane.showMessageDialog(reqFrame, "Please select a request from the table.");
            }
        });

        reqFrame.add(new JScrollPane(table), BorderLayout.CENTER);
        reqFrame.add(updateBtn, BorderLayout.SOUTH);
        reqFrame.setVisible(true);
    }

    private void submitResult(String reqId, String result) {
        try (Connection con = DriverManager.getConnection(URL, DB_USER, DB_PASS)) {
            String sql = "UPDATE lab_requests SET result_details = ?, status = 'Completed' WHERE request_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, result);
            pst.setString(2, reqId);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(null, "Results submitted successfully and sent to Doctor!");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    void showProfileWindow() {
        JFrame profFrame = new JFrame("My Profile");
        profFrame.setSize(400, 300);
        profFrame.setLocationRelativeTo(null);

        JPanel card = new JPanel(new GridLayout(4, 2, 10, 20));
        card.setBorder(new EmptyBorder(30, 30, 30, 30));

        card.add(new JLabel("Staff ID:")); card.add(new JLabel(labTechnicianId));
        card.add(new JLabel("Full Name:")); card.add(new JLabel(fullName));
        card.add(new JLabel("Contact:")); card.add(new JLabel(contactNumber));
        card.add(new JLabel("Email:")); card.add(new JLabel(email));

        profFrame.add(card);
        profFrame.setVisible(true);
    }

    public static void main(String[] args) {
        labratory lab = new labratory();
        lab.username = "yabu";
        lab.showDashboard();
    }
}