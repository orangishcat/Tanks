package tanks.replay.tests.test;

import tanks.replay.tests.Test;

public class TestBullet extends Test
{
    public TestBullet()
    {
        super("bullet test");
        this.name = "Bullet test";
        this.maximumTime = 60 * 100;
        this.fixedFPS = true;
        expectAllyWin();
    }
}
