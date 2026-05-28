package graph;

import javax.swing.*;
import java.awt.*;

public class Footer extends Bar {

    private final JLabel statusLabel;

    public Footer() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 28));
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COL));

        statusLabel = new JLabel("Gotowy");
        statusLabel.setFont(FONT);
        statusLabel.setForeground(new Color(140, 140, 165));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
        add(statusLabel, BorderLayout.WEST);
    }

    /** Thread-safe — can be called from any thread. */
    public void setStatus(String msg) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(msg));
    }
}