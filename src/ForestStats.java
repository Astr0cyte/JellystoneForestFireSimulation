import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collector;

public record ForestStats(
        long trees,
        long burning,
        double averageHealth,
        Map<Tree.TreeSpecies, Long> speciesCounts
) {

    // This replaces the "healthy" tree stream operation in Main.java.
    public long healthy() {
        return trees - burning;
    }

    // Custom collector.
    public static Collector<Tree, ?, ForestStats> collector() {
        return Collector.of(Tally::new, Tally::add, Tally::combine, Tally::finish);
    }

    // Mutable container for processing each tree in the forest.
    private static class Tally {

        private long trees;
        private long burning;
        private double healthTotal;

        // Species remain in correct enum order thanks to enum map.
        private final Map<Tree.TreeSpecies, Long> speciesCounts = new EnumMap<>(Tree.TreeSpecies.class);

        // ACCUMULATOR: called once for every tree in the stream.
        void add(Tree tree) {
            trees += 1;

            if(tree.isBurning()) { burning += 1; }

            healthTotal += tree.getHealth();
            speciesCounts.merge(tree.getSpecies(), 1L, Long::sum);
        }

        // COMBINER: merge recursive tally instances together (during parallel streams).
        Tally combine(Tally other) {
            this.trees += other.trees;
            this.burning += other.burning;
            this.healthTotal += other.healthTotal;

            other.speciesCounts.forEach((species, count) ->
                speciesCounts.merge(species, count, Long::sum)
            );
            
            return this;
        }

        // FINISHER: build result.
        ForestStats finish() {
            double averageHealth;

            if(trees > 0) {
                averageHealth = healthTotal / trees;
            } else {
                averageHealth = 0.0;
            }

            return new ForestStats(trees, burning, averageHealth, speciesCounts);
        }
    }
}
