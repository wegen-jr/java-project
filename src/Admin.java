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

        // Staff Accounts: No "New Entry" (canAdd = false)
        addNavButton(navPanel, "Staff Accounts", "AUTH", "authentication", "id", false, true, true);

        // Patient Registry: All operations allowed
        addNavButton(navPanel, "Patient Registry", "PATIENTS", "patients", "patient_id", true, true, false);

        // Doctor List: All operations allowed
        addNavButton(navPanel, "Doctor List", "DOCTORS", "doctors", "doctor_id", true, true, true);

        // Receptionist List: All operations allowed
        addNavButton(navPanel, "Receptionist List", "RECEPTIONISTS", "receptionists", "receptionist_id", true, true, true);

        // Pharmacist List: All operations allowed
        addNavButton(navPanel, "Pharmacist List", "PHARMACIST", "pharmacists", "pharmacist_id", true, true, true);

        // LabTechnician List: All operations allowed
        addNavButton(navPanel, "LabTechnician List", "LABTECHNICIAN", "laboratory_technician", "labtechnician_id", true, true, true);

        // Lab Requests: can only view
        addNavButton(navPanel, "Lab Requests", "LABREQUEST", "lab_requests", "request_id", false, false, false);

        // Appointments: can only view
        addNavButton(navPanel, "Appointments", "APPS", "appointments", "appointment_id", false, false, false);

        // Medical Records: can only view
        addNavButton(navPanel, "Medical Records", "MEDICALRECORD", "medical_records", "record_id", false, false, false);

        // Prescriptions: can only view
        addNavButton(navPanel, "Prescriptions", "MEDICALRECORD", "prescriptions", "prescription_id", false, false, false);

        // Billing: can only view
        addNavButton(navPanel, "Billing", "BILL", "billing", "bill_id", false, false, false);

        // Reception Logs: can only view
        addNavButton(navPanel, "Reception Logs", "RECEPTIONLOG", "reception_logs", "log_id", false, false, false);

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

    private JPanel createModernCrudPanel(String tableName, String pkName, boolean canAdd, boolean canEdit, boolean canDelete) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 250));

        String[] staffs = {"doctors", "receptionists", "pharmacists", "laboratory_technician"};

        // --- DATA TABLE SETUP ---
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

        // --- DYNAMIC BOTTOM RIGHT ACTION BUTTONS ---
        JPanel actionContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        actionContainer.setOpaque(false);

        // Only add the "New Entry" button if canAdd is true
        if (canAdd) {
            JButton btnAdd = createActionButton("+ NEW ENTRY", SUCCESS_GREEN);
            btnAdd.addActionListener(e -> showCreateDialog(tableName, columns, model));
            actionContainer.add(btnAdd);
        }

        if (canEdit) {
            JButton btnEdit = createActionButton("✎ EDIT", WARNING_ORANGE);
            btnEdit.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row != -1) showEditDialog(tableName, columns, model, row, pkName);
                else JOptionPane.showMessageDialog(frame, "Select a record to edit.");
            });
            actionContainer.add(btnEdit);
        }

        if (canDelete) {
            JButton btnDelete = createActionButton("🗑 DELETE", DANGER_RED);
            btnDelete.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row != -1) {
                    Object id = model.getValueAt(row, 0);
                    if (JOptionPane.showConfirmDialog(frame, "Delete ID " + id + "?") == 0) {
                        if(Arrays.asList(staffs).contains(tableName)) {
                            AdminDAO.deleteStaffWithAuth(tableName, pkName, id);
                        } else {
                        AdminDAO.deleteRecord(tableName, pkName, id);
                        }
                        refreshTable(tableName, model, columns);
                    }
                }
            });
            actionContainer.add(btnDelete);
        }

        JButton btnRefresh = createActionButton("↻ REFRESH", new Color(100, 116, 139)); // Slate Gray
        btnRefresh.addActionListener(e -> {
            refreshTable(tableName, model, columns);
            JOptionPane.showMessageDialog(frame, "Table synchronized with database.");
        });
        actionContainer.add(btnRefresh);

        mainPanel.add(scroll, BorderLayout.CENTER);
        mainPanel.add(actionContainer, BorderLayout.SOUTH);

        return mainPanel;
    }

    private void showCreateDialog(String table, Vector<String> cols, DefaultTableModel model) {
        // 1. Create a container with custom width
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setPreferredSize(new Dimension(550, 600)); // Increased width
        wrapper.setBackground(Color.WHITE);

        // 2. Form Panel with GridBagLayout for better spacing
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5); // Spacing between components
        int row = 0;

        Map<String, JComponent> staffFields = new HashMap<>();
        Map<String, JComponent> authFields = new HashMap<>();


        Map<String, String> roles = Map.of(
                "doctors", "DOCTOR",
                "receptionists", "RECEPTIONIST",
                "pharmacists", "PHARMACIST",
                "laboratory_technician", "LABTECHNICIAN"
        );



        // Section Header: Authentication
        if (table.equalsIgnoreCase("doctors") ||
                table.equalsIgnoreCase("receptionists") ||
                table.equalsIgnoreCase("pharmacists") ||
                table.equalsIgnoreCase("laboratory_technician")
        ) {
            addSectionHeader(form, gbc, "🔐 ACCOUNT CREDENTIALS", row++);

            row = addStyledComponent(form, gbc, "username", authFields, "", row);
            row = addStyledComponent(form, gbc, "password", authFields, "", row);

            // Separator
            gbc.gridx = 0; gbc.gridy = row++;
            gbc.gridwidth = 2;
            form.add(Box.createVerticalStrut(15), gbc);
            addSectionHeader(form, gbc, "👨‍⚕️ PROFESSIONAL DETAILS", row++);
        }

        // 3. Dynamic Fields
        for (String col : cols) {
            if (col.toLowerCase().contains("_id") ||
                col.equalsIgnoreCase("created_at")) {
                continue;
            }

            String labelName = col.replace("_", " ").toUpperCase();
            row = addStyledComponent(form, gbc, labelName, staffFields, "", row);
        }

        wrapper.add(new JScrollPane(form), BorderLayout.CENTER);

        // 4. Show Dialog
        int res = JOptionPane.showConfirmDialog(
                frame,
                wrapper,
                "Create New " + table.substring(0, 1).toUpperCase() + table.substring(1),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (res == JOptionPane.OK_OPTION) {
            boolean anyError = false;
            Map<String, String> authData = new HashMap<>();
            Map<String, String> staffData = new HashMap<>();

            authFields.forEach((k, comp) -> authData.put(k, ((JTextField) comp).getText().trim()));

            staffFields.forEach((k, comp) -> {
                String colKey = k.replace(" ", "_").toLowerCase();
                if (comp instanceof JTextField) {
                    staffData.put(colKey, ((JTextField) comp).getText().trim());
                } else if (comp instanceof JComboBox) {
                    staffData.put(colKey, ((JComboBox<?>) comp).getSelectedItem().toString());
                }
            });
            // ... call DAO ...


            boolean success = AdminDAO.createStaffWithAuth(authData, staffData, roles.get(table), table);
            if(!success) anyError = true;

            if (!anyError) {
                JOptionPane.showMessageDialog(frame, "Record created successfully!");
                refreshTable(table, model, cols);
            } else {
                JOptionPane.showMessageDialog(frame, "Error while creating a record.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditDialog(String table, Vector<String> cols, DefaultTableModel model, int row, String pk) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setPreferredSize(new Dimension(550, 600));
        wrapper.setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        int rowIdx = 0;

        Map<String, JComponent> inputs = new HashMap<>();
        Object pkValue = model.getValueAt(row, 0);

        addSectionHeader(form, gbc, "📝 EDITING RECORD: " + pkValue, rowIdx++);

        for (int i = 0; i < cols.size(); i++) {
            String colName = cols.get(i);

            // 1. Skip System fields
            if (colName.equalsIgnoreCase("created_at") || colName.equalsIgnoreCase("updated_at")) continue;

            // 2. USE THE HELPER ONLY (This adds both label and input)
            String currentVal = (model.getValueAt(row, i) == null) ? "" : model.getValueAt(row, i).toString();
            rowIdx = addStyledComponent(form, gbc, colName, inputs, currentVal, rowIdx);

            // 3. APPLY LOCKS to the component just created by the helper
            JComponent comp = inputs.get(colName);
            if (colName.equalsIgnoreCase(pk) || colName.equalsIgnoreCase("auth_id")) {
                if (comp instanceof JTextField) {
                    JTextField tf = (JTextField) comp;
                    tf.setEditable(false);
                    tf.setBackground(new Color(245, 245, 245));
                    tf.setForeground(Color.GRAY);
                    tf.setToolTipText("System ID cannot be modified");
                }
            }
        }

        wrapper.add(new JScrollPane(form), BorderLayout.CENTER);

        int res = JOptionPane.showConfirmDialog(frame, wrapper, "Edit Record Details",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            boolean anyError = false;
            boolean wasChanged = false;

            for (int i = 0; i < cols.size(); i++) {
                String colName = cols.get(i);
                if (colName.equalsIgnoreCase(pk) || colName.equalsIgnoreCase("auth_id") || colName.equalsIgnoreCase("created_at")) continue;

                String newValue = "";
                JComponent comp = inputs.get(colName);

                // Safety check in case component wasn't added to map
                if (comp == null) continue;

                if (comp instanceof JTextField) {
                    newValue = ((JTextField) comp).getText().trim();
                } else if (comp instanceof JComboBox) {
                    newValue = ((JComboBox<?>) comp).getSelectedItem().toString();
                }

                Object oldValue = model.getValueAt(row, i);
                String oldValueStr = (oldValue == null) ? "" : oldValue.toString();

                if (!newValue.equals(oldValueStr)) {
                    wasChanged = true;
                    boolean success = AdminDAO.updateRecord(table, pk, pkValue, colName, newValue);
                    if (!success) anyError = true;
                }
            }

            if (wasChanged && !anyError) {
                JOptionPane.showMessageDialog(frame, "Record updated successfully!");
                refreshTable(table, model, cols);
            } else if (anyError) {
                JOptionPane.showMessageDialog(frame, "Error updating some fields.", "Error", JOptionPane.ERROR_MESSAGE);
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

    private void addSectionHeader(JPanel panel, GridBagConstraints gbc, String text, int row) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEAL_ACCENT);
        label.setBorder(new EmptyBorder(10, 0, 5, 0));
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(label, gbc);
        gbc.gridwidth = 1; // Reset
    }

    private int addStyledComponent(JPanel panel, GridBagConstraints gbc, String colName, Map<String, JComponent> map, String existingValue, int row) {
        // Label Styling
        JLabel lbl = new JLabel(colName.replace("_", " ").toUpperCase() + ":");
        lbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        lbl.setForeground(new Color(70, 70, 70));
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        panel.add(lbl, gbc);

        JComponent inputField;

        // Check if the field should be a ComboBox
        if (colName.equalsIgnoreCase("availability")) {
            String[] options = {"Available", "Busy", "On Leave"};
            JComboBox<String> combo = new JComboBox<>(options);
            combo.setPreferredSize(new Dimension(250, 35));
            combo.setBackground(Color.WHITE);

            // If editing, set the current value
            if (existingValue != null && !existingValue.isEmpty()) {
                combo.setSelectedItem(existingValue);
            } else {
                combo.setSelectedItem("Available"); // Default
            }

            inputField = combo;
        } else if(colName.equalsIgnoreCase("shift type")) {
            String[] options = {"Day","Night","Rotating"};
            JComboBox<String> combo = new JComboBox<>(options);
            combo.setPreferredSize(new Dimension(250, 35));
            combo.setBackground(Color.WHITE);

            // If editing, set the current value
            if (existingValue != null && !existingValue.isEmpty()) {
                combo.setSelectedItem(existingValue);
            } else {
                combo.setSelectedItem("Day"); // Default
            }

            inputField = combo;
        } else if(colName.equalsIgnoreCase("status")) {
            String[] options = {"Active","On Leave","Inactive"};
            JComboBox<String> combo = new JComboBox<>(options);
            combo.setPreferredSize(new Dimension(250, 35));
            combo.setBackground(Color.WHITE);

            // If editing, set the current value
            if (existingValue != null && !existingValue.isEmpty()) {
                combo.setSelectedItem(existingValue);
            } else {
                combo.setSelectedItem("Active"); // Default
            }

            inputField = combo;
        }

        else {
            // Standard TextField
            JTextField tf = new JTextField(existingValue);
            tf.setPreferredSize(new Dimension(250, 35));
            tf.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(210, 210, 210), 1),
                    new EmptyBorder(5, 8, 5, 8)
            ));
            inputField = tf;
        }

        map.put(colName, inputField);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 0.7;
        panel.add(inputField, gbc);

        return row + 1;
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

    private void addNavButton(JPanel container, String title, String card, String table, String pk, boolean canAdd, boolean canEdit, boolean canDelete) {
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
        contentPanel.add(createModernCrudPanel(table, pk, canAdd, canEdit, canDelete), card);
    }

    private void refreshTable(String tableName, DefaultTableModel model, Vector<String> cols) {
        Vector<Vector<Object>> data = new Vector<>();
        AdminDAO.loadTableData(tableName, data, cols);

        // This line resets the table structure
        model.setDataVector(data, cols);

        // FIND THE TABLE: Since we need the JTable object to re-apply styles
        // We search for the table within the contentPanel
        Component[] comps = contentPanel.getComponents();
        for (Component c : comps) {
            if (c instanceof JPanel) {
                findAndRestyleTable((JPanel) c);
            }
        }
    }

    // Helper to find the table in the current card and fix it
    private void findAndRestyleTable(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JScrollPane) {
                JTable table = (JTable) ((JScrollPane) comp).getViewport().getView();
                applyTableStyling(table); // Re-apply the centering and colors
            }
        }
    }

    private void applyTableStyling(JTable table) {
        table.setRowHeight(45);
        // ... your other styles ...

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // This sets the default for the entire table even after data changes
        table.setDefaultRenderer(Object.class, centerRenderer);

        // Also apply to headers specifically
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    @Override void logout() { frame.dispose(); new LoginPage().setVisible(true); }
}