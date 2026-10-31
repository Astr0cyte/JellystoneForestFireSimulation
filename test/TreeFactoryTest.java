import static org.junit.jupiter.api.Assertions.*;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class TreeFactoryTest {

    // ---------- TreeFactory ----------

    @Test
    void generatedTreesAreFreshAndValid() {
        TreeFactory factory = new TreeFactory();

        for (int i = 0; i < 500; i++) {
            Tree tree = factory.generateTree(List.of());

            assertNotNull(tree);
            assertTrue(tree.isAlive());
            assertFalse(tree.isBurning());
            assertEquals(tree.getSpecies().getMaxHealth(), tree.getHealth());
        }
    }

    @Test
    void generatedSpreadabilityStaysInRange() {
        // Base range is 0.3-1.0; the lowest species multiplier is 0.8 (JUNGLE).
        TreeFactory factory = new TreeFactory();

        for (int i = 0; i < 500; i++) {
            double spreadability = factory.generateTree(List.of()).getSpreadability();
            assertTrue(spreadability >= 0.3 * 0.8 && spreadability <= 1.0,
                    "spreadability " + spreadability);
        }
    }

    @Test
    void factoryProducesEverySpecies() {
        // JUNGLE is the rarest at 1/15, so missing it in 2000 trees is
        // practically impossible.
        TreeFactory factory = new TreeFactory();
        Set<Tree.TreeSpecies> seen = EnumSet.noneOf(Tree.TreeSpecies.class);

        for (int i = 0; i < 2000; i++) {
            seen.add(factory.generateTree(List.of()).getSpecies());
        }

        assertEquals(EnumSet.allOf(Tree.TreeSpecies.class), seen);
    }

    // ---------- WeightedSelector ----------

    @Test
    void onlyNonZeroWeightIsAlwaysSelected() {
        EnumMap<Tree.TreeSpecies, Double> weights = new EnumMap<>(Tree.TreeSpecies.class);
        weights.put(Tree.TreeSpecies.OAK, 0.0);
        weights.put(Tree.TreeSpecies.SPRUCE, 3.0);
        weights.put(Tree.TreeSpecies.BIRCH, 0.0);

        WeightedSelector selector = new WeightedSelector(weights);

        for (int i = 0; i < 200; i++) {
            assertEquals(Tree.TreeSpecies.SPRUCE, selector.selectSpecies());
        }
    }

    @Test
    void selectionOnlyReturnsWeightedSpecies() {
        EnumMap<Tree.TreeSpecies, Double> weights = new EnumMap<>(Tree.TreeSpecies.class);
        weights.put(Tree.TreeSpecies.BIRCH, 1.0);
        weights.put(Tree.TreeSpecies.JUNGLE, 1.0);

        WeightedSelector selector = new WeightedSelector(weights);

        for (int i = 0; i < 200; i++) {
            assertTrue(weights.containsKey(selector.selectSpecies()));
        }
    }

    @Test
    void allZeroWeightsThrow() {
        EnumMap<Tree.TreeSpecies, Double> weights = new EnumMap<>(Tree.TreeSpecies.class);
        weights.put(Tree.TreeSpecies.OAK, 0.0);

        assertThrows(IllegalStateException.class, () -> new WeightedSelector(weights).selectSpecies());
    }

    @Test
    void emptyWeightsThrow() {
        EnumMap<Tree.TreeSpecies, Double> weights = new EnumMap<>(Tree.TreeSpecies.class);
        assertThrows(IllegalStateException.class, () -> new WeightedSelector(weights).selectSpecies());
    }

    // ---------- AllOakSelector ----------

    @Test
    void allOakSelectorAlwaysReturnsOak() {
        assertEquals(Tree.TreeSpecies.OAK, new AllOakSelector().selectSpecies());
    }

    @Test
    void speciesSelectorCanBeALambda() {
        SpeciesSelector selector = neighbourSpecies -> Tree.TreeSpecies.EUCALYPTUS;
        assertEquals(Tree.TreeSpecies.EUCALYPTUS, selector.selectSpecies());
    }
}
