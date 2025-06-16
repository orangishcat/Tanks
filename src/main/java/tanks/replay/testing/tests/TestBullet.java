package tanks.replay.testing.tests;

import tanks.Game;
import tanks.Movable;
import tanks.bullet.Bullet;
import tanks.replay.testing.Test;
import tanks.replay.testing.TestType;

public class TestBullet extends Test
{
    public TestBullet()
    {
        super("bullet test", TestType.REPLAY);
        this.name = "Bullet test";
        this.maximumTime = 55 * 100;

        expectOnce(() ->
        {
            for (Movable m : Game.movables)
                if (m instanceof Bullet && ((Bullet) m).effect == Bullet.BulletEffect.dark_fire)
                    return true;
            return false;
        }).setName("Dark fire bullet check");
        expectOnce(() ->
        {
            for (Movable m : Game.movables)
                if (m instanceof Bullet && ((Bullet) m).typeName.equals("gas"))
                    return true;
            return false;
        }).setName("Flame bullet check");
        expectAllyWin();
    }
}
