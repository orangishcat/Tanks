package tanks.replay.testing.tests;

import tanks.replay.testing.Test;
import tanks.replay.testing.TestType;

public class TestExplosion extends Test
{
    public TestExplosion()
    {
        super("explosion test", TestType.REPLAY);
        this.name = "Explosion test";
        this.maximumTime = 40 * 100;
        expectAllyWin();
    }
}
