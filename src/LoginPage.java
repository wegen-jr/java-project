import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.sql.*;

public class LoginPage extends JFrame {

    public LoginPage() {
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
        Color titleColor =new Color(2,48,71);

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

        // 5. Database Logic
        String URL = "jdbc:mysql://localhost:3306/HMS";
        String DBusername = "phpmyadmin";
        String DBpassword = "wegen@1996";

        loginBtn.addActionListener(e -> {
            String inputUser = uNameField.getText();
            String inputPass = new String(passField.getPassword());
            if (inputUser.isEmpty()){
                JOptionPane.showMessageDialog(this,"username required!");
                return;
            } else if (inputPass.isEmpty()) {
                JOptionPane.showMessageDialog(this,"password required!");
                return;
            }
            // Use PreparedStatement to prevent SQL Injection
            String query = "SELECT * FROM authentication WHERE Username = ? AND Passworrd = ?";

            try (Connection con = DriverManager.getConnection(URL, DBusername, DBpassword);
                 PreparedStatement pst = con.prepareStatement(query)) {

                pst.setString(1, inputUser);
                pst.setString(2, inputPass);

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        String role=rs.getString("Role");
                        if(role!=null){
                            role=role.trim();
                            JOptionPane.showMessageDialog(this, "Login Successful!");
                            this.dispose();
                        switch (role.toUpperCase()){
                            case "RECEPTION":
                                new receptionist().showDashboard();
                                    break;
                            case "DOCTOR":
                             //   new DoctorDashboard().setVisible(true);
                                break;

                            case "PHARMACIST":
                               // new PharmacistDashboard().setVisible(true);
                                break;

                            case "ADMIN":
                              //  new AdminDashboard().setVisible(true);
                                break;
                            case "LABTECHNICIAN":
                                break;
                            default:
                                JOptionPane.showMessageDialog(this,
                                        "Unknown role: " + role,
                                        "Access Error",
                                        JOptionPane.ERROR_MESSAGE);
                                // Show login again
                                new LoginPage().setVisible(true);
                        }}else {
                            JOptionPane.showMessageDialog(this,
                                    "No role assigned to user",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }

                        // Open Dashboard here: new Dashboard().setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
                        uNameField.setText("");
                        passField.setText("");
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // 6. Assemble everything
        centerPanel.add(titleWrapper);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30))); // Space between title and form
        centerPanel.add(form);

        mainBackgroundPanel.add(centerPanel); // GridBagLayout centers this
        add(mainBackgroundPanel);
    }

    public static void main(String[] args) {
        // Ensure Database Driver is loaded (for older Java versions)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found!");
        }

        SwingUtilities.invokeLater(() -> {
            new LoginPage().setVisible(true);
        });
    }
}