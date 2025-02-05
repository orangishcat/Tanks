package tanks.replay.tests.test;

import tanks.Game;
import tanks.gui.screen.ScreenGame;
import tanks.replay.tests.Test;
import tanks.tank.TankAIControlled;

public class TestPathfinding extends Test
{
    public TestPathfinding()
    {
        super("pathfinding_test", false);
        this.name = "Pathfinding test";
        maximumTime = 60 * 100;
        expectAtEnd(() -> Game.movables.stream().noneMatch(m -> m instanceof TankAIControlled && ((TankAIControlled) m).name.equals("aaaa"))).setPassMessage("Desired path taken").setFailMessage("The path taken was incorrect");
        expectAtEnd(() -> ScreenGame.finishedQuick).setPassMessage("Level finished").setFailMessage("Pathfinding not completed fast enough");
    }
}
