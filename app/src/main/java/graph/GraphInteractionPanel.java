package graph;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static graph.GraphSettingsCard.*;

public class GraphInteractionPanel extends JPanel {

    private final Graph      graph;
    private final GraphPanel graphPanel;
    private final Footer     footer;
    private final LeftPanel  leftPanel;

    private String inputFilePath = "";   // set by activate()

    public GraphInteractionPanel(Graph graph, GraphPanel graphPanel,
                                 Footer footer, LeftPanel leftPanel) {
        this.graph      = graph;
        this.graphPanel = graphPanel;
        this.footer     = footer;
        this.leftPanel  = leftPanel;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BG);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // ── display toggles ───────────────────────────────────────────
        add(sectionLabel("Wyświetlanie"));

        JCheckBox showIDs = styledCheck("Identyfikatory wierzchołków");
        showIDs.setSelected(true);
        showIDs.addActionListener(e ->
            graphPanel.setShowVertexDecorators(showIDs.isSelected()));
        add(showIDs);
        add(vgap(4));

        JCheckBox showWeights = styledCheck("Wagi krawędzi");
        showWeights.setSelected(true);
        showWeights.addActionListener(e ->
            graphPanel.setShowEdgeDecorators(showWeights.isSelected()));
        add(showWeights);
        add(vgap(14));

        // ── view controls ─────────────────────────────────────────────
        add(sectionLabel("Widok"));

        JButton resetZoomBtn = styledButton("Resetuj zoom");
        resetZoomBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        resetZoomBtn.addActionListener(e -> {
            graphPanel.setZoom(1.0);
            footer.setStatus("Zoom zresetowany");
        });
        add(resetZoomBtn);
        add(vgap(6));

        JButton centerBtn = styledButton("Wyśrodkuj widok");
        centerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        centerBtn.addActionListener(e -> {
            graphPanel.resetView();
            footer.setStatus("Widok wyśrodkowany");
        });
        add(centerBtn);
        add(vgap(14));

        // ── save ──────────────────────────────────────────────────────
        add(sectionLabel("Eksport"));

        JButton saveBtn = styledButton("Zapisz zmiany pozycji");
        saveBtn.setBackground(new Color(30, 90, 60));
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        saveBtn.addActionListener(e -> savePositions());
        add(saveBtn);

        add(Box.createVerticalGlue());

        // ── back ──────────────────────────────────────────────────────
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(48, 48, 72));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        add(sep);
        add(vgap(8));

        JButton backBtn = styledButton("← Wróć do ustawień");
        backBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        backBtn.addActionListener(e -> leftPanel.showCard("settings"));
        add(backBtn);
    }

    /** Called by GraphSettingsCard right before switching to this panel. */
    public void activate(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    // ─────────────────────────────────────────────────────────────────
    // Save positions
    // ─────────────────────────────────────────────────────────────────

    private void savePositions() {
        if (inputFilePath.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Brak wczytanego grafu.", "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File   inputFile = new File(inputFilePath);
        String baseName  = inputFile.getName().replaceFirst("[.][^.]+$", "");
        File   outFile   = new File(inputFile.getParentFile(),
                                    baseName + "_newPositions.txt");

        try {
            Collection<Vertex> vertices = graph.getVertices();
            GraphFileReader.writeTextOutput(vertices, outFile.getAbsolutePath());
            footer.setStatus("Zapisano: " + outFile.getName());
            JOptionPane.showMessageDialog(this,
                "Pozycje zapisane do:\n" + outFile.getAbsolutePath(),
                "Sukces", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Błąd zapisu:\n" + ex.getMessage(),
                "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }
}