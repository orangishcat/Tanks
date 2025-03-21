package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.tank.TankPlayerRemote;

public class ScreenOptionsPartyHost extends Screen
{
    public static final String anticheatText = "Anticheat: ";
    public static final String disableFriendlyFireText = "Friendly fire: ";

    public static final String weakText = "\u00A7200100000255weak";
    public static final String strongText = "\u00A7000200000255strong";

    public static final String defaultText = "\u00A7000200000255default";
    public static final String disabledText = "\u00A7200000000255off";

    public boolean fromParty = false;

    Button anticheat = new Button(this.centerX, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            if (!TankPlayerRemote.checkMotion)
            {
                TankPlayerRemote.checkMotion = true;
                TankPlayerRemote.weakTimeCheck = false;
                TankPlayerRemote.anticheatMaxTimeOffset = TankPlayerRemote.anticheatStrongTimeOffset;
            }
            else if (!TankPlayerRemote.weakTimeCheck)
            {
                TankPlayerRemote.weakTimeCheck = true;
                TankPlayerRemote.anticheatMaxTimeOffset = TankPlayerRemote.anticheatWeakTimeOffset;
            }
            else
                TankPlayerRemote.checkMotion = false;

            if (!TankPlayerRemote.checkMotion)
                anticheat.setText(anticheatText, ScreenOptions.offText);
            else if (!TankPlayerRemote.weakTimeCheck)
                anticheat.setText(anticheatText, strongText);
            else
                anticheat.setText(anticheatText, weakText);
        }
    },
            "When this option is enabled---while hosting a party,---other players' positions and---velocities will be checked---and corrected if invalid.------Weaker settings work better---with less stable connections.");

    Button disableFriendlyFire = new Button(this.centerX, this.centerY + this.objYSpace * 1, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.disablePartyFriendlyFire = !Game.disablePartyFriendlyFire;

            if (Game.disablePartyFriendlyFire)
                disableFriendlyFire.setText(disableFriendlyFireText, disabledText);
            else
                disableFriendlyFire.setText(disableFriendlyFireText, defaultText);
        }
    },
            "Disables all friendly fire in the party.---Tanks on the same team will---not damage each other.---Useful for co-op in bigger parties.");

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () ->
    {
        if (fromParty)
        {
            Game.screen = ScreenPartyHost.activeScreen;
            ScreenOptions.saveOptions(Game.homedir);
        }
        else
            Game.screen = new ScreenOptionsMultiplayer();
    }
    );

    Button tests = new Button(0, 0, this.objHeight * 1.5, this.objHeight * 1.5, "",
            () -> Game.screen = new ScreenAutomatedTests(), "Automated tests");

    public TextBox timer;
    public TextBox bots;

    public ScreenOptionsPartyHost()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        if (!TankPlayerRemote.checkMotion)
            anticheat.setText(anticheatText, ScreenOptions.offText);
        else if (!TankPlayerRemote.weakTimeCheck)
            anticheat.setText(anticheatText, strongText);
        else
            anticheat.setText(anticheatText, weakText);

        tests.fullInfo = true;
        tests.image = "icons/gear.png";
        tests.imageSizeX = this.objHeight;
        tests.imageSizeY = this.objHeight;

        if (Game.disablePartyFriendlyFire)
            disableFriendlyFire.setText(disableFriendlyFireText, disabledText);
        else
            disableFriendlyFire.setText(disableFriendlyFireText, defaultText);

        timer = new TextBox(this.centerX, this.centerY + this.objYSpace * 0, this.objWidth, this.objHeight, "Countdown time", () ->
        {
            if (timer.inputText.isEmpty())
                timer.inputText = Game.partyStartTime / 100.0 + "";
            else
                Game.partyStartTime = Double.parseDouble(timer.inputText) * 100;
        }, Game.partyStartTime / 100.0 + "", "The wait time in seconds after---all players are ready before---the battle begins.");

        timer.maxValue = 60;
        timer.maxChars = 4;
        timer.checkMaxValue = true;
        timer.allowDoubles = true;
        timer.allowLetters = false;
        timer.allowSpaces = false;

        bots = new TextBox(this.centerX, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Bot players", () ->
        {
            if (bots.inputText.isEmpty())
                bots.inputText = Game.botPlayerCount + "";
            else
                Game.botPlayerCount = Integer.parseInt(bots.inputText);

            if (ScreenPartyHost.isServer)
                ScreenPartyHost.setBotCount(Game.botPlayerCount);
        }, Game.botPlayerCount + "", "How many extra bot players---to add to parties");

        bots.maxValue = 1000;
        bots.maxChars = 3;
        bots.checkMaxValue = true;
        bots.allowLetters = false;
        bots.allowSpaces = false;
    }

    @Override
    public void update()
    {
        back.update();
        timer.update();
        anticheat.update();
        tests.update();
        bots.update();
        disableFriendlyFire.update();
    }

    @Override
    public void draw()
    {
        tests.posX = (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
                + Drawing.drawing.interfaceSizeX - 50 * Drawing.drawing.interfaceScaleZoom - Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale;
        tests.posY = ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2
                + Drawing.drawing.interfaceSizeY - (ScreenPartyHost.isServer ? 100 : 50) * Drawing.drawing.interfaceScaleZoom;

        this.drawDefaultBackground();
        back.draw();
        anticheat.draw();
        tests.draw();
        disableFriendlyFire.draw();
        bots.draw();
        timer.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Party host options");
    }

}
