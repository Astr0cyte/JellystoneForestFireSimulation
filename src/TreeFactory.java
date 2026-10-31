import java.util.EnumMap;
import java.util.List;

public class TreeFactory {
    
    private static final double MIN_SPREADABILITY = 0.3;
    private static final double MAX_SPREADABILITY = 1.0;

    // 2 ingredients for weighted random selection of tree species.
        // 1 - A table of weights
        // 2 - A random selector which reads those weights
    private final EnumMap<Tree.TreeSpecies, Double> weights = new EnumMap<>(Tree.TreeSpecies.class);
    private final WeightedSelector weightedSelector = new WeightedSelector(weights);

    // Used if the weighted selector can't make a selection.
    private final SpeciesSelector fallbackSelector = new AllOakSelector();

    public TreeFactory() {
        applyWeights();
    }

    // Add weights for weighted random selection of trees.
    private void applyWeights() {
        weights.put(Tree.TreeSpecies.OAK, 5.0);
        weights.put(Tree.TreeSpecies.BIRCH, 2.5);
        weights.put(Tree.TreeSpecies.SPRUCE, 2.5);
        weights.put(Tree.TreeSpecies.EUCALYPTUS, 4.0);
        weights.put(Tree.TreeSpecies.JUNGLE, 1.0);
    }

    public Tree generateTree(List<Tree.TreeSpecies> neighbourSpecies) {

        //Generate random spreadability value - random variation between individual trees.
        double spreadability = MIN_SPREADABILITY + Math.random() * (MAX_SPREADABILITY - MIN_SPREADABILITY);
        
        // Select a tree based on the weights
        Tree.TreeSpecies selectedTreeSpecies;

        try {
            selectedTreeSpecies = weightedSelector.selectSpecies();
        }
        catch (IllegalStateException e) {
            // Weights are unusable (e.g. all zero) - plant an oak rather than crash.
            selectedTreeSpecies = fallbackSelector.selectSpecies();
        }
        return new Tree(selectedTreeSpecies, spreadability);
    }
}