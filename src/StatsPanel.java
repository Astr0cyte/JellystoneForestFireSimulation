import java.awt.*;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.*;

public class StatsPanel extends JPanel {

    private static final int WIDTH = 220;

    private final JLabel tickLabel = new JLabel();
    private final JLabel treesLabel = new JLabel();
    private final JLabel healthyTreesLabel = new JLabel();
    private final JLabel burningLabel = new JLabel();
    private final JLabel healthLabel = new JLabel();
    private final JLabel speciesLabel = new JLabel();
    private final JLabel windLabel = new JLabel("Wind: none");

    public StatsPanel() {

        // BoxLayout stacks the labels top to bottom.
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Only the width matters - BorderLayout.EAST stretches the height to fit the window.
        setPreferredSize(new Dimension(WIDTH, 0));

        JLabel title = new JLabel("Forest Stats");

        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        tickLabel.setFont(tickLabel.getFont().deriveFont(16.f));
        treesLabel.setFont(treesLabel.getFont().deriveFont(16.f));
        healthyTreesLabel.setFont(healthyTreesLabel.getFont().deriveFont(16.f));
        burningLabel.setFont(burningLabel.getFont().deriveFont(16.f));
        healthLabel.setFont(healthLabel.getFont().deriveFont(16.f));
        speciesLabel.setFont(speciesLabel.getFont().deriveFont(16.f));
        windLabel.setFont(windLabel.getFont().deriveFont(16.f));

        setBackground(Color.BLACK);
    
        add(title);
        add(Box.createVerticalStrut(10));
        add(tickLabel);
        add(windLabel);
        add(treesLabel);
        add(healthyTreesLabel);
        add(burningLabel);
        add(healthLabel);
        add(Box.createVerticalStrut(10));
        add(speciesLabel);

        for (Component component : getComponents()) {
            component.setForeground(Color.WHITE);
        }

        update(0, new ForestStats(0, 0, 0.0, Map.of()));
    }

    // Called once at startup - the wind doesn't change during a run.
    // A null wind (creation failed in Main) leaves the label as "Wind: none".
    public void showWind(Wind wind) {
        if (wind != null) {
            windLabel.setText("Wind: " + wind.getDirection());
        }
    }

    public void update(int tick, ForestStats stats) {

        tickLabel.setText("Tick: " + tick);
        treesLabel.setText("Trees: " + stats.trees());
        healthyTreesLabel.setText("Unburnt trees: " + stats.healthy());
        burningLabel.setText("Burning: " + stats.burning());
        healthLabel.setText("Average health: " + stats.averageHealth());

        // Yields one "SPECIES: count" line per species.
        String speciesLines = stats.speciesCounts().entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("<br>"));

        speciesLabel.setText("<html>Count by species:<br>" + speciesLines + "</html>");
    }
}
