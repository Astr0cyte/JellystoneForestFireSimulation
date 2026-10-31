// This class is a placeholder until more advanced selectors are made.

import java.util.List;

public class AllOakSelector implements SpeciesSelector{
    
    @Override
    public Tree.TreeSpecies selectSpecies(List<Tree.TreeSpecies> neighbourSpecies) {
        return Tree.TreeSpecies.OAK;
    }
}

