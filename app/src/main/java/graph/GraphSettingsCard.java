package graph;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.List;

public class GraphSettingsCard extends JPanel {

    // ── shared colours / fonts (used by interaction panel too) ────────
    static final Color BG        = new Color(20, 20, 30);
    static final Color BG_FIELD  = new Color(32, 32, 50);
    static final Color FG        = new Color(205, 205, 220);
    static final Color FG_DIM    = new Color(130, 130, 155);
    static final Font  FONT      = new Font("Segoe UI", Font.PLAIN, 13);

    // ── references ────────────────────────────────────────────────────
    private final Graph       graph;
    private final GraphPanel  graphPanel;
    private final Footer      footer;
    private final LeftPanel   leftPanel;   // needed to trigger card switch

    // ── widgets ───────────────────────────────────────────────────────
    private final JTextField   inputPathField = styledField();
    private final JRadioButton radioFrucht    = styledRadio("Fruchterman-Reingold");
    private final JRadioButton radioTriang    = styledRadio("Triangulacja");
    private final JRadioButton radioLoadTxt   = styledRadio("Wczytaj gotowy plik .txt");
    private final JRadioButton radioLoadBin   = styledRadio("Wczytaj gotowy plik .bin");
    private final JTextField   posPathField   = styledField();
    private final JCheckBox    checkBin       = styledCheck(".bin");
    private final JCheckBox    checkTxt       = styledCheck(".txt");

    // toggled sections
    private final JPanel algoSection;
    private final JPanel loadSection;

    public GraphSettingsCard(Graph graph, GraphPanel graphPanel,
                             Footer footer, LeftPanel leftPanel) {
        this.graph      = graph;
        this.graphPanel = graphPanel;
        this.footer     = footer;
        this.leftPanel  = leftPanel;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BG);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // ── input graph file ──────────────────────────────────────────
        add(sectionLabel("Plik grafu (lista krawędzi)"));
        add(browseRow(inputPathField, false));
        add(vgap(12));

        // ── mode radio buttons ────────────────────────────────────────
        add(sectionLabel("Tryb"));
        ButtonGroup group = new ButtonGroup();
        for (JRadioButton r : new JRadioButton[]{
                radioFrucht, radioTriang, radioLoadTxt, radioLoadBin}) {
            group.add(r);
        }
        radioFrucht.setSelected(true);

        JPanel modePanel = box();
        modePanel.add(radioFrucht);
        modePanel.add(radioTriang);
        modePanel.add(separator());
        modePanel.add(radioLoadTxt);
        modePanel.add(radioLoadBin);
        add(modePanel);
        add(vgap(10));

        // ── load section: shown when load mode ────────────────────────
        loadSection = box();
        loadSection.add(sectionLabel("Plik pozycji"));
        loadSection.add(browseRow(posPathField, false));
        add(loadSection);
        add(vgap(6));

        // ── algo section: format checkboxes, shown when algo mode ─────
        algoSection = box();
        algoSection.add(sectionLabel(
            "Format zapisu  (folder 'positions' obok pliku grafu)"));
        JPanel fmtRow = box();
        fmtRow.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        checkBin.setSelected(true);
        fmtRow.add(checkBin);
        fmtRow.add(Box.createHorizontalStrut(10));
        fmtRow.add(checkTxt);
        algoSection.add(fmtRow);
        add(algoSection);

        // ── hook radio buttons to show/hide sections ──────────────────
        Runnable sync = () -> {
            boolean load = radioLoadTxt.isSelected() || radioLoadBin.isSelected();
            loadSection.setVisible(load);
            algoSection.setVisible(!load);
            revalidate();
        };
        radioFrucht.addActionListener(e  -> sync.run());
        radioTriang.addActionListener(e  -> sync.run());
        radioLoadTxt.addActionListener(e -> sync.run());
        radioLoadBin.addActionListener(e -> sync.run());
        sync.run();

