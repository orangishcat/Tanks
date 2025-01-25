package tanks.replay;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.Panel;
import tanks.gui.ScreenElement;
import tanks.gui.TextBox;
import tanks.gui.screen.ScreenGame;
import static tanks.replay.Replay.*;

public class ReplayHandler
{
    static TextBox saveBox;

    public static void draw()
    {
        Replay r = Replay.currentPlaying;

        if (r != null)
            drawPlayingReplay(r);
        if (Replay.currentReplayToSave != null)
            drawSavePopup(Replay.currentReplayToSave);
    }

    private static void drawSavePopup(Replay r)
    {
        double sX = 400, sY = 120;
        double x = Drawing.drawing.getInterfaceEdgeX(true) - 10 - sX / 2, y = Drawing.drawing.interfaceSizeY * 0.25;
        Drawing.drawing.setColor(0, 0, 0, 128);
        Drawing.drawing.drawPopup(x, y, sX, sY, 5, 3);

        if (saveBox != null)
        {
            saveBox.setPosition(x, y + 13);
            saveBox.draw();
        }
    }

    private static void drawPlayingReplay(Replay r)
    {
        double x = Drawing.drawing.getInterfaceEdgeX(true) - 10, y = Drawing.drawing.getInterfaceEdgeY(true) - 20;

        if (r.queue.size() > 100)
            r.queue.remove();

        int i = 0;
        Drawing.drawing.setColor(255, 0, 0);
        for (double events : r.queue)
            Drawing.drawing.fillInterfaceOval(x - (i++) * 2, y - 150 - events * 20, 5, 5);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(16);

        ReplayEvents.IReplayEvent event = r.getCurrentEvent();
        ReplayEvents.IReplayEvent prev = r.getPrevEvent();

        if (event != null)
            event.drawDebug(x, y);

        if (event instanceof ReplayEvents.Tick)
        {
            double mx = ((ReplayEvents.Tick) event).mouseX, my = ((ReplayEvents.Tick) event).mouseY;
            double prevMX = mx, prevMY = my;

            if (prev instanceof ReplayEvents.Tick)
            {
                prevMX = ((ReplayEvents.Tick) prev).mouseX;
                prevMY = ((ReplayEvents.Tick) prev).mouseY;
            }

            double percent = r.getTickDelta();

            int br = Level.isDark() ? 200 : 100;
            Drawing.drawing.setColor(br, br, br);
            Drawing.drawing.drawImage("cursor.png", interp(prevMX, mx, percent), interp(prevMY, my, percent), 50, 50);
        }
    }

    public static void preUpdate()
    {
        if (currentPlaying == null)
            return;

        frameFreq = Panel.frameFrequency;
    }

    public static void update()
    {
        if (currentPlaying != null)
        {
            currentPlaying.play();
            currentPlaying.updateControls();

            if (currentPlaying.finished() && currentPlaying.waitEnded())
            {
                Panel.notifs.add(new ScreenElement.Notification("Finished playing recording"));
                currentPlaying = null;
            }
        }

        if (Replay.currentReplayToSave != null)
            saveBox.update();

        ScreenGame g = ScreenGame.getInstance();
        if (g == null)
            Replay.currentPlaying = null;

        if ((g != null && g.playingReplay) && currentPlaying == null)
            Game.exitToTitle();
    }
}
