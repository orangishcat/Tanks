package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;
import tanks.gui.SpeedrunTimer;
import tanks.translation.Translation;

import java.util.ArrayList;
import java.util.Arrays;

public class ScreenCrusadeDetails extends Screen implements ICrusadePreviewScreen
{
    public Crusade crusade;
    public DisplayCrusadeLevels background;
    public ArrayList<String> description = new ArrayList<>();

    public double bestTime = -1;

    protected int textOffset = 0;
    protected int levelsTextOffset = 0;
    protected int sizeY = 9;

    public Button begin = new Button(this.centerX, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Play", new Runnable()
    {
        @Override
        public void run()
        {
            Crusade.currentCrusade = crusade;
            Crusade.crusadeMode = true;
            Crusade.currentCrusade.begin();
            Game.screen = new ScreenGame(Crusade.currentCrusade);
        }
    });

    public Button resume = new Button(this.centerX, this.centerY - this.objYSpace * 0.5, this.objWidth, this.objHeight, "Resume", new Runnable()
    {
        @Override
        public void run()
        {
            Crusade.currentCrusade = crusade;
            Crusade.crusadeMode = true;
            Crusade.currentCrusade.loadLevel();
            Game.screen = new ScreenGame(Crusade.currentCrusade);
        }
    });

    public Button startOver = new Button(this.centerX, this.centerY + this.objYSpace * 0.5, this.objWidth, this.objHeight, "Start over", new Runnable()
    {
        @Override
        public void run()
        {
            Crusade c;

            if (crusade.internal)
            {
                String[] l = crusade.contents.split("\n");
                c = new Crusade(new ArrayList<>(Arrays.asList(l)), crusade.name, crusade.fileName);
            }
            else
                c = new Crusade(Game.game.fileManager.getFile(crusade.fileName), crusade.name);

            Crusade.currentCrusade = c;
            Crusade.crusadeMode = true;
            Crusade.currentCrusade.begin();
            Game.screen = new ScreenGame(Crusade.currentCrusade);
        }
    });

    public Button edit = new Button(this.centerX, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Edit", () ->
    {
        if (crusade.started)
            Game.screen = new ScreenCrusadeEditWarning(Game.screen, crusade);
        else
            Game.screen = new ScreenCrusadeEditor(crusade);
    });

    public Button delete = new Button(this.centerX, this.centerY + this.objYSpace * 2.5, this.objWidth, this.objHeight, "Delete crusade", () -> Game.screen = new ScreenConfirmDeleteCrusade(Game.screen, crusade));

    public Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () ->
    {
        if (ScreenPartyHost.isServer)
            Game.screen = new ScreenPartyCrusades();
        else
            Game.screen = new ScreenCrusades();
    });

    public Button back2 = new Button(this.centerX, this.centerY + this.objYSpace * 2.5, this.objWidth, this.objHeight, "Back", () ->
    {
        if (ScreenPartyHost.isServer)
            Game.screen = new ScreenPartyCrusades();
        else
            Game.screen = new ScreenCrusades();
    });

    Button showRecordButton = new Button(this.centerX + Drawing.drawing.baseInterfaceSizeX * 0.35 - 30, this.centerY + this.objYSpace * 4, 30, 30, "i", () ->
            Game.screen = new ScreenCrusadeStats(crusade, this), "View best run");


    public ScreenCrusadeDetails(Crusade c)
    {
        this.crusade = c;

        this.music = "menu_5.ogg";
        this.musicID = "menu";

        showRecordButton.fullInfo = true;
        showRecordButton.bgColR = 0;
        showRecordButton.bgColG = 127;
        showRecordButton.bgColB = 255;
        showRecordButton.selectedColR = 0;
        showRecordButton.selectedColG = 0;
        showRecordButton.selectedColB = 255;
        showRecordButton.textColR = 255;
        showRecordButton.textColG = 255;
        showRecordButton.textColB = 255;
        showRecordButton.fontSize = 22;

        if (Game.previewCrusades)
            this.forceInBounds = true;

        if (c.levels.isEmpty())
        {
            begin.enabled = false;
            begin.enableHover = true;
            begin.setHoverText("This crusade has no levels.---Add some to play it!");
        }

        if (crusade.description != null)
            this.levelsTextOffset += (int) this.objYSpace;

        if (!c.internal)
        {
            if (!c.started)
            {
                if (!ScreenPartyHost.isServer)
                {
                    begin.posY -= this.objYSpace;

                    edit.posY -= this.objYSpace;
                    delete.posY -= this.objYSpace;
                    back.posY -= this.objYSpace;

                    back2.posY -= this.objYSpace;
                    begin.posY -= this.objYSpace;
                }
            }
            else
                sizeY += 2;
        }
        else if (c.started)
            sizeY++;

        if (!c.started)
            textOffset += 50;

        if (Game.previewCrusades)
            this.background = new DisplayCrusadeLevels(this.crusade);

        if (crusade.description != null)
            this.description = Drawing.drawing.wrapText(crusade.description.replaceAll("---", " "), 800, 24);

        if (crusade.description != null)
        {
            double addY = this.objYSpace * (this.description.size() * 0.5 + 0.5) * 0.8;

            this.textOffset -= (int) (addY / 3);

            if (!crusade.internal && !ScreenPartyHost.isServer)
                begin.posY += addY;

            resume.posY += addY;
            startOver.posY += addY;
            edit.posY += addY;
            delete.posY += addY;
            back.posY += addY;

            levelsTextOffset = (int) (resume.posY - this.centerY + this.objYSpace / 2 + addY / 3);
            sizeY += (int) (this.description.size() * 0.75);

            if (crusade.started)
                back2.posY = (crusade.internal ? startOver.posY : delete.posY) + this.objYSpace;
        }

        if (crusade.internal)
        {
            BaseFile f = Game.game.fileManager.getFile(Game.homedir + Game.crusadeDir + "/records/internal/" + crusade.name.replace(" ", "_").toLowerCase() + ".record");
            if (f.exists())
            {
                try
                {
                    f.startReading();
                    this.bestTime = 0;

                    while (f.hasNextLine())
                    {
                        this.bestTime += Double.parseDouble(f.nextLine());
                    }

                    f.stopReading();
                }
                catch (Exception e)
                {
                    Game.exitToCrash(e);
                }
            }
        }
    }

    @Override
    public void update()
    {
        if (crusade.started && !ScreenPartyHost.isServer)
        {
            resume.update();
            startOver.update();
        }
        else
            begin.update();

        if (!(crusade.readOnly || crusade.internal || ScreenPartyHost.isServer))
        {
            edit.update();
            delete.update();
            back.update();
        }
        else
            back2.update();

        if (this.bestTime >= 0 && !ScreenPartyHost.isServer)
            this.showRecordButton.update();
    }

    @Override
    public void draw()
    {
        if (Game.previewCrusades)
        {
            this.background.draw();

            if (!Game.game.window.drawingShadow)
                Game.game.window.clearDepth();

            Panel.darkness = Math.max(Panel.darkness - Panel.frameFrequency * 3, 0);

            Drawing.drawing.setColor(0, 0, 0, Math.max(0, Panel.darkness));
            Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth, Game.game.window.absoluteHeight - Drawing.drawing.statsHeight);
        }
        else
            this.drawDefaultBackground();

        Drawing.drawing.setColor(0, 0, 0, 255);

        if (Game.previewCrusades)
        {
            Drawing.drawing.setColor(0, 0, 0, 115);
            Drawing.drawing.drawPopup(this.centerX, this.centerY, Drawing.drawing.baseInterfaceSizeX * 0.7, this.objYSpace * sizeY + 20);
            Drawing.drawing.setColor(255, 255, 255);
        }

        Drawing.drawing.setInterfaceFontSize(this.textSize * 2);

        if (this.crusade.internal)
            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY + this.textOffset - this.objYSpace * 3.5, Translation.translate(crusade.name.replace("_", " ")));
        else
            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY + this.textOffset - this.objYSpace * 3.5, crusade.name.replace("_", " "));

        if (this.bestTime >= 0)
        {
            Drawing.drawing.setInterfaceFontSize(this.textSize * 0.75);

            if (Game.previewCrusades)
            {
                Drawing.drawing.displayInterfaceText(this.centerX + Drawing.drawing.baseInterfaceSizeX * 0.35 - 50, this.centerY + this.objYSpace * (sizeY / 2. - 0.5), true, "Best completion time: %s", SpeedrunTimer.getTime(this.bestTime));
                showRecordButton.posY = this.centerY + this.objYSpace * (sizeY / 2.0 - 0.5);
            }
            else
            {
                showRecordButton.posX = this.centerX + this.objXSpace / 2;
                showRecordButton.posY = this.centerY + textOffset + this.objYSpace * 4;
                Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + this.textOffset + this.objYSpace * 4, "Best completion time: %s", SpeedrunTimer.getTime(this.bestTime));
            }

            this.showRecordButton.draw();
        }

        if (!Game.previewCrusades)
            Drawing.drawing.setColor(0, 0, 0, 255);

        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + this.textOffset + this.levelsTextOffset - this.objYSpace * 2.5, "Levels: %d", crusade.levels.size());

        if (crusade.started && !ScreenPartyHost.isServer)
        {
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + this.textOffset + this.levelsTextOffset - this.objYSpace * 2, "Current battle: %d", (crusade.currentLevel + 1));
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + this.textOffset + this.levelsTextOffset - this.objYSpace * 1.5, "Remaining lives: %d", Game.player.remainingLives);
        }

        if (!(crusade.readOnly || crusade.internal || ScreenPartyHost.isServer))
        {
            edit.draw();
            delete.draw();
            back.draw();
        }
        else
            back2.draw();

        if (crusade.started && !ScreenPartyHost.isServer)
        {
            resume.draw();
            startOver.draw();
        }
        else
            begin.draw();

        if (crusade.description != null)
        {
            double pos = this.centerY - this.objYSpace * 2.5;

            if (Game.previewCrusades)
                Drawing.drawing.setColor(255, 255, 255);
            else
                Drawing.drawing.setColor(0, 0, 0);

            Drawing.drawing.setInterfaceFontSize(24);

            for (String s : this.description)
            {
                Drawing.drawing.displayInterfaceText(this.centerX, pos + this.textOffset, s);
                pos += this.objYSpace * 0.5;
            }
        }
    }
}
