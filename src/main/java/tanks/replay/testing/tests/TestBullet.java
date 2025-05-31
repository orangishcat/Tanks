package tanks.replay.testing.tests;

import tanks.replay.testing.Test;
import tanks.replay.testing.TestType;

public class TestBullet extends Test
{
    public TestBullet()
    {
        super("bullet test", TestType.REPLAY);
        this.name = "Bullet test";
        this.maximumTime = 55 * 100;
//        this.fixedFPS = true;
        expectAllyWin();
    }
}
