package graph;

import java.util.List;
import java.awt.*;
import javax.swing.*;

public class GraphPanel extends JPanel {
    private List<Vertex> verticies;
    private List<Edge> edges;


    private final int RADIUS = 12;
    private final int TEXT_OFFSET = 15;

    public GraphPanel(List<Vertex> verticies, List<Edge> edges) {
        this.verticies = verticies;
        this.edges = edges;

        setBackground(Color.black);


    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // DRAW EDGES
        for (Edge edge: edges) {
            int x1 = edge.from.x;
            int y1 = edge.from.y;

            int x2 = edge.to.x;
            int y2 = edge.to.y;


            g2.setColor(Color.green);
            g2.drawLine(x1, y1, x2, y2);
            
            int midX = (x1+x2)/2;
            int midY = (y1+y2)/2;
            g2.setColor(Color.white);
            g2.drawString(edge.name + " " + String.valueOf(edge.weight), midX, midY);
        }

        // DRAW VERTICIES
        for (Vertex v : verticies) {
            int x = v.x;
            int y = v.y;

            g2.setColor(Color.blue);
            
            g2.fillOval(x-RADIUS, y-RADIUS, RADIUS*2, RADIUS*2);

            g2.setColor(Color.white);
            g2.drawString(String.valueOf(v.id), x, y-TEXT_OFFSET);
        }
    }



}
