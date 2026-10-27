import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ForestTest {

    // Forest plants trees at random (80% chance per cell), so most tests
    // clear the grid first and plant trees by hand where they need them.
    private static Forest<Tree> emptyForest(int rows, int columns) {
        Forest<Tree> forest = new Forest<>(rows, columns, () -> new Tree(0.0));
        forest.cellStream().forEach(Cell::removeTree);
        return forest;
    }

    private static Tree plant(Forest<Tree> forest, int row, int col, double spreadability) {
        Tree tree = new Tree(spreadability);
        forest.getCell(row, col).plantTree(tree);
        return tree;
    }

    private static long burningCount(Forest<Tree> forest) {
        return forest.cellStream()
                .filter(Cell::hasTree)
                .filter(cell -> cell.getTree().isBurning())
                .count();
    }

    // ---------- Structure ----------

    @Test
    void dimensionsMatchConstructorArguments() {
        Forest<Tree> forest = new Forest<>(4, 7, () -> new Tree(0.5));

        assertEquals(4, forest.getRows());
        assertEquals(7, forest.getColumns());
    }

    @Test
    void cellStreamCoversEveryCell() {
        Forest<Tree> forest = new Forest<>(4, 7, () -> new Tree(0.5));
        assertEquals(28, forest.cellStream().count());
    }

    @Test
    void getCellReturnsTheSameCellEachTime() {
        Forest<Tree> forest = emptyForest(3, 3);
        assertSame(forest.getCell(1, 2), forest.getCell(1, 2));
        assertNotSame(forest.getCell(1, 2), forest.getCell(2, 1));
    }

    @Test
    void isInBoundsChecksAllEdges() {
        Forest<Tree> forest = emptyForest(3, 4);

        assertTrue(forest.isInBounds(0, 0));
        assertTrue(forest.isInBounds(2, 3));

        assertFalse(forest.isInBounds(-1, 0));
        assertFalse(forest.isInBounds(0, -1));
        assertFalse(forest.isInBounds(3, 0));
        assertFalse(forest.isInBounds(0, 4));
    }

    @Test
    void failingSupplierLeavesCellsEmptyInsteadOfCrashing() {
        Forest<Tree> forest = new Forest<>(3, 3, () -> {
            throw new IllegalArgumentException("bad tree");
        });

        assertEquals(0, forest.cellStream().filter(Cell::hasTree).count());
    }

    @Test
    void plantedTreesComeFromTheSupplier() {
        Forest<Tree> forest = new Forest<>(10, 10, () -> new Tree(Tree.TreeSpecies.JUNGLE, 0.5));

        assertTrue(forest.cellStream()
                .filter(Cell::hasTree)
                .allMatch(cell -> cell.getTree().getSpecies() == Tree.TreeSpecies.JUNGLE));
    }

    // ---------- Fire spread ----------

    @Test
    void fireSpreadsToAllEightNeighbours() {
        // Spreadability 1.0 means a 100% spread chance.
        Forest<Tree> forest = emptyForest(3, 3);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                plant(forest, row, col, 1.0);
            }
        }
        forest.getCell(1, 1).getTree().ignite();

        forest.update();

        assertEquals(9, burningCount(forest));
    }

    @Test
    void fireDoesNotSpreadToUnflammableTrees() {
        // Spreadability 0.0 means a 0% spread chance.
        Forest<Tree> forest = emptyForest(3, 3);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                plant(forest, row, col, 0.0);
            }
        }
        forest.getCell(1, 1).getTree().ignite();

        forest.update();

        assertEquals(1, burningCount(forest));
    }

    @Test
    void fireOnlySpreadsOneStepPerUpdate() {
        // A row of 5 flammable trees lit at one end: the fire should creep
        // along one cell per update, not jump the whole row at once.
        Forest<Tree> forest = emptyForest(1, 5);
        for (int col = 0; col < 5; col++) {
            plant(forest, 0, col, 1.0);
        }
        forest.getCell(0, 0).getTree().ignite();

        forest.update();
        assertEquals(2, burningCount(forest));

        forest.update();
        assertEquals(3, burningCount(forest));
    }

    @Test
    void fireDoesNotJumpEmptyCells() {
        Forest<Tree> forest = emptyForest(1, 3);
        plant(forest, 0, 0, 1.0);
        Tree farTree = plant(forest, 0, 2, 1.0);

        forest.getCell(0, 0).getTree().ignite();
        forest.update();

        assertFalse(farTree.isBurning());
    }

    @Test
    void fireAtCornerDoesNotGoOutOfBounds() {
        Forest<Tree> forest = emptyForest(2, 2);
        Tree corner = plant(forest, 0, 0, 1.0);
        corner.ignite();

        assertDoesNotThrow(forest::update);
    }

    // ---------- Burning down ----------

    @Test
    void burningTreeLosesHealthEachUpdate() {
        Forest<Tree> forest = emptyForest(1, 1);
        Tree tree = plant(forest, 0, 0, 0.0);
        tree.ignite();

        forest.update();

        assertEquals(70.0, tree.getHealth(), 1e-9);
    }

    @Test
    void burntOutTreeIsRemovedFromCell() {
        Forest<Tree> forest = emptyForest(1, 1);
        plant(forest, 0, 0, 0.0).ignite();

        // Oak: 100 health, 30 damage per tick -> dead on the 4th tick
        for (int i = 0; i < 4; i++) {
            forest.update();
        }

        assertTrue(forest.getCell(0, 0).isEmpty());
    }

    @Test
    void unlitTreesAreUntouchedByUpdate() {
        Forest<Tree> forest = emptyForest(2, 2);
        Tree tree = plant(forest, 1, 1, 1.0);

        forest.update();

        assertEquals(100.0, tree.getHealth());
        assertFalse(tree.isBurning());
    }

    // ---------- Wind ----------

    @Test
    void setWindStoresAndAppliesWind() {
        Forest<Tree> forest = emptyForest(5, 5);
        Wind wind = new Wind(50, "EAST", 0, 0, 10);

        forest.setWind(wind);

        assertSame(wind, forest.getWind());
        assertTrue(forest.getCell(0, 0).getWindExposure() > 0.0);
    }

    @Test
    void forestWithoutWindStillUpdates() {
        Forest<Tree> forest = emptyForest(3, 3);
        plant(forest, 1, 1, 1.0).ignite();

        assertNull(forest.getWind());
        assertDoesNotThrow(forest::update);
    }
}