        // ── execute button ────────────────────────────────────────────
        add(Box.createVerticalGlue());
        add(vgap(12));
        JButton btn = styledButton("Wykonaj");
        btn.setBackground(new Color(40, 80, 160));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.addActionListener(e -> onExecute());
        add(btn);
    }

    // ─────────────────────────────────────────────────────────────────
    // Execute
    // ─────────────────────────────────────────────────────────────────

    private void onExecute() {
        String inputPath = inputPathField.getText().trim();
        if (inputPath.isEmpty()) {
            error("Nie podano pliku grafu."); return;
        }
        if (!new File(inputPath).exists()) {
            error("Plik grafu nie istnieje:\n" + inputPath); return;
        }

        // 1. load edges into graph
        try {
            loadGraphFromFile(inputPath);
        } catch (IOException ex) {
            error("Błąd wczytywania grafu:\n" + ex.getMessage()); return;
        }

        // 2. get / compute positions
        if (radioLoadTxt.isSelected()) {
            String posPath = posPathField.getText().trim();
            if (posPath.isEmpty()) { error("Nie podano pliku pozycji."); return; }
            try {
                applyPositionsTxt(posPath);
                footer.setStatus("Wczytano pozycje z .txt");
                switchToInteraction(inputPath);
            } catch (IOException ex) {
                error("Błąd wczytywania pozycji:\n" + ex.getMessage());
            }

        } else if (radioLoadBin.isSelected()) {
            String posPath = posPathField.getText().trim();
            if (posPath.isEmpty()) { error("Nie podano pliku pozycji."); return; }
            try {
                applyPositionsBin(posPath);
                footer.setStatus("Wczytano pozycje z .bin");
                switchToInteraction(inputPath);
            } catch (IOException ex) {
                error("Błąd wczytywania pozycji:\n" + ex.getMessage());
            }

        } else {
            runEngine(inputPath);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Graph loading
    // ─────────────────────────────────────────────────────────────────

    private void loadGraphFromFile(String inputPath) throws IOException {
        List<Edge> loadedEdges = GraphFileReader.readTextEdges(inputPath);

        // collect unique vertex objects (shared across edges)
        Map<Integer, Vertex> vMap = new LinkedHashMap<>();
        for (Edge e : loadedEdges) {
            vMap.put(e.from.id, e.from);
            vMap.put(e.to.id,   e.to);
        }

        // clear graph in-place so GraphPanel's list references stay valid
        graph.getVertices().clear();
        graph.getEdges().clear();
        for (Vertex v : vMap.values()) graph.addVertex(v);
        for (Edge   e : loadedEdges)  graph.addEdge(e);
    }

    // ─────────────────────────────────────────────────────────────────
    // Position application
    // ─────────────────────────────────────────────────────────────────

    private void applyPositionsTxt(String path) throws IOException {
        // Uses GraphFileReader from your friend
        List<Vertex> positions = GraphFileReader.readTextVertices(path);
        applyToGraph(positions);
    }

    private void applyPositionsBin(String path) throws IOException {
        // NOTE: GraphFileReader.readBinaryInput uses big-endian DataInputStream,
        // but the C engine writes little-endian doubles. Using a manual
        // little-endian reader here to match the engine's actual output.
        List<Vertex> positions = readBinLittleEndian(path);
        applyToGraph(positions);
    }

    private void applyToGraph(List<Vertex> positions) {
        for (Vertex src : positions) {
            for (Vertex dst : graph.getVertices()) {
                if (dst.id == src.id) {
                    dst.x = src.x;
                    dst.y = src.y;
                    break;
                }
            }
        }
        graphPanel.repaint();
    }

    // ─────────────────────────────────────────────────────────────────
    // Engine runner
    // ─────────────────────────────────────────────────────────────────

    private void runEngine(String inputPath) {
        if (!checkBin.isSelected() && !checkTxt.isSelected()) {
            checkBin.setSelected(true);
        }

        String enginePath = findEnginePath();
        if (!new File(enginePath).exists()) {
            error("Nie znaleziono silnika obliczeniowego.\n\n" +
                  "Oczekiwane lokalizacje:\n" +
                  "  • bin/engine/engine.exe\n" +
                  "  • app/bin/engine/engine.exe\n\n" +
                  "Bieżący katalog: " + new File(".").getAbsolutePath());
            return;
        }

        // output: <inputDir>/positions/<name>_positions(.txt/.bin)
        File   inputFile  = new File(inputPath);
        String baseName   = inputFile.getName().replaceFirst("[.][^.]+$", "");
        File   outDir     = new File(inputFile.getParentFile(), "positions");
        outDir.mkdirs();

        boolean saveTxt = checkTxt.isSelected();
        boolean saveBin = checkBin.isSelected();
        String  ext     = saveTxt ? ".txt" : ".bin";
        String  outPath = new File(outDir, baseName + "_positions" + ext).getAbsolutePath();

        String algo = radioFrucht.isSelected() ? "fruchterman" : "triangulation";
        footer.setStatus("Obliczanie (" + algo + ")…");

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<String> cmd = new ArrayList<>();
                cmd.add(enginePath);
                cmd.add(inputPath);
                cmd.add("-a"); cmd.add(algo);
                cmd.add("-o"); cmd.add(outPath);
                if (saveTxt) cmd.add("-t");
                if (saveBin) cmd.add("-b");

                Process proc = new ProcessBuilder(cmd)
                        .redirectErrorStream(true)
                        .start();

                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(proc.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) publish(line);
                }

                int code = proc.waitFor();
                if (code != 0)
                    throw new IOException("Silnik zakończył z kodem błędu: " + code);
                return null;
            }

            @Override
            protected void process(List<String> lines) {
                footer.setStatus(lines.get(lines.size() - 1));
            }

            @Override
            protected void done() {
                try {
                    get();
                    if (saveTxt) applyPositionsTxt(outPath);
                    else         applyPositionsBin(outPath);
                    footer.setStatus("Gotowy — " + graph.getVertices().size() + " wierzchołków");
                    switchToInteraction(inputPath);
                } catch (Exception ex) {
                    error("Błąd:\n" + ex.getMessage());
                    footer.setStatus("Błąd obliczania");
                }
            }
        };
        worker.execute();
    }

    private void switchToInteraction(String inputPath) {
        leftPanel.getInteractionPanel().activate(inputPath);
        leftPanel.showCard("interaction");
    }

    // ─────────────────────────────────────────────────────────────────
    // Engine path resolution
    // ─────────────────────────────────────────────────────────────────

    private static String findEnginePath() {
        String[] candidates = {
            "bin/engine/engine.exe",        // working dir = app/
            "app/bin/engine/engine.exe",    // working dir = project root
            "../bin/engine/engine.exe",     // just in case
        };
        for (String c : candidates) {
            if (new File(c).exists()) return new File(c).getAbsolutePath();
        }
        // return first candidate as absolute so error message shows real path
        return new File("bin/engine/engine.exe").getAbsolutePath();
    }

    // ─────────────────────────────────────────────────────────────────
    // Little-endian binary reader (matches C engine output)
    // ─────────────────────────────────────────────────────────────────

    private static List<Vertex> readBinLittleEndian(String path) throws IOException {
        List<Vertex> list = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(path)) {
            byte[] buf8 = new byte[8];
            while (true) {
                int nameLen = fis.read();
                if (nameLen == -1) break;
                byte[] nameBytes = new byte[nameLen];
                if (fis.read(nameBytes) != nameLen)
                    throw new IOException("Plik binarny niekompletny");
                if (fis.read(buf8) != 8)
                    throw new IOException("Plik binarny niekompletny");
                double x = ByteBuffer.wrap(buf8.clone())
                                     .order(ByteOrder.LITTLE_ENDIAN).getDouble();
                if (fis.read(buf8) != 8)
                    throw new IOException("Plik binarny niekompletny");
                double y = ByteBuffer.wrap(buf8.clone())
                                     .order(ByteOrder.LITTLE_ENDIAN).getDouble();
                int id = Integer.parseInt(new String(nameBytes).trim());
                list.add(new Vertex(id, (int) x, (int) y));
            }
        }
        if (list.isEmpty()) throw new IOException("Plik binarny jest pusty");
        return list;
    }

    // ─────────────────────────────────────────────────────────────────
    // Style helpers (package-visible for GraphInteractionPanel)
    // ─────────────────────────────────────────────────────────────────

    static JPanel box() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG);
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    private JPanel browseRow(JTextField field, boolean dirMode) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBackground(BG);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.add(field);
        row.add(Box.createHorizontalStrut(4));
        JButton btn = styledButton("…");
        btn.setPreferredSize(new Dimension(32, 28));
        btn.setMaximumSize(new Dimension(32, 28));
        btn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (dirMode) fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
                field.setText(fc.getSelectedFile().getAbsolutePath());
        });
        row.add(btn);
        return row;
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Błąd", JOptionPane.ERROR_MESSAGE);
    }

    static JTextField styledField() {
        JTextField f = new JTextField();
        f.setBackground(BG_FIELD); f.setForeground(FG);
        f.setCaretColor(FG); f.setFont(FONT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 75)),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        f.setAlignmentX(LEFT_ALIGNMENT);
        return f;
    }

    static JButton styledButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(38, 38, 62)); b.setForeground(FG);
        b.setFont(FONT); b.setFocusPainted(false);
        b.setBorderPainted(false); b.setOpaque(true);
        return b;
    }

    static JRadioButton styledRadio(String text) {
        JRadioButton r = new JRadioButton(text);
        r.setBackground(BG); r.setForeground(FG);
        r.setFont(FONT); r.setAlignmentX(LEFT_ALIGNMENT);
        return r;
    }

    static JCheckBox styledCheck(String text) {
        JCheckBox c = new JCheckBox(text);
        c.setBackground(BG); c.setForeground(FG);
        c.setFont(FONT); c.setAlignmentX(LEFT_ALIGNMENT);
        return c;
    }

    static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(FG_DIM);
        l.setBorder(BorderFactory.createEmptyBorder(6, 0, 3, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    static Component separator() {
        JSeparator s = new JSeparator();
        s.setForeground(new Color(48, 48, 72));
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        return s;
    }

    static Component vgap(int h) { return Box.createVerticalStrut(h); }
}