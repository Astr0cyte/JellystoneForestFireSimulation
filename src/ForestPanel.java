import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ForestPanel extends JPanel {

    // The panel only draws the forest, so any Tree subtype will do.
    private Forest<? extends Tree> forest;

    private Image oakTreeImage;
    private Image birchTreeImage;
    private Image spruceTreeImage;
    private Image eucalyptusTreeImage;
    private Image jungleTreeImage;
    private Image fireImage;

    private Lightning lightning;
    private Image lightningImage;

    private Cell<? extends Tree> lastLightningCell;
    private long lightningStartTime;

    // Pannel becomes 60 * 15 by 40 * 15
    private static final int CELL_SIZE = 15;

    public ForestPanel(Forest<? extends Tree> forest, Lightning lightning) {
        this.forest = forest;
        this.lightning = lightning;

        setPreferredSize(new Dimension(forest.getColumns() * CELL_SIZE, forest.getRows() * CELL_SIZE));

        setBackground(new Color(20, 29, 0));

        oakTreeImage = loadImage("images/oak_tree.png");
        birchTreeImage = loadImage("images/birch_tree.png");
        spruceTreeImage = loadImage("images/spruce_tree.png");
        eucalyptusTreeImage = loadImage("images/eucalyptus_tree.png");
        jungleTreeImage = loadImage("images/jungle_tree.png");
        fireImage = loadImage("images/fire.png");
        lightningImage = loadImage("images/lightning.png");
    }

    // ImageIO.read throws a checked IOException if the file can't be read,
    // so the compiler forces us to handle it. Returning null means
    // drawCell() falls back to a coloured square.
    private Image loadImage(String path) {
        try {
            return ImageIO.read(new File(path));
        }
        catch (IOException e) {
            System.err.println("Could not load " + path + " (" + e.getMessage() + ") - using a coloured square instead.");
            return null;
        }
    }

    private void drawCell(Graphics g, Image image, Color fallbackColor, int x, int y, int width, int height) {
        if (image != null) {
            g.drawImage(image, x, y, width, height, this);
        }
        else {
            g.setColor(fallbackColor);
            g.fillRect(x, y, width, height);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int cellWidth = getWidth() / forest.getColumns();
        int cellHeight = getHeight() / forest.getRows();

        Cell<? extends Tree> currentLightningCell = null;

        if (lightning != null) {
            currentLightningCell = lightning.getLastStruckCell();

            if (currentLightningCell != lastLightningCell) {
                lastLightningCell = currentLightningCell;
                lightningStartTime = System.currentTimeMillis();
            }
        }

        // Worked out once per repaint rather than once per cell.
        boolean showLightning = currentLightningCell != null
                && System.currentTimeMillis() - lightningStartTime < 3000;

        for (int row = 0; row < forest.getRows(); row++) {

            for (int col = 0; col < forest.getColumns(); col++) {

                Cell<? extends Tree> cell = forest.getCell(row, col);

                // Position and size of the image inside this cell (2px padding).
                int x = col * cellWidth + 2;
                int y = row * cellHeight + 2;
                int width = cellWidth - 4;
                int height = cellHeight - 4;

                // Each check handles one case and moves on to the next cell,
                // so there's no nesting. Empty cells need no drawing at all -
                // super.paintComponent() has already filled the background black.
                if (showLightning && cell == currentLightningCell) {
                    drawCell(g, lightningImage, Color.YELLOW, x, y, width, height);
                    continue;
                }

                if (!cell.hasTree()) {
                    continue;
                }

                Tree tree = cell.getTree();

                if (tree.isBurning()) {
                    drawCell(g, fireImage, Color.ORANGE, x, y, width, height);
                    continue;
                }

                // A switch expression picks a value for each case. Arrow cases
                // never fall through, so no break is needed. Every species is
                // listed and there's no default, so adding a species to the
                // enum without an image here is a compile error.
                Image speciesImage = switch (tree.getSpecies()) {
                    case OAK -> oakTreeImage;
                    case BIRCH -> birchTreeImage;
                    case SPRUCE -> spruceTreeImage;
                    case EUCALYPTUS -> eucalyptusTreeImage;
                    case JUNGLE -> jungleTreeImage;
                };

                drawCell(g, speciesImage, Color.GREEN, x, y, width, height);
            }
        }
    }
}