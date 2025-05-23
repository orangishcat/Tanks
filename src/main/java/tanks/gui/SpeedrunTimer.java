package tanks.gui;

import tanks.*;
import tanks.gui.screen.IDarkScreen;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;

public class SpeedrunTimer
{
    public static void draw()
    {
        double alpha = 127;

        if (!(Game.screen instanceof ScreenGame) || ScreenGame.finishedQuick)
            alpha += 64 + 64 * Math.sin(System.currentTimeMillis() / 1000.0 * Math.PI * 4);

        if (Level.isDark() || (Game.screen instanceof IDarkScreen && Panel.win && Game.effectsEnabled))
            Drawing.drawing.setColor(255, 255, 255, alpha);
        else
            Drawing.drawing.setColor(0, 0, 0, alpha);

        double posX = -(Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2 + Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale + 50;
        double posY = -((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2 + 50;

        String levelDiff = "";
        String crusadeDiff = "";

        if (Crusade.crusadeMode && ScreenGame.finishedQuick && Panel.win && Crusade.currentCrusade.bestTimes != null && !ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
        {
            double time = 0;
            for (int i = 0; i <= Crusade.currentCrusade.currentLevel; i++)
                time += Crusade.currentCrusade.bestTimes.get(i);

            double ltime = Crusade.currentCrusade.bestTimes.get(Crusade.currentCrusade.currentLevel);

            if (ltime > ScreenGame.lastTimePassed)
                levelDiff = "\u00A7000255000255-" + getTime(ltime - ScreenGame.lastTimePassed);
            else if (ScreenGame.lastTimePassed == ltime)
                levelDiff = "\u00A7255255000255" + getTime(ScreenGame.lastTimePassed - ltime);
            else
                levelDiff = "\u00A7255000000255+" + getTime(ScreenGame.lastTimePassed - ltime);

            if (time > Crusade.currentCrusade.timePassed)
                crusadeDiff = "\u00A7000255000255-" + getTime(time - Crusade.currentCrusade.timePassed);
            else if (time == Crusade.currentCrusade.timePassed)
                crusadeDiff = "\u00A7255255000255" + getTime(Crusade.currentCrusade.timePassed - time);
            else
                crusadeDiff = "\u00A7255000000255+" + getTime(Crusade.currentCrusade.timePassed - time);
        }

        if (!Game.showSpeedrunTimer)
            return;

        if (Level.isDark() || (Game.screen instanceof IDarkScreen && Panel.win && Game.effectsEnabled))
            Drawing.drawing.setColor(255, 255, 255, alpha);
        else
            Drawing.drawing.setColor(0, 0, 0, alpha);

        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.drawInterfaceText(posX, posY, "Level time: " + getTime(ScreenGame.lastTimePassed) + levelDiff, false);

        if (Crusade.crusadeMode)
        {
            if (Level.isDark() || (Game.screen instanceof IDarkScreen && Panel.win && Game.effectsEnabled))
                Drawing.drawing.setColor(255, 255, 255, alpha);
            else
                Drawing.drawing.setColor(0, 0, 0, alpha);

            Drawing.drawing.setInterfaceFontSize(12);
            Drawing.drawing.drawInterfaceText(posX, posY + 20, "Crusade time: " + getTime(Crusade.currentCrusade.timePassed) + crusadeDiff, false);
        }
    }

    public static String getTime(double time)
    {
        long milliTotal = Math.round(time * 10);
        long sec = milliTotal / 1000;
        long min = sec / 60;
        long hr = min / 60;

        String timer = "";

        if (hr > 0)
            timer += hr + ":";

        if (min % 60 >= 10 || hr <= 0)
            timer += min % 60 + ":";
        else
            timer += "0" + min % 60 + ":";

        timer += String.format("%02d.%03d", sec % 60, milliTotal % 1000);
        return timer;
    }
}
