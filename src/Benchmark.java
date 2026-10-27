import java.util.DoubleSummaryStatistics;
import java.util.stream.DoubleStream;

// AI-generated benchmark code using Claude Opus 5.5

// Times Forest.spreadFire() run sequentially vs as a parallel stream, across
// several grid sizes. Run this class's main() on its own - it is separate
// from the simulation in Main.
//
// Results are indicative only: this is hand-rolled timing (not JMH), and
// every forest is randomly generated.
public class Benchmark {

    // {rows, columns}. 40x60 is the real simulation's grid.
    private static final int[][] GRID_SIZES = {
        {40, 60}, {200, 200}, {500, 500}, {1000, 1000}
    };

    private static final int MEASUREMENTS = 10;  // fresh forest for each one
    private static final int TIMED_CALLS = 5;    // spreadFire calls timed per forest
    private static final int WARMUP_ROUNDS = 3;  // untimed rounds before measuring

    public static void main(String[] args) {

        // The JVM only compiles code to fast machine code after it has run a
        // few times (JIT compilation). Run both modes untimed first, so the
        // first real measurements aren't unfairly slow.
        System.out.println("Warming up...");
        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            timeOneForest(200, 200, false);
            timeOneForest(200, 200, true);
        }

        System.out.printf("%n%-11s %-11s %10s %10s %10s%n",
                "Grid", "Mode", "avg ms", "min ms", "max ms");

        for (int[] size : GRID_SIZES) {
            for (boolean parallel : new boolean[] {false, true}) {

                DoubleSummaryStatistics stats = measure(size[0], size[1], parallel);

                System.out.printf("%-11s %-11s %10.3f %10.3f %10.3f%n",
                        size[0] + "x" + size[1],
                        parallel ? "parallel" : "sequential",
                        stats.getAverage(), stats.getMin(), stats.getMax());
            }
        }
    }

    // Times MEASUREMENTS fresh forests and summarises the results.
    private static DoubleSummaryStatistics measure(int rows, int cols, boolean parallel) {

        double[] times = new double[MEASUREMENTS];

        for (int i = 0; i < MEASUREMENTS; i++) {
            times[i] = timeOneForest(rows, cols, parallel);
        }

        // One stream call gives count, min, max, sum and average together.
        return DoubleStream.of(times).summaryStatistics();
    }

    // Builds a fresh burning forest and returns the average time of one
    // spreadFire() call on it, in milliseconds. Building the forest is not timed.
    private static double timeOneForest(int rows, int cols, boolean parallel) {

        Forest<Tree> forest = new Forest<>(rows, cols, new TreeFactory()::generateTree);
        forest.setParallel(parallel);
        startLargeFire(forest);

        long start = System.nanoTime();

        for (int i = 0; i < TIMED_CALLS; i++) {
            forest.spreadFire();
        }

        return (System.nanoTime() - start) / 1_000_000.0 / TIMED_CALLS;
    }

    // Lights every 4th row so there are plenty of burning cells to process.
    private static void startLargeFire(Forest<Tree> forest) {

        for (int row = 0; row < forest.getRows(); row += 4) {
            new IgnitionSource("Benchmark", 0.7, 0.9, IgnitionBehaviours.backburning(row))
                    .ignite(forest);
        }
    }
}
