import Database.DatabaseConnection;
import Database.AuthenticationDAO;
import Database.DatabaseSetup;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

public class LoginPage extends JFrame {

    // Your original design colors
    private final Color titleColor = new Color(2, 48, 71);
    private final Color hoverColor = new Color(6, 75, 110); // Subtle hover variant

    public LoginPage() {
        initializeDatabase();

        setTitle("HMS - Hospital Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);

        // --- 1. Background Image Panel (Exactly your original logic) ---
        ImageIcon backgroundIcon = new ImageIcon("assets/homePage.jpg");
        JPanel mainBackgroundPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };

        // --- 2. Center Container ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Title
        JLabel title = new JLabel("WELCOME TO DEMO HOSPITAL");
        title.setFont(new Font("SansSerif", Font.BOLD, 48));
        title.setForeground(titleColor);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel titleWrapper = new JPanel();
        titleWrapper.setBackground(new Color(255, 255, 255, 150));
        titleWrapper.add(title);

        // --- 3. Login Form Panel (Restored your BevelBorder) ---
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(350, 200));
        form.setMaximumSize(new Dimension(350, 200));
        form.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        form.setBackground(new Color(255, 255, 255, 200));

        // Username Row
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
        namePanel.setOpaque(false);
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(titleColor); // Added to ensure visibility
        JTextField uNameField = new JTextField(15);
        namePanel.add(usernameLabel);
        namePanel.add(uNameField);

        // Password Row
        JPanel passPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 9, 10));
        passPanel.setOpaque(false);
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(titleColor); // Added to ensure visibility
        JPasswordField passField = new JPasswordField(15);
        passPanel.add(passwordLabel);
        passPanel.add(passField);

        // Button Row
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        btnPanel.setOpaque(false);

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(titleColor);
        loginBtn.setForeground(Color.WHITE); // Changed from light_gray for better visibility
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false); // Keeps it clean
        loginBtn.setOpaque(true);

        // HOVER EFFECT logic
        loginBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { loginBtn.setBackground(hoverColor); }
            @Override
            public void mouseExited(MouseEvent e) { loginBtn.setBackground(titleColor); }
        });

        JButton forgotBtn = new JButton("Forgot Password");
        forgotBtn.setBackground(titleColor);
        forgotBtn.setForeground(Color.WHITE);
        forgotBtn.setFocusPainted(false);
        forgotBtn.setBorderPainted(false);
        forgotBtn.setOpaque(true);

        btnPanel.add(loginBtn);
        btnPanel.add(forgotBtn);

        // Assemble rows to form
        form.add(Box.createVerticalGlue());
        form.add(namePanel);
        form.add(passPanel);
        form.add(btnPanel);
        form.add(Box.createVerticalGlue());

        // --- 4. Database Logic ---
        loginBtn.addActionListener(e -> {
            String inputUser = uNameField.getText();
            String inputPass = new String(passField.getPassword());

            if (inputUser.isEmpty() || inputPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fields cannot be empty!");
                return;
            }

            Map<String, Object> userData = AuthenticationDAO.authenticateUser(inputUser, inputPass);
            if (userData != null) {
                String role = ((String) userData.get("role")).trim();
                JOptionPane.showMessageDialog(this, "Login Successful!");
                this.dispose();

                if (role.equalsIgnoreCase("DOCTOR")) {
                    new Doctor(inputUser).showDashboard();
                } else if (role.equalsIgnoreCase("RECEPTION")) {
                    new receptionist().showDashboard();
                } else {
                    JOptionPane.showMessageDialog(this, "Dashboard for " + role + " not found.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Assemble everything
        centerPanel.add(titleWrapper);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        centerPanel.add(form);

        mainBackgroundPanel.add(centerPanel);
        add(mainBackgroundPanel);

        setLocationRelativeTo(null);
    }

    private void initializeDatabase() {
        try {
            DatabaseSetup.initializeDatabase();
        } catch (Exception e) {
            System.err.println("DB Setup failed.");
        }
    }

    public static void main(String[] args) {
        // SYSTEM LOOK AND FEEL REMOVED: This protects your design
        SwingUtilities.invokeLater(() -> {
            new LoginPage().setVisible(true);
        });
    }
}