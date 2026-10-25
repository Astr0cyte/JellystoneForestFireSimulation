
@FunctionalInterface
public interface IgnitionBehaviour {
    void ignite(Forest<? extends Tree> forest, IgnitionSource source);

}
