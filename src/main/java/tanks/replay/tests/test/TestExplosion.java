package tanks.replay.tests.test;

import tanks.replay.tests.Test;

public class TestExplosion extends Test
{
    public TestExplosion()
    {
        super("explosion test");
        this.name = "Explosion test";
        this.maximumTime = 40 * 100;
        expectAllyWin();
    }
}
