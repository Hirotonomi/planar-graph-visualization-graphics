package graph.bar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public abstract class Bar extends JPanel {

    protected static final Color BG          = new Color(22, 22, 34);
    protected static final Color BORDER_COL  = new Color(48, 48, 72);
    protected static final Color TEXT        = new Color(205, 205, 220);
    protected static final Font  FONT        = new Font("Segoe UI", Font.PLAIN, 13);

    public Bar() {
        setBackground(BG);
        setFont(FONT);
    }
}