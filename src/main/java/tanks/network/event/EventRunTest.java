package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.replay.testing.Test;

public class EventRunTest extends PersonalEvent
{
    public int testID;

    public EventRunTest() {}

    public EventRunTest(int testID)
    {
        this.testID = testID;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(testID);
    }

    @Override
    public void read(ByteBuf b)
    {
        testID = b.readInt();
    }

    @Override
    public void execute()
    {
        Test.runner.loadTest(testID);
    }
}
