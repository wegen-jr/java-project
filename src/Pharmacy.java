import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Pharmacy extends staffUser {
    // Styling Colors
    private final Color NAVY = new Color(20, 33, 61);
    private final Color TEAL = new Color(42, 157, 143);
    private final Color BACKGROUND = new Color(240, 242, 245);

    private JFrame mainFrame;
    private JPanel contentPanel; // The dynamic area that changes based on selection

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

        // 1. Sidebar Navigation
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(NAVY);
        sidebar.setPreferredSize(new Dimension(260, 0));

        JLabel logoLabel = new JLabel("PHARMA-SYNC v1.0", SwingConstants.CENTER);
        logoLabel.setForeground(TEAL);
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoLabel.setBorder(new EmptyBorder(40, 0, 40, 0));
        sidebar.add(logoLabel);

        // Nav Buttons
        sidebar.add(createNavBtn("Overview", e -> showOverview()));
        sidebar.add(createNavBtn("Prescription Queue", e -> showPrescriptionQueue()));
        sidebar.add(createNavBtn("Inventory Manager", e -> showInventoryManager()));
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(createNavBtn("Logout", e -> logout()));

        // 2. Main Content Area
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BACKGROUND);

        mainFrame.add(sidebar, BorderLayout.WEST);
        mainFrame.add(contentPanel, BorderLayout.CENTER);

        showOverview(); // Default view
        mainFrame.setVisible(true);
    }

    // --- WORKSPACE VIEWS ---

    private void showOverview() {
        contentPanel.removeAll();

        JPanel header = new JPanel(new GridLayout(1, 3, 20, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(30, 30, 30, 30));

        header.add(createStatCard("Pending Prescriptions", "12", TEAL));
        header.add(createStatCard("Low Stock Alerts", "4", new Color(230, 57, 70)));
        header.add(createStatCard("Daily Dispensed", "48", NAVY));

        contentPanel.add(header, BorderLayout.NORTH);

        // Quick view table
        String[] cols = {"Recent Activity", "Time", "Status"};
        Object[][] data = {{"Aspirin Dispensed", "10:30 AM", "Completed"}, {"Stock Update: Insulin", "09:15 AM", "Verified"}};
        JTable table = new JTable(new DefaultTableModel(data, cols));
        contentPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        refreshPanel();
    }

    private void showPrescriptionQueue() {
        contentPanel.removeAll();
        JLabel title = new JLabel("Incoming Prescription Requests");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] cols = {"RX ID", "Patient Name", "Medication", "Doctor", "Priority"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        // In real app: model = PharmacyDAO.getPendingRX();
        model.addRow(new Object[]{"RX-102", "Sarah Connor", "Amoxicillin", "Dr. Silberman", "Urgent"});

        JTable table = new JTable(model);
        table.setRowHeight(40);

        JButton processBtn = new JButton("Mark as Dispensed");
        processBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row != -1) JOptionPane.showMessageDialog(mainFrame, "Prescription Processed & Inventory Updated!");
        });

        contentPanel.add(title, BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        contentPanel.add(processBtn, BorderLayout.SOUTH);

        refreshPanel();
    }

    private void showInventoryManager() {
        contentPanel.removeAll();
        // Similar to Prescription Queue but with Stock columns (Qty, Expiry, Category)
        contentPanel.add(new JLabel("Inventory Management System"), BorderLayout.NORTH);
        refreshPanel();
    }

    // --- HELPER METHODS ---

    private JButton createNavBtn(String text, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(260, 50));
        btn.setForeground(Color.WHITE);
        btn.setBackground(NAVY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.addActionListener(action);
        return btn;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new LineBorder(new Color(230, 230, 230), 1));

        JLabel valLbl = new JLabel(value, SwingConstants.CENTER);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 40));
        valLbl.setForeground(color);

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        card.add(valLbl, BorderLayout.CENTER);
        card.add(titleLbl, BorderLayout.SOUTH);
        return card;
    }

    private void refreshPanel() {
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    @Override
    void logout() {
        mainFrame.dispose();
        System.out.println("User " + usename + " logged out.");
        // Redirect to Login Screen
    }
    public static void main(String args[]){
        new Pharmacy("1","eyob").showDashboard();
    }
}