import Database.AdminDAO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;

public class Admin extends staffUser {
    private JFrame frame;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JButton lastSelectedBtn = null;

    // Theme Colors
    private final Color NAVY_DARK = new Color(1, 22, 39);
    private final Color TEAL_ACCENT = new Color(33, 158, 188);
    private final Color SUCCESS_GREEN = new Color(46, 196, 182);
    private final Color WARNING_ORANGE = new Color(255, 159, 28);
    private final Color DANGER_RED = new Color(231, 76, 60);

    public Admin(String username) {
        this.usename = username;

    }

    @Override
    void showDashboard() {
        frame = new JFrame("HMS - Master Admin Console");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // --- SIDEBAR ---
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(NAVY_DARK);
        sidebar.setPreferredSize(new Dimension(280, 0));

        // Sidebar Navigation
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);

        JLabel logo = new JLabel("HMS ADMIN");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setBorder(new EmptyBorder(40, 35, 40, 0));
        navPanel.add(logo);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Add Menu Items
        addNavButton(navPanel, "Staff Accounts", "AUTH", "authentication", "id");
        addNavButton(navPanel, "Patient Registry", "PATIENTS", "patients", "patient_id");
        addNavButton(navPanel, "Doctor List", "DOCTORS", "doctors", "doctor_id");
        addNavButton(navPanel, "Appointments", "APPS", "appointments", "appointment_id");
        addNavButton(navPanel, "Billing", "BILL", "billing", "bill_id");

        // --- LOGOUT AT BOTTOM ---
        JPanel bottomSidebar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 30));
        bottomSidebar.setOpaque(false);
        JButton logoutBtn = createLogoutButton();
        bottomSidebar.add(logoutBtn);

        sidebar.add(navPanel, BorderLayout.CENTER);
        sidebar.add(bottomSidebar, BorderLayout.SOUTH);

        frame.add(sidebar, BorderLayout.WEST);
        frame.add(contentPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private JPanel createModernCrudPanel(String tableName, String pkName) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 250));

        // --- DATA TABLE ---
        Vector<Vector<Object>> data = new Vector<>();
        Vector<String> columns = new Vector<>();
        AdminDAO.loadTableData(tableName, data, columns);

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(model);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(20, 20, 0, 20));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        // --- BOTTOM RIGHT ACTION BUTTONS ---
        JPanel actionContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        actionContainer.setOpaque(false);
        actionContainer.setBorder(new EmptyBorder(0, 0, 20, 20));

        JButton btnAdd = createActionButton("+ NEW ENTRY", SUCCESS_GREEN);
        JButton btnEdit = createActionButton("✎ EDIT", WARNING_ORANGE);
        JButton btnDelete = createActionButton("🗑 DELETE", DANGER_RED);

        // Functional Logic
        btnAdd.addActionListener(e -> showCreateDialog(tableName, columns, model));
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) showEditDialog(tableName, columns, model, row, pkName);
            else JOptionPane.showMessageDialog(frame, "Select a record to edit.");
        });
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                Object id = model.getValueAt(row, 0);
                if (JOptionPane.showConfirmDialog(frame, "Delete ID " + id + "?") == 0) {
                    AdminDAO.deleteRecord(tableName, pkName, id);
                    refreshTable(tableName, model, columns);
                }
            }
        });

        actionContainer.add(btnAdd);
        actionContainer.add(btnEdit);
        actionContainer.add(btnDelete);

        mainPanel.add(scroll, BorderLayout.CENTER);
        mainPanel.add(actionContainer, BorderLayout.SOUTH);

        return mainPanel;
    }

    private void showCreateDialog(String table, Vector<String> cols, DefaultTableModel model) {
        JPanel form = new JPanel(new GridLayout(0, 1, 5, 5));
        Map<String, JTextField> inputs = new HashMap<>();

        for (String col : cols) {
            // SKIP ID COLUMN FOR CREATION
            if (col.toLowerCase().contains("id")) continue;

            form.add(new JLabel("Enter " + col.replace("_", " ").toUpperCase() + ":"));
            JTextField tf = new JTextField();
            inputs.put(col, tf);
            form.add(tf);
        }

        int res = JOptionPane.showConfirmDialog(frame, form, "ADD NEW RECORD", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            Map<String, Object> dataToInsert = new HashMap<>();

            // Collect data from fields
            for (Map.Entry<String, JTextField> entry : inputs.entrySet()) {
                String val = entry.getValue().getText().trim();
                // If empty, we can put null or handle validation here
                dataToInsert.put(entry.getKey(), val.isEmpty() ? null : val);
            }

            // Call the DAO
            if (AdminDAO.createRecord(table, dataToInsert)) {
                JOptionPane.showMessageDialog(frame, "Record created successfully!");
                // Refresh the table view to show the new record
                refreshTable(table, model, cols);
            } else {
                JOptionPane.showMessageDialog(frame, "Error: Could not save record.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditDialog(String table, Vector<String> cols, DefaultTableModel model, int row, String pk) {
        JPanel form = new JPanel(new GridLayout(0, 1, 5, 5));
        Map<String, JTextField> inputs = new HashMap<>();

        // Get the Primary Key Value (usually in the first column, index 0)
        Object pkValue = model.getValueAt(row, 0);

        for (int i = 0; i < cols.size(); i++) {
            String colName = cols.get(i);
            form.add(new JLabel("Update " + colName.toUpperCase() + ":"));

            // Get existing value to pre-fill the text field
            String existingValue = (model.getValueAt(row, i) == null) ? "" : model.getValueAt(row, i).toString();
            JTextField tf = new JTextField(existingValue);

            // LOCK THE ID FIELD: Prevent user from changing the Primary Key
            if (colName.equalsIgnoreCase(pk)) {
                tf.setEditable(false);
                tf.setBackground(new Color(240, 240, 240)); // Visual cue it's locked
            }

            inputs.put(colName, tf);
            form.add(tf);
        }

        int res = JOptionPane.showConfirmDialog(frame, form, "UPDATE RECORD ID: " + pkValue, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            boolean anyError = false;

            // Loop through all columns to check for changes
            for (int i = 0; i < cols.size(); i++) {
                String colName = cols.get(i);

                // Skip the Primary Key column itself
                if (colName.equalsIgnoreCase(pk)) continue;

                String newValue = inputs.get(colName).getText().trim();
                Object oldValue = model.getValueAt(row, i);
                String oldValueStr = (oldValue == null) ? "" : oldValue.toString();

                // ONLY call the database if the value was actually changed
                if (!newValue.equals(oldValueStr)) {
                    boolean success = AdminDAO.updateRecord(table, pk, pkValue, colName, newValue);
                    if (!success) {
                        anyError = true;
                    }
                }
            }

            if (!anyError) {
                JOptionPane.showMessageDialog(frame, "Record updated successfully!");
                // Refresh the table UI
                Vector<String> colNames = new Vector<>(cols);
                refreshTable(table, model, colNames);
            } else {
                JOptionPane.showMessageDialog(frame, "Some fields failed to update. Check console for details.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void styleTable(JTable table) {
        table.setRowHeight(45);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(2, 2));

        JTableHeader header = table.getTableHeader();
        header.setBackground(TEAL_ACCENT);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 45));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private JButton createActionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI Bold", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createLogoutButton() {
        JButton btn = new JButton("LOGOUT");
        btn.setPreferredSize(new Dimension(200, 45));
        btn.setBackground(DANGER_RED);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(DANGER_RED, 1, true));
        btn.addActionListener(e -> logout());
        return btn;
    }

    private void addNavButton(JPanel container, String title, String card, String table, String pk) {
        JButton btn = new JButton(title);
        btn.setMaximumSize(new Dimension(280, 50));
        btn.setBackground(NAVY_DARK);
        btn.setForeground(new Color(180, 180, 180));
        btn.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        btn.setBorder(new EmptyBorder(0, 35, 0, 0));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);

        btn.addActionListener(e -> {
            cardLayout.show(contentPanel, card);
            if(lastSelectedBtn != null) lastSelectedBtn.setForeground(new Color(180, 180, 180));
            btn.setForeground(Color.WHITE);
            lastSelectedBtn = btn;
        });

        container.add(btn);
        contentPanel.add(createModernCrudPanel(table, pk), card);
    }

    private void refreshTable(String tableName, DefaultTableModel model, Vector<String> cols) {
        Vector<Vector<Object>> data = new Vector<>();
        AdminDAO.loadTableData(tableName, data, cols);
        model.setDataVector(data, cols);
    }

    @Override void logout() { frame.dispose(); new LoginPage().setVisible(true); }
}