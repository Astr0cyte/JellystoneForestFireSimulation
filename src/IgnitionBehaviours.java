// A utility class holding the ignition algorithms (the "strategies").
// Each method builds and returns an IgnitionBehaviour as a lambda, which an
// IgnitionSource record then holds and runs when it is ignited.
public final class IgnitionBehaviours {

    private static final double BB_WAVELENGTH = 3.0; // For backburning ignition behaviour

    private IgnitionBehaviours() {

    }

    static IgnitionBehaviour backburning(int row) {

        return (forest, ignitionSource) -> {

            if (row < 0 || row >= forest.getRows()) {
                return;
            }

            double frequency = 2 * Math.PI / BB_WAVELENGTH;

            for (int col = 0; col < forest.getColumns(); col++) {

                Cell<? extends Tree> cell = forest.getCell(row, col);

                if (cell.hasTree()) {

                    double wave = (Math.sin(col * frequency) + 1) / 2;

                    // Only the troughs of the wave catch immediately - the
                    // rest of the line fills in as fire spreads from there.
                    // Severity and spreadability are read from the record
                    // that's running this behaviour.
                    if (wave < ignitionSource.spreadability()) {
                        cell.getTree().ignite(ignitionSource.severity());
                    }
                }
            }
        };
    }

    static IgnitionBehaviour arson(int row, int col) {
        
        return (forest, ignitionSource) -> {

            // Local copy: the captured variables (row) must be effectively final.
            int searchRow = row;

            if (!forest.isInBounds(row, col)) {

                throw new IllegalArgumentException(
                    "Coordinates must be within x: 0-"+forest.getRows()+
                    " & y: 0-"+forest.getColumns());
            }

            Cell<? extends Tree> cell = forest.getCell(row, col);


            // "While there is no valid tree and we are within bounds".
            while(!cell.hasTree() && forest.isInBounds(searchRow, col)) {
                cell = forest.getCell(searchRow, col);
                searchRow++;
            }

            // Ran out of bounds while retrying for a valid cell.
            // hasTree() would still be false if this is the case.
            if (!cell.hasTree()) {

                throw new IllegalArgumentException(
                    "Ran out of bounds while trying to find a tree to ignite.");
            }
            
            if (cell.getTree().getSpreadability() >= ignitionSource.spreadability()) {
                cell.getTree().ignite(ignitionSource.severity());
            }
        };
    }
}
