import java.util.EnumMap;
import java.util.List;

public class ClusteringSelector implements SpeciesSelector {
    
    private static final double CLUSTER_BONUS = 12.0;

    private EnumMap<Tree.TreeSpecies, Double> weights;

    public ClusteringSelector(EnumMap<Tree.TreeSpecies, Double> weights) {
        this.weights = weights;
    }

    @Override
    public Tree.TreeSpecies selectSpecies(List<Tree.TreeSpecies> neighbourSpecies) {
    
        // Temporary copy to be used for adding cluster bonuses 
        EnumMap<Tree.TreeSpecies, Double> boostedWeights = new EnumMap<>(weights);

        for(Tree.TreeSpecies species : neighbourSpecies) {
            boostedWeights.merge(species, CLUSTER_BONUS, Double::sum);
        }

        // This code is just weighted selection + adding a bonus for neighbours!
        // Weighted selection functionality can be reused here!
        return new WeightedSelector(boostedWeights).selectSpecies();
    }
}
