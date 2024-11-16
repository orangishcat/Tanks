package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;

public class ScreenTestFireworks extends Screen implements IDarkScreen
{
    public String debugFireworks = "Manual fireworks: ";

    public FireworksDisplay fireworksDisplay = new FireworksDisplay();

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 150, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenDebug());

    Button fireworksMode = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 90, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            FireworksDisplay.debug = !FireworksDisplay.debug;

            if (FireworksDisplay.debug)
                fireworksMode.setText(debugFireworks, ScreenOptions.onText);
            else
                fireworksMode.setText(debugFireworks, ScreenOptions.offText);
        }
    });

    public ScreenTestFireworks()
    {
        if (FireworksDisplay.debug)
            fireworksMode.setText(debugFireworks, ScreenOptions.onText);
        else
            fireworksMode.setText(debugFireworks, ScreenOptions.offText);

        this.music = "win_music.ogg";
    }

    @Override
    public void update()
    {
        fireworksMode.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        Panel.darkness = Math.min(Panel.darkness + Panel.frameFrequency * 1.5, 191);

        Drawing.drawing.setInterfaceFontSize(this.titleSize * 4);
        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, "Fireworks!!!");

        if (!Game.game.window.drawingShadow)
            fireworksDisplay.draw();

        fireworksMode.draw();
        back.draw();
    }
}
