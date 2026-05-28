package graph;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static graph.GraphSettingsCard.*;

/**
 * Mock version of GraphInteractionPanel.
 * <p>
 * Provides the same UI layout and user interactions, but all actual graph
 * operations are replaced with dummy actions that only update the Footer.
 * The back button still switches to the settings panel.
 */
public class GraphInteractionPanel extends JPanel {

    private final Footer    footer;
    private final LeftPanel leftPanel;

    private String inputFilePath = "";   // stored but not used (mock)

    // Constructor signature unchanged – graph and graphPanel are ignored
    public GraphInteractionPanel(Graph graph, GraphPanel graphPanel,
                                 Footer footer, LeftPanel leftPanel) {
        this.footer    = footer;
        this.leftPanel = leftPanel;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BG);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // ── display toggles (mock – only updates footer) ─────────────
        add(sectionLabel("Wyświetlanie"));

        JCheckBox showIDs = styledCheck("Identyfikatory wierzchołków");
        showIDs.setSelected(true);
        showIDs.addActionListener(e ->
            footer.setStatus("Mock: wyświetlanie identyfikatorów = " + showIDs.isSelected())
        );
        add(showIDs);
        add(vgap(4));

        JCheckBox showWeights = styledCheck("Wagi krawędzi");
        showWeights.setSelected(true);
        showWeights.addActionListener(e ->
            footer.setStatus("Mock: wyświetlanie wag = " + showWeights.isSelected())
        );
        add(showWeights);
        add(vgap(14));

        // ── view controls (mock – no actual zoom/center) ─────────────
        add(sectionLabel("Widok"));

        JButton resetZoomBtn = styledButton("Resetuj zoom");
        resetZoomBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        resetZoomBtn.addActionListener(e ->
            footer.setStatus("Mock: zoom zresetowany (brak rzeczywistej akcji)")
        );
        add(resetZoomBtn);
        add(vgap(6));

        JButton centerBtn = styledButton("Wyśrodkuj widok");
        centerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        centerBtn.addActionListener(e ->
            footer.setStatus("Mock: widok wyśrodkowany (brak rzeczywistej akcji)")
        );
        add(centerBtn);
        add(vgap(14));

        // ── export (mock – no file I/O) ──────────────────────────────
        add(sectionLabel("Eksport"));

        JButton saveBtn = styledButton("Zapisz zmiany pozycji");
        saveBtn.setBackground(new Color(30, 90, 60));
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        saveBtn.addActionListener(e -> mockSavePositions());
        add(saveBtn);

        add(Box.createVerticalGlue());

        // ── back button (switches to settings card, same as original) ─
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

    /**
     * Called by GraphSettingsCard right before switching to this panel.
     * In the mock, it only stores the path and updates the footer.
     */
    public void activate(String inputFilePath) {
        this.inputFilePath = inputFilePath;
        footer.setStatus("Mock: aktywowano panel interakcji dla " + inputFilePath);
    }

    // ─────────────────────────────────────────────────────────────────
    // Mock save – no real file writing, only UI feedback
    // ─────────────────────────────────────────────────────────────────
    private void mockSavePositions() {
        if (inputFilePath.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Brak wczytanego grafu (mock).", "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Simulate a successful save
        footer.setStatus("Mock: zapisano pozycje (żadne pliki nie zostały zmodyfikowane)");
        JOptionPane.showMessageDialog(this,
            "Mock: pozycje zostałyby zapisane.\n" +
            "Plik wejściowy: " + inputFilePath,
            "Sukces (mock)", JOptionPane.INFORMATION_MESSAGE);
    }
}