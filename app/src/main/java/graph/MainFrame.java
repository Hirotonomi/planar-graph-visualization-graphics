package graph;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        super("Graph Visualization");

        Graph       graph      = new Graph();
        Vertex a = new Vertex(1, 100.0f, 100.0f);
        Vertex b = new Vertex(2, 200.0f, 100.0f);
        Vertex c = new Vertex(3, 200.0f, 200.0f);
        Vertex d = new Vertex(4, 100.0f, 200.0f);

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(d);

        graph.addEdge(new Edge("AB", a, b, 1));
        graph.addEdge(new Edge("BC", b, c, 1));
        graph.addEdge(new Edge("CD", c, d, 1));
        graph.addEdge(new Edge("DA", d, a, 1.407));

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