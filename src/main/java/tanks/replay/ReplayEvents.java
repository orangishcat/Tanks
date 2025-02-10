package tanks.replay;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.Movable;
import tanks.gui.screen.ScreenAutomatedTests;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.network.NetworkEventMap;
import tanks.network.NetworkUtils;
import tanks.network.event.*;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ReplayEvents
{
    public static class Tick implements IReplayEvent
    {
        public ArrayList<INetworkEvent> events;
        public double ms;
        public double mouseX, mouseY;

        public Tick setParams(ArrayList<INetworkEvent> events, double ms)
        {
            this.events = events;
            this.ms = ms;
            this.mouseX = Drawing.drawing.getMouseX();
            this.mouseY = Drawing.drawing.getMouseY();
            return this;
        }

        @Override
        public void write(ByteBuf b)
        {
            b.writeInt(events.size()).writeDouble(ms);
            b.writeDouble(mouseX).writeDouble(mouseY);
            for (INetworkEvent e : events)
            {
                b.writeInt(get(e.getClass()));
                e.write(b);
            }
        }

        @Override
        public void read(ByteBuf b)
        {
            int eventCnt = b.readInt();
            ms = b.readDouble();
            events = new ArrayList<>();
            mouseX = b.readDouble();
            mouseY = b.readDouble();
            try
            {
                for (int i = 0; i < eventCnt; i++)
                {
                    INetworkEvent e = get(b.readInt()).getConstructor().newInstance();
                    e.read(b);
                    events.add(e);
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void execute()
        {
            for (INetworkEvent e : events)
                e.execute();
        }

        @Override
        public void drawDebug(double x, double y)
        {
            IReplayEvent.super.drawDebug(x, y - 40);
            String text = "Event types: " + String.join(", ", events.stream().map(e -> e.getClass().getSimpleName()).collect(Collectors.toSet()));
            Drawing.drawing.setBoundedInterfaceFontSize(16, Drawing.drawing.interfaceSizeX - 50, text);
            Drawing.drawing.drawInterfaceText(x, y - 20, text, true);
            Drawing.drawing.drawInterfaceText(x, y, String.format("Ms: %.2f", ms), true);
        }

        @Override
        public double delay()
        {
            return ms;
        }
    }

    public static class LevelChange implements IReplayEvent
    {
        public String levelString;

        public LevelChange setLS(String levelString)
        {
            this.levelString = levelString;
            return this;
        }

        @Override
        public void write(ByteBuf b)
        {
            NetworkUtils.writeString(b, levelString);
        }

        @Override
        public void read(ByteBuf b)
        {
            levelString = NetworkUtils.readString(b);
        }

        @Override
        public void execute()
        {
            Game.cleanUp();
            boolean remote = Replay.currentPlaying == null || !Replay.currentPlaying.forTests;
            ScreenPartyHost.isServer = remote;
            new Level(levelString).loadLevel(remote);
            Game.currentLevel.clientStartingItems = Game.currentLevel.startingItems;
            ScreenPartyHost.isServer = false;

            int ind = -1;
            for (Movable m : Game.movables)
            {
                ind++;
                if (!(m instanceof TankPlayer))
                    continue;

                Game.movables.set(ind, new TankReplayPlayer((Tank) m));
            }
            enterGame();
            Drawing.drawing.terrainRenderer.reset();
        }

        public static void enterGame()
        {
            ScreenGame g = new ScreenGame();
            g.playingReplay = g.playing = true;

            if (Game.screen instanceof ScreenAutomatedTests)
                ((ScreenAutomatedTests) Game.screen).game = g;
            else
                Game.screen = g;
        }
    }

    public interface IReplayEvent
    {
        void write(ByteBuf b);
        void read(ByteBuf b);
        void execute();

        default double delay()
        {
            return 0;
        }
        default void drawDebug(double x, double y)
        {
            Drawing.drawing.drawInterfaceText(x, y, getClass().getSimpleName(), true);
        }
    }

    protected static NetworkEventMap inst = new NetworkEventMap();

    @SafeVarargs
    public static void register(Class<? extends INetworkEvent>... classes)
    {
        for (Class<? extends INetworkEvent> c : classes)
        {
            inst.map1.put(inst.id, c);
            inst.map2.put(c, inst.id);
            inst.id++;
        }
    }

    public static int get(Class<? extends INetworkEvent> c)
    {
        Integer i = inst.map2.get(c);

        if (i == null)
            return -1;

        return i;
    }

    public static Class<? extends INetworkEvent> get(int i)
    {
        return inst.map1.get(i);
    }

    private static boolean registered = false;

    public static void registerEvents()
    {
        if (registered)
            return;

        registered = true;
        register(EventSendClientDetails.class, EventPing.class, EventConnectionSuccess.class, EventKick.class,
                EventAnnounceConnection.class, EventChat.class, EventPlayerChat.class, EventLoadLevel.class,
                EventEnterLevel.class, EventLevelEndQuick.class, EventLevelEnd.class, EventReturnToLobby.class,
                EventBeginCrusade.class, EventReturnToCrusade.class, EventShowCrusadeStats.class,
                EventLoadCrusadeHotbar.class, EventSetupHotbar.class, EventAddShopItem.class, EventSortShopButtons.class,
                EventPurchaseItem.class, EventSetItem.class, EventSetItemBarSlot.class, EventLoadItemBarSlot.class,
                EventUpdateTankAbility.class, EventUpdateCoins.class, EventPlayerReady.class, EventPlayerAutoReady.class,
                EventPlayerAutoReadyConfirm.class, EventPlayerSetBuild.class, EventPlayerRevealBuild.class,
                EventUpdateReadyPlayers.class, EventUpdateRemainingLives.class, EventBeginLevelCountdown.class,
                EventTankUpdate.class, EventTankControllerUpdateS.class, EventTankControllerUpdateC.class,
                EventTankControllerUpdateAmmunition.class, EventTankControllerAddVelocity.class, EventTankPlayerCreate.class,
                EventTankCreate.class, EventTankCustomCreate.class, EventTankSpawn.class, EventAirdropTank.class,
                EventTankUpdateHealth.class, EventTankRemove.class, EventShootBullet.class, EventBulletBounce.class,
                EventBulletUpdate.class, EventBulletDestroyed.class, EventBulletInstantWaypoint.class,
                EventBulletAddAttributeModifier.class, EventBulletStunEffect.class, EventBulletUpdateTarget.class,
                EventBulletReboundIndicator.class, EventAddObstacleBullet.class, EventLayMine.class, EventMineRemove.class,
                EventMineChangeTimer.class, EventExplosion.class, EventTankTeleport.class, EventTankUpdateVisibility.class,
                EventTankUpdateColor.class, EventTankTransformPreset.class, EventTankTransformCustom.class, EventTankCharge.class,
                EventTankMimicTransform.class, EventTankMimicLaser.class, EventTankAddAttributeModifier.class,
                EventCreateFreezeEffect.class, EventObstacleDestroy.class, EventObstacleHit.class, EventObstacleShrubberyBurn.class,
                EventObstacleSnowMelt.class, EventObstacleBoostPanelEffect.class, EventPlaySound.class,
                EventSendTankColors.class, EventUpdateTankColors.class, EventShareLevel.class, EventShareCrusade.class,
                EventItemDrop.class, EventItemPickup.class, EventItemDropDestroy.class, EventStatusEffectBegin.class,
                EventStatusEffectDeteriorate.class, EventStatusEffectEnd.class, EventArcadeHit.class,
                EventArcadeRampage.class, EventClearMovables.class, EventArcadeFrenzy.class, EventArcadeEnd.class,
                EventArcadeBonuses.class, EventPurchaseBuild.class);
    }
}
