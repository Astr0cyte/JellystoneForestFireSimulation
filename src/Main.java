import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        Forest forest = new Forest(40, 60);

        Wind wind = new Wind(30, "EAST", 30, 20, 15);
        forest.setWind(wind);

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
            source = new Backburning(20);
            System.out.println("Ignition source: Backburning");
        }

        try {
            source.ignite(forest);
        }
        catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("Ignition failed: " + e.getMessage());
        }

        JFrame window = new JFrame("Forest Fire Simulation");

        ForestPanel panel = new ForestPanel(forest, lightning);

        window.add(panel);
        window.pack();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        Timer timer = new Timer(500, event -> {
            forest.update();
            panel.repaint();
        });

        timer.start();
    }
}