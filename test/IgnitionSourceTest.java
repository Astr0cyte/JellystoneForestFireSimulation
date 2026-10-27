import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class IgnitionSourceTest {

    // A behaviour that does nothing - for tests that only care about the record.
    private static final IgnitionBehaviour NO_OP = (forest, source) -> { };

    private static Forest<Tree> emptyForest(int rows, int columns) {
        Forest<Tree> forest = new Forest<>(rows, columns, () -> new Tree(0.0));
        forest.cellStream().forEach(Cell::removeTree);
        return forest;
    }

    private static Forest<Tree> fullForest(int rows, int columns, double spreadability) {
        Forest<Tree> forest = emptyForest(rows, columns);
        forest.cellStream().forEach(cell -> cell.plantTree(new Tree(spreadability)));
        return forest;
    }

    private static long burningCount(Forest<Tree> forest) {
        return forest.cellStream()
                .filter(Cell::hasTree)
                .filter(cell -> cell.getTree().isBurning())
                .count();
    }

    // ---------- IgnitionSource record ----------

    @Test
    void recordStoresComponents() {
        IgnitionSource source = new IgnitionSource("Test", 0.6, 0.4, NO_OP);

        assertEquals("Test", source.sourceName());
        assertEquals(0.6, source.severity());
        assertEquals(0.4, source.spreadability());
        assertSame(NO_OP, source.behaviour());
    }

    @Test
    void nullNameIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new IgnitionSource(null, 0.5, 0.5, NO_OP));
    }

    @Test
    void nullBehaviourIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new IgnitionSource("Test", 0.5, 0.5, null));
    }

    @Test
    void severityOutOfRangeIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new IgnitionSource("Test", -0.1, 0.5, NO_OP));
        assertThrows(IllegalArgumentException.class,
                () -> new IgnitionSource("Test", 1.1, 0.5, NO_OP));
    }

    @Test
    void spreadabilityOutOfRangeIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new IgnitionSource("Test", 0.5, -0.1, NO_OP));
        assertThrows(IllegalArgumentException.class,
                () -> new IgnitionSource("Test", 0.5, 1.1, NO_OP));
    }

    @Test
    void boundaryValuesAreAccepted() {
        assertDoesNotThrow(() -> new IgnitionSource("Test", 0.0, 0.0, NO_OP));
        assertDoesNotThrow(() -> new IgnitionSource("Test", 1.0, 1.0, NO_OP));
    }

    @Test
    void igniteHandsForestAndItselfToBehaviour() {
        Forest<Tree> forest = emptyForest(2, 2);
        List<Object> received = new ArrayList<>();

        IgnitionSource source = new IgnitionSource("Spy", 0.5, 0.5, (f, s) -> {
            received.add(f);
            received.add(s);
        });
        source.ignite(forest);

        assertEquals(List.of(forest, source), received);
    }

    @Test
    void recordsWithSameComponentsAreEqual() {
        assertEquals(new IgnitionSource("A", 0.5, 0.5, NO_OP),
                     new IgnitionSource("A", 0.5, 0.5, NO_OP));
    }

    // ---------- Backburning ----------

    @Test
    void backburningWithFullSpreadabilityLightsWholeRow() {
        Forest<Tree> forest = fullForest(5, 9, 0.5);
        new IgnitionSource("Backburn", 0.7, 1.0, IgnitionBehaviours.backburning(2)).ignite(forest);

        for (int col = 0; col < 9; col++) {
            assertTrue(forest.getCell(2, col).getTree().isBurning());
        }
        assertEquals(9, burningCount(forest)); // nothing outside row 2
    }

    @Test
    void backburningWithZeroSpreadabilityLightsNothing() {
        Forest<Tree> forest = fullForest(5, 9, 0.5);
        new IgnitionSource("Backburn", 0.7, 0.0, IgnitionBehaviours.backburning(2)).ignite(forest);

        assertEquals(0, burningCount(forest));
    }

    @Test
    void backburningLightsOnlyTheWaveTroughs() {
        // Wavelength 3: the trough falls on every third column (2, 5, 8),
        // where the wave value is ~0.067.
        Forest<Tree> forest = fullForest(1, 9, 0.5);
        new IgnitionSource("Backburn", 0.7, 0.1, IgnitionBehaviours.backburning(0)).ignite(forest);

        for (int col = 0; col < 9; col++) {
            boolean expected = col % 3 == 2;
            assertEquals(expected, forest.getCell(0, col).getTree().isBurning(), "column " + col);
        }
    }

    @Test
    void backburningSkipsEmptyCells() {
        Forest<Tree> forest = emptyForest(1, 3);
        forest.getCell(0, 1).plantTree(new Tree(0.5));

        new IgnitionSource("Backburn", 0.7, 1.0, IgnitionBehaviours.backburning(0)).ignite(forest);

        assertEquals(1, burningCount(forest));
    }

    @Test
    void backburningOutOfRangeRowDoesNothing() {
        Forest<Tree> forest = fullForest(3, 3, 0.5);

        new IgnitionSource("Backburn", 0.7, 1.0, IgnitionBehaviours.backburning(-1)).ignite(forest);
        new IgnitionSource("Backburn", 0.7, 1.0, IgnitionBehaviours.backburning(3)).ignite(forest);

        assertEquals(0, burningCount(forest));
    }

    // ---------- Arson ----------

    @Test
    void arsonLightsTargetTree() {
        Forest<Tree> forest = fullForest(3, 3, 1.0);
        new IgnitionSource("Arson", 0.55, 0.5, IgnitionBehaviours.arson(1, 2)).ignite(forest);

        assertTrue(forest.getCell(1, 2).getTree().isBurning());
        assertEquals(1, burningCount(forest));
    }

    @Test
    void arsonFailsOnTreeLessFlammableThanSource() {
        Forest<Tree> forest = fullForest(3, 3, 0.2);
        new IgnitionSource("Arson", 0.55, 0.5, IgnitionBehaviours.arson(1, 1)).ignite(forest);

        assertEquals(0, burningCount(forest));
    }

    @Test
    void arsonWithLowSeverityDoesNotTakeHold() {
        // Tree.ignite(severity) ignores anything under 0.5.
        Forest<Tree> forest = fullForest(3, 3, 1.0);
        new IgnitionSource("Arson", 0.3, 0.5, IgnitionBehaviours.arson(1, 1)).ignite(forest);

        assertEquals(0, burningCount(forest));
    }

    @Test
    void arsonOnEmptyCellSearchesDownTheColumn() {
        Forest<Tree> forest = emptyForest(4, 3);
        Tree below = new Tree(1.0);
        forest.getCell(2, 1).plantTree(below);

        new IgnitionSource("Arson", 0.55, 0.5, IgnitionBehaviours.arson(0, 1)).ignite(forest);

        assertTrue(below.isBurning());
    }

    @Test
    void arsonThrowsWhenColumnHasNoTreeBelowTarget() {
        Forest<Tree> forest = emptyForest(4, 3);
        forest.getCell(0, 1).plantTree(new Tree(1.0)); // above the target, so never reached

        IgnitionSource source = new IgnitionSource("Arson", 0.55, 0.5, IgnitionBehaviours.arson(1, 1));

        assertThrows(IllegalArgumentException.class, () -> source.ignite(forest));
    }

    @Test
    void arsonThrowsForOutOfBoundsTarget() {
        Forest<Tree> forest = fullForest(3, 3, 1.0);
        IgnitionSource source = new IgnitionSource("Arson", 0.55, 0.5, IgnitionBehaviours.arson(5, 5));

        assertThrows(IllegalArgumentException.class, () -> source.ignite(forest));
    }

    // ---------- Lightning ----------

    @Test
    void lightningStrikesExactlyOneTree() {
        Forest<Tree> forest = fullForest(5, 5, 0.5);
        Lightning lightning = new Lightning();

        new IgnitionSource("Lightning", 0.8, 0.7, lightning).ignite(forest);

        assertEquals(1, burningCount(forest));
        assertNotNull(lightning.getLastStruckCell());
        assertTrue(lightning.getLastStruckCell().getTree().isBurning());
    }

    @Test
    void lastStruckCellIsNullBeforeAnyStrike() {
        assertNull(new Lightning().getLastStruckCell());
    }

    @Test
    void lightningNeverStrikesTheSameCellTwice() {
        // 1x2 forest: the second strike must land on the other cell.
        Forest<Tree> forest = fullForest(1, 2, 0.5);
        Lightning lightning = new Lightning();
        IgnitionSource source = new IgnitionSource("Lightning", 0.8, 0.7, lightning);

        source.ignite(forest);
        Cell<? extends Tree> first = lightning.getLastStruckCell();

        // The second strike could, in theory, miss the free cell in all 10
        // attempts (1 in 1024) and throw - that's the class's own limit.
        try {
            source.ignite(forest);
            assertNotSame(first, lightning.getLastStruckCell());
        }
        catch (IllegalStateException e) {
            assertSame(first, lightning.getLastStruckCell());
        }
    }

    @Test
    void lightningThrowsOnForestWithNoTrees() {
        Forest<Tree> forest = emptyForest(3, 3);
        IgnitionSource source = new IgnitionSource("Lightning", 0.8, 0.7, new Lightning());

        assertThrows(IllegalStateException.class, () -> source.ignite(forest));
    }
}
