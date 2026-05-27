package graph;

public class Edge {
    Vertex from;
    Vertex to;

    double weight;

    public Edge(Vertex from, Vertex to, double weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }
}
