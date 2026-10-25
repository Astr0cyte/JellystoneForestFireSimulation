import java.awt.BorderLayout;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        TreeFactory factory = new TreeFactory();

        // factory::generateTree hands the forest the generateTree method
        // itself, which the forest calls once per tree it plants.
        Forest<Tree> forest = new Forest<>(40, 60, factory::generateTree);

        // Wind is optional (Forest checks for null), so if it can't be
        // created or applied, run the simulation without it.
        try {
            Wind wind = new Wind(30, "EAST", 30, 20, 15);
            forest.setWind(wind);
        }
        catch (WindArgumentException e) {
            System.out.println("Wind disabled: " + e.getMessage());
        }

        IgnitionSource source;
        Lightning lightning = null;

        // Math.random() gives 0.0-0.99..., so * 3 then (int) gives 0, 1 or 2
        int choice = (int) (Math.random() * 3);

        if (choice == 0) {
            lightning = new Lightning();
            source = lightning;
            System.out.println("Ignition source: Lightning");

        } else if (choice == 1) {
            source = new Arson(20, 20);
            System.out.println("Ignition source: Arson");

        } else {
            // Controlled and deterministic (unlike Arson/Lightning): lit in a
            // staggered, wavelike pattern along the row rather than all at once.
            source = new IgnitionSource(
                "Backburning",
                0.7,                                // severity
                0.4,                                // spreadability
                IgnitionBehaviours.backburning(20)  // behaviour: wave along row 20
            );
            System.out.println("Ignition source: " + source.sourceName());
        }

        try {
            source.ignite(forest);
        }
        catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("Ignition failed: " + e.getMessage());
        }

        JFrame window = new JFrame("Forest Fire Simulation");

        ForestPanel panel = new ForestPanel(forest, lightning);
        StatsPanel statsPanel = new StatsPanel();

        // Stats panel sits on the right next to simulation.
        window.add(panel, BorderLayout.CENTER);
        window.add(statsPanel, BorderLayout.EAST);
        window.pack();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        AtomicInteger tick = new AtomicInteger(0);

        Timer timer = new Timer(500, event -> {
            forest.update();
            int currentTick = tick.incrementAndGet();

            panel.repaint();
            statsPanel.update(currentTick);
        });

        timer.start();
    }
}