package tanks.test;

import tanks.Level;
import tanks.ToBooleanFunction;
import tanks.replay.Replay;

public abstract class Test
{
    public Replay replay;
    public String levelString;
    public Level level;
    public ToBooleanFunction additionalCondition = () -> true;

    public Test(String levelString, Replay replay)
    {
        this.levelString = levelString;
        this.replay = replay;
        if (!replay.forTests)
            System.err.println("Replay should be player only");
    }

    public void load()
    {
        level = new Level(levelString);
    }

    public void expectAtEnd(ToBooleanFunction condition)
    {
        additionalCondition = additionalCondition.and(condition);
    }

    public boolean testPassed()
    {
        return level != null && replay != null && additionalCondition.apply();
    }
}
