import java.util.EnumMap;

public class TreeFactory {
    
    private static final double MIN_SPREADABILITY = 0.3;
    private static final double MAX_SPREADABILITY = 1.0;

    // 2 ingredients for weighted random selection of tree species.
        // 1 - A table of weights
        // 2 - A random selector which reads those weights
    private final EnumMap<Tree.TreeSpecies, Double> weights = new EnumMap<>(Tree.TreeSpecies.class);
    private final WeightedSelector weightedSelector = new WeightedSelector(weights);

    public Tree generateTree() {

        //Generate random spreadability value - random variation between individual trees.
        double spreadability = MIN_SPREADABILITY + Math.random() * (MAX_SPREADABILITY - MIN_SPREADABILITY);

        // Add weights for weighted random selection of trees
        weights.put(Tree.TreeSpecies.OAK, 5.0);
        weights.put(Tree.TreeSpecies.BIRCH, 2.5);
        weights.put(Tree.TreeSpecies.SPRUCE, 2.5);
        weights.put(Tree.TreeSpecies.EUCALYPTUS, 4.0);
        weights.put(Tree.TreeSpecies.JUNGLE, 1.0);
        
        // Select a tree based on the weights
        Tree.TreeSpecies selectedTreeSpecies = weightedSelector.selectSpecies();

        return new Tree(selectedTreeSpecies, spreadability);
    }
}
