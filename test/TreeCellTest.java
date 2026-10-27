import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TreeCellTest {

    // ---------- Tree: construction ----------

    @Test
    void newTreeStartsAliveHealthyAndNotBurning() {
        Tree tree = new Tree(Tree.TreeSpecies.BIRCH, 0.5);

        assertTrue(tree.isAlive());
        assertFalse(tree.isBurning());
        assertEquals(Tree.TreeSpecies.BIRCH.getMaxHealth(), tree.getHealth());
        assertEquals(0.0, tree.getBurnIntensity());
        assertEquals(Tree.TreeSpecies.BIRCH, tree.getSpecies());
        assertEquals("BIRCH", tree.getTreeType());
    }

    @Test
    void singleArgConstructorDefaultsToOak() {
        assertEquals(Tree.TreeSpecies.OAK, new Tree(0.5).getSpecies());
    }

    @Test
    void spreadabilityIsScaledBySpeciesMultiplier() {
        // BIRCH multiplier is 0.9
        Tree tree = new Tree(Tree.TreeSpecies.BIRCH, 0.5);
        assertEquals(0.45, tree.getSpreadability(), 1e-9);
    }

    @Test
    void scaledSpreadabilityIsCappedAtOne() {
        // EUCALYPTUS multiplier is 1.6, so 0.9 * 1.6 would be 1.44 without the cap
        Tree tree = new Tree(Tree.TreeSpecies.EUCALYPTUS, 0.9);
        assertEquals(1.0, tree.getSpreadability());
    }

    @Test
    void spreadabilityBoundariesAreAccepted() {
        assertDoesNotThrow(() -> new Tree(0.0));
        assertDoesNotThrow(() -> new Tree(1.0));
    }

    @Test
    void invalidSpreadabilityIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Tree(-0.1));
        assertThrows(IllegalArgumentException.class, () -> new Tree(1.1));
        assertThrows(IllegalArgumentException.class, () -> new Tree(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new Tree(Double.POSITIVE_INFINITY));
    }

    // ---------- Tree: ignition ----------

    @Test
    void igniteSetsBurningAndSpeciesIntensity() {
        Tree tree = new Tree(Tree.TreeSpecies.SPRUCE, 0.5);
        tree.ignite();

        assertTrue(tree.isBurning());
        assertEquals(Tree.TreeSpecies.SPRUCE.getBurnIntensityFactor(), tree.getBurnIntensity());
    }

    @Test
    void igniteWithSeverityScalesIntensity() {
        Tree tree = new Tree(Tree.TreeSpecies.EUCALYPTUS, 0.5);
        tree.ignite(0.8);

        assertTrue(tree.isBurning());
        assertEquals(0.8 * 1.4, tree.getBurnIntensity(), 1e-9);
    }

    @Test
    void severityBelowHalfDoesNotIgnite() {
        Tree tree = new Tree(0.5);
        tree.ignite(0.49);

        assertFalse(tree.isBurning());
        assertEquals(0.0, tree.getBurnIntensity());
    }

    @Test
    void severityOfExactlyHalfIgnites() {
        Tree tree = new Tree(0.5);
        tree.ignite(0.5);
        assertTrue(tree.isBurning());
    }

    @Test
    void weakerSecondIgnitionDoesNotLowerIntensity() {
        Tree tree = new Tree(0.5);
        tree.ignite(1.0);
        tree.ignite(0.6);

        assertEquals(1.0, tree.getBurnIntensity(), 1e-9);
    }

    @Test
    void deadTreeCannotBeIgnited() {
        Tree tree = new Tree(0.5);
        tree.kill();

        tree.ignite();
        assertFalse(tree.isBurning());

        tree.ignite(1.0);
        assertFalse(tree.isBurning());
    }

    @Test
    void extinguishStopsBurning() {
        Tree tree = new Tree(0.5);
        tree.ignite();
        tree.extinguish();

        assertFalse(tree.isBurning());
        assertEquals(0.0, tree.getBurnIntensity());
        assertTrue(tree.isAlive());
    }

    // ---------- Tree: damage & burning down ----------

    @Test
    void damageReducesHealth() {
        Tree tree = new Tree(0.5);
        tree.damage(25.0);
        assertEquals(75.0, tree.getHealth());
    }

    @Test
    void damageNeverTakesHealthBelowZeroAndKillsTree() {
        Tree tree = new Tree(0.5);
        tree.damage(1000.0);

        assertEquals(0.0, tree.getHealth());
        assertFalse(tree.isAlive());
    }

    @Test
    void invalidDamageIsRejected() {
        Tree tree = new Tree(0.5);

        assertThrows(IllegalArgumentException.class, () -> tree.damage(-1.0));
        assertThrows(IllegalArgumentException.class, () -> tree.damage(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> tree.damage(Double.POSITIVE_INFINITY));
    }

    @Test
    void burnTickDoesNothingToUnlitTree() {
        Tree tree = new Tree(0.5);
        tree.burnTick();
        assertEquals(100.0, tree.getHealth());
    }

    @Test
    void burnTickDamagesBurningTree() {
        // OAK: intensity 1.0, so each tick does 30 damage
        Tree tree = new Tree(0.5);
        tree.ignite();
        tree.burnTick();

        assertEquals(70.0, tree.getHealth(), 1e-9);
    }

    @Test
    void oakBurnsOutAfterFourTicks() {
        Tree tree = new Tree(0.5);
        tree.ignite();

        for (int i = 0; i < 3; i++) {
            tree.burnTick();
        }
        assertTrue(tree.isAlive());

        tree.burnTick();
        assertFalse(tree.isAlive());
        assertFalse(tree.isBurning());
    }

    @Test
    void killStopsBurning() {
        Tree tree = new Tree(0.5);
        tree.ignite();
        tree.kill();

        assertFalse(tree.isAlive());
        assertFalse(tree.isBurning());
        assertEquals(0.0, tree.getBurnIntensity());
    }

    // ---------- Cell ----------

    @Test
    void newCellIsEmpty() {
        Cell<Tree> cell = new Cell<>(2, 3);

        assertTrue(cell.isEmpty());
        assertFalse(cell.hasTree());
        assertNull(cell.getTree());
    }

    @Test
    void cellStoresCoordinates() {
        Cell<Tree> cell = new Cell<>(2, 3);
        assertEquals(2, cell.getX());
        assertEquals(3, cell.getY());

        cell.setX(5);
        cell.setY(7);
        assertEquals(5, cell.getX());
        assertEquals(7, cell.getY());
    }

    @Test
    void plantAndRemoveTree() {
        Cell<Tree> cell = new Cell<>(0, 0);
        Tree tree = new Tree(0.5);

        cell.plantTree(tree);
        assertTrue(cell.hasTree());
        assertFalse(cell.isEmpty());
        assertSame(tree, cell.getTree());

        cell.removeTree();
        assertTrue(cell.isEmpty());
        assertNull(cell.getTree());
    }

    @Test
    void windExposureDefaultsToZeroAndCanBeSet() {
        Cell<Tree> cell = new Cell<>(0, 0);
        assertEquals(0.0, cell.getWindExposure());

        cell.setWindExposure(0.35);
        assertEquals(0.35, cell.getWindExposure());
    }
}
