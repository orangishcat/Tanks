package tanks;

import tanks.gui.Button;
import tanks.gui.screen.*;
import tanks.network.SynchronizedList;
import tanks.tank.TankPlayer;

import java.util.*;
import java.util.stream.*;

public class PartyServer
{
    public static final String highlight_color = "\u00a7255127000255", error_color = "\u00a7255000000255";

    public static boolean isPartyServer = false;
    public static UUID host;

    public static String serverName = "\u00a7255127000255[SERVER]", commandPrefix = "/";

    public static HashMap<String, Command> commands = new LinkedHashMap<>();
    public static Command playRandom, playVersus, restart, startNow, exitLevel, nextLevel;

    public static void onGameInit()
    {
        Game.player.username = serverName;
        registerCommands();
        ScreenParty.createParty();
    }

    public static void registerCommands()
    {
        commands.clear();
        playRandom = Command.register("play_random", "Plays a random level", (s, uuid) -> ScreenPartyHost.playRandomLevel());
        playVersus = Command.register("play_versus", "Plays a versus level", (s, u) -> ScreenPartyHost.playVersus());
        Command.register("list_levels", "Lists all levels",
            (parts, clientID) ->
            {
                SynchronizedList<ScreenPartyHost.SharedLevel> levels = ScreenPartyHost.activeScreen.sharedLevels;
                if (levels.isEmpty())
                {
                    ScreenPartyHost.privateChat("No levels available!", clientID);
                    return;
                }
                ScreenPartyHost.privateChat(
                    "Available levels:\n" +
                        IntStream.range(0, levels.size())
                            .mapToObj(i -> String.format("  %s%d\u00a7r: %s", highlight_color, i+1, levels.get(i).name))
                            .collect(Collectors.joining("\n")), clientID
                );
            }
        );
        Command.register("play_level", "Plays a shared level",
            (parts, clientID) ->
            {
                int id;
                try
                {
                    id = Integer.parseInt(parts[0]) - 1;
                }
                catch (NumberFormatException e)
                {
                    ScreenPartyHost.privateChat("Invalid level id!", clientID);
                    return;
                }

                try
                {
                    Game.cleanUp();
                    new Level(ScreenPartyHost.activeScreen.sharedLevels.get(id).level).loadLevel();
                    Game.screen = new ScreenGame();
                }
                catch (Exception e)
                {
                    ScreenPartyHost.privateChat("Level load failed!", clientID);
                }
            }, "id"
        );
        restart = Command.register("restart", "Restarts the current level");
        nextLevel = Command.register("next", "Goes to the next level in a crusade");
        startNow = Command.register("start_now", "Starts the game immediately");
        exitLevel = Command.register("exit", "Exits the current level");
        Command.register("transfer_host", "Transfers host to another player",
            (parts, clientID) ->
            {
                List<UUID> newHost = ScreenPartyHost.server.connections.stream()
                    .filter(c -> c.username.equals(parts[0]) && !c.clientID.equals(host))
                    .map(c -> c.clientID).collect(Collectors.toList());
                if (newHost.isEmpty())
                {
                    ScreenPartyHost.privateChat("Player not found! (or already host)", clientID);
                    return;
                }
                if (newHost.size() > 1)
                {
                    ScreenPartyHost.privateChat("Multiple players found!", clientID);
                    return;
                }
                host = newHost.get(0);
                ScreenPartyHost.sendChatMessage("Host was transferred to " + parts[0]);
                ScreenPartyHost.privateChat("You are the party host!", host);
            }, "player_username"
        );
        Command.register("reload", "Reload commands",
            (parts, clientID) ->
            {
                registerCommands();
                ScreenPartyHost.privateChat("Commands reloaded!", clientID);
            }
        );
        Command.register("help", "Displays this help message",
            (parts, clientID) ->
            {
                if (commands.isEmpty())
                {
                    ScreenPartyHost.privateChat("No commands available!", clientID);
                    return;
                }

                ScreenPartyHost.privateChat(
                    "Available commands:\n" +
                        commands.values().stream()
                            .map(c -> String.format("  %s%s§r: %s", highlight_color, c.name, c.description))
                            .collect(Collectors.joining("\n")), clientID
                );
            }
        );
    }

    public static void onLevelLoad(Level l)
    {
        Game.players.remove(Game.player);
        Game.player.username = serverName;
        Game.playerTank = new TankPlayer(0, 0, 0);
        Game.playerTank.team = Game.playerTeam;
        Game.player.tank = Game.playerTank;
    }

    public static void onClientConnect(UUID clientID)
    {
        if (ScreenPartyHost.server.connections.size() == 1)
            setHost(clientID);
        else
            addClient(clientID);
    }

    public static void setHost(UUID clientID)
    {
        ScreenPartyHost.privateChat("You are the party host!", clientID);
        host = clientID;
    }

    public static void addClient(UUID clientID)
    {
        ScreenPartyHost.privateChat("Welcome to the party!", clientID);
    }

    public static void onClientDisconnect(UUID clientID)
    {
        if (clientID.equals(host))
        {
            host = ScreenPartyHost.server.connections.get(0).clientID;
            ScreenPartyHost.privateChat("You are the party host!", host);
        }
    }

    public static boolean onChatMessage(Player player, String message)
    {
        if (!message.startsWith(commandPrefix))
            return true;

        if (!player.clientID.equals(host))
        {
            ScreenPartyHost.privateChat(error_color + "You don't have permission to use commands!", player.clientID);
            return false;
        }

        String[] commandParts = message.substring(commandPrefix.length()).split(" ");
        if (commands.containsKey(commandParts[0]))
            commands.get(commandParts[0]).run(Arrays.copyOfRange(commandParts, 1, commandParts.length), player.clientID);
        else
            ScreenPartyHost.privateChat(error_color + "Unknown command '" + commandParts[0] + "' !", player.clientID);

        return false;
    }

    public static class Command
    {
        private static final BiConsumer<String[], UUID> reject_func = (p, u) -> ScreenPartyHost.privateChat(error_color + "Can't do this right now!", u);

        public String name;
        public String description;
        public String[] partNames;
        public Button button;
        public BiConsumer<String[], UUID> function;

        public static Command register(String name, String description, String... partNames)
        {
            return register(name, description, reject_func, partNames);
        }

        public static Command register(String name, String description, BiConsumer<String[], UUID> function, String... partNames)
        {
            Command c = new Command();
            c.name = name;
            c.description = description;
            c.function = function;
            c.partNames = partNames;
            commands.put(name, c);
            return c;
        }

        /**
         * Ties this command to a button, so that the button's function is run when the command is executed
         */
        public void tieToButton(Screen s, Button b)
        {
            if (button == b)
                return;

            button = b;
            function = (parts, clientID) ->
            {
                if (Game.screen != s)
                {
                    ScreenPartyHost.privateChat(error_color + "Can't do this right now!", clientID);
                    return;
                }
                b.function.run();
            };
        }

        public void run(String[] parts, UUID clientID)
        {
            try
            {
                if (parts.length != partNames.length)
                {
                    ScreenPartyHost.privateChat("\u00a7255000000255Invalid number of arguments! Expected " + partNames.length + " but got " + parts.length +
                        "\nUsage: " + commandPrefix + name + " " +
                        Arrays.stream(partNames).map(p -> "<" + p + ">").collect(Collectors.joining(" ")), clientID);
                    return;
                }
                function.accept(parts, clientID);
            }
            catch (Exception e)
            {
                ScreenPartyHost.privateChat(error_color + "An error occurred while running this command!", clientID);
            }
        }
    }
}
