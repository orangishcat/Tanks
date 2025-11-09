package tanks;

import tanks.gui.*;
import tanks.gui.screen.*;
import tanks.handle.*;
import tanks.network.*;
import tanks.network.event.EventChat;
import tanks.tank.TankPlayer;

import java.util.*;
import java.util.stream.*;

public class PartyServer
{
    public static final String highlight_color = "\u00a7255127000255", error_color = "\u00a7255000000255";

    public static boolean isPartyServer = false;
    public static UUID host;

    public static String serverName = "\u00a7255127000255[Server]", commandPrefix = "/";
    public static List<String> defaultCrusades = Arrays.asList("adventure_crusade", "classic_crusade", "castle_crusade", "beginner_crusade");

    public static HashMap<String, Command> commands = new LinkedHashMap<>();
    public static Command playRandom, playVersus, restart, startNow, exitLevel, nextLevel;

    public static void onGameInit()
    {
        Game.player.username = serverName;
        registerCommands();
        ScreenParty.createParty();
        HandleRegistry.registry.register(IChatHandle.class, PartyServer::onChatMessage);
        HandleRegistry.registry.register(ILevelLoadHandler.class, PartyServer::onLevelLoad);
        HandleRegistry.registry.register(ICrashHandler.class, PartyServer::onCrash);
        System.out.println("Party server started!");
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
                    privateChat("No levels available!", clientID);
                    return;
                }
                privateChat(
                    "Available levels:\n" +
                        IntStream.range(0, levels.size())
                            .mapToObj(i -> String.format("  %s%d\u00a7r: %s", highlight_color, i+1, levels.get(i).name))
                            .collect(Collectors.joining("\n")), clientID
                );
            }
        ).setPublic(true);
        Command.register("pm", "Sends a private message", (parts, clientID) ->
            {
                List<UUID> targets = ScreenPartyHost.server.connections.stream()
                    .filter(c -> (c.username.equals(parts[0]) || c.clientID.toString().equals(parts[0])) && !c.clientID.equals(clientID))
                    .map(c -> c.clientID).collect(Collectors.toList());
                if (targets.isEmpty())
                {
                    privateChat("Player not found!", clientID);
                    return;
                }
                if (targets.size() > 1)
                {
                    privateChat("Multiple players found!", clientID);
                    return;
                }
                String message = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
                privateChatRaw("\u00a7255127000255[PM to " + usernameFromId(targets.get(0)) + "]:\u00a7r " + message, clientID);
                privateChatRaw("\u00a7255127000255[PM from " + usernameFromId(clientID) + "]:\u00a7r " + message, targets.get(0));
            }, "player_username", "message..."
        ).setPublic(true);
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
                    privateChat("Invalid level id!", clientID);
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
                    privateChat("Level load failed!", clientID);
                    e.printStackTrace();
                }
            }, "id"
        );
        Command.register("list_crusades", "Lists all crusades",
            (parts, clientID) ->
            {
                SynchronizedList<ScreenPartyHost.SharedCrusade> crusades = ScreenPartyHost.activeScreen.sharedCrusades;
                privateChat(
                    "Available crusades:\n" +
                        IntStream.range(0, defaultCrusades.size())
                            .mapToObj(i -> String.format("  %s%d\u00a7r: %s", highlight_color, i + 1, Game.formatString(defaultCrusades.get(i))))
                            .collect(Collectors.joining("\n")) + "\n" +
                        IntStream.range(0, crusades.size())
                            .mapToObj(i -> String.format("  %s%d\u00a7r: %s", highlight_color, i + 1 + defaultCrusades.size(), crusades.get(i).name))
                            .collect(Collectors.joining("\n")), clientID
                );
            }
        ).setPublic(true);
        Command.register("play_crusade", "Plays a shared crusade",
            (parts, clientID) ->
            {
                int id;
                try
                {
                    id = Integer.parseInt(parts[0]) - 1;
                }
                catch (NumberFormatException e)
                {
                    privateChat("Invalid crusade id!", clientID);
                    return;
                }
                try
                {
                    if (id < defaultCrusades.size())
                    {
                        Crusade.currentCrusade = new Crusade(String.join("\n",
                            Game.game.fileManager.getInternalFileContents("/crusades/" + defaultCrusades.get(id) + ".tanks")),
                            defaultCrusades.get(id));
                        Crusade.crusadeMode = true;
                        Crusade.currentCrusade.begin();
                        Game.screen = new ScreenGame(Crusade.currentCrusade);
                        return;
                    }
                    ScreenPartyHost.SharedCrusade shared = ScreenPartyHost.activeScreen.sharedCrusades.get(id);
                    new Crusade(shared.crusade, shared.name).begin();
                }
                catch (Exception e)
                {
                    privateChat("Crusade load failed!", clientID);
                    Game.cleanUp();
                    e.printStackTrace();
                }
            }
            , "id"
        );

        nextLevel = Command.register("next", "Goes to the next level in a crusade");
        restart = Command.register("restart", "Restarts the current level");
        startNow = Command.register("start_now", "Starts the game immediately");
        exitLevel = Command.register("exit", "Exits the current level");
        Command.register("option", "Changes an option", (parts, clientID) ->
            {
                try
                {
                    switch (parts[0])
                    {
                        case "bot_count":
                            Game.botPlayerCount = Integer.parseInt(parts[1]);
                            ScreenPartyHost.setBotCount(Game.botPlayerCount);
                            break;
                        case "countdown":
                            Game.partyStartTime = Double.parseDouble(parts[1]) * 100;
                            break;
                        case "friendly_fire":
                            Game.disablePartyFriendlyFire = !Boolean.parseBoolean(parts[1]);
                            break;
                        default:
                            privateChat("Unknown option! Available options: bot_count, countdown, friendly_fire", clientID);
                            return;
                    }
                    sendChatMessage("Option " + parts[0] + " set to " + parts[1]);
                }
                catch (Exception e)
                {
                    privateChat("Invalid value!", clientID);
                }
            }, "option", "value"
        );
        Command.register("transfer_host", "Transfers host to another player",
            (parts, clientID) ->
            {
                List<UUID> newHost = ScreenPartyHost.server.connections.stream()
                    .filter(c -> c.username.equals(parts[0]) && !c.clientID.equals(host))
                    .map(c -> c.clientID).collect(Collectors.toList());
                if (newHost.isEmpty())
                {
                    privateChat("Player not found! (or already host)", clientID);
                    return;
                }
                if (newHost.size() > 1)
                {
                    privateChat("Multiple players found!", clientID);
                    return;
                }
                host = newHost.get(0);
                sendChatMessage("Host was transferred to " + parts[0]);
                privateChat("You are the party host!", host);
            }, "player_username"
        );
        Command.register("reload", "Reload commands",
            (parts, clientID) ->
            {
                registerCommands();
                privateChat("Commands reloaded!", clientID);
            }
        );
        Command.register("help", "Displays this help message",
            (parts, clientID) ->
            {
                if (commands.isEmpty())
                {
                    privateChat("No commands available!", clientID);
                    return;
                }

                privateChat(
                    "Available commands:\n" +
                        commands.values().stream()
                            .map(c -> String.format("  %s%s§r: %s", highlight_color, c.name, c.description))
                            .collect(Collectors.joining("\n")), clientID
                );
            }
        );
    }

    private static String usernameFromId(UUID id)
    {
        for (ServerHandler s: ScreenPartyHost.server.connections)
        {
            if (s.clientID.equals(id))
                return s.username;
        }
        return "Unknown";
    }

    public static void onCrash(Throwable throwable)
    {
        throwable.printStackTrace();
        ScreenParty.createParty();
    }

    public static void onLevelLoad(Level l)
    {
        clearHostPlayer();
    }

    public static void onCrusadeStart(Crusade crusade)
    {
        clearHostPlayer();
    }

    private static void clearHostPlayer()
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

    public static void sendChatMessage(String s)
    {
        String message = "\u00a7255127000255[Server]\u00a7r " + s;
        Game.eventsOut.add(new EventChat(message));
        ScreenPartyHost.chat.add(0, new ChatMessage(message));
    }

    public static void privateChat(String s, UUID id)
    {
        privateChatRaw("\u00a7150150150255(PM) \u00a7255127000255[Server]\u00a7r " + s, id);
    }

    public static void privateChatRaw(String s, UUID id)
    {
        String message = s.replaceAll("\n", " \n ");
        ScreenPartyHost.sendEventTo(new EventChat(message), id);

        String logMessage = new Date() + " " + "\u00a7150150150255(To " + usernameFromId(id) + ") " + message;
        ScreenPartyHost.chat.add(0, new ChatMessage(logMessage));
        if (Game.headless)
            System.out.println(logMessage.replaceAll("§(\\d{12}|r)", ""));
    }

    public static void setHost(UUID clientID)
    {
        privateChat("You are the party host!", clientID);
        host = clientID;
    }

    public static void addClient(UUID clientID)
    {
        privateChat("Welcome to the party!", clientID);
    }

    public static void onClientDisconnect(UUID clientID)
    {
        if (clientID.equals(host) && !ScreenPartyHost.server.connections.isEmpty())
        {
            host = ScreenPartyHost.server.connections.get(0).clientID;
            privateChat("You are the party host!", host);
        }
    }

    public static boolean onChatMessage(Player player, String message)
    {
        if (!message.startsWith(commandPrefix))
            return true;

        String[] commandParts = message.substring(commandPrefix.length()).split(" ");
        if (commands.containsKey(commandParts[0]))
        {
            Command c = commands.get(commandParts[0]);
            if (!c.publicCommand && !player.clientID.equals(host))
            {
                privateChat(error_color + "You don't have permission to use this command!", player.clientID);
                return false;
            }

            c.run(Arrays.copyOfRange(commandParts, 1, commandParts.length), player.clientID);
        }
        else
            privateChat(error_color + "Unknown command '" + commandParts[0] + "' !", player.clientID);

        return false;
    }

    public static class Command
    {
        private static final BiConsumer<String[], UUID> reject_func = (p, u) -> privateChat(error_color + "Can't do this right now!", u);

        public String name;
        public String description;
        public String[] partNames;
        public Button button;
        public Screen screen;
        public boolean publicCommand = false;
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
            if (button == b && screen == s)
                return;

            button = b;
            screen = s;
            function = (parts, clientID) ->
            {
                if (Game.screen != s)
                {
                    privateChat(error_color + "Can't do this right now!", clientID);
                    return;
                }
                b.function.run();
            };
        }

        public Command setPublic(boolean publicCommand)
        {
            this.publicCommand = publicCommand;
            return this;
        }

        public void run(String[] parts, UUID clientID)
        {
            try
            {
                if (parts.length != partNames.length && !(partNames[partNames.length - 1].equals("...") && parts.length >= partNames.length - 1))
                {
                    privateChat("\u00a7255000000255Invalid number of arguments! Expected " + partNames.length + " but got " + parts.length +
                        "\nUsage: " + commandPrefix + name + " " +
                        Arrays.stream(partNames).map(p -> "<" + p + ">").collect(Collectors.joining(" ")), clientID);
                    return;
                }
                function.accept(parts, clientID);
            }
            catch (Throwable e)
            {
                privateChat(error_color + "An error occurred while running this command!", clientID);
                if (e instanceof GameCrashedException)
                    e = ((GameCrashedException) e).originalException;
                e.printStackTrace();
            }
        }
    }
}
