package graph;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GraphSettingsCard extends JPanel {

    // ── colours (kept for visual consistency) ─────────────────────────
    static final Color BG       = new Color(20, 20, 30);
    static final Color BG_FIELD = new Color(32, 32, 50);
    static final Color FG       = new Color(205, 205, 220);
    static final Color FG_DIM   = new Color(130, 130, 155);
    static final Color ACCENT   = new Color(70, 130, 220);
    static final Font  FONT     = new Font("Segoe UI", Font.PLAIN, 13);

    private final Footer footer;

    // Mock constructor – Graph and GraphPanel are ignored
    public GraphSettingsCard(Graph graph, GraphPanel graphPanel, Footer footer) {
        this.footer = footer;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BG);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // Simple mock label
        JLabel mockLabel = new JLabel("Mock Graph Settings");
        mockLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mockLabel.setForeground(FG);
        mockLabel.setAlignmentX(LEFT_ALIGNMENT);
        add(mockLabel);
        add(Box.createVerticalStrut(10));

        JLabel infoLabel = new JLabel("This is a placeholder – no real graph operations.");
        infoLabel.setFont(FONT);
        infoLabel.setForeground(FG_DIM);
        infoLabel.setAlignmentX(LEFT_ALIGNMENT);
        add(infoLabel);
        add(Box.createVerticalStrut(20));

        // Button that demonstrates Footer communication
        JButton mockBtn = styledButton("Mock Action");
        mockBtn.setAlignmentX(LEFT_ALIGNMENT);
        mockBtn.addActionListener(e -> {
            if (footer != null) {
                footer.setStatus("Mock action performed (no real graph work)");
            }
        });
        add(mockBtn);

        add(Box.createVerticalGlue());
    }

    // ── minimal styling helpers (same as original) ────────────────────
    private static JButton styledButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(38, 38, 62));
        b.setForeground(FG);
        b.setFont(FONT);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        return b;
    }
}