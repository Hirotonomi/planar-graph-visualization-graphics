    package graph;

    import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
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
         * Reads a binary position file produced by the engine.
         * * Actual per-record layout discovered from the binary dump:
         * [8 bytes]  x coordinate — IEEE 754 double, LITTLE-ENDIAN
         * [8 bytes]  y coordinate — IEEE 754 double, LITTLE-ENDIAN
         * [4 bytes]  vertex ID    — 32-bit integer, LITTLE-ENDIAN
         * * Total record size: 20 bytes.
         */
        public static List<Vertex> readBinaryInput(String path) throws IOException {
            List<Vertex> list = new ArrayList<>();
            File file = new File(path);
            if (!file.exists()) return list;

            // Wczytujemy zawartość całego pliku do tablicy bajtów
            byte[] allBytes = Files.readAllBytes(file.toPath());
            
            // Wrapujemy w ByteBuffer i ustawiamy porządek bajtów silnika (Little-Endian)
            ByteBuffer buffer = ByteBuffer.wrap(allBytes).order(ByteOrder.LITTLE_ENDIAN);

            // Każdy rekord wierzchołka zajmuje dokładnie 20 bajtów (8 + 8 + 4)
            while (buffer.remaining() >= 20) {
                double x = buffer.getDouble();
                double y = buffer.getDouble();
                int id = buffer.getInt();

                // Dodajemy wierzchołek, rzutując współrzędne double na int (zgodnie z konstruktorem Twojej klasy Vertex)
                list.add(new Vertex(id, (int) x, (int) y));
            }

            if (list.isEmpty()) {
                throw new IOException("Plik binarny jest pusty lub ma nieprawidłowy format");
            }

            return list;
        }
    }