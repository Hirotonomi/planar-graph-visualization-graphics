package graph;

import javax.swing.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;


public class GraphPanel extends JPanel {
    private Graph graph;
    private List<Vertex> vertices;
    private List<Edge> edges;

    private double scale = 1.0;
    private int panX = 0;
    private int panY = 0;
    
    private Vertex draggedVertex = null;
    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private boolean panning = false;

    private final int RADIUS = 12;
    private final int TEXT_OFFSET = 15;

    public GraphPanel(Graph graph) {
        this.graph = graph;
        this.vertices = graph.getVertices();
        this.edges = graph.getEdges();

        setBackground(new Color(15, 15, 20));  // Deep dark blue-black

        // ZOOMING
        addMouseWheelListener(e -> {
            if (e.getWheelRotation() < 0) {
                scale *= 1.1; // Zoom in
            } else {
                scale /= 1.1; // Zoom out
            }
            revalidate();
            repaint();
        });

        // VERTEX DRAGGING & PANNING
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON2) {
                    panning = true;
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                } else if (e.getButton() == MouseEvent.BUTTON1) {
                    int x = (int) ((e.getX() - panX) / scale);
                    int y = (int) ((e.getY() - panY) / scale);

                    for (Vertex v : vertices) {
                        double dx = x - v.x;
                        double dy = y - v.y;
                        double distance = Math.sqrt(dx*dx + dy*dy);

                        if (distance <= RADIUS) {
                            draggedVertex = v;
                            break;
                        }
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON2) {
                    panning = false;
                } else if (draggedVertex != null) {
                    draggedVertex = null;  // Set back to blue
                    repaint();  // Ensure immediate color update
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (panning) {
                    int deltaX = e.getX() - lastMouseX;
                    int deltaY = e.getY() - lastMouseY;
                    
                    panX += deltaX;
                    panY += deltaY;
                    
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                    
                    revalidate();
                    repaint();
                } else if (draggedVertex != null) {
                    int x = (int) ((e.getX() - panX) / scale);
                    int y = (int) ((e.getY() - panY) / scale);

                    draggedVertex.x = x;
                    draggedVertex.y = y;

                    revalidate();
                    repaint();
                }
            }
        });

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // APPLY PAN AND SCALE
        g2.translate(panX, panY);
        g2.scale(scale, scale);

        // DRAW EDGES
        for (Edge edge: edges) {
            int x1 = (int) edge.from.x;
            int y1 = (int) edge.from.y;

            int x2 = (int) edge.to.x;
            int y2 = (int) edge.to.y;

            g2.setColor(new Color(100, 220, 120));  // Greenish edges
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawLine(x1, y1, x2, y2);
            
            int midX = (x1+x2)/2;
            int midY = (y1+y2)/2;
            g2.setColor(new Color(220, 220, 230));  // Light gray text
            g2.drawString(edge.name + " " + String.valueOf(edge.weight), midX, midY);
        }

        // DRAW VERTICIES
        for (Vertex v : vertices) {
            int x = (int) v.x;
            int y = (int) v.y;

            if (v == draggedVertex) {
                g2.setColor(new Color(255, 60, 60));  // Red when dragging
            } else {
                g2.setColor(new Color(70, 150, 255));  // Nice blue
            }
            
            g2.fillOval(x-RADIUS, y-RADIUS, RADIUS*2, RADIUS*2);

            g2.setColor(new Color(250, 250, 250));  // Bright white text
            g2.drawString(String.valueOf(v.id), x, y-TEXT_OFFSET);
        }
    }

    public double getZoom() {
        return scale;
    }

    public void setZoom(double zoomLevel) {
        if (zoomLevel > 0) {
            scale = zoomLevel;
            revalidate();
            repaint();
        }
    }



}
