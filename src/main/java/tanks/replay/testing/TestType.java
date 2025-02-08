package tanks.replay.testing;

import tanks.BiConsumer;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenAutomatedTests;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenInterlevel;
import tanks.minigames.Minigame;
import tanks.replay.Replay;
import tanks.replay.ReplayEvents;

public enum TestType
{
    REPLAY((test, name) -> Replay.read(name).allowControls(false).loadAndPlay()),
    LEVEL((test, levelName) ->
    {
        Game.cleanUp();
        new Level(Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + levelName + ".tanks").read()).loadLevel();
        Drawing.drawing.terrainRenderer.reset();
        Drawing.drawing.trackRenderer.reset();
        ReplayEvents.LevelChange.enterGame();
    }),
    MINIGAME((test, minigameName) ->
    {
        try
        {
            ScreenInterlevel.fromMinigames = true;
            Minigame m = Game.registryMinigame.getEntry(minigameName).getConstructor().newInstance();
            Screen prevScreen = Game.screen;
            m.loadLevel();
            if (prevScreen instanceof ScreenAutomatedTests)
                ((ScreenAutomatedTests) prevScreen).game = (ScreenGame) Game.screen;
            Game.screen = prevScreen;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    });

    private final BiConsumer<Test, String> onLoad;

    TestType(BiConsumer<Test, String> onLoad)
    {
        this.onLoad = onLoad;
    }

    public void load(Test test, String name)
    {
        onLoad.accept(test, name);
    }
}
