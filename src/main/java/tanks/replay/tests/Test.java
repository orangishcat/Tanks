package tanks.replay.tests;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.replay.Replay;
import tanks.replay.ReplayEvents;
import tanks.replay.TankReplayPlayer;
import tanks.tank.TankPlayer;

import java.util.ArrayList;
import java.util.Objects;

public abstract class Test
{
    public static TestRegistry registry;
    public static TestRunner runner;

    public Replay replay;
    public String replayName;
    public Level level;
    public String levelName;
    public final ArrayList<TestFunction> expectOnce = new ArrayList<>();
    public final ArrayList<TestFunction> expectAtEnd = new ArrayList<>();
    public boolean oncePassed = false, passed;
    public boolean loaded = false;

    public TankReplayPlayer playerTank = new TankReplayPlayer(new TankPlayer(-1, -1, -1));
    public String name = "Test";
    public double maximumTime = 10 * 60 * 100;
    public boolean fixedFPS = false;
    private int expectOncePos = 0;
    public boolean finished = false;
    protected int passedEndCases, totalCases;

    public Test(String path, boolean isReplay)
    {
        if (isReplay)
            this.replayName = path;
        else
            this.levelName = path;
    }

    public Test(String path)
    {
        this(path, true);
    }

    public static void reset()
    {
        if (registry == null)
            registry = new TestRegistry();
        else
            registry.refreshCache();

        runner = new TestRunner();
    }

    public void onLoad()
    {
        loaded = true;
        if (fixedFPS)
            Panel.setTickSprint(true);

        if (replayName != null)
        {
            (replay = Replay.read(replayName)).loadAndPlay();
            replay.allowControls = false;
        }
        else
        {
            Game.cleanUp();
            Drawing.drawing.terrainRenderer.reset();
            Drawing.drawing.trackRenderer.reset();
            (level = new Level(Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + levelName + ".tanks").read())).loadLevel();
            ReplayEvents.LevelChange.enterGame();
        }
    }

    /** @return true if test completed */
    public boolean update()
    {
        if (finished)
            return true;

        if (playerTank.angle == -1)
        {
            for (Movable m : Game.movables)
            {
                if (m instanceof TankReplayPlayer)
                {
                    playerTank = (TankReplayPlayer) m;
                    break;
                }
            }
        }

        if (!oncePassed)
        {
            if (expectOncePos >= expectOnce.size())
                oncePassed = true;
            else if (expectOnce.get(expectOncePos).checkPassed())
                expectOncePos++;
        }

        return finished = ((maximumTime -= Panel.frameFrequency) <= 0 || ScreenGame.finished);
    }

    public TestFunction expectOnce(ToBooleanFunction condition)
    {
        totalCases++;
        return new TestFunction(condition).addTo(expectOnce);
    }

    public TestFunction expectAtEnd(ToBooleanFunction condition)
    {
        totalCases++;
        return new TestFunction(condition).addTo(expectAtEnd);
    }

    public TestFunction expectAllyWin()
    {
        return expectTeamWin("ally").setPassMessage("Allies won").setFailMessage("Allies didn't win");
    }

    public TestFunction expectTeamWin(String... teamNames)
    {
        return expectTeamWin(false, teamNames);
    }

    public TestFunction expectTeamWin(boolean strict, String... teamNames)
    {
        return expectAtEnd(() ->
        {
            ScreenGame g = ScreenGame.getInstance();
            if (g == null || !ScreenGame.finishedQuick)
                return false;

            ObjectArrayList<Team> teamsToWin = g.aliveTeams;
            if (strict && teamsToWin.size() != teamNames.length)
                return false;

            for (String s : teamNames)
            {
                boolean found = false;
                for (Team t : teamsToWin)
                {
                    if (Objects.equals(t.name, s))
                    {
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
            }
            return true;
        });
    }

    public boolean testPassed()
    {
        if (!oncePassed)
            return passed = false;

        passedEndCases = 0;
        passed = true;
        for (TestFunction f : expectAtEnd)
        {
            if (!f.checkPassed())
                passed = false;
            else
                passedEndCases++;
        }
        return passed;
    }

    public int passedCases()
    {
        return passedEndCases + expectOncePos;
    }

    public int totalCases()
    {
        return totalCases;
    }

    public static class TestFunction
    {
        public ToBooleanFunction func;
        public String passMessage, failMessage;
        public boolean passed;

        public TestFunction(ToBooleanFunction func)
        {
            this.func = func;
        }

        public TestFunction setName(String name)
        {
            String s = "'" + name + "'";
            return setPassMessage(s).setFailMessage(s);
        }

        public TestFunction setPassMessage(String message)
        {
            passMessage = message;
            return this;
        }

        public TestFunction setFailMessage(String message)
        {
            failMessage = message;
            return this;
        }

        public boolean checkPassed()
        {
            return passed = func.apply();
        }

        public TestFunction addTo(ArrayList<TestFunction> arr)
        {
            arr.add(this);
            return this;
        }
    }
}
