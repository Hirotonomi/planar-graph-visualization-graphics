package graph;

import javax.swing.*;
import java.awt.*;

public class LeftPanel extends JPanel {

    private final CardLayout             cardLayout        = new CardLayout();
    private final GraphInteractionPanel  interactionPanel;

    public LeftPanel(Graph graph, GraphPanel graphPanel, Footer footer) {
        setLayout(cardLayout);
        setPreferredSize(new Dimension(270, 0));
        setBackground(GraphSettingsCard.BG);

        interactionPanel = new GraphInteractionPanel(
                graph, graphPanel, footer, this);

        add(new GraphSettingsCard(graph, graphPanel, footer, this), "settings");
        add(interactionPanel, "interaction");
    }

    public void showCard(String name) {
        cardLayout.show(this, name);
    }

    public GraphInteractionPanel getInteractionPanel() {
        return interactionPanel;
    }
}