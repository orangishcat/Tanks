package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenOptionsSpeedrun extends Screen
{
    public static final String deterministicText = "Deterministic: ";
    public static final String timerText = "Timer: ";
    public static final String bestRunText = "Show best run: ";

    public static final String deterministic30 = "\u00A700010020025530 FPS";
    public static final String deterministic60 = "\u00A700020000025560 FPS";

    Button timer = new Button(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.options.speedrun.showSpeedrunTimer = !Game.options.speedrun.showSpeedrunTimer;

            if (Game.options.speedrun.showSpeedrunTimer)
                timer.setText(timerText, ScreenOptions.onText);
            else
                timer.setText(timerText, ScreenOptions.offText);
        }
    },
            "When enabled, time spent---in the current level attempt---and crusade will be displayed");

    Button bestRun = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.options.speedrun.showBestTime = !Game.options.speedrun.showBestTime;

            if (Game.options.speedrun.showBestTime)
                bestRun.setText(bestRunText, ScreenOptions.onText);
            else
                bestRun.setText(bestRunText, ScreenOptions.offText);
        }
    },
            "When enabled and playing a built-in---crusade that you've already completed---at least once, the time difference from---your best run's time will show upon---clearing a level.");


    Button deterministic = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            if (Game.options.speedrun.deterministicMode == GameOptions.Deterministic._30fps)
            {
                Game.options.speedrun.deterministicMode != GameOptions.Deterministic.off = false;
                Game.options.speedrun.deterministicMode == GameOptions.Deterministic._30fps = false;
            }
            else if (Game.options.speedrun.deterministicMode != GameOptions.Deterministic.off)
                Game.options.speedrun.deterministicMode == GameOptions.Deterministic._30fps = true;
            else
                Game.options.speedrun.deterministicMode != GameOptions.Deterministic.off = true;

            if (Game.options.speedrun.deterministicMode != GameOptions.Deterministic.off && Game.options.speedrun.deterministicMode == GameOptions.Deterministic._30fps)
                deterministic.setText(deterministicText, deterministic30);
            else if (Game.options.speedrun.deterministicMode != GameOptions.Deterministic.off)
                deterministic.setText(deterministicText, deterministic60);
            else
                deterministic.setText(deterministicText, ScreenOptions.offText);
        }
    },
            "Deterministic mode changes the random number---generation to be fixed based on a seed, and---the game speed to be locked and independent---of framerate." +
                    "------This is useful for fair speedruns but may---provide for a less smooth experience.------If your device can't run Tanks at 60 FPS,---use 30 FPS mode to prevent slowdowns.");

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenOptions()
    );

    public ScreenOptionsSpeedrun()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        if (Game.options.speedrun.showSpeedrunTimer)
            timer.setText(timerText, ScreenOptions.onText);
        else
            timer.setText(timerText, ScreenOptions.offText);

        if (Game.options.speedrun.deterministicMode != GameOptions.Deterministic.off && Game.options.speedrun.deterministicMode == GameOptions.Deterministic._30fps)
            deterministic.setText(deterministicText, deterministic30);
        else if (Game.options.speedrun.deterministicMode != GameOptions.Deterministic.off)
            deterministic.setText(deterministicText, deterministic60);
        else
            deterministic.setText(deterministicText, ScreenOptions.offText);

        if (Game.options.speedrun.showBestTime)
            bestRun.setText(bestRunText, ScreenOptions.onText);
        else
            bestRun.setText(bestRunText, ScreenOptions.offText);
    }

    @Override
    public void update()
    {
        back.update();
        timer.update();
        deterministic.update();
        bestRun.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        back.draw();
        deterministic.draw();
        timer.draw();
        bestRun.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Speedrunning options");
    }
}
