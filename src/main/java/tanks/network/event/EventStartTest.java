package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.screen.ScreenAutomatedTests;
import tanks.replay.testing.Test;
import tanks.replay.testing.TestRunner;

public class EventStartTest extends PersonalEvent
{
    @Override
    public void execute()
    {
        Game.screen = new ScreenAutomatedTests();
        Test.reset();
        TestRunner.startTests();
    }

    public EventStartTest()
    {
    }

    @Override
    public void write(ByteBuf b)
    {

    }

    @Override
    public void read(ByteBuf b)
    {

    }
}
