package tanks.replay;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import tanks.Game;
import tanks.network.NetworkUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class ReplayIO
{
    public static Replay read(String replayPath)
    {
        ReplayEvents.registerEvents();

        Replay r = new Replay();
        File f = new File(replayPath);

        int size, lastEvent = -1;
        try (FileInputStream in = new FileInputStream(f))
        {
            FileChannel channel = in.getChannel();
            size = (int) channel.size();
            ByteBuf buf = Unpooled.buffer().alloc().directBuffer(size, size);
            buf.writeBytes(channel, 0, size);
            r.name = NetworkUtils.readString(buf);
            r.forTests = buf.readBoolean();
            int eventCnt = buf.readInt();

            for (int i = 0; i < eventCnt; i++)
            {
                int eventID = buf.readInt();
                ReplayEvents.IReplayEvent event = ReplayEventMap.get(eventID).getConstructor().newInstance();
                event.read(buf);
                r.events.add(event);
                lastEvent = eventID;
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error reading replay file (last event: " + (lastEvent >= 0 ? ReplayEventMap.get(lastEvent).getSimpleName() : null) + ")\n", e);
        }

        System.out.println("Replay read from " + f.getPath());
        System.out.println("Size: " + size / 1024 + " KB");

        return r;
    }

    public static void save(Replay r, String path)
    {
        try
        {
            File f = new File(path);
            assert f.exists() || f.createNewFile();

            int bytesWritten = path.length();
            try (FileOutputStream out = new FileOutputStream(f))
            {
                ByteBuf buf = Unpooled.buffer();
                NetworkUtils.writeString(buf, path);
                buf.writeBoolean(r.forTests);
                buf.writeInt(r.events.size());
                for (ReplayEvents.IReplayEvent event : r.events)
                {
                    buf.writeInt(ReplayEventMap.get(event.getClass()));
                    event.write(buf);
                }

                bytesWritten += out.getChannel().write(buf.nioBuffer());
            }
            catch (Exception e)
            {
                Game.exitToCrash(e);
            }

            System.out.println("Replay saved at " + f.getPath());
            System.out.println("Size: " + bytesWritten / 1024 + " KB");
        }
        catch (IOException e)
        {
            Game.exitToCrash(e);
        }
    }
}
