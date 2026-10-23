// Interface for future additional species selection algorithms.
@FunctionalInterface
public interface SpeciesSelector {
    // Given a cell's position, tell me what species goes there. 
    Tree.TreeSpecies selectSpecies();
}
