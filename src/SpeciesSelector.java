// Interface for future additional species selection algorithms.

import java.util.List;

@FunctionalInterface
public interface SpeciesSelector {
    
    // "Given a cell's position, tell me what species goes there". 
    Tree.TreeSpecies selectSpecies(List<Tree.TreeSpecies> neighbourSpecies);

    default Tree.TreeSpecies selectSpecies() {
        return selectSpecies(List.of());
    };

}
