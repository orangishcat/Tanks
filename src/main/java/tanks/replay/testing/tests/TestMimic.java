package tanks.replay.testing.tests;

import tanks.Game;
import tanks.Movable;
import tanks.bullet.Laser;
import tanks.replay.testing.Test;
import tanks.replay.testing.TestType;
import tanks.tank.TankAIControlled;
import tanks.tank.TankMimic;

import java.lang.reflect.Field;

public class TestMimic extends Test
{
    private int lasers, mimics;

    public TestMimic()
    {
        super("mimic test", TestType.REPLAY);
        this.name = "Mimic test";
        this.maximumTime = 100 * 100;
        try
        {
            Field f = TankAIControlled.class.getDeclaredField("mimicRevertTime");
            f.setAccessible(true);

            expectAlways(() ->
            {
                try
                {
                    if (age > 360)
                        return true;
                    for (Movable m : Game.movables)
                        if (m instanceof TankMimic && f.getDouble(m) < 190)
                            return false;
                    return true;
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }).setName("Mimic revert time should stay at 200 when mustard is still alive");
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        expectAlways(() ->
        {
            if (age < 50 || age > 340)
                return true;

            return lasers == 8;

        }).setName("There should be lasers present at all times when mustard is still alive");

        expectOnce(() ->
        {
            if (age < 340 || age > 450)
                return false;

            return lasers == 0 && mimics == 0;
        }).setName("While mustard unalive and all mimics' revert times are ticking, there should be no lasers");

        expectOnce(() ->
        {
            if (age < 340)
                return false;

            return mimics == 8;
        }).setName("All mimic tanks should have reverted after mustard got nuked");

        expectAllyWin();
    }

    @Override
    public boolean update()
    {
        lasers = 0;
        for (Movable m : Game.movables)
            if (m instanceof Laser)
                lasers++;

        mimics = 0;
        for (Movable m : Game.movables)
            if (m instanceof TankMimic)
                mimics++;

        return super.update();
    }
}
