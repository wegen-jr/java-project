import Database.DatabaseConnection;
import Database.AuthenticationDAO;
import Database.DatabaseSetup;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

public class LoginPage extends JFrame {

    // Styling Constants to match your design
    private final Color titleColor = new Color(2, 48, 71); // Navy Blue
    private final Color hoverColor = new Color(6, 75, 110); // Lighter Blue for hover
    private final Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);

    public LoginPage() {
        // Initialize database
        initializeDatabase();

        setTitle("HMS - Hospital Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);

        // Ensure the base frame is white
        this.getContentPane().setBackground(Color.WHITE);

        // 1. Background Image Panel
        ImageIcon backgroundIcon = new ImageIcon("assets/homePage.jpg");
        JPanel mainBackgroundPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };

        // 2. Center Container
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // --- Title Section ---
        JLabel title = new JLabel("WELCOME TO DEMO HOSPITAL");
        title.setFont(new Font("Segoe UI", Font.BOLD, 48));
        title.setForeground(titleColor);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel titleWrapper = new JPanel();
        titleWrapper.setBackground(new Color(255, 255, 255, 180)); // Semi-transparent white
        titleWrapper.setBorder(new EmptyBorder(10, 20, 10, 20));
        titleWrapper.add(title);

        // --- Login Form Panel ---
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(400, 280));
        form.setMaximumSize(new Dimension(400, 280));
        form.setBackground(new Color(255, 255, 255, 220)); // Stronger white for the form
        form.setBorder(BorderFactory.createLineBorder(titleColor, 2));

        // Username Row
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        namePanel.setOpaque(false);
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        usernameLabel.setForeground(titleColor);
        JTextField uNameField = new JTextField(15);
        uNameField.setPreferredSize(new Dimension(0, 30));
        namePanel.add(usernameLabel);
        namePanel.add(uNameField);

        // Password Row
        JPanel passPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        passPanel.setOpaque(false);
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        passwordLabel.setForeground(titleColor);
        JPasswordField passField = new JPasswordField(15);
        passField.setPreferredSize(new Dimension(0, 30));
        passPanel.add(passwordLabel);
        passPanel.add(passField);

        // Button Row
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        btnPanel.setOpaque(false);

        // --- Modern Login Button ---
        JButton loginBtn = new JButton("LOGIN");
        loginBtn.setBackground(titleColor);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginBtn.setPreferredSize(new Dimension(130, 40));
        loginBtn.setFocusPainted(false);
        loginBtn.setBorder(null);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover Effect Logic
        loginBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { loginBtn.setBackground(hoverColor); }
            @Override
            public void mouseExited(MouseEvent e) { loginBtn.setBackground(titleColor); }
        });

        // --- Forgot Password Button ---
        JButton forgotBtn = new JButton("Forgot Password?");
        forgotBtn.setForeground(titleColor);
        forgotBtn.setContentAreaFilled(false);
        forgotBtn.setBorderPainted(false);
        forgotBtn.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        forgotBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnPanel.add(loginBtn);

        // Add rows to form
        form.add(Box.createVerticalGlue());
        form.add(namePanel);
        form.add(passPanel);
        form.add(btnPanel);
        form.add(forgotBtn);
        forgotBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.add(Box.createVerticalGlue());

        // Login Logic
        loginBtn.addActionListener(e -> {
            String inputUser = uNameField.getText();
            String inputPass = new String(passField.getPassword());

            if (inputUser.isEmpty() || inputPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter credentials!");
                return;
            }

            if (!DatabaseConnection.testConnection()) {
                JOptionPane.showMessageDialog(this, "Database Connection Failed!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Map<String, Object> userData = AuthenticationDAO.authenticateUser(inputUser, inputPass);

            if (userData != null) {
                String role = (String) userData.get("role");
                JOptionPane.showMessageDialog(this, "Login Successful!");
                this.dispose();

                switch (role.toUpperCase().trim()) {
                    case "DOCTOR":
                        new Doctor(inputUser).showDashboard();
                        break;
                    case "RECEPTION":
                        new receptionist().showDashboard();
                        break;
                    default:
                        JOptionPane.showMessageDialog(this, "Dashboard for " + role + " is under construction.");
                        new LoginPage().setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password");
            }
        });

        forgotBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Contact IT for password reset."));

        // Assemble
        centerPanel.add(titleWrapper);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        centerPanel.add(form);
        mainBackgroundPanel.add(centerPanel);
        add(mainBackgroundPanel);

        setLocationRelativeTo(null);
    }

    private void initializeDatabase() {
        try {
            DatabaseSetup.initializeDatabase();
        } catch (Exception e) {
            System.err.println("DB Init Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Look and Feel removed to prevent system overriding your colors
        SwingUtilities.invokeLater(() -> {
            new LoginPage().setVisible(true);
        });
    }
}