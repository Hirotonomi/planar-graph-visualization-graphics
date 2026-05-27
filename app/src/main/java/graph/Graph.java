package graph;

import java.util.*;

public class Graph {
    private List<Vertex> vertices;
    private List<Edge> edges;

    public Graph() {
        this.vertices = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public void addVertex(Vertex vertex) {
        vertices.add(vertex);
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    public void addVertices(List<Vertex> verticesToAdd) {
        vertices.addAll(verticesToAdd);
    }

    public void addEdges(List<Edge> edgesToAdd) {
        edges.addAll(edgesToAdd);
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Vertex getVertexById(int id) {
        for (Vertex v : vertices) {
            if (v.id == id) {
                return v;
            }
        }
        return null;
    }

    public void updateVertexPosition(int vertexId, int x, int y) {
        Vertex v = getVertexById(vertexId);
        if (v != null) {
            v.x = x;
            v.y = y;
        }
    }

    public void clear() {
        vertices.clear();
        edges.clear();
    }
}
