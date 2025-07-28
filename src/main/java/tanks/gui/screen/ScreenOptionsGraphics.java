package tanks.gui.screen;

import tanks.*;
import tanks.gui.Button;

public class ScreenOptionsGraphics extends Screen
{
    public static final String terrainText = "Terrain: ";
    public static final String trailsText = "Bullet trails: ";
    public static final String glowText = "Glow effects: ";
    public static final String tankTexturesText = "Tank textures: ";

    public static final String graphics3dText = "3D graphics: ";
    public static final String ground3dText = "3D ground: ";
    public static final String perspectiveText = "View: ";
    public static final String antialiasingText = "Antialiasing: ";
    public static final String xrayBulletsText = "X-ray bullets: ";

    public static final String fancyText = "\u00A7000100200255fancy";
    public static final String fastText = "\u00A7200100000255fast";

    public static final String birdsEyeText = "\u00A7000100200255bird's-eye";
    public static final String angledText = "\u00A7200100000255angled";

    public static int viewNo = 0;

    public ScreenOptionsGraphics()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        if (Game.options.graphics.fancyTerrain)
            terrain.setText(terrainText, fancyText);
        else
            terrain.setText(terrainText, fastText);

        if (Game.options.graphics.bulletTrails != GameOptions.BulletTrails.off)
        {
            if (Game.options.graphics.bulletTrails == GameOptions.BulletTrails.fancy)
                bulletTrails.setText(trailsText, fancyText);
            else
                bulletTrails.setText(trailsText, fastText);
        }
        else
            bulletTrails.setText(trailsText, ScreenOptions.offText);

        if (Game.options.graphics.glowEnabled)
            glow.setText(glowText, ScreenOptions.onText);
        else
            glow.setText(glowText, ScreenOptions.offText);

        if (Game.options.graphics.enable3d)
            graphics3d.setText(graphics3dText, ScreenOptions.onText);
        else
            graphics3d.setText(graphics3dText, ScreenOptions.offText);

        update3dGroundButton();

        switch (viewNo)
        {
            case 0:
                altPerspective.setText(perspectiveText, birdsEyeText);

                Game.options.graphics.angledView = false;
                Game.options.debug.followingCam = false;
                Game.options.debug.firstPerson = false;
                break;
            case 1:
                altPerspective.setText(perspectiveText, angledText);

                Game.options.graphics.angledView = true;
                Game.options.debug.followingCam = false;
                Game.options.debug.firstPerson = false;
                break;
            case 2:
                altPerspective.setText(perspectiveText, "\u00a7200000000255third person");

                Game.options.graphics.angledView = false;
                Game.options.debug.followingCam = true;
                Game.options.debug.firstPerson = false;
                break;
            case 3:
                altPerspective.setText(perspectiveText, "\u00a7255000000255first person");

                Game.options.graphics.angledView = false;
                Game.options.debug.followingCam = true;
                Game.options.debug.firstPerson = true;
                break;
        }

        if (!Game.options.graphics.antialiasing)
            antialiasing.setText(antialiasingText, ScreenOptions.offText);
        else
            antialiasing.setText(antialiasingText, ScreenOptions.onText);

//        if (Game.framework == Game.Framework.libgdx)
//        {
//            altPerspective.enabled = false;
//            shadows.enabled = false;
//            maxFPS.enabled = false;
//        }

        if (!Game.game.window.antialiasingSupported)
        {
            antialiasing.setText(antialiasingText, ScreenOptions.offText);
            antialiasing.enabled = false;
        }

//        if (Game.framework == Game.Framework.libgdx)
//            Game.options.graphics.shadows.shadowsEnabled = false;

        if (!Game.options.graphics.shadow.shadowsEnabled)
            shadows.setText("Shadows: ", ScreenOptions.offText);
        else
            shadows.setText("Shadow quality: %s", (Object)("\u00A7000200000255" + Game.options.graphics.shadow.shadowQuality));

        if (!Game.options.graphics.effect.particleEffects)
            effects.setText("Particle effects: ", ScreenOptions.offText);
        else if (Game.options.graphics.effect.particlePercentage < 1)
            effects.setText("Particle effects: %s", (Object)("\u00A7200100000255" + (int) Math.round(Game.options.graphics.effect.particlePercentage * 100) + "%"));
        else
            effects.setText("Particle effects: ", ScreenOptions.onText);

        if (Game.options.graphics.tankTextures)
            tankTextures.setText(tankTexturesText, ScreenOptions.onText);
        else
            tankTextures.setText(tankTexturesText, ScreenOptions.offText);

        if (Game.options.graphics.vsync)
            maxFPS.setText("Max FPS: \u00A7200100000255V-Sync");
        else if (Game.options.graphics.maxFps > 0)
            maxFPS.setText("Max FPS: %s", (Object)("\u00A7000200000255" + Game.options.graphics.maxFps));
        else
            maxFPS.setText("Max FPS: \u00A7000100200255unlimited");

        if (Game.options.speedrun.deterministicMode != GameOptions.Deterministic.off)
        {
            maxFPS.setText("Max FPS: %s", (Object) ("\u00A7000200000255" + 60));
            maxFPS.enabled = false;
            maxFPS.setHoverText("Maximum framerate is locked to 60---because of deterministic mode");
        }
    }

    protected void update3dGroundButton()
    {
        if (Game.options.graphics.fancyTerrain && Game.options.graphics.enable3d)
        {
            ground3d.enabled = true;

            if (Game.options.graphics.enable3dBg)
                ground3d.setText(ground3dText, ScreenOptions.onText);
            else
                ground3d.setText(ground3dText, ScreenOptions.offText);
        }
        else
        {
            ground3d.enabled = false;
            ground3d.setText(ground3dText, ScreenOptions.offText);
        }

        if (Game.options.graphics.enable3d)
        {
            if (Game.options.graphics.xrayBullets)
                xrayBullets.setText(xrayBulletsText, ScreenOptions.onText);
            else
                xrayBullets.setText(xrayBulletsText, ScreenOptions.offText);

            xrayBullets.enabled = true;
        }
        else
        {
            xrayBullets.setText(xrayBulletsText, ScreenOptions.offText);
            xrayBullets.enabled = false;
        }
    }

    Button terrain = new Button(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace * 2.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.options.graphics.fancyTerrain = !Game.options.graphics.fancyTerrain;

            if (Game.options.graphics.fancyTerrain)
                terrain.setText(terrainText, fancyText);
            else
                terrain.setText(terrainText, fastText);

            update3dGroundButton();

            Drawing.drawing.terrainRenderer.reset();
            Game.resetTiles();
        }
    },
            "Fancy terrain enables varied block---and ground colors------May impact performance on larger levels");

    Button bulletTrails = new Button(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            if (Game.options.graphics.bulletTrails != GameOptions.BulletTrails.off)
            {
                if (Game.options.graphics.bulletTrails == GameOptions.BulletTrails.fancy)
                    bulletTrails.setText(trailsText, fancyText);
                else
                    bulletTrails.setText(trailsText, fastText);
            }
            else
                bulletTrails.setText(trailsText, ScreenOptions.offText);
        }
    }, "Bullet trails show the paths of bullets------Fancy bullet trails enable some extra particle---effects for certain bullet types");

    Button glow = new Button(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace * 0.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.options.graphics.glowEnabled = !Game.options.graphics.glowEnabled;

            if (Game.options.graphics.glowEnabled)
                glow.setText(glowText, ScreenOptions.onText);
            else
                glow.setText(glowText, ScreenOptions.offText);
        }
    },
            "Glow effects may significantly---impact performance");

    Button graphics3d = new Button(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace * 2.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.options.graphics.enable3d = !Game.options.graphics.enable3d;

            if (Game.options.graphics.enable3d)
                graphics3d.setText(graphics3dText, ScreenOptions.onText);
            else
                graphics3d.setText(graphics3dText, ScreenOptions.offText);

            update3dGroundButton();

            Drawing.drawing.terrainRenderer.reset();
            Game.resetTiles();
        }
    },
            "3D graphics may impact performance");

    Button ground3d = new Button(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.options.graphics.enable3dBg = !Game.options.graphics.enable3dBg;

            if (Game.options.graphics.enable3dBg)
                ground3d.setText(ground3dText, ScreenOptions.onText);
            else
                ground3d.setText(ground3dText, ScreenOptions.offText);

            Drawing.drawing.terrainRenderer.reset();
            Game.resetTiles();
        }
    },
            "Enabling 3D ground may impact---performance in large levels");


    Button altPerspective = new Button(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace * 0.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            viewNo = (viewNo + 1);
            if (!Game.debug)
                viewNo = viewNo % 2;
            else
                viewNo = viewNo % 4;

            switch (viewNo)
            {
                case 0:
                    altPerspective.setText(perspectiveText, birdsEyeText);

                    Game.options.graphics.angledView = false;
                    Game.options.debug.followingCam = false;
                    Game.options.debug.firstPerson = false;
                    break;
                case 1:
                    altPerspective.setText(perspectiveText, angledText);

                    Game.options.graphics.angledView = true;
                    Game.options.debug.followingCam = false;
                    Game.options.debug.firstPerson = false;
                    break;
                case 2:
                    altPerspective.setText(perspectiveText, "\u00a7200000000255third person");

                    Game.options.graphics.angledView = false;
                    Game.options.debug.followingCam = true;
                    Game.options.debug.firstPerson = false;
                    break;
                case 3:
                    altPerspective.setText(perspectiveText, "\u00a7255000000255first person");

                    Game.options.graphics.angledView = false;
                    Game.options.debug.followingCam = true;
                    Game.options.debug.firstPerson = true;
                    break;
            }
        }
    },
            "Changes the angle at which---you view the game field");

    Button antialiasing = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.options.graphics.antialiasing = !Game.options.graphics.antialiasing;

            if (!Game.options.graphics.antialiasing)
                antialiasing.setText(antialiasingText, ScreenOptions.offText);
            else
                antialiasing.setText(antialiasingText, ScreenOptions.onText);

            if (Game.options.graphics.antialiasing != Game.game.window.antialiasingEnabled)
                Game.screen = new ScreenAntialiasingWarning();

            ScreenOptions.saveOptions(Game.homedir);
        }
    },
            "May fix flickering in thin edges---at the cost of performance------Requires restarting the game---to take effect");

    Button tankTextures = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.options.graphics.tankTextures = !Game.options.graphics.tankTextures;

            if (Game.options.graphics.tankTextures)
                tankTextures.setText(tankTexturesText, ScreenOptions.onText);
            else
                tankTextures.setText(tankTexturesText, ScreenOptions.offText);
        }
    },
            "Adds designs to the built-in tanks---which can help differentiate them");

    Button xrayBullets = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 2.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.options.graphics.xrayBullets = !Game.options.graphics.xrayBullets;

            if (Game.options.graphics.xrayBullets)
                xrayBullets.setText(xrayBulletsText, ScreenOptions.onText);
            else
                xrayBullets.setText(xrayBulletsText, ScreenOptions.offText);
        }
    },
            "Shows indicators for bullets---hidden behind terrain");

    //Button window = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 2.5, this.objWidth, this.objHeight, "Window options", () -> Game.screen = new ScreenOptionsWindow());

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenOptions());

    Button shadows = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 0.5, this.objWidth, this.objHeight, "", () -> Game.screen = new ScreenOptionsShadows(), "Shadows are quite graphically intense---and may significantly reduce framerate");

    Button effects = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 0.5, this.objWidth, this.objHeight, "", () -> Game.screen = new ScreenOptionsEffects(), "Particle effects may significantly---impact performance");

    Button maxFPS = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 2.5, this.objWidth, this.objHeight, "", () -> Game.screen = new ScreenOptionsFramerate(), "Limiting your framerate may---decrease battery consumption");

    @Override
    public void update()
    {
        terrain.update();
        bulletTrails.update();
        glow.update();
        effects.update();
        tankTextures.update();
        xrayBullets.update();

        graphics3d.update();
        ground3d.update();
        altPerspective.update();
        shadows.update();
        antialiasing.update();
        maxFPS.update();

        back.update();

        if (Game.options.graphics.antialiasing != Game.game.window.antialiasingEnabled)
        {
            antialiasing.bgColG = 238;
            antialiasing.bgColB = 220;
        }
        else
        {
            antialiasing.bgColG = 255;
            antialiasing.bgColB = 255;
        }
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        back.draw();

        maxFPS.draw();
        antialiasing.draw();
        shadows.draw();
        altPerspective.draw();
        ground3d.draw();
        graphics3d.draw();

        xrayBullets.draw();
        tankTextures.draw();
        effects.draw();
        glow.draw();
        bulletTrails.draw();
        terrain.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Graphics options");
    }
}
