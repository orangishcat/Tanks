package tanks.handle;

@FunctionalInterface
public interface ICrashHandler extends IHandle
{
    void handle(Throwable e);
}
