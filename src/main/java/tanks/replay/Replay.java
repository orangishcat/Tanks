package tanks.replay;

import basewindow.InputCodes;
import tanks.Game;
import tanks.Level;
import tanks.Panel;
import tanks.bullet.Bullet;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.input.InputBinding;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.screen.ScreenGame;
import tanks.network.event.*;
import tanks.replay.ReplayEvents.IReplayEvent;
import tanks.replay.ReplayEvents.LevelChange;
import tanks.replay.ReplayEvents.Tick;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Replay
{
    public static double frameFreq;
    public static boolean isRecording;
    public static Replay currentReplay, currentPlaying;
    public static double deltaCS = 100 / 20.;

    public static Replay currentReplayToSave;

    public double age = 0;
    public int pos = 0;
    public double endTimer = 50;

    public boolean forTests = false;

    public Level prevLevel;
    public String name = "test";
    public double lastAge, stackUpdateTimer = deltaCS;
    public ArrayList<IReplayEvent> events = new ArrayList<>();

    public ScreenGame prevGame;
    public Queue<Double> queue = new LinkedList<>();

    public Replay allowControls(boolean allowControls)
    {
        this.allowControls = allowControls;
        return this;
    }

    public boolean allowControls = true;

    private static boolean fromPlayer(INetworkEvent e)
    {
        if (e instanceof EventTankUpdate)
            return isPlayer(((EventTankUpdate) e).tank);
        else if (e instanceof EventShootBullet)
            return isPlayer(((EventShootBullet) e).tank);
        else if (e instanceof EventLayMine)
            return isPlayer(((EventLayMine) e).tank);
        else if (e instanceof EventBulletInstantWaypoint)
            return isPlayerBullet(((EventBulletInstantWaypoint) e).bullet);
        else if (e instanceof EventBulletDestroyed)
            return isPlayerBullet(((EventBulletDestroyed) e).bullet);
        return false;
    }

    private static boolean isPlayerBullet(int bullet)
    {
        Bullet b = Bullet.idMap.get(bullet);
        return b != null && b.tank instanceof TankPlayer;
    }

    private static boolean isPlayer(int id)
    {
        Tank t = Tank.idMap.get(id);
        return t instanceof TankPlayer;
    }

    public double getTickDelta()
    {
        return (age - lastAge) / (getCurrentEvent().delay() * 0.1);
    }

    public IReplayEvent getCurrentEvent()
    {
        if (pos >= events.size())
            return null;
        return events.get(pos);
    }

    public IReplayEvent getPrevEvent()
    {
        if (0 < pos && pos < events.size())
            return events.get(pos - 1);
        return null;
    }

    public boolean finished()
    {
        return pos >= events.size();
    }

    public boolean waitEnded()
    {
        return (endTimer -= Panel.frameFrequency) <= 0;
    }

    public void loadAndPlay()
    {
        Replay.currentPlaying = this;
    }

    public void shiftFrame(int frame)
    {
        pos += frame;
        getCurrentEvent().execute();

        if (Game.screen instanceof ScreenGame)
        {
            boolean prev = ((ScreenGame) Game.screen).paused;
            ((ScreenGame) Game.screen).paused = false;
            Game.screen.update();
            ((ScreenGame) Game.screen).paused = prev;
        }

        lastAge += getCurrentEvent().delay() * 0.1;
        age = lastAge;
    }

    public void shiftPosition(double ms)
    {
        IReplayEvent e;

        if (ms < 0)
        {
            while (ms < 0 && pos > 0)
            {
                ms += (e = events.get(pos--)).delay();
                e.execute();
            }
        }
        else
        {
            while (ms > 0 && pos < events.size() - 1)
            {
                ms -= (e = events.get(pos++)).delay();
                e.execute();
            }
        }
    }

    public void play()
    {
        ScreenGame g = ScreenGame.getInstance();
        if (g != null && (!g.playing || g.paused))
            return;

        age += frameFreq;
        playNextTick();
    }

    InputBindingGroup left = new InputBindingGroup("replay.left", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_LEFT));
    InputBindingGroup right = new InputBindingGroup("replay.right", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_RIGHT));
    InputBindingGroup leftFrame = new InputBindingGroup("replay.lf", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_COMMA));
    InputBindingGroup rightFrame = new InputBindingGroup("replay.rf", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_PERIOD));

    public void updateControls()
    {
        if (!allowControls)
            return;

        if (left.isValid())
        {
            left.invalidate();
            shiftPosition(-1000);
        }
        if (right.isValid())
        {
            right.invalidate();
            shiftPosition(1000);
        }
        if (leftFrame.isValid())
        {
            leftFrame.invalidate();
            shiftFrame(-1);
        }
        if (rightFrame.isValid())
        {
            rightFrame.invalidate();
            shiftFrame(1);
        }
    }

    public void playNextTick()
    {
        if (pos >= events.size())
            return;

        try
        {
            IReplayEvent event = getCurrentEvent();
            double eventCnt = 0;
            while (event != null && age - lastAge >= event.delay() * 0.1)
            {
                eventCnt++;
                event.execute();

                lastAge += event.delay() * 0.1;
                pos++;
                event = getCurrentEvent();
            }
            queue.add(eventCnt);
        }
        catch (Exception e)
        {
            currentPlaying = null;
            throw new RuntimeException(e);
        }
    }

    public void updateRecording(ArrayList<INetworkEvent> eventsThisFrame)
    {
        ScreenGame g = ScreenGame.getInstance();
        if (g == null || g.paused || !g.playing)
            return;

        double now = g.gameAge;
        if (!forTests)
        {
            if ((stackUpdateTimer -= Panel.frameFrequency) <= 0)
            {
                eventsThisFrame.removeIf(IStackableEvent.class::isInstance);
                stackUpdateTimer = deltaCS;
            }
        }
        else
            eventsThisFrame.removeIf(e -> !Replay.fromPlayer(e));

        if (Game.screen != prevGame && Game.screen instanceof ScreenGame)
        {
            prevGame = (ScreenGame) Game.screen;
            lastAge = 0;
        }

        if (eventsThisFrame.isEmpty())
            return;

        Tick t = new Tick().setParams(eventsThisFrame, (now - lastAge) * 10);
        if (prevLevel != Game.currentLevel)
            events.add(new LevelChange().setLS(Game.currentLevel.levelString));

        lastAge = now;
        prevLevel = Game.currentLevel;
        events.add(t);
    }

    public void save(String name)
    {
        ReplayIO.save(this, Game.homedir + Game.replaysDir + name + ".tanks");
    }

    public static void toggleRecording()
    {
        isRecording = !isRecording;
        currentPlaying = null;

        if (isRecording)
            startRecording();
        else
            stopRecording();
    }

    public static void startRecording()
    {
        currentReplay = new Replay();
    }

    public static void stopRecording()
    {
        currentReplayToSave = currentReplay;
        ReplayHandler.saveBox = new TextBox(0, 0, 350, 40, "Save replay", () -> {},
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH_mm_ss")));
        ReplayHandler.saveButton = new Button(0, 0, 170, 35, "Save", () ->
        {
            currentReplayToSave.save(ReplayHandler.saveBox.inputText);
            currentReplayToSave = null;
        });
        ReplayHandler.cancel = new Button(0, 0, 170, 35, "Cancel", () -> currentReplayToSave = null);
        currentReplay = null;
    }

    public static Replay read(String replayName)
    {
        return ReplayIO.read(Game.homedir + Game.replaysDir + replayName + ".tanks");
    }

    public static double interp(double start, double end, double percentage)
    {
        return start + (end - start) * percentage;
    }
}
