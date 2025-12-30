import Database.DatabaseConnection;
import Database.AuthenticationDAO;
import Database.DatabaseSetup;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.Map;

public class LoginPage extends JFrame {

    public LoginPage() {
        // Initialize database first
        initializeDatabase();

        setTitle("HMS - Hospital Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);

        // Background Image Panel
        ImageIcon backgroundIcon = new ImageIcon("assets/homePage.jpg");
        JPanel mainBackgroundPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };

        // Styling
        Font titleFont = new Font("SansSerif", Font.BOLD, 48);
        Color titleColor = new Color(2, 48, 71);

        // Center Container
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Title
        JLabel title = new JLabel("WELCOME TO DEMO HOSPITAL");
        title.setFont(titleFont);
        title.setForeground(titleColor);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel titleWrapper = new JPanel();
        titleWrapper.setBackground(new Color(255, 255, 255, 150));
        titleWrapper.add(title);

        // Login Form
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
        JTextField uNameField = new JTextField(15);
        namePanel.add(usernameLabel);
        namePanel.add(uNameField);

        // Password Row
        JPanel passPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 9, 10));
        passPanel.setOpaque(false);
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField(15);
        passPanel.add(passwordLabel);
        passPanel.add(passField);

        // Button Row
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        btnPanel.setOpaque(false);
        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(titleColor);
        loginBtn.setForeground(Color.LIGHT_GRAY);
        JButton forgotBtn = new JButton("Forgot Password");
        forgotBtn.setBackground(titleColor);
        forgotBtn.setForeground(Color.LIGHT_GRAY);
        btnPanel.add(loginBtn);
        btnPanel.add(forgotBtn);

        // Add rows to form
        form.add(Box.createVerticalGlue());
        form.add(namePanel);
        form.add(passPanel);
        form.add(btnPanel);
        form.add(Box.createVerticalGlue());

        // Login button action
        loginBtn.addActionListener(e -> {
            String inputUser = uNameField.getText();
            String inputPass = new String(passField.getPassword());

            if (inputUser.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username required!");
                return;
            } else if (inputPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password required!");
                return;
            }

            // Test database connection
            if (!DatabaseConnection.testConnection()) {
                JOptionPane.showMessageDialog(this,
                        "Cannot connect to database!\nPlease check MySQL server is running.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Authenticate user and fetch full_name
            Map<String, Object> userData = AuthenticationDAO.authenticateUser(inputUser, inputPass);

            if (userData != null) {
                String role = ((String) userData.get("role")).trim();
                String fullName = (String) userData.get("full_name"); // New field from table

                JOptionPane.showMessageDialog(this, "Login Successful!\nWelcome " + fullName);

                this.dispose();

                switch (role.toUpperCase()) {
                    case "DOCTOR":
//                        new Doctor(fullName).showDashboard();
                        break;
                    case "RECEPTIONIST":
                        new receptionist(fullName).showDashboard();
                        break;
                    case "ADMIN":
//                        new Admin(fullName).showDashboard();
                        break;
                    case "PHARMACIST":
//                        new Pharmacist(fullName).showDashboard();
                        break;
                    case "LABTECHNICIAN":
//                        new LabTechnician(fullName).showDashboard();
                        break;
                    default:
                        JOptionPane.showMessageDialog(this,
                                "Unknown role: " + role,
                                "Access Error",
                                JOptionPane.ERROR_MESSAGE);
                        new LoginPage().setVisible(true);
                }

            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
                uNameField.setText("");
                passField.setText("");
                uNameField.requestFocus();
            }
        });

        // Forgot Password
        forgotBtn.addActionListener(e -> {
            String username = JOptionPane.showInputDialog(this,
                    "Enter your username:", "Password Recovery", JOptionPane.QUESTION_MESSAGE);

            if (username != null && !username.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please contact system administrator\n" +
                                "to reset your password.",
                        "Password Recovery", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Assemble
        centerPanel.add(titleWrapper);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        centerPanel.add(form);

        mainBackgroundPanel.add(centerPanel);
        add(mainBackgroundPanel);

        setLocationRelativeTo(null);
    }

    private void initializeDatabase() {
        JDialog loadingDialog = new JDialog(this, "Initializing System", true);
        loadingDialog.setSize(300, 150);
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setLayout(new BorderLayout());

        JLabel loadingLabel = new JLabel("Setting up database...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        loadingDialog.add(loadingLabel, BorderLayout.CENTER);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        loadingDialog.add(progressBar, BorderLayout.SOUTH);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                DatabaseSetup.initializeDatabase();
                return null;
            }

            @Override
            protected void done() {
                loadingDialog.dispose();
            }
        };
        worker.execute();
        loadingDialog.setVisible(true);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception ignored){}
        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
    }
}
