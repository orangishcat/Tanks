package tanks.registry;

import tanks.Game;
import tanks.tank.Tank;
import tanks.tank.TankUnknown;

import java.util.ArrayList;

public class RegistryTank 
{
	public ArrayList<TankEntry> tankEntries = new ArrayList<TankEntry>();
	protected double maxTankWeight = 0;

	public static class TankEntry
	{
		public final Class<? extends Tank> tank;
		public final String name;
		public final double weight;

		protected double startWeight;
		protected double endWeight;

		protected RegistryTank registryTank;

		public TankEntry(RegistryTank r, Class<? extends Tank> tank, String name, double weight)
		{
			this.tank = tank;
			this.name = name;
			this.weight = weight;
			this.registryTank = r;

			r.tankEntries.add(this);
		}

		public void initialize()
		{
			this.startWeight = registryTank.maxTankWeight;
			registryTank.maxTankWeight += weight;
			this.endWeight = registryTank.maxTankWeight;
		}

		protected TankEntry()
		{
			this.tank = TankUnknown.class;
			this.name = "unknown";
			this.weight = 0;
		}

		protected TankEntry(String name)
		{
			this.tank = TankUnknown.class;
			this.name = name;
			this.weight = 0;
		}

		public Tank getTank(double x, double y, double a)
		{
			try 
			{
				return tank.getConstructor(String.class, double.class, double.class, double.class).newInstance(this.name, x, y, a);
			}
			catch (Exception e)
			{
				Game.exitToCrash(e);
				return null;
			}
		}

		public static TankEntry getUnknownEntry()
		{
			return new TankEntry();
		}

		public static TankEntry getUnknownEntry(String name)
		{
			return new TankEntry(name);
		}
	}

	public TankEntry getRandomTank()
	{
		if (this.tankEntries.size() <= 0)
			throw new RuntimeException("the tank registry file is empty. please register some tanks!");

		double random = Math.random() * maxTankWeight;
		for (int i = 0; i < tankEntries.size(); i++)
		{
			TankEntry r = tankEntries.get(i);

			if (random >= r.startWeight && random < r.endWeight)
			{
				return r;
			}
		}

		return TankEntry.getUnknownEntry();
	}

	public TankEntry getEntry(String name)
	{		
		for (int i = 0; i < tankEntries.size(); i++)
		{
			TankEntry r = tankEntries.get(i);

			if (r.name.equals(name))
			{
				return r;
			}
		}

		return TankEntry.getUnknownEntry(name);
	}

	public TankEntry getEntry(int number)
	{		
		return tankEntries.get(number);
	}
}
