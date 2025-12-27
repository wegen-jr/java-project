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

        // 1. Background Image Panel
        ImageIcon backgroundIcon = new ImageIcon("assets/homePage.jpg");
        JPanel mainBackgroundPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };

        // 2. Styling Constants
        Font titleFont = new Font("SansSerif", Font.BOLD, 48);
        Color titleColor = new Color(2, 48, 71);

        // 3. Center Container (Holds Title and Form)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false); // Make transparent to see background

        // Title
        JLabel title = new JLabel("WELCOME TO DEMO HOSPITAL");
        title.setFont(titleFont);
        title.setForeground(titleColor);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel titleWrapper = new JPanel();
        titleWrapper.setBackground(new Color(255, 255, 255, 150)); // Semi-transparent white
        titleWrapper.add(title);

        // 4. Login Form Panel
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(350, 200));
        form.setMaximumSize(new Dimension(350, 200));
        form.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        form.setBackground(new Color(255, 255, 255, 200)); // Semi-transparent white

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

        // 5. Updated Database Logic using AuthenticationDAO
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

            // Test database connection first
            if (!DatabaseConnection.testConnection()) {
                JOptionPane.showMessageDialog(this,
                        "Cannot connect to database!\n" +
                                "Please check MySQL server is running.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Use AuthenticationDAO for login
            Map<String, Object> userData = AuthenticationDAO.authenticateUser(inputUser, inputPass);

            if (userData != null) {
                String role = (String) userData.get("role");

                if (role != null) {
                    role = role.trim();
                    JOptionPane.showMessageDialog(this, "Login Successful!");
                    this.dispose();

                    switch (role.toUpperCase()) {
                        case "RECEPTION":
                            new receptionist().showDashboard();
                            break;
                        case "DOCTOR":
                            //   new DoctorDashboard(fullName).setVisible(true);
                            new Doctor().showDashboard();
                            break;
                        case "PHARMACIST":
                            //   new PharmacistDashboard(fullName).setVisible(true);
                            JOptionPane.showMessageDialog(this, "Pharmacist dashboard coming soon!");
                            new LoginPage().setVisible(true);
                            break;
                        case "ADMIN":
                            //   new AdminDashboard(fullName).setVisible(true);
                            JOptionPane.showMessageDialog(this, "Admin dashboard coming soon!");
                            new LoginPage().setVisible(true);
                            break;
                        case "LABTECHNICIAN":
                            new labratory().showDashboard();
                            break;
                        default:
                            JOptionPane.showMessageDialog(this,
                                    "Unknown role: " + role,
                                    "Access Error",
                                    JOptionPane.ERROR_MESSAGE);
                            // Show login again
                            new LoginPage().setVisible(true);
                    }
                } else {
                    JOptionPane.showMessageDialog(this,
                            "No role assigned to user",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
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

        // Forgot Password button action
        forgotBtn.addActionListener(e -> {
            String username = JOptionPane.showInputDialog(this,
                    "Enter your username:",
                    "Password Recovery",
                    JOptionPane.QUESTION_MESSAGE);

            if (username != null && !username.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please contact system administrator\n" +
                                "to reset your password.",
                        "Password Recovery",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // 6. Assemble everything
        centerPanel.add(titleWrapper);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30))); // Space between title and form
        centerPanel.add(form);

        mainBackgroundPanel.add(centerPanel); // GridBagLayout centers this
        add(mainBackgroundPanel);

        // Center the frame
        setLocationRelativeTo(null);
    }

    private void initializeDatabase() {
        // Show loading dialog
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

        // Start database initialization in background
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    System.out.println("🔄 Initializing database...");
                    DatabaseSetup.initializeDatabase();
                    System.out.println("✅ Database initialization complete");
                } catch (Exception e) {
                    System.err.println("❌ Database initialization failed: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                loadingDialog.dispose();

                // Test database connection
                boolean connected = DatabaseConnection.testConnection();
                if (!connected) {
                    JOptionPane.showMessageDialog(LoginPage.this,
                            "⚠️  Database connection failed!\n" +
                                    "Some features may not work properly.\n\n" +
                                    "Please ensure:\n" +
                                    "1. MySQL server is running\n" +
                                    "2. Credentials are correct in DatabaseConnection.java\n" +
                                    "3. MySQL connector JAR is in classpath",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        };

        worker.execute();
        loadingDialog.setVisible(true);
    }

    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            LoginPage loginPage = new LoginPage();
            loginPage.setVisible(true);

        });
    }
}