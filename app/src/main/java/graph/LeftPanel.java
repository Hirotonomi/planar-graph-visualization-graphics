package graph;

import javax.swing.*;
import java.awt.*;

public class LeftPanel extends JPanel {

    private final CardLayout cardLayout = new CardLayout();

    public LeftPanel(Graph graph, GraphPanel graphPanel, Footer footer) {
        setLayout(cardLayout);
        setPreferredSize(new Dimension(270, 0));
        setBackground(new Color(20, 20, 30));

        add(new GraphSettingsCard(graph, graphPanel, footer), "graph");
        // add(new GraphInteractionCard(graph, graphPanel, footer),          "edit");
    }

    public void showCard(String name) {
        cardLayout.show(this, name);
    }
}