import java.awt.BorderLayout;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.swing.*;

public class Main {

    private static final int TICK_DELAY = 750;

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
            // Unlike the lambdas, this object is also needed by ForestPanel to
            // draw where the strike landed, so it's stored in `lightning` too.

            lightning = new Lightning();

            source = new IgnitionSource(
                "Lightning", // label
                0.8, 
                0.7, 
                lightning // behaviour parameter - random strike.
            );
            
            System.out.println("Ignition source: Lightning");

        } else if (choice == 1) {
            // Weaker than a lightning strike (severity 0.8): a deliberately
            // lit fire is smaller and only takes hold on sufficiently
            // flammable fuel.

            source = new IgnitionSource(
                "Arson", 
                0.55, 
                0.5, 
                IgnitionBehaviours.arson(20,20) // behaviour parameter (lambda)
            );
            
            System.out.println("Ignition source: Arson");

        } else {
            // Controlled and deterministic (unlike Arson/Lightning): lit in a
            // staggered, wavelike pattern along the row rather than all at once.
            source = new IgnitionSource(
                "Backburning",
                0.7,                                
                0.4,                           
                IgnitionBehaviours.backburning(20)  // behaviour parameter (lambda)
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

        Timer timer;
        timer = new Timer(TICK_DELAY, event -> {
            forest.update();

        int currentTick = tick.incrementAndGet();

        // Stats stream with custom collector.
        ForestStats stats = forest.cellStream()
                                    .filter(Cell::hasTree)
                                    .map(Cell::getTree)
                                    .collect(ForestStats.collector());

/* DEPRECATED - 5 sequential streams replaced by custom ForestStats.collector().

            long trees = forest.cellStream()
                    .filter((Cell<Tree> cell) -> cell.hasTree())
                    .count();
            
            long healthy = forest.cellStream()
                    .filter((Cell<Tree> cell) -> cell.hasTree())
                    .map(cell -> cell.getTree())
                    .filter((Tree tree) -> !tree.isBurning())
                    .count();
            
            long burning = forest.cellStream()
                    .filter((Cell<Tree> cell) -> cell.hasTree())
                    .map(cell -> cell.getTree())
                    .filter((Tree tree) -> tree.isBurning())
                    .count();
            
            double health = forest.cellStream()
                    .filter((Cell<Tree> cell) -> cell.hasTree())
                    .mapToDouble(cell -> cell.getTree().getHealth()) // Map into the health value
                    .average()
                    .orElse(0.0);
            
            Map<Tree.TreeSpecies, Long> speciesCount = forest.cellStream()
                    .filter((Cell<Tree> cell) -> cell.hasTree())
                    .map(cell -> cell.getTree())
                    .collect(Collectors.groupingBy(Tree::getSpecies, Collectors.counting()));
  */

            panel.repaint();
            statsPanel.update(currentTick, stats);
        });

        timer.start();
    }
}