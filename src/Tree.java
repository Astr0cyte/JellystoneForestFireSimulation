public class Tree implements Burnable {

    private boolean burning;
    private double spreadability;
    private double burnIntensity;
    private boolean alive;
    private double health;
    private double maxHealth;
    private double burnIntensityFactor;
    private TreeSpecies species;

    public enum TreeSpecies {
        OAK(100.0, 1.0, 1.0),
        BIRCH(60.0, 0.6, 0.9),
        SPRUCE(90.0, 1.15, 1.2),
        EUCALYPTUS(240.0, 1.4, 1.6),
        JUNGLE(240.0, 0.7, 0.8);

        private final double maxHealth;
        private final double burnIntensityFactor;
        private final double spreadabilityMultiplier;

        TreeSpecies(
                double maxHealth,
                double burnIntensityFactor,
                double spreadabilityMultiplier) {

            this.maxHealth = maxHealth;
            this.burnIntensityFactor = burnIntensityFactor;
            this.spreadabilityMultiplier = spreadabilityMultiplier;
        }

        public double getMaxHealth() {
            return maxHealth;
        }

        public double getBurnIntensityFactor() {
            return burnIntensityFactor;
        }

        public double getSpreadabilityMultiplier() {
            return spreadabilityMultiplier;
        }
    }

    public Tree(double spreadability) {
        this(TreeSpecies.OAK, spreadability);
    }

    public Tree(TreeSpecies species, double spreadability) {
        this(
                species,
                Math.min(1.0, spreadability * species.getSpreadabilityMultiplier()),
                species.getMaxHealth(),
                species.getBurnIntensityFactor()
        );
    }

    private Tree(
            TreeSpecies species,
            double spreadability,
            double maxHealth,
            double burnIntensityFactor) {

        validateSpreadability(spreadability);
        validateMaxHealth(maxHealth);
        validateBurnIntensityFactor(burnIntensityFactor);

        this.species = species;
        this.burning = false;
        this.spreadability = spreadability;
        this.burnIntensity = 0.0;
        this.burnIntensityFactor = burnIntensityFactor;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.alive = true;
    }

    @Override
    public void ignite() {
        if (!alive) {
            return;
        }

        burning = true;
        burnIntensity = burnIntensityFactor;
    }

    // OVERLOADED version
    public void ignite(double severity) {
        if (alive && severity >= 0.5) {
            burning = true;
            burnIntensity = Math.max(
                    burnIntensity,
                    severity * burnIntensityFactor
            );
        }
    }

    public double getHealth() {
        return health;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public void damage(double amount) {
        if (!Double.isFinite(amount) || amount < 0.0) {
            throw new IllegalArgumentException(
                    "Damage must be a finite, non-negative value"
            );
        }

        if (!alive) {
            return;
        }

        health = Math.max(0.0, health - amount);

        if (health == 0.0) {
            kill();
        }
    }

    @Override
    public boolean isBurning() {
        return burning;
    }
    public boolean isAlive() {
        return alive;
    }

    public double getSpreadability() {
        return spreadability;
    }

    public double getBurnIntensity() {
        return burnIntensity;
    }

    public void extinguish() {
        burning = false;
        burnIntensity = 0.0;
    }

    public void kill() {
        alive = false;
        burning = false;
        burnIntensity = 0.0;
    }

    public void advanceBurning() {
        if (!burning || !alive) {
            return;
        }

        damage(30.0 * burnIntensity);
    }

    @Override
    public String toString() {
        return "[health=" + health + "/" + maxHealth
                + ", spreadability=" + spreadability
                + ", burnIntensity=" + burnIntensity
                + ", burning=" + burning
                + ", alive=" + alive
                + "]";
    }

    public String getTreeType() {
        return species.name();
    }

    public TreeSpecies getSpecies() {
        return species;
    }

    //exceptions

    private static void validateSpreadability(double spreadability) {
        if (!Double.isFinite(spreadability)
                || spreadability < 0.0
                || spreadability > 1.0) {
            throw new IllegalArgumentException(
                    "Spreadability must be between 0.0 and 1.0"
            );
        }
    }

    private static void validateMaxHealth(double maxHealth) {
        if (!Double.isFinite(maxHealth) || maxHealth <= 0.0) {
            throw new IllegalArgumentException(
                    "Maximum health must be positive"
            );
        }
    }

    private static void validateBurnIntensityFactor(
            double burnIntensityFactor) {

        if (!Double.isFinite(burnIntensityFactor)
                || burnIntensityFactor < 0.0) {
            throw new IllegalArgumentException(
                    "Burn intensity factor must be non-negative"
            );
        }
    }
}