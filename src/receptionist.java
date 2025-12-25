import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class receptionist extends staffUser {
    @Override
    void showDashboard() {
        JFrame frame=new JFrame("HMS - Hospital Management System(Receptionist)");
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        ImageIcon backgroundIcon = new ImageIcon("assets/receptionBg.jpeg");
        ImageIcon dashboardIcon = new ImageIcon("assets/dashboard.png");
        ImageIcon patientIcon=new ImageIcon("assets/user.png");
        ImageIcon appointmentIcon=new ImageIcon("assets/appointment.png");
        ImageIcon doctorIcon=new ImageIcon("assets/stethoscope.png");
        ImageIcon billIcon=new ImageIcon("assets/medical.png");
        ImageIcon portalIcon=new ImageIcon("assets/portal.png");

        Image scaledImage = dashboardIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        Image scaledImage1 = patientIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon1 = new ImageIcon(scaledImage1);
        Image scaledImage2 = appointmentIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon2 = new ImageIcon(scaledImage2);
        Image scaledImage3 = doctorIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon3 = new ImageIcon(scaledImage3);
        Image scaledImage4 = billIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon4 = new ImageIcon(scaledImage4);
        Image scaledImage5 = portalIcon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon5 = new ImageIcon(scaledImage5);

        Font subtitle=new Font("SansSerif",Font.BOLD,12);
        JPanel mainBackgroundPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };

        JPanel leftPanel=new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel,BoxLayout.Y_AXIS));
        leftPanel.setBackground(new Color(255,255,255));
        leftPanel.setBorder(BorderFactory.createLineBorder(Color.black,1,true));
        leftPanel.setMaximumSize(new Dimension(200,Integer.MAX_VALUE));
        JPanel navContainer =new JPanel();
        navContainer.setLayout(new BoxLayout(navContainer,BoxLayout.Y_AXIS));
        navContainer.setMaximumSize(new Dimension(200,500));
        navContainer.setBackground(new Color(255,255,255));
        navContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel portalPanel=new JPanel();
        JLabel portalName=new JLabel("Receptionist portal");
        portalName.setFont(new Font("Arial",Font.BOLD,18));
        JLabel portalImage=new JLabel(new ImageIcon(scaledIcon5.getImage()));
        portalPanel.add(portalImage);
        portalPanel.add(portalName);

        JPanel dashboardWrapper=new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
        JButton dashboardLogo=new JButton(new ImageIcon(scaledIcon.getImage()));
        JLabel dashboardLabel = new JLabel("Dashboard");
        dashboardLabel.setFont(subtitle);
        dashboardWrapper.add(dashboardLogo);
        dashboardWrapper.add(dashboardLabel);

        JPanel patientWrapper=new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
        JButton patientLogo=new JButton(new ImageIcon(scaledIcon1.getImage()));
        JLabel patientLabel = new JLabel("Patients");
        patientLabel.setFont(subtitle);
        patientLogo.addActionListener(e->patientsDashboard());
        patientWrapper.add(patientLogo);
        patientWrapper.add(patientLabel);

        JPanel appointmentWrapper=new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
        JButton appointmentLogo=new JButton(new ImageIcon(scaledIcon2.getImage()));
        JLabel appointmentLabel = new JLabel("Appointment");
        appointmentLabel.setFont(subtitle);
        appointmentWrapper.add(appointmentLogo);
        appointmentWrapper.add(appointmentLabel);

        JPanel doctorWrapper=new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
        JButton doctorLogo=new JButton(new ImageIcon(scaledIcon3.getImage()));
        JLabel doctorLabel = new JLabel("Doctors");
        doctorLabel.setFont(subtitle);
        doctorWrapper.add(doctorLogo);
        doctorWrapper.add(doctorLabel);


        JPanel billWrapper=new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
        JButton billLogo=new JButton(new ImageIcon(scaledIcon4.getImage()));
        JLabel billLabel = new JLabel("Bills");
        billLabel.setFont(subtitle);
        billWrapper.add(billLogo);
        billWrapper.add(billLabel);


        JSeparator separator1=new JSeparator();
        navContainer.add(portalPanel);
        navContainer.add(separator1);
        navContainer.add(dashboardWrapper);
        navContainer.add(patientWrapper);
        navContainer.add(appointmentWrapper);
        navContainer.add(doctorWrapper);
        navContainer.add(billWrapper);
        leftPanel.add(navContainer);
        mainBackgroundPanel.add(leftPanel,BorderLayout.WEST);
        frame.add(mainBackgroundPanel);
        frame.setVisible(true);
    }
void patientsDashboard(){

}
    @Override
    Boolean login(String String) {
        return null;
    }

    @Override
    void logout() {

    }
}
