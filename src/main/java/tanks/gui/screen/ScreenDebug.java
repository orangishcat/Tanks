package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.replay.ReplayIO;
import tanks.tank.TankPlayer;

import java.util.ArrayList;
import java.util.Arrays;

public class ScreenDebug extends Screen
{
    public String traceText = "Trace rays: ";
    public String firstPersonText = "First person: ";
    public String followingCamText = "Immersive camera: ";
    public String showPathfindingText = "Show pathfinding: ";
    public String tankIDsText = "Show tank IDs: ";
    public String invulnerableText = "Invulnerable: ";
    public String fancyLightsText = "Fancy lighting: ";
    public String destroyCheatText = "Destroy cheat: ";
    public String facesText = "Draw faces: ";
    public String immutableFacesText = "Immutable faces: ";

    public ButtonList debugButtons;
    Button test = new Button(0, 0, this.objWidth, this.objHeight, "Test stuff", () -> Game.screen = new ScreenTestDebug());

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace * 4, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenTitle());

    public ScreenDebug()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        debugButtons = new ButtonList(new ArrayList<>(Arrays.asList(
                test, traceAllRays, firstPerson, followingCam, destroyCheat, invulnerable,
                fancyLighting, tankIDs, showPathfinding, drawFaces, immutableFaces
        )), 0, 0, -30);
        debugButtons.setRowsAndColumns(4, 3);

        if (Game.traceAllRays)
            traceAllRays.setText(traceText, ScreenOptions.onText);
        else
            traceAllRays.setText(traceText, ScreenOptions.offText);

        if (Game.firstPerson)
            firstPerson.setText(firstPersonText, ScreenOptions.onText);
        else
            firstPerson.setText(firstPersonText, ScreenOptions.offText);

        if (Game.followingCam)
            followingCam.setText(followingCamText, ScreenOptions.onText);
        else
            followingCam.setText(followingCamText, ScreenOptions.offText);

        showPathfinding.setText(showPathfindingText, Game.showPathfinding ? ScreenOptions.onText : ScreenOptions.offText);

        if (Game.showTankIDs)
            tankIDs.setText(tankIDsText, ScreenOptions.onText);
        else
            tankIDs.setText(tankIDsText, ScreenOptions.offText);

        if (Game.invulnerable)
            invulnerable.setText(invulnerableText, ScreenOptions.onText);
        else
            invulnerable.setText(invulnerableText, ScreenOptions.offText);

        if (Game.fancyLights)
            fancyLighting.setText(fancyLightsText, ScreenOptions.onText);
        else
            fancyLighting.setText(fancyLightsText, ScreenOptions.offText);

        if (TankPlayer.enableDestroyCheat)
            destroyCheat.setText(destroyCheatText, ScreenOptions.onText);
        else
            destroyCheat.setText(destroyCheatText, ScreenOptions.offText);

        if (Game.drawFaces)
            drawFaces.setText(facesText, ScreenOptions.onText);
        else
            drawFaces.setText(facesText, ScreenOptions.offText);

        if (Game.immutableFaces)
            immutableFaces.setText(immutableFacesText, ScreenOptions.onText);
        else
            immutableFaces.setText(immutableFacesText, ScreenOptions.offText);
    }

    @Override
    public void update()
    {
        debugButtons.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Debug menu");

        debugButtons.draw();
        back.draw();
    }

    Button traceAllRays = new Button(0, 0, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.traceAllRays = !Game.traceAllRays;

            if (Game.traceAllRays)
                traceAllRays.setText(traceText, ScreenOptions.onText);
            else
                traceAllRays.setText(traceText, ScreenOptions.offText);
        }
    });



    Button firstPerson = new Button(0, 0, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.firstPerson = !Game.firstPerson;

            if (Game.firstPerson)
                firstPerson.setText(firstPersonText, ScreenOptions.onText);
            else
                firstPerson.setText(firstPersonText, ScreenOptions.offText);
        }
    });

    Button followingCam = new Button(0, 0, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.followingCam = !Game.followingCam;

            if (Game.followingCam)
                followingCam.setText(followingCamText, ScreenOptions.onText);
            else
                followingCam.setText(followingCamText, ScreenOptions.offText);
        }
    });

    Button showPathfinding = new Button(0, 0, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.showPathfinding = !Game.showPathfinding;

            if (Game.showPathfinding)
                showPathfinding.setText(showPathfindingText, ScreenOptions.onText);
            else
                showPathfinding.setText(showPathfindingText, ScreenOptions.offText);
        }
    });

    Button tankIDs = new Button(0, 0, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.showTankIDs = !Game.showTankIDs;

            if (Game.showTankIDs)
                tankIDs.setText(tankIDsText, ScreenOptions.onText);
            else
                tankIDs.setText(tankIDsText, ScreenOptions.offText);
        }
    });

    Button invulnerable = new Button(0, 0, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.invulnerable = !Game.invulnerable;

            if (Game.invulnerable)
                invulnerable.setText(invulnerableText, ScreenOptions.onText);
            else
                invulnerable.setText(invulnerableText, ScreenOptions.offText);
        }
    });

    Button fancyLighting = new Button(0, 0, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.fancyLights = !Game.fancyLights;

            if (Game.fancyLights)
                fancyLighting.setText(fancyLightsText, ScreenOptions.onText);
            else
                fancyLighting.setText(fancyLightsText, ScreenOptions.offText);
        }
    });

    Button destroyCheat = new Button(0, 0, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            TankPlayer.enableDestroyCheat = !TankPlayer.enableDestroyCheat;

            if (TankPlayer.enableDestroyCheat)
                destroyCheat.setText(destroyCheatText, ScreenOptions.onText);
            else
                destroyCheat.setText(destroyCheatText, ScreenOptions.offText);
        }
    });

    Button drawFaces = new Button(0, 0, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.drawFaces = !Game.drawFaces;
            if (Game.drawFaces)
                drawFaces.setText(facesText, ScreenOptions.onText);
            else
                drawFaces.setText(facesText, ScreenOptions.offText);
        }
    });

    Button immutableFaces = new Button(0, 0, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.immutableFaces = !Game.immutableFaces;
            if (Game.immutableFaces)
                immutableFaces.setText(immutableFacesText, ScreenOptions.onText);
            else
                immutableFaces.setText(immutableFacesText, ScreenOptions.offText);
        }
    });


    @Override
    public void onFilesDropped(String... filePaths)
    {
        ReplayIO.read(filePaths[0]).loadAndPlay();
    }
}
