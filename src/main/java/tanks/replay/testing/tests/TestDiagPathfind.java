package tanks.replay.testing.tests;

import tanks.replay.testing.Test;
import tanks.replay.testing.TestType;

public class TestDiagPathfind extends Test
{
    public TestDiagPathfind()
    {
        super("the_mafia", TestType.LEVEL);
        this.name = "Diagonal pathfinding";
        maximumTime = 30 * 100;
        expectAllyWin();
    }
}
