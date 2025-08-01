package tanks;

import basewindow.BaseFile;
import tanks.hotbar.Hotbar;
import tanks.hotbar.ItemBar;
import tanks.item.Item;
import tanks.network.ConnectedPlayer;
import tanks.tank.Tank;
import tanks.tank.Turret;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class Player
{
    public int remainingLives;
    public Hotbar hotbar = new Hotbar(this);

    public UUID clientID;
    public String username;
    public Tank tank;
    public String buildName = "player";
    public HashSet<String> ownedBuilds = new HashSet<>();

    public int colorR = 0, colorG = 150, colorB = 255;
    public int colorR2 = (int) Turret.calculateSecondaryColor(colorR), colorG2 = (int) Turret.calculateSecondaryColor(colorG), colorB2 = (int) Turret.calculateSecondaryColor(colorB);
    public int colorR3 = (this.colorR + this.colorR2) / 2, colorG3 = (this.colorG + this.colorG2) / 2, colorB3 = (this.colorB + this.colorB2) / 2;
    public boolean enableSecondaryColor = false, enableTertiaryColor = false;

    public boolean isBot = false;

    protected ConnectedPlayer connectedPlayer;

    public Player(UUID clientID, String username)
    {
        this.clientID = clientID;
        this.username = username;
        this.connectedPlayer = new ConnectedPlayer(clientID, username);
    }

    public String toString()
    {
        return this.username + " (" + this.clientID + ")";
    }

    public Crusade loadCrusade(BaseFile f)
    {
        try
        {
            if (!f.exists())
                return null;

            f.startReading();
            String name = f.nextLine();
            String fileName = f.nextLine();
            boolean internal = Boolean.parseBoolean(f.nextLine());

            Crusade c;

            if (internal)
                c = new Crusade(Game.game.fileManager.getInternalFileContents("/crusades" + fileName), name, fileName);
            else
                c = new Crusade(Game.game.fileManager.getFile(fileName), name);

            this.hotbar = new Hotbar(this);
            this.hotbar.itemBar = new ItemBar(this);

            c.currentLevel = Integer.parseInt(f.nextLine());
            c.saveLevel = c.currentLevel;
            c.started = true;

            CrusadePlayer cp = new CrusadePlayer(this);
            this.remainingLives = Integer.parseInt(f.nextLine());
            cp.coins = Integer.parseInt(f.nextLine());
            cp.itemBar = new ItemBar(this);
            c.crusadePlayers.put(this, cp);

            String[] items = f.nextLine().split("\\|");

            if (f.hasNextLine())
                c.timePassed = Double.parseDouble(f.nextLine());

            if (f.hasNextLine())
            {
                parseStringIntHashMap(cp.tankKills, f.nextLine());
                parseStringIntHashMap(cp.tankDeaths, f.nextLine());
            }

            if (f.hasNextLine())
                parseLevelPerformances(c.performances, f.nextLine());

            if (f.hasNextLine())
            {
                parseStringIntHashMap(cp.itemUses, f.nextLine());
                parseStringIntHashMap(cp.itemHits, f.nextLine());
            }

            if (f.hasNextLine())
            {
                parseIntHashSet(c.livingTankIDs, f.nextLine());
                c.retry = !c.livingTankIDs.isEmpty();
            }

            if (f.hasNextLine())
            {
                parseStringHashSet(cp.ownedBuilds, f.nextLine());
                cp.currentBuild = f.nextLine();
            }

            f.stopReading();

            ArrayList<Item.ShopItem> shop = c.getShop();

            for (int i = 0; i < items.length; i++)
            {
                if (items[i].isEmpty())
                    continue;

                String[] sec = items[i].split(",");
                String itemName = sec[0];
                int count = Integer.parseInt(sec[1]);

                for (Item.ShopItem it : shop)
                {
                    if (it.itemStack.item.name.equals(itemName))
                    {
                        cp.itemBar.slots[i] = Item.CrusadeShopItem.fromString(it.toString()).itemStack;
                        cp.itemBar.slots[i].player = this;
                        cp.itemBar.slots[i].stackSize = count;
                    }
                }
            }

            return c;
        }
        catch (Exception e)
        {
            System.err.println("Failed to load saved crusade progress (log file includes contents): ");
            e.printStackTrace();
            Game.logger.println("Failed to load saved crusade progress: ");
            e.printStackTrace(Game.logger);
            Game.logger.println("Progress file contents:");

            try
            {
                f.startReading();
                while (f.hasNextLine())
                    Game.logger.println(f.nextLine());
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }
        }

        return null;
    }

    public static void parseStringIntHashMap(HashMap<String, Integer> map, String str)
    {
        String[] parts = str.replace("{", "").replace("}", "").split(", ");

        for (String s: parts)
        {
            if (s.isEmpty())
                continue;

            String[] sec = s.split("=");
            map.put(sec[0], Integer.parseInt(sec[1]));
        }
    }

    public static void parseIntHashSet(HashSet<Integer> set, String str)
    {
        String[] parts = str.replace("[", "").replace("]", "").split(", ");

        for (String s: parts)
        {
            if (s.isEmpty())
                continue;

            set.add(Integer.parseInt(s));
        }
    }

    public static void parseStringHashSet(HashSet<String> set, String str)
    {
        String[] parts = str.replace("[", "").replace("]", "").split(", ");

        for (String s: parts)
        {
            if (s.isEmpty())
                continue;

            set.add(s);
        }
    }

    public static void parseLevelPerformances(ArrayList<Crusade.LevelPerformance> performances, String str)
    {
        String[] parts = str.replace("[", "").replace("]", "").split(", ");

        for (String s: parts)
        {
            if (s.isEmpty())
                continue;

            String[] sec = s.split("/");
            Crusade.LevelPerformance l = new Crusade.LevelPerformance(Integer.parseInt(sec[0]));
            l.attempts = Integer.parseInt(sec[1]);
            l.bestTime = Double.parseDouble(sec[2]);
            l.totalTime = Double.parseDouble(sec[3]);
            performances.add(l);
        }
    }

    public ConnectedPlayer getConnectedPlayer()
    {
        if (this == Game.player)
            this.connectedPlayer = new ConnectedPlayer(Game.player.clientID, Game.player.username);

        this.connectedPlayer.colorR = this.colorR;
        this.connectedPlayer.colorG = this.colorG;
        this.connectedPlayer.colorB = this.colorB;
        this.connectedPlayer.colorR2 = this.colorR2;
        this.connectedPlayer.colorG2 = this.colorG2;
        this.connectedPlayer.colorB2 = this.colorB2;
        this.connectedPlayer.colorR3 = this.colorR3;
        this.connectedPlayer.colorG3 = this.colorG3;
        this.connectedPlayer.colorB3 = this.colorB3;

        if (this.tank != null && this.tank.team != null && this.tank.team.enableColor)
        {
            this.connectedPlayer.teamColorR = this.tank.team.teamColorR;
            this.connectedPlayer.teamColorG = this.tank.team.teamColorG;
            this.connectedPlayer.teamColorB = this.tank.team.teamColorB;
        }
        else
        {
            this.connectedPlayer.teamColorR = 255;
            this.connectedPlayer.teamColorG = 255;
            this.connectedPlayer.teamColorB = 255;
        }

        return this.connectedPlayer;
    }
}
