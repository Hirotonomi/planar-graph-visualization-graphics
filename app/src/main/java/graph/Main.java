package graph;

import javax.swing.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        // temporary variables 
        List<Vertex> vertices = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        Vertex a = new Vertex(1, 100, 100);
        Vertex b = new Vertex(2, 500, 100);
        Vertex c = new Vertex(3, 200, 200);
        Vertex d = new Vertex(4, 100, 200);

        vertices.add(a);
        vertices.add(b);
        vertices.add(c);
        vertices.add(d);

        edges.add(new Edge("AB", a, b, 1));
        edges.add(new Edge("BC", b, c, 1));
        edges.add(new Edge("CD", c, d, 1));
        edges.add(new Edge("DA", d, a, 1.407));

        JFrame frame = new JFrame("Graph Visualization");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        // uncomment when GraphPanel is implemented
        frame.add(new GraphPanel(vertices, edges));
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);


    }
}
