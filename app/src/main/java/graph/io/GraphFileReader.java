    package graph.io;

    import graph.Edge;
    import graph.Vertex;

    import java.io.*;
    import java.util.*;

    public class GraphFileReader {

        /**
         * Reads an edge-list text file and returns the list of edges.
         * Vertices are deduplicated by id and shared across edges.
         *
         * Expected line format: <name> <fromId> <toId> <weight>
         */
        public static List<Edge> read(String path) throws IOException {
            Map<Integer, Vertex> vertexMap = new LinkedHashMap<>();
            List<Edge> edges = new ArrayList<>();

            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                String line;
                int lineNumber = 0;

                while ((line = br.readLine()) != null) {
                    lineNumber++;
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] parts = line.split("\\s+");
                    if (parts.length < 4) {
                        throw new IOException(
                            "Nieprawidłowy format danych w linii " + lineNumber);
                    }

                    String name = parts[0];
                    int fromId, toId;
                    double weight;

                    try {
                        fromId = Integer.parseInt(parts[1]);
                        toId   = Integer.parseInt(parts[2]);
                        weight = Double.parseDouble(parts[3]);
                    } catch (NumberFormatException e) {
                        throw new IOException(
                            "Nieprawidłowy format danych w linii " + lineNumber);
                    }

                    vertexMap.putIfAbsent(fromId, new Vertex(fromId, 0, 0));
                    vertexMap.putIfAbsent(toId,   new Vertex(toId,   0, 0));

                    edges.add(new Edge(
                        name,
                        vertexMap.get(fromId),
                        vertexMap.get(toId),
                        weight
                    ));
                }
            }

            if (edges.isEmpty()) {
                throw new IOException("Błąd: wczytany graf jest pusty");
            }

            return edges;
        }
    }