import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class WindTest {

    // ---------- Construction ----------

    @Test
    void constructorStoresValues() {
        Wind wind = new Wind(30, "EAST", 4, 5, 10);

        assertEquals(30.0, wind.getSpeed());
        assertEquals("EAST", wind.getDirection());
        assertEquals(4, wind.getStartX());
        assertEquals(5, wind.getStartY());
        assertEquals(10, wind.getRadius());
    }

    @Test
    void directionIsNormalised() {
        assertEquals("NORTH", new Wind(10, "  north ", 0, 0, 5).getDirection());
        assertEquals("WEST", new Wind(10, "West", 0, 0, 5).getDirection());
    }

    @Test
    void invalidArgumentsThrowWindArgumentException() {
        assertThrows(WindArgumentException.class, () -> new Wind(-1, "EAST", 0, 0, 5));
        assertThrows(WindArgumentException.class, () -> new Wind(Double.NaN, "EAST", 0, 0, 5));
        assertThrows(WindArgumentException.class, () -> new Wind(10, null, 0, 0, 5));
        assertThrows(WindArgumentException.class, () -> new Wind(10, "UP", 0, 0, 5));
        assertThrows(WindArgumentException.class, () -> new Wind(10, "EAST", -1, 0, 5));
        assertThrows(WindArgumentException.class, () -> new Wind(10, "EAST", 0, -1, 5));
        assertThrows(WindArgumentException.class, () -> new Wind(10, "EAST", 0, 0, -1));
    }

    @Test
    void windArgumentExceptionIsAnIllegalArgumentException() {
        // Lets callers catch it as either type.
        assertThrows(IllegalArgumentException.class, () -> new Wind(-1, "EAST", 0, 0, 5));
    }

    // ---------- Exposure ----------

    @Test
    void exposureAtStartIsSpeedOverHundred() {
        Wind wind = new Wind(50, "EAST", 0, 0, 10);
        assertEquals(0.5, wind.calculateExposure(0, 0), 1e-9);
    }

    @Test
    void exposureIsCappedAtOne() {
        Wind wind = new Wind(500, "EAST", 0, 0, 10);
        assertEquals(1.0, wind.calculateExposure(0, 0), 1e-9);
    }

    @Test
    void exposureFadesWithDistance() {
        Wind wind = new Wind(50, "EAST", 0, 0, 10);

        // 0.5 * (1 - 5 / 11)
        assertEquals(0.5 * (1.0 - 5.0 / 11.0), wind.calculateExposure(5, 0), 1e-9);
        assertTrue(wind.calculateExposure(2, 0) > wind.calculateExposure(8, 0));
    }

    @Test
    void noExposureBeyondRadius() {
        Wind wind = new Wind(50, "EAST", 0, 0, 10);
        assertEquals(0.0, wind.calculateExposure(11, 0));
    }

    @Test
    void noExposureBehindTheWind() {
        Wind wind = new Wind(50, "EAST", 5, 5, 10);
        assertEquals(0.0, wind.calculateExposure(3, 5));
    }

    @Test
    void noExposureOutsideTheCone() {
        // EAST cone: sideways distance must not exceed forward distance.
        Wind wind = new Wind(50, "EAST", 0, 0, 10);

        assertTrue(wind.calculateExposure(3, 3) > 0.0);   // on the cone edge
        assertEquals(0.0, wind.calculateExposure(2, 5));  // outside it
    }

    @Test
    void negativeCoordinatesHaveNoExposure() {
        Wind wind = new Wind(50, "WEST", 5, 5, 10);
        assertEquals(0.0, wind.calculateExposure(-1, 5));
    }

    @Test
    void southWindBlowsDownTheGrid() {
        Wind wind = new Wind(50, "SOUTH", 5, 0, 10);

        assertTrue(wind.calculateExposure(5, 4) > 0.0);
        assertEquals(0.0, wind.calculateExposure(9, 0)); // directly sideways
    }

    // ---------- Spread modifier ----------

    @Test
    void downwindSpreadIsBoosted() {
        Wind wind = new Wind(50, "EAST", 0, 0, 10);
        double exposure = wind.calculateExposure(2, 0);

        assertEquals(exposure * 0.4, wind.getSpreadModifier(1, 0, 2, 0), 1e-9);
    }

    @Test
    void upwindSpreadIsReduced() {
        Wind wind = new Wind(50, "EAST", 0, 0, 10);
        double exposure = wind.calculateExposure(2, 0);

        assertEquals(exposure * -0.2, wind.getSpreadModifier(3, 0, 2, 0), 1e-9);
    }

    @Test
    void sidewaysSpreadIsSlightlyBoosted() {
        Wind wind = new Wind(50, "EAST", 0, 0, 10);
        double exposure = wind.calculateExposure(4, 1);

        assertEquals(exposure * 0.1, wind.getSpreadModifier(4, 0, 4, 1), 1e-9);
    }

    @Test
    void noModifierOutsideTheWind() {
        Wind wind = new Wind(50, "EAST", 0, 0, 3);
        assertEquals(0.0, wind.getSpreadModifier(9, 0, 10, 0));
    }

    // ---------- Applying to a forest ----------

    @Test
    void applyWindSetsExposureOnEveryCell() {
        Forest<Tree> forest = new Forest<>(4, 6, () -> new Tree(0.5));
        Wind wind = new Wind(50, "EAST", 1, 2, 10);

        wind.applyWind(forest);

        for (int row = 0; row < forest.getRows(); row++) {
            for (int col = 0; col < forest.getColumns(); col++) {
                // Wind uses x = column, y = row.
                assertEquals(wind.calculateExposure(col, row),
                        forest.getCell(row, col).getWindExposure(), 1e-9);
            }
        }
    }

    @Test
    void applyWindRejectsNullForest() {
        Wind wind = new Wind(50, "EAST", 0, 0, 10);
        assertThrows(WindArgumentException.class, () -> wind.applyWind(null));
    }

    @Test
    void applyWindRejectsStartOutsideForest() {
        Forest<Tree> forest = new Forest<>(3, 3, () -> new Tree(0.5));
        Wind wind = new Wind(50, "EAST", 10, 0, 10);

        assertThrows(WindArgumentException.class, () -> wind.applyWind(forest));
    }
}
