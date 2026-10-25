public record IgnitionSource(String sourceName, double severity, 
                            double spreadability, IgnitionBehaviour behaviour) {

    public IgnitionSource {
        if(sourceName == null) {
            throw new IllegalArgumentException(
                "A name label for the ignition source is required."
            );
        }

        if(behaviour == null) {
            throw new IllegalArgumentException(
                "An ignition behaviour type is required."
            );
        }
        
        validateValue("Severity", severity);
        validateValue("Spreadability", spreadability);
    }

    private void validateValue(String name, double value) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(
                    name + " must be between 0.0 and 1.0"
            );
        }
    }

    public void ignite(Forest<? extends Tree> forest) {
        behaviour.ignite(forest, this);
    }
}