package tanks;

import tanks.effect.AttributeModifier;

public abstract class AreaEffect extends Movable
{	
	public boolean constantlyImbue = true;
	public double maxAge = 1000;
	
	public AreaEffect(double x, double y)
	{
		super(x, y);
		this.drawLevel = 5;
	}

	@Override
	public void update()
	{
		this.age += Panel.frameFrequency * em().getAttributeValue(AttributeModifier.clock_speed, 1);
		
		if (constantlyImbue)
            this.imbueEffects();
		
		if (this.age > this.maxAge)
			Game.removeMovables.add(this);
	}

	public abstract void imbueEffects();
}
