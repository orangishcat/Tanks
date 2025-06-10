package tanks.replay.testing.tests;

import tanks.Game;
import tanks.Movable;
import tanks.effect.AttributeModifier;
import tanks.effect.StatusEffect;
import tanks.replay.testing.Test;
import tanks.replay.testing.TestType;
import tanks.tank.TankPlayer;

import java.util.Arrays;
import java.util.List;

public class TestAttribute extends Test
{
    private static final List<StatusEffect> attributes = Arrays.asList(StatusEffect.boost_tank, StatusEffect.snow_friction,
            StatusEffect.snow_velocity, StatusEffect.ice, StatusEffect.mud);

    public TestAttribute()
    {
        super("attribute test", TestType.REPLAY);

        this.name = "Attribute test";

        for (StatusEffect e : attributes)
            expectOnce(() -> playerTank.em().contains(e)).setName(e.name + " check");
        expectOnce(() -> playerTank.em().getAttributeValue(AttributeModifier.velocity, 1) == 16)
                .setName("double boost check").setFailMessage("Double boost not found (!=9)");

        expectAtEnd(() ->
        {
            for (Movable m : Game.movables)
                if (m instanceof TankPlayer != (m.em().getAttributeValue(AttributeModifier.healray, 0) == 1))
                    return false;
            return true;
        }).setName("healray check").setFailMessage("Healray attribute missing or != 1");

        expectAllyWin();
    }
}
