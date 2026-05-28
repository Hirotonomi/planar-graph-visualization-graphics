package graph;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class GraphSettingsCard extends JPanel {

    static final Color BG       = new Color(20, 20, 30);
    static final Color BG_FIELD = new Color(32, 32, 50);
    static final Color FG       = new Color(205, 205, 220);
    static final Color FG_DIM   = new Color(130, 130, 155);
    static final Font  FONT     = new Font("Segoe UI", Font.PLAIN, 13);

    private final Graph      graph;
    private final GraphPanel graphPanel;
    private final Footer     footer;
    private final LeftPanel  leftPanel;

    private final JTextField   inputPathField = styledField();
    private final JRadioButton radioFrucht    = styledRadio("Fruchterman-Reingold");
    private final JRadioButton radioTriang    = styledRadio("Triangulacja");
    private final JRadioButton radioLoadTxt   = styledRadio("Wczytaj gotowy plik .txt");
    private final JRadioButton radioLoadBin   = styledRadio("Wczytaj gotowy plik binarny");
    private final JTextField   posPathField   = styledField();
    private final JCheckBox    checkBin       = styledCheck("binarny (bez rozszerzenia)");
    private final JCheckBox    checkTxt       = styledCheck(".txt");

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

        add(sectionLabel("Plik grafu (lista krawędzi)"));
        add(browseRow(inputPathField, false));
        add(vgap(12));

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

        loadSection = box();
        loadSection.add(sectionLabel("Plik pozycji"));
        loadSection.add(browseRow(posPathField, false));
        add(loadSection);
        add(vgap(6));

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
        if (inputPath.isEmpty())          { error("Nie podano pliku grafu.");           return; }
        if (!new File(inputPath).exists()) { error("Plik grafu nie istnieje:\n" + inputPath); return; }

        try {
            loadGraphFromFile(inputPath);
        } catch (IOException ex) {
            error("Błąd wczytywania grafu:\n" + describe(ex)); return;
        }

        if (radioLoadTxt.isSelected()) {
            String posPath = posPathField.getText().trim();
            if (posPath.isEmpty()) { error("Nie podano pliku pozycji."); return; }
            try {
                applyPositionsTxt(posPath);
                footer.setStatus("Wczytano pozycje z .txt");
                switchToInteraction(inputPath);
            } catch (IOException ex) { error("Błąd wczytywania pozycji:\n" + describe(ex)); }

        } else if (radioLoadBin.isSelected()) {
            String posPath = posPathField.getText().trim();
            if (posPath.isEmpty()) { error("Nie podano pliku pozycji."); return; }
            try {
                applyPositionsBin(posPath);
                footer.setStatus("Wczytano pozycje binarne");
                switchToInteraction(inputPath);
            } catch (IOException ex) { error("Błąd wczytywania pozycji:\n" + describe(ex)); }

        } else {
            runEngine(inputPath);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Graph loading
    // ─────────────────────────────────────────────────────────────────

    private void loadGraphFromFile(String inputPath) throws IOException {
        List<Edge> loadedEdges = GraphFileReader.readTextEdges(inputPath);

        Map<Integer, Vertex> vMap = new LinkedHashMap<>();
        for (Edge e : loadedEdges) {
            vMap.put(e.from.id, e.from);
            vMap.put(e.to.id,   e.to);
        }

        graph.getVertices().clear();
        graph.getEdges().clear();
        for (Vertex v : vMap.values()) graph.addVertex(v);
        for (Edge   e : loadedEdges)  graph.addEdge(e);
    }

    // ─────────────────────────────────────────────────────────────────
    // Position application
    // ─────────────────────────────────────────────────────────────────

    private void applyPositionsTxt(String path) throws IOException {
        applyToGraph(GraphFileReader.readTextVertices(path));
    }

    private void applyPositionsBin(String path) throws IOException {
        // Uses the correct little-endian reader to match C engine output.
        applyToGraph(GraphFileReader.readBinaryInput(path));
    }

    private void applyToGraph(List<Vertex> positions) {
        for (Vertex src : positions)
            for (Vertex dst : graph.getVertices())
                if (dst.id == src.id) { dst.x = src.x; dst.y = src.y; break; }
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

        File   inputFile = new File(inputPath);
        String baseName  = inputFile.getName().replaceFirst("[.][^.]+$", "");
        File   outDir    = new File(inputFile.getParentFile(), "positions");
        outDir.mkdirs();

        boolean saveTxt = checkTxt.isSelected();
        boolean saveBin = checkBin.isSelected();

        // Pass output path WITHOUT extension — engine uses this raw base.
        String outBase = new File(outDir, baseName + "_positions").getAbsolutePath();

        String algo = radioFrucht.isSelected() ? "fruchterman" : "triangulation";
        footer.setStatus("Obliczanie (" + algo + ")…");

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<String> cmd = new ArrayList<>();
                cmd.add(enginePath);
                cmd.add(inputPath);
                cmd.add("-a"); cmd.add(algo);
                cmd.add("-o"); cmd.add(outBase);
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
                    String ext = saveTxt ? ".txt" : "";
                    File expectedFile = new File(outBase + ext);

                    if (!expectedFile.exists()) {
                        boolean created = expectedFile.createNewFile();
                        if (!created) {
                            throw new IOException("Nie udało się utworzyć wymaganego pliku: " + expectedFile.getAbsolutePath());
                        }
                    }
                    String resolved = expectedFile.getAbsolutePath();

                if (saveTxt) applyPositionsTxt(resolved);
                else applyPositionsBin(resolved);
                footer.setStatus("Gotowy — " + graph.getVertices().size() + " wierzchołków");
                switchToInteraction(inputPath);

                }
                
                catch (IOException e) {
                    error("Błąd operacji na pliku:\n" + describe(e));
                    footer.setStatus("Błąd zapisu/odczytu");
                }   
                catch (ExecutionException ex) {
                    // unwrap: ExecutionException wraps the real exception from doInBackground
                    Throwable cause = ex.getCause();
                    error("Błąd silnika:\n" + (cause != null ? describe(cause) : describe(ex)));
                    footer.setStatus("Błąd obliczania");
                } catch (Exception ex) {
                    error("Błąd:\n" + describe(ex));
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
            "bin/engine/engine.exe",
            "app/bin/engine/engine.exe",
            "../bin/engine/engine.exe",
        };
        for (String c : candidates)
            if (new File(c).exists()) return new File(c).getAbsolutePath();
        return new File("bin/engine/engine.exe").getAbsolutePath();
    }

    // ─────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────

    /** Extracts a non-null, non-blank description from any Throwable. */
    static String describe(Throwable t) {
        String msg = t.getMessage();
        if (msg != null && !msg.isBlank()) return msg;
        return t.getClass().getSimpleName() + " (brak opisu)";
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Błąd", JOptionPane.ERROR_MESSAGE);
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

    static JPanel box() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG);
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
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