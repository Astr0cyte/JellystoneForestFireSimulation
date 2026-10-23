import java.awt.*;
import javax.swing.*;

public class ForestPanel extends JPanel {

    private Forest forest;

    private Image treeImage;
    private Image fireImage;

    private Lightning lightning;
    private Image lightningImage;

    private Cell lastLightningCell;
    private long lightningStartTime;

    // Pannel becomes 60 * 15 by 40 * 15
    private static final int CELL_SIZE = 15;

    public ForestPanel(Forest forest, Lightning lightning) {
        this.forest = forest;
        this.lightning = lightning;

        setPreferredSize(new Dimension(forest.getColumns() * CELL_SIZE, forest.getRows() * CELL_SIZE));

        setBackground(Color.BLACK);

        System.out.println(new java.io.File("images/tree.png").exists());
        System.out.println(new java.io.File("images/fire.png").exists());
        System.out.println(new java.io.File("images/lightning.png").exists());

        treeImage = new ImageIcon("images/tree.png").getImage();
        fireImage = new ImageIcon("images/fire.png").getImage();
        lightningImage = new ImageIcon("images/lightning.png").getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int cellWidth = getWidth() / forest.getColumns();
        int cellHeight = getHeight() / forest.getRows();

        Cell currentLightningCell = null;

        if (lightning != null) {
            currentLightningCell = lightning.getLastStruckCell();

            if (currentLightningCell != lastLightningCell) {
                lastLightningCell = currentLightningCell;
                lightningStartTime = System.currentTimeMillis();
            }
        }

        for (int row = 0; row < forest.getRows(); row++) {

            for (int col = 0; col < forest.getColumns(); col++) {

                Cell cell = forest.getCell(row, col);

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

                    g.drawImage(
                        lightningImage,
                        col * cellWidth + 2,
                        row * cellHeight + 2,
                        cellWidth - 4,
                        cellHeight - 4,
                        this
                    );
                }
                else {
                    if (!cell.hasTree()) {

                        // Empty cell stays black

                    }
                    else if (cell.getTree().isBurning()) {

                        g.drawImage(
                            fireImage,
                            col * cellWidth + 2,
                            row * cellHeight + 2,
                            cellWidth - 4,
                            cellHeight - 4,
                            this
                        );
                    }
                    else {

                        g.drawImage(
                            treeImage,
                            col * cellWidth + 2,
                            row * cellHeight + 2,
                            cellWidth - 4,
                            cellHeight - 4,
                            this
                        );
                    }
                }
            }
        }
    }
}