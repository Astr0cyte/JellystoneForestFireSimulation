import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ForestPanel extends JPanel {

    // The panel only draws the forest, so any Tree subtype will do.
    private Forest<? extends Tree> forest;

    private Image treeImage;
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

        setBackground(Color.BLACK);

        treeImage = loadImage("images/tree.png");
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

        for (int row = 0; row < forest.getRows(); row++) {

            for (int col = 0; col < forest.getColumns(); col++) {

                Cell<? extends Tree> cell = forest.getCell(row, col);

                g.setColor(Color.BLACK);

                g.fillRect(
                    col * cellWidth,
                    row * cellHeight,
                    cellWidth,
                    cellHeight
                );

                if (lightning != null &&
                    cell == currentLightningCell &&
                    System.currentTimeMillis() - lightningStartTime < 3000) {

                    drawCell(
                        g,
                        lightningImage,
                        Color.YELLOW,
                        col * cellWidth + 2,
                        row * cellHeight + 2,
                        cellWidth - 4,
                        cellHeight - 4
                    );
                }
                else {
                    if (!cell.hasTree()) {

                        // Empty cell stays black

                    }
                    else if (cell.getTree().isBurning()) {

                        drawCell(
                            g,
                            fireImage,
                            Color.ORANGE,
                            col * cellWidth + 2,
                            row * cellHeight + 2,
                            cellWidth - 4,
                            cellHeight - 4
                        );
                    }
                    else {

                        drawCell(
                            g,
                            treeImage,
                            Color.GREEN,
                            col * cellWidth + 2,
                            row * cellHeight + 2,
                            cellWidth - 4,
                            cellHeight - 4
                        );
                    }
                }
            }
        }
    }
}