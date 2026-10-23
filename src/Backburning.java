public class Backburning extends IgnitionSource {

    private static final double WAVELENGTH = 3.0;

    private int row;

    public Backburning(int row) {

        // Controlled and deterministic (unlike Arson/Lightning): lit in a
        // staggered, wavelike pattern along the row rather than all at once.
        super(0.7, 0.4);

        this.row = row;
    }

    @Override
    public void ignite(Forest<? extends Tree> forest) {

        if (row < 0 || row >= forest.getRows()) {
            return;
        }

        double frequency = 2 * Math.PI / WAVELENGTH;

        for (int col = 0; col < forest.getColumns(); col++) {

            Cell<? extends Tree> cell = forest.getCell(row, col);

            if (cell.hasTree()) {

                double wave = (Math.sin(col * frequency) + 1) / 2;

                // Only the troughs of the wave catch immediately - the
                // rest of the line fills in as fire spreads from there.
                if (wave < spreadability) {
                    cell.getTree().ignite(severity);
                }
            }
        }
    }
}