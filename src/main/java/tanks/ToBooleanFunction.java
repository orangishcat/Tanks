package tanks;

@FunctionalInterface
public interface ToBooleanFunction
{
    boolean apply();

    default ToBooleanFunction and(ToBooleanFunction other)
    {
        return () -> apply() && other.apply();
    }

    default ToBooleanFunction not()
    {
        return () -> !apply();
    }
}