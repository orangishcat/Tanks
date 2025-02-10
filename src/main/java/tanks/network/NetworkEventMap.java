package tanks.network;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import tanks.Game;
import tanks.network.event.INetworkEvent;

import java.util.HashMap;

public class NetworkEventMap 
{
	protected static NetworkEventMap inst = new NetworkEventMap();
	public Int2ObjectOpenHashMap<Class<? extends INetworkEvent>> map1 = new Int2ObjectOpenHashMap<>();
	public HashMap<Class<? extends INetworkEvent>, Integer> map2 = new HashMap<>();
	public int id = 0;
	
	public static void register(Class<? extends INetworkEvent> c)
	{
		try
		{
			c.getConstructor();
		}
		catch (Exception e)
		{
			Game.exitToCrash(new RuntimeException("The network event " + c + " does not have a no-parameter constructor. Please give it one."));
		}

		inst.map1.put(inst.id, c);
		inst.map2.put(c, inst.id);
		inst.id++;
	}
	
	public static int get(Class<? extends INetworkEvent> c)
	{
		Integer i = inst.map2.get(c);

		if (i == null)
			return -1;

		return i;
	}
	
	public static Class<? extends INetworkEvent> get(int i)
	{
		return inst.map1.get(i);
	}

	public static void print()
	{
		for (int i = 0; i < inst.id; i++)
            System.out.println(i + " " + NetworkEventMap.get(i));
	}
}
