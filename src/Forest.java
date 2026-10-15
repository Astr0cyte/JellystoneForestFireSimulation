public class Forest {

    private Cell[][] grid;

    private Wind wind;

    private static final double MIN_SPREADABILITY = 0.3;
    private static final double MAX_SPREADABILITY = 1.0;

    public Forest(int rows, int columns) {

        grid = new Cell[rows][columns];

        for (int row = 0; row < rows; row++) {

            for (int col = 0; col < columns; col++) {

                grid[row][col] = new Cell(row, col);

                // 80% chance of there being a tree
                if (Math.random() < 0.8) {

                    double spreadability = MIN_SPREADABILITY + Math.random() * (MAX_SPREADABILITY - MIN_SPREADABILITY);

                    grid[row][col].plantTree(new Tree(spreadability));
                }
            }
        }
    }

    public Cell[][] getGrid() {
        return grid;
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

        for (Cell[] row : grid) {

            for (Cell cell : row) {

                if (!cell.hasTree()) {
                    continue;
                }

                Tree tree = cell.getTree();

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

        boolean[][] igniteNext = new boolean[grid.length][grid[0].length];

        for (int row = 0; row < grid.length; row++) {

            for (int col = 0; col < grid[row].length; col++) {

                Cell cell = grid[row][col];

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
        for (int row = 0; row < grid.length; row++) {

            for (int col = 0; col < grid[row].length; col++) {

                if (igniteNext[row][col]) {
                    grid[row][col].getTree().ignite();
                }
            }
        }
    }

    private void trySpread(int sourceRow, int sourceCol, int row, int col, boolean[][] igniteNext) {

        // If out of bounds
        if (row < 0 || row >= grid.length || col < 0 || col >= grid[0].length) {
            return;
        }

        Cell target = grid[row][col];

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

        for (Cell[] row : grid) {

            for (Cell cell : row) {

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
