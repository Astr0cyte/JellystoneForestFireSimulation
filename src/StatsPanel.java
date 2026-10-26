import java.awt.*;
import javax.swing.*;

public class StatsPanel extends JPanel {

    private static final int WIDTH = 220;

    private final JLabel tickLabel = new JLabel();
    private final JLabel treesLabel = new JLabel();
    private final JLabel burningLabel = new JLabel();
    private final JLabel healthLabel = new JLabel();
    private final JLabel speciesLabel = new JLabel();


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
        burningLabel.setFont(burningLabel.getFont().deriveFont(16.f));
        healthLabel.setFont(healthLabel.getFont().deriveFont(16.f));
        speciesLabel.setFont(speciesLabel.getFont().deriveFont(16.f));

        setBackground(Color.BLACK);
    
        add(title);
        add(Box.createVerticalStrut(10));
        add(tickLabel);
        add(treesLabel);
        add(burningLabel);
        add(healthLabel);
        add(Box.createVerticalStrut(10));
        add(speciesLabel);

        for (Component component : getComponents()) {
            component.setForeground(Color.WHITE);
        }

        update(0, 0, 0);
    }

    // TODO: add a ForestStats parameter and fill the labels from it.
    public void update(int tick, long amtOfTrees, long amtBurning) {

        tickLabel.setText("Tick: " + tick);
        treesLabel.setText("Trees: -" + amtOfTrees);
        burningLabel.setText("Burning: " + amtBurning);
        healthLabel.setText("Average health: -");

        // JLabel understands basic HTML, which is the easiest way to get multiple lines.
        speciesLabel.setText("<html>By species:<br>-</html>");
    }
}
