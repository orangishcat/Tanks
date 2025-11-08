package tanks.handle;

import tanks.network.event.INetworkEvent;

@FunctionalInterface
public interface INetworkEventHandle extends IHandle
{
    /**
     * @param e The event to handle
     * @param incoming True if the event is incoming from <code>Game.eventsIn</code>,
     *                false if it is outgoing into <code>Game.eventsOut</code>
     */
    void handle(INetworkEvent e, boolean incoming);
}
