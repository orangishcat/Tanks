package tanks.handle;

import tanks.network.event.INetworkEvent;

@FunctionalInterface
public interface INetworkEventHandle extends IHandle
{
    void handle(INetworkEvent e, boolean incoming);
}
