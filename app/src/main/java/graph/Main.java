package graph;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        Graph graph = new Graph();

        Vertex a = new Vertex(1, 100, 100);
        Vertex b = new Vertex(2, 500, 100);
        Vertex c = new Vertex(3, 200, 200);
        Vertex d = new Vertex(4, 100, 200);

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(d);

        graph.addEdge(new Edge("AB", a, b, 1));
        graph.addEdge(new Edge("BC", b, c, 1));
        graph.addEdge(new Edge("CD", c, d, 1));
        graph.addEdge(new Edge("DA", d, a, 1.407));

        JFrame frame = new JFrame("Graph Visualization");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.add(new GraphPanel(graph));
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }
}
