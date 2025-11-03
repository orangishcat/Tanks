package tanks.handle;

import tanks.Player;

/**
 * Handles valid chat messages from clients.
 * If any handle returns false, the message will not be broadcasted to the global chat.
 */
@FunctionalInterface
public interface IChatHandle extends IHandle
{
    boolean handleMessage(Player player, String message);
}
