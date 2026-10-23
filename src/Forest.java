import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

// T is the class of object this forest holds: Forest owns and plants
// its trees, so it needs to know the exact type, unlike the classes
// that only read it, which use Forest<? extends Tree>.
public class Forest<T extends Tree> {

    // The grid is a list of rows, where each row is a list of cells.
    private final List<List<Cell<T>>> grid;
    private Wind wind;

    // treeSupplier is anything that can produce a new generic on demand,
    // e.g. factory::generateTree.
    public Forest(int rows, int columns, Supplier<T> treeSupplier) {

        grid = new ArrayList<>(rows);

        for (int row = 0; row < rows; row++) {

            List<Cell<T>> currentRow = new ArrayList<>(columns);

            for (int col = 0; col < columns; col++) {

                Cell<T> cell = new Cell<>(row, col);

                // 80% chance of there being a tree
                if (Math.random() < 0.8) {

                    // Here marks the distinction between planting the tree (forest responsibility)
                    // and generating the tree (factory responsibility)
                    try {
                        cell.plantTree(treeSupplier.get());
                    }
                    catch (IllegalArgumentException e) {
                        // An invalid tree shouldn't stop the whole forest being
                        // built - leave this cell empty and carry on.
                        System.err.println("Skipped tree at (" + row + ", " + col + "): " + e.getMessage());
                    }
                }

                currentRow.add(cell);
            }

            grid.add(currentRow);
        }
    }

    // public Stream toTreeStream(){
    //     // stream converter from grid
    //     return Stream();
    // }
    // public toFireStream(){

    // }

    // Accessors for other classes.
    public Cell<T> getCell(int row, int col) {
        return grid.get(row).get(col);
    }

    public int getRows() {
        return grid.size();
    }

    public int getColumns() {
        return grid.get(0).size();
    }

    public boolean isInBounds(int row, int col) {
        return row >= 0 && row < getRows() && col >= 0 && col < getColumns();
    }

    public void setWind(Wind wind) {
        this.wind = wind;
        wind.applyWind(this);
    }

    public Wind getWind() {
        return wind;
    }

    public void update() {

        spreadFire();
        advanceOneBurnTick();
    }

    // 1 burn tick = deplete tree health by certain amount.
    private void advanceOneBurnTick() {

        for (List<Cell<T>> row : grid) {

            for (Cell<T> cell : row) {

                if (!cell.hasTree()) {
                    continue;
                }

                T tree = cell.getTree();

                if (tree.isBurning()) {
                    tree.burnTick();
                }

                if (!tree.isAlive()) {
                    cell.removeTree();
                }
            }
        }
    }

    private void spreadFire() {

        boolean[][] igniteNext = new boolean[getRows()][getColumns()];

        for (int row = 0; row < getRows(); row++) {

            for (int col = 0; col < getColumns(); col++) {

                Cell<T> cell = getCell(row, col);

                if (cell.hasTree() && cell.getTree().isBurning()) {

                    trySpread(row, col, row - 1, col, igniteNext);
                    trySpread(row, col, row + 1, col, igniteNext);
                    trySpread(row, col, row, col - 1, igniteNext);
                    trySpread(row, col, row, col + 1, igniteNext);
                    trySpread(row, col, row - 1, col - 1, igniteNext);
                    trySpread(row, col, row - 1, col + 1, igniteNext);
                    trySpread(row, col, row + 1, col - 1, igniteNext);
                    trySpread(row, col, row + 1, col + 1, igniteNext);
                }
            }
        }

        // Ignite AFTER checking the entire grid
        for (int row = 0; row < getRows(); row++) {

            for (int col = 0; col < getColumns(); col++) {

                if (igniteNext[row][col]) {
                    getCell(row, col).getTree().ignite();
                }
            }
        }
    }

    private void trySpread(int sourceRow, int sourceCol, int row, int col, boolean[][] igniteNext) {

        if (!isInBounds(row, col)) {
            return;
        }

        Cell<T> target = getCell(row, col);

        if (!target.hasTree()) {
            return;
        }

        if (target.getTree().isBurning()) {
            return;
        }

        // Base chance is the tree's spreadability (0.0-1.0, already scaled by species in Tree).
        double chance = target.getTree().getSpreadability();

        if (wind != null) {
            chance += wind.getSpreadModifier(sourceCol, sourceRow, col, row);
        }

        chance = Math.max(0.0, Math.min(1.0, chance));

        if (Math.random() < chance) {
            igniteNext[row][col] = true;
        }
    }

     public void cliTextDisplay() {

        for (List<Cell<T>> row : grid) {

            for (Cell<T> cell : row) {

                if (!cell.hasTree()) {
                    System.out.print(". ");

                } else if (cell.getTree().isBurning()) {
                    System.out.print("X ");

                } else {
                    System.out.print("T ");
                }
            }

            System.out.println();
        }
    }
}
