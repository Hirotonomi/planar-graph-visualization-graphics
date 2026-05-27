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
         * Writes vertices to a binary file.
         * Format for each vertex:
         * 1. Name length (1 byte, unsigned char)
         * 2. Name (UTF-8 string)
         * 3. Coordinate x (8 bytes, double)
         * 4. Coordinate y (8 bytes, double)
         */
        public static void writeBinaryOutput(Collection<Vertex> vertices, String path) throws IOException {
            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(path))) {
                for (Vertex v : vertices) {
                    String vertexName = String.valueOf(v.id);
                    byte[] nameBytes = vertexName.getBytes("UTF-8");
                    
                    dos.writeByte(nameBytes.length);
                    dos.write(nameBytes);
                    dos.writeDouble(v.x);
                    dos.writeDouble(v.y);
                }
            }
        }

        /**
         * Reads vertices from a binary file.
         * Expects format: name_length (1 byte), name (UTF-8), x (double), y (double)
         */
        public static List<Vertex> readBinaryInput(String path) throws IOException {
            List<Vertex> vertices = new ArrayList<>();

            try (DataInputStream dis = new DataInputStream(new FileInputStream(path))) {
                int nextId = 1;
                while (dis.available() > 0) {
                    int nameLength = dis.readUnsignedByte();
                    byte[] nameBytes = new byte[nameLength];
                    dis.readFully(nameBytes);
                    
                    double x = dis.readDouble();
                    double y = dis.readDouble();
                    
                    Vertex v = new Vertex(nextId++, x, y);
                    vertices.add(v);
                }
            }

            if (vertices.isEmpty()) {
                throw new IOException("Błąd: wczytany plik binarny jest pusty");
            }

            return vertices;
        }
    }