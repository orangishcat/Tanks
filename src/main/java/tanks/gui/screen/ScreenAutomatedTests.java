package tanks.gui.screen;

import basewindow.InputCodes;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;
import tanks.replay.Replay;
import tanks.replay.testing.Test;
import tanks.replay.testing.TestRunner;

import java.util.ArrayList;

public class ScreenAutomatedTests extends Screen implements IGameOverlayScreen
{
    private static final String[] messages = new String[]{
            "Ya come back here often?", "Releasing another update?", "Testing the tests?",
            "How's your day?", "[insert inspirational quote here]", "Automated tests go brr",
            "Place your bets!", "Janks: the Crusades", "up up down down left right left right b a enter"
    };
    private static int messagePos = (int) (Math.random() * messages.length);
    private static final String testMusic = "battle_editor.ogg", testMusicPaused = "battle_timed_paused.ogg", testMusicID = "tests";

    public static boolean autoContinue = true;
    public boolean testOngoing = false;

    public Screen previous = Game.screen;
    public ScreenGame game;

    public Button prevTest = new Button(isx() * 0.85 - 50, this.objYSpace * 1.75, 45, 45, "",
            () -> adjacentTest(-1), "Previous test").fullImage("icons/back.png", 25, 27).imageOffset(-2, 0);
    public Button autoContButton = new Button(isx() * 0.85, this.objYSpace * 1.75, 45, 45, "",
            () -> setAutoCont(!autoContinue), "");
    public Button nextTest = new Button(isx() * 0.85 + 50, this.objYSpace * 1.75, 45, 45, "",
            () -> adjacentTest(1), "Next test").fullImage("icons/forward.png", 25, 27).imageOffset(2, 0);

    public Button runTests = new Button(isx() * 0.85, Drawing.drawing.interfaceSizeY - this.objYSpace * 2.5, this.objWidth, this.objHeight, "Run tests", this::startTests);
    public Button retry = new Button(isx() * 0.85 - this.objXSpace / 4, Drawing.drawing.interfaceSizeY - this.objYSpace * 2.5, this.objWidth / 2, this.objHeight, "Rerun", this::retryTest);
    public Button next = new Button(isx() * 0.85 + this.objXSpace / 4, Drawing.drawing.interfaceSizeY - this.objYSpace * 2.5, this.objWidth / 2, this.objHeight, "Next", this::runNextTest);

    public Button back = new Button(isx() * 0.85, Drawing.drawing.interfaceSizeY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Back", () ->
    {
        Game.screen = previous;
        Game.cleanUp();
    });

    public String currentName;
    public ArrayList<String> currentMessage;
    public boolean centered = true;
    public boolean paused;

    public boolean popup = true, prevPopup = true;
    private double fadeAge = -9999, posChangeAge = -9999;
    private int hoveredInd;

    public ScreenAutomatedTests()
    {
        this.music = testMusic;
        this.musicID = testMusicID;
        Test.reset();

        setAutoCont(autoContinue);
        this.currentName = "Automated tests";
        this.currentMessage = wrapMessage(Test.registry.size() + " test" + (Test.registry.size() != 1 ? "s" : "") + " loaded \n " +
                messages[messagePos = (messagePos + 1) % messages.length]);
    }

    public void setAutoCont(boolean b)
    {
        autoContinue = b;
        autoContButton.fullImage(autoContinue ? "icons/pause.png" : "icons/play.png", autoContinue ? 27 : 18, autoContinue ? 27 : 20)
                .imageOffset(autoContinue ? 0 : 2, 0)
                .setHoverText("Auto-continue: %s", autoContinue ? ScreenOptions.onText : ScreenOptions.offText);
    }

    public void adjacentTest(int i)
    {
        runTest(Test.runner.pos + i);
    }

    public void runTest(int i)
    {
        if (!Game.isOrdered(true, 0, i, Test.registry.size() + 1))
            return;

        currentName = null;
        Drawing.drawing.trackRenderer.reset();
        Replay.currentPlaying = null;
        testOngoing = true;
        setAutoCont(false);
        Test.runner.loadTest(i);
    }

    @Override
    public void update()
    {
        if (testOngoing && !paused)
        {
            if (!game.paused && Test.runner.updateTest())
            {
                if (Test.runner.current != null && (!Test.runner.currentTestPassed() || !autoContinue))
                {
                    setCurrentMessage();
                    pauseTests();
                }
                else
                    runNextTest();
            }
        }
        else if (paused)
        {
            retry.update();
            next.update();
        }
        else
        {
            runTests.update();
        }

        prevTest.update();
        nextTest.update();
        autoContButton.update();

        if (paused && game != null)
        {
            if (game.finishQuickTimer >= 220)
                game = null;
        }

        double isx = isx();
        hoveredInd = -1;
        for (int i = Math.max(0, Test.runner.pos - 8); i <= Math.min(Test.registry.size() - 1, Test.runner.pos + 6); i++)
        {
            if (i + 1 == Test.runner.pos && testOngoing) continue;
            double lineY = getTestDrawY(i);
            if (Game.isOrdered(isx * 0.7 + 10, Drawing.drawing.getInterfaceMouseX(), isx - 10) &&
                    Game.isOrdered(lineY - 20, Drawing.drawing.getInterfaceMouseY(), lineY + 20))
            {
                hoveredInd = i;
                break;
            }
        }

        if (hoveredInd >= 0 && Game.game.window.pressedButtons.contains(InputCodes.MOUSE_BUTTON_1))
        {
            runTest(hoveredInd);
            Game.game.window.pressedButtons.remove((Integer) InputCodes.MOUSE_BUTTON_1);
        }

        popup = currentName != null;
        if (popup != prevPopup)
        {
            prevPopup = popup;
            fadeAge = screenAge;
        }

        if (game != null)
        {
            Game.screen = game;
            game.playSounds = false;
            music = game.paused ? testMusicPaused : testMusic;
            musicID = testMusicID;
            game.update();
            if (shouldResetScreen())
                Game.screen = this;
        }

        back.update();
    }

    private void retryTest()
    {
        currentName = null;
        resumeTests();
        Test.runner.retryTest();
    }

    private void runNextTest()
    {
        if (Test.runner.pos > Test.registry.size())
        {
            testOngoing = false;
            Panel.setTickSprint(false);
            currentName = "All tests finished";
            currentMessage = new ArrayList<>();
            currentMessage.add("Run again?");
            centered = true;
            return;
        }

        resumeTests();
        Test.runner.runNextTest();
        posChangeAge = screenAge;
        currentName = null;
    }

    private void setCurrentMessage()
    {
        Test t = Test.runner.current;
        if (t == null) return;
        StringBuilder s = new StringBuilder();
        int i = 0;
        for (Test.TestFunction func : t.expectOnce)
            addTestDetails(i++, "Once", func, s);
        i = 0;
        for (Test.TestFunction func : t.expectAtEnd)
            addTestDetails(i++, "End", func, s);

        currentName = t.name;
        currentMessage = wrapMessage(s.toString());
        centered = false;
    }

    private static ArrayList<String> wrapMessage(String s)
    {
        return Drawing.drawing.wrapText(s, isx() * 0.55, 20);
    }

    private void pauseTests()
    {
        paused = true;
        Panel.setTickSprint(false);
    }

    private void resumeTests()
    {
        Drawing.drawing.trackRenderer.reset();
        paused = false;
    }

    @Override
    public void draw()
    {
        if (game != null)
        {
            Game.screen = game;
            game.draw();
            if (shouldResetScreen())
                Game.screen = this;
        }
        else
            drawDefaultBackground();

        double isx = isx(), isy = Drawing.drawing.interfaceSizeY;
        double cy = this.centerY;
        double a = Math.min(50, screenAge - fadeAge) / 50;
        Drawing.drawing.setColor(0, 0, 0, 128);
        Drawing.drawing.fillInterfaceRect(isx * 0.85, cy, isx * 0.3, isy);

        if (testOngoing)
        {
            Drawing.drawing.setColor(0, 0, 0, 128);
            Drawing.drawing.fillInterfaceRect(isx * 0.85, cy - 40, isx * 0.3 - 20, 40);
        }

        Drawing.drawing.setInterfaceFontSize(32);
        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.drawInterfaceText(isx() * 0.85, 40, testOngoing ?
                        Test.runner.passedCases + "/" + Test.registry.size() + " tests passed" : "Controls");

        Drawing.drawing.setInterfaceFontSize(24);
        for (int i = Math.max(0, Test.runner.pos - 8); i <= Math.min(Test.registry.size() - 1, Test.runner.pos + 6); i++)
        {
            if (i == hoveredInd)
            {
                Drawing.drawing.setColor(50, 50, 50, 128);
                Drawing.drawing.fillInterfaceRect(isx * 0.85, getTestDrawY(i), isx * 0.3 - 20, 40);
            }

            double a1 = 255 - Math.abs(i - Test.runner.pos + 1) * 32;
            switch (Test.runner.getState(i))
            {
                case notRun:
                    Drawing.drawing.setColor(255, 255, 255, a1);
                    break;
                case passed:
                    Drawing.drawing.setColor(0, 250, 0, a1);
                    break;
                case running:
                    Drawing.drawing.setColor(255, 128, 0, a1);
                    break;
                case failed:
                    Drawing.drawing.setColor(250, 50, 0, a1);
                    break;
            }

            Test t = Test.registry.getTest(i);
            Drawing.drawing.drawInterfaceText(isx * 0.85, getTestDrawY(i), i + 1 + ": " + t.name + " " + "(" + t.passedCases() + "/" + t.totalCases() + ")");
        }

        if (popup)
        {
            double sY = Math.max(300, 150 + 40 * currentMessage.size());

            Drawing.drawing.setColor(0, 0, 0, 128 * a);
            Drawing.drawing.fillInterfaceRect(isx * 0.35, cy, isx * 0.6, sY);

            Drawing.drawing.setColor(255, 255, 255, 255 * a);
            Drawing.drawing.setInterfaceFontSize(32);
            Drawing.drawing.drawInterfaceText(isx * 0.35, cy - sY / 2 + 50, currentName);
            Drawing.drawing.setInterfaceFontSize(20);

            if (currentMessage != null)
            {
                int ind = 0;
                for (String s : currentMessage)
                {
                    double y = cy + 30 + 40 * (-currentMessage.size() / 2. + ind++);
                    if (centered)
                        Drawing.drawing.drawInterfaceText(isx * 0.35, y, s);
                    else
                        Drawing.drawing.drawUncenteredInterfaceText(isx * 0.075, y, s);
                }
            }
        }

        if (testOngoing)
        {
            if (paused)
            {
                retry.draw();
                next.draw();
            }
        }
        else
        {
            runTests.draw();
        }

        nextTest.draw();
        prevTest.draw();
        autoContButton.draw();
        back.draw();
    }

    private boolean shouldResetScreen()
    {
        return true;
    }

    private double getTestDrawY(int i)
    {
        return centerY + 40 * (i - Test.runner.pos + 1 - Math.min(50, screenAge - posChangeAge) / 50);
    }

    public static void addTestDetails(int i, String testType, Test.TestFunction t, StringBuilder s)
    {
        s.append(testType).append(" ").append(i + 1).append(": ")
                .append(t.passed ? "§000220000255passed" : "§220000000255failed").append("§255255255255");
        if (t.passed && t.passMessage != null)
            s.append(" - ").append(t.passMessage);
        if (!t.passed && t.failMessage != null)
            s.append(" - ").append(t.failMessage);
        s.append(" \n ");
    }

    public void startTests()
    {
        testOngoing = true;
        TestRunner.startTests();
    }

    @Override
    public ScreenGame getGameScreen()
    {
        return game;
    }

    private static double isx()
    {
        return Drawing.drawing.getInterfaceEdgeX(true);
    }
}
