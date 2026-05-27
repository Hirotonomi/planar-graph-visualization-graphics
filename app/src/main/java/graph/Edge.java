package graph;

public class Edge {
    String name;

    Vertex from;
    Vertex to;


    double weight;

    public Edge(String name, Vertex from, Vertex to, double weight) {
        this.name = name;
        this.from = from;
        this.to = to;
        this.weight = weight;
    }
}
