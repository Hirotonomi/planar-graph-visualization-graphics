package graph;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        super("Graph Visualization");

        Graph       graph      = new Graph();
        GraphPanel  graphPanel = new GraphPanel(graph);

        setLayout(new BorderLayout());
        JPanel center = new JPanel(new BorderLayout());
        center.add(graphPanel, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 820);
        setLocationRelativeTo(null);
    }
}