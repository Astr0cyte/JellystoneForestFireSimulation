import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Lightning extends IgnitionSource {
    private Random random = new Random();
    private List<Cell> struckCells = new ArrayList<>();
    private Cell lastStruckCell;
    private static final int MAX_ATTEMPTS = 10;

    public Lightning() {
        super(0.8, 0.7);
    }

    public Cell getLastStruckCell() {
        return lastStruckCell;
    }
    
    public void ignite(Forest forest) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            int row = random.nextInt(forest.getRows());
            int col = random.nextInt(forest.getColumns());
            Cell cell = forest.getCell(row, col);

            if (struckCells.contains(cell)) {
                continue;
            }
            
            if (cell.hasTree()) {
                lastStruckCell = cell;
                struckCells.add(cell);
                cell.getTree().ignite(this.severity);
                return;
            }
        }

        throw new IllegalStateException(
            "Lightning found no ignitable cell after " + MAX_ATTEMPTS + " attempts"
        );
    }
}