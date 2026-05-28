    package graph;

    import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

    public class GraphFileReader {

        /**
         * Reads an edge-list text file and returns the list of edges.
         * Vertices are deduplicated by id and shared across edges.
         *
         * Expected line format: <name> <fromId> <toId> <weight>
         */
        public static List<Edge> readTextEdges(String path) throws IOException {
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

        /**
         * Reads vertices from a text file with coordinates.
         * Format: <vertex_id> <x> <y>
         */
        public static List<Vertex> readTextVertices(String path) throws IOException {
            List<Vertex> vertices = new ArrayList<>();

            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                String line;
                int lineNumber = 0;

                while ((line = br.readLine()) != null) {
                    lineNumber++;
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] parts = line.split("\\s+");
                    if (parts.length != 3) {
                        throw new IOException(
                            "Nieprawidłowy format danych w linii " + lineNumber);
                    }

                    int id;
                    double x, y;

                    try {
                        id = Integer.parseInt(parts[0]);
                        x  = Double.parseDouble(parts[1]);
                        y  = Double.parseDouble(parts[2]);
                    } catch (NumberFormatException e) {
                        throw new IOException(
                            "Nieprawidłowy format danych w linii " + lineNumber);
                    }

                    vertices.add(new Vertex(id, x, y));
                }
            }

            if (vertices.isEmpty()) {
                throw new IOException("Błąd: wczytany plik wierzchołków jest pusty");
            }

            return vertices;
        }

        /**
         * Writes vertices to a text file with coordinates in format: %.2f
         * Format: <vertex_id> <x> <y>
         */
        public static void writeTextOutput(Collection<Vertex> vertices, String path) throws IOException {
            try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
                for (Vertex v : vertices) {
                    writer.printf("%d %.2f %.2f%n", v.id, v.x, v.y);
                }
            }
        }

        /**
         * Reads a binary position file produced by the Vyshnia/Udavichenka engine.
         *
         * Per-record layout:
         *   [1 byte]   name length (unsigned)
         *   [n bytes]  vertex name as ASCII
         *   [8 bytes]  x coordinate — IEEE 754 double, LITTLE-ENDIAN
         *   [8 bytes]  y coordinate — IEEE 754 double, LITTLE-ENDIAN
         *
         * NOTE: replaces readBinaryInput() which used DataInputStream.readDouble()
         * (big-endian). The C engine writes little-endian on x86 — this method
         * reads correctly.
         */
        public static List<Vertex> readBinaryInput(String path) throws IOException {
            List<Vertex> list = new ArrayList<>();

            try (FileInputStream fis = new FileInputStream(path)) {
                byte[] buf8 = new byte[8];

                while (true) {
                    int nameLen = fis.read();
                    if (nameLen == -1) break;                    // clean EOF

                    byte[] nameBytes = new byte[nameLen];
                    int nameRead = fis.read(nameBytes);
                    if (nameRead != nameLen)
                        throw new IOException("Plik binarny niekompletny (nazwa wierzchołka)");

                    if (fis.read(buf8) != 8)
                        throw new IOException("Plik binarny niekompletny (współrzędna x)");
                    double x = ByteBuffer.wrap(buf8.clone())
                                        .order(ByteOrder.LITTLE_ENDIAN)
                                        .getDouble();

                    if (fis.read(buf8) != 8)
                        throw new IOException("Plik binarny niekompletny (współrzędna y)");
                    double y = ByteBuffer.wrap(buf8.clone())
                                        .order(ByteOrder.LITTLE_ENDIAN)
                                        .getDouble();

                    String name = new String(nameBytes, "ASCII").trim();
                    int id;
                    try {
                        id = Integer.parseInt(name);
                    } catch (NumberFormatException e) {
                        throw new IOException("Nieprawidłowa nazwa wierzchołka w pliku binarnym: '" + name + "'");
                    }

                    list.add(new Vertex(id, (int) x, (int) y));
                }
            }

            if (list.isEmpty())
                throw new IOException("Plik binarny jest pusty lub ma nieprawidłowy format");

            return list;
        }
    }