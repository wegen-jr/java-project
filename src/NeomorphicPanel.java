import java.awt.*;
import javax.swing.*;

class NeomorphicPanel extends JPanel {
    private int round = 30;
    private Color baseColor = new Color(240, 242, 245);

    public NeomorphicPanel() {
        setOpaque(false);
        setBackground(baseColor);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Dark Shadow (Bottom Right)
        g2.setColor(new Color(190, 195, 205));
        g2.fillRoundRect(8, 8, width - 16, height - 16, round, round);

        // Light Highlight (Top Left)
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(2, 2, width - 16, height - 16, round, round);

        // Main Surface
        g2.setColor(baseColor);
        g2.fillRoundRect(5, 5, width - 15, height - 15, round, round);

        g2.dispose();
        super.paintComponent(g);
    }
}