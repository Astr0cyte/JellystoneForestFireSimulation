import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.*;

public class Forest<T extends Tree> {

    // The grid is a list of rows, where each row is a list of cells.
    // COMP2100: ROW-MAJOR ORDERING!
    private final List<List<Cell<T>>> grid; 
    private Wind wind;

    // For "neighbours()" method.
    private static final int[][] NEIGHBOUR_OFFSETS = {{-1, -1}, {-1, 0}, {-1, +1},
                                                      {0, -1},  /*CELL*/ {0, +1},
                                                      {+1, -1}, {+1, 0}, {+1, +1}};

    public Forest(int rows, int columns, Supplier<T> treeSupplier) {
        this(rows, columns, neighbourSpecies -> treeSupplier.get());
    }

    public Forest(int rows, int columns, Function<List<Tree.TreeSpecies>, T> treeSource) {

        // Building is now done in TWO passes. 
        // In pass 2, The functionality of neighbour() is required, and that relies on a fully built grid.
        // neighbour() is required for the NEW clustering (biome) species selector. 

        // First pass: build grid.
        grid = new ArrayList<>(rows);

        for (int row = 0; row < rows; row++) {
            List<Cell<T>> currentRow = new ArrayList<>(columns);

            for (int col = 0; col < columns; col++) {
                currentRow.add(new Cell<>(row, col));
            }

            grid.add(currentRow);

        }
    
    // Second pass: plant trees + get neighbour information.
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {

                Cell<T> cell = getCell(row, col);

                // 80% chance of there being a tree
                if (Math.random() < 0.8) {

                    // Get neighbour information using a stream & collecting toList().
                    List<Tree.TreeSpecies> neighbourSpecies = neighbours(cell).stream()
                            .filter(Cell::hasTree)
                            .map(neighbour -> neighbour.getTree().getSpecies())
                            .toList();

                    // Planting the tree: forest responsibility.
                    // Generating the tree: supplier's responsibility - TreeFactory, passed in from Main.
                    try {
                        cell.plantTree(treeSource.apply(neighbourSpecies));
                    }
                    catch (IllegalArgumentException e) {
                        // An invalid tree shouldn't stop the whole forest being
                        // built - leave this cell empty and carry on.
                        System.err.println("Skipped tree at (" + row + ", " + col + "): " + e.getMessage());
                    }
                }
            }
        }
    }

    public Stream<Cell <T>> cellStream() {
        return grid.stream()
                   .flatMap(List::stream); // Turns the stream of 40 rows into a single stream of (2400) cells.
        
    }

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

    // Returns every cell touching the given cell (up to 8).
    // Skips any out of bounds cell selections.
    public List<Cell<T>> neighbours(Cell<T> cell) {

        List<Cell<T>> result = new ArrayList<>();

        for (int[] offset : NEIGHBOUR_OFFSETS) {

            int row = cell.getRow() + offset[0];
            int col = cell.getCol() + offset[1];

            if (isInBounds(row, col)) {
                result.add(getCell(row, col));
            }
        }

        return result;
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
                    // The tree burned down: remove it, and leave scorched ground behind.
                    cell.removeTree();
                    cell.markScorched();
                }
            }
        }
    }

    // Benchmark switch: true = parallel stream (default), false = sequential.
    private boolean parallel = true;

    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }

    void spreadFire() {

        Set<Cell<T>> igniteTheseTrees = (parallel ? cellStream().parallel() : cellStream()) // PARALLEL BY DEFAULT.
                                        .filter((Cell<T> cell) -> cell.hasTree() && cell.getTree().isBurning())
                                        .flatMap((Cell<T> source) -> neighbours(source).stream()
                                        .filter(target -> catchesFire(source, target)))
                                        .collect(Collectors.toSet()); // A set of tree neighbours to be ignited.

        for(Cell<T> cell : igniteTheseTrees) {
            cell.getTree().ignite();
        }
    }

    // Read-only version of trySpread() - returns a boolean instead of writing the boolean into array.
    private boolean catchesFire(Cell<T> source, Cell<T> target) {
        if (!target.hasTree() || target.getTree().isBurning()) {
            return false;
        }

        double chance = target.getTree().getSpreadability();

        if (wind != null) {
            // Wind uses x = column, y = row, so columns come first.
            // This was a decision grandfathered in from AST1 - not important enough to change.
            chance += wind.getSpreadModifier(
                source.getCol(), source.getRow(),
                target.getCol(), target.getRow()
            );
        }

        chance = Math.max(0.0, Math.min(1.0, chance));

        return ThreadLocalRandom.current().nextDouble() < chance;
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
