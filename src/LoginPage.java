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

    public LoginPage() {
        initializeDatabase();

        setTitle("HMS - Hospital Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);

        ImageIcon backgroundIcon = new ImageIcon("assets/homePage.jpg");
        JPanel mainBackgroundPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };

        Font titleFont = new Font("SansSerif", Font.BOLD, 48);
        Color navyBlue = new Color(2, 48, 71);
        Color forceBlack = Color.BLACK;
        Color forceWhite = Color.WHITE;

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel title = new JLabel("WELCOME TO DEMO HOSPITAL");
        title.setFont(titleFont);
        title.setForeground(navyBlue);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel titleWrapper = new JPanel();
        titleWrapper.setBackground(new Color(255, 255, 255, 150));
        titleWrapper.add(title);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(350, 200));
        form.setMaximumSize(new Dimension(350, 200));
        form.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        form.setBackground(new Color(255, 255, 255, 200));

        // Username
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
        namePanel.setOpaque(false);
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(forceBlack);
        JTextField uNameField = new JTextField(15);
        uNameField.setBackground(forceWhite);
        uNameField.setForeground(forceBlack);
        uNameField.setCaretColor(forceBlack);
        namePanel.add(usernameLabel);
        namePanel.add(uNameField);

        // Password
        JPanel passPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 9, 10));
        passPanel.setOpaque(false);
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(forceBlack);
        JPasswordField passField = new JPasswordField(15);
        passField.setBackground(forceWhite);
        passField.setForeground(forceBlack);
        passField.setCaretColor(forceBlack);
        passPanel.add(passwordLabel);
        passPanel.add(passField);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        btnPanel.setOpaque(false);

        JButton loginBtn = new JButton("Login");
        applyNavClickEffect(loginBtn, navyBlue, forceWhite);

        JButton forgotBtn = new JButton("Forgot Password");
        applyNavClickEffect(forgotBtn, navyBlue, forceWhite);

        btnPanel.add(loginBtn);
        btnPanel.add(forgotBtn);

        form.add(Box.createVerticalGlue());
        form.add(namePanel);
        form.add(passPanel);
        form.add(btnPanel);
        form.add(Box.createVerticalGlue());

        // LOGIN ACTION WITH ALL SWITCH STATEMENTS
        // LOGIN ACTION WITH ALL SWITCH STATEMENTS
        loginBtn.addActionListener(e -> {
            String inputUser = uNameField.getText();
            String inputPass = new String(passField.getPassword());

            if (inputUser.isEmpty() || inputPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and Password required!");
                return;
            }

            if (!DatabaseConnection.testConnection()) {
                JOptionPane.showMessageDialog(this, "Database Connection Error!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Map<String, Object> userData = AuthenticationDAO.authenticateUser(inputUser, inputPass);

            if (userData != null) {
                // 1. EXTRACT DATA FROM MAP
                String role = ((String) userData.get("role")).trim();
                String fullName = (String) userData.get("full_name");

                // This extracts the ID from the database result to pass to the Doctor class
                int authId = (int) userData.get("auth_id");

                JOptionPane.showMessageDialog(this, "Login Successful!\nWelcome " + fullName);

                // 2. RESTORED SWITCH STATEMENTS
                switch (role.toUpperCase()) {
                    case "DOCTOR":
                        // Now we pass the real authId extracted from the database
                        new Doctor(authId).showDashboard();
                        this.dispose();
                        break;

                    case "RECEPTIONIST":
                        new receptionist(fullName).showDashboard();
                        this.dispose();
                        break;

                    case "ADMIN":
                        // new Admin(authId).showDashboard();
                        this.dispose();
                        break;

                    case "PHARMACIST":
                        // new Pharmacist(authId).showDashboard();
                        this.dispose();
                        break;

                    case "LABTECHNICIAN":
                        // new LabTechnician(authId).showDashboard();
                        this.dispose();
                        break;

                    default:
                        JOptionPane.showMessageDialog(this, "Unknown role: " + role, "Access Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        centerPanel.add(titleWrapper);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        centerPanel.add(form);
        mainBackgroundPanel.add(centerPanel);
        add(mainBackgroundPanel);
        setLocationRelativeTo(null);
    }

    private void applyNavClickEffect(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(bg.brighter()); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
            @Override public void mousePressed(MouseEvent e) { btn.setBackground(bg.darker()); }
            @Override public void mouseReleased(MouseEvent e) { btn.setBackground(bg.brighter()); }
        });
    }

    private void initializeDatabase() {
        DatabaseSetup.initializeDatabase();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("TextField.background", Color.WHITE);
            UIManager.put("TextField.foreground", Color.BLACK);
            UIManager.put("PasswordField.background", Color.WHITE);
            UIManager.put("PasswordField.foreground", Color.BLACK);
        } catch(Exception ignored){}
        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
    }
}