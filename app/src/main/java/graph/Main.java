package graph;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame("Graph Viewer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);

                    Graphics2D g2 = (Graphics2D) g;

                    g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                    );

                    g2.setColor(Color.BLACK);
                    g2.drawOval(100, 100, 30, 30);
                    g2.drawOval(300, 200, 30, 30);
                    g2.drawLine(115, 115, 315, 215);
                }
            };

            panel.setBackground(Color.WHITE);

            frame.setContentPane(panel); // IMPORTANT FIX

            frame.setVisible(true);
        });
    }
}
