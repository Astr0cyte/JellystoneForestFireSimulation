
import java.util.EnumMap;
import java.util.List;

public class WeightedSelector implements SpeciesSelector {
    
    private EnumMap<Tree.TreeSpecies, Double> weights;

    public WeightedSelector(EnumMap<Tree.TreeSpecies, Double> weights) {
        this.weights = weights;
    }
    //Oak, birch, spruce, eucalyptus, jungle

    @Override
    public Tree.TreeSpecies selectSpecies(List<Tree.TreeSpecies> neighbourSpecies) {
        
        double total = 0.0;

        for(double weight : weights.values()) {
            total += weight;
        }

        double point = Math.random() * total;

        double runningSum = 0.0; // I still have no idea what this is. Come back to this as the next thing I do.

        for (Tree.TreeSpecies species : weights.keySet()) {
            runningSum += weights.get(species);
            if (point < runningSum) {
                return species;
            }
        }

        throw new IllegalStateException("Weights must add up to more than zero");
    }
}
