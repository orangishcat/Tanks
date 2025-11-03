package tanks.handle;

import tanks.Panel;

import java.util.*;

public class HandleRegistry
{
    public static ArrayList<HandleRegistry> registries = new ArrayList<>();
    public static HandleRegistry registry = new HandleRegistry();
    static
    {
        registries.add(registry);
    }

    HashMap<Class<? extends IHandle>, ArrayList<IHandle>> handles = new HashMap<>();

    public static void callAllHandles(Class<? extends IHandle> handleClass, IHandleCall func)
    {
        for (HandleRegistry reg : registries)
            reg.callHandle(handleClass, func);
    }

    public void callHandle(Class<? extends IHandle> handleClass, IHandleCall func)
    {
        ArrayList<IHandle> handleList = handles.get(handleClass);
        if (handleList == null)
            return;
        for (IHandle h : handleList)
            func.call(h);
    }

    public static <R> R callAllHandles(Class<? extends IHandle> handleClass, IHandleCallWithValue<R> func, R initialValue)
    {
        R r = initialValue;
        for (HandleRegistry reg : registries)
            r = reg.callHandle(handleClass, func, initialValue);
        return r;
    }

    public <R> R callHandle(Class<? extends IHandle> handleClass, IHandleCallWithValue<R> func, R initialValue)
    {
        ArrayList<IHandle> handleList = handles.get(handleClass);
        if (handleList == null)
            return initialValue;

        R r = initialValue;
        for (IHandle h : handleList)
            r = func.call(h, r);
        return r;
    }

    public <T extends IHandle> void register(Class<T> handleClass, T handle)
    {
        handles.computeIfAbsent(handleClass, k -> new ArrayList<>()).add(handle);
        callHandle(IAddHandler.class, h -> ((IAddHandler) h).onAdd(handle));
    }

    public <T extends IHandle> void remove(Class<T> handleClass, T handle)
    {
        handles.get(handleClass).remove(handle);
        callHandle(IRemoveHandler.class, h -> ((IRemoveHandler) h).onRemove(handle));
    }

    public static void registerDefault()
    {
        registry.register(IAddHandler.class, h ->
        {
            if (h instanceof INetworkEventHandle)
                Panel.panel.networkEventHandles.add((INetworkEventHandle) h);
        });
        registry.register(IRemoveHandler.class, h ->
        {
            if (h instanceof INetworkEventHandle)
                Panel.panel.networkEventHandles.remove(h);
        });
    }

    @FunctionalInterface
    public interface IHandleCall
    {
        void call(IHandle handle);
    }

    @FunctionalInterface
    public interface IHandleCallWithValue<R>
    {
        /**
         * @param handle The handle to call
         * @param prevValue The value returned by the previous handle, or null if this is the first handle
         * @return The value to pass to the next handle, or the final value if this is the last handle
         */
        R call(IHandle handle, R prevValue);
    }
}
