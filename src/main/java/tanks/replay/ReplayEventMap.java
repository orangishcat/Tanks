package tanks.replay;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.HashMap;

public class ReplayEventMap
{
    public static Int2ObjectOpenHashMap<Class<? extends ReplayEvents.IReplayEvent>> replayEventMap = new Int2ObjectOpenHashMap<>();
    public static HashMap<Class<? extends ReplayEvents.IReplayEvent>, Integer> replayIDMap = new HashMap<>();
    static
    {
        registerEvent(ReplayEvents.Tick.class);
        registerEvent(ReplayEvents.LevelChange.class);
    }

    public static int eventID = 0;
    public static void registerEvent(Class<? extends ReplayEvents.IReplayEvent> event)
    {
        int p = eventID++;
        replayEventMap.put(p, event);
        replayIDMap.put(event, p);
    }

    public static Class<? extends ReplayEvents.IReplayEvent> get(int id)
    {
        return replayEventMap.get(id);
    }

    public static int get(Class<? extends ReplayEvents.IReplayEvent> event)
    {
        return replayIDMap.get(event);
    }
}
