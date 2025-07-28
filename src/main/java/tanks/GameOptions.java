package tanks;

import basewindow.BaseWindow;

public class GameOptions
{
    public GraphicsOptions graphics = new GraphicsOptions();
    public SoundOptions sound = new SoundOptions();
    public MultiplayerOptions multiplayer = new MultiplayerOptions();
    public MiscellaneousOptions misc = new MiscellaneousOptions();
    public WindowOptions window = new WindowOptions();
    public SpeedrunningOptions speedrun = new SpeedrunningOptions();
    public OtherOptions other = new OtherOptions();

    public DebugOptions debug = new DebugOptions();

    public enum BulletTrails {fancy, fast, off}
    public enum Anticheat {strong, weak, off}
    public enum Deterministic {_60fps, _30fps, off}


    public static class DebugOptions
    {
        public boolean alwaysDebug = false;
        public boolean traceAllRays = false;
        public boolean showTankIDs = false;
        public boolean drawAutoZoom = false;
        public boolean drawFaces = false;
        public boolean immutableFaces = false;
        public boolean followingCam = false;
        public boolean invulnerable = false;
        public boolean firstPerson = false;
        public boolean fancyLights = false;
    }

    public static class GraphicsOptions
    {
        public boolean fancyTerrain = true;
        public boolean enable3d = true;
        public BulletTrails bulletTrails = BulletTrails.fancy;
        public boolean enable3dBg = true;
        public boolean glowEnabled = true;
        public boolean angledView = false;
        public EffectOptions effect = new EffectOptions();
        public ShadowOptions shadow = new ShadowOptions();
        public boolean tankTextures = true;
        public boolean antialiasing = false;
        public boolean xrayBullets = true;
        public int maxFps = 60;
        public boolean vsync;

        public static class EffectOptions
        {
            public boolean particleEffects = true;
            public double particlePercentage = 1;
        }

        public static class ShadowOptions
        {
            public boolean shadowsEnabled = Game.framework != Game.Framework.libgdx;
            public int shadowQuality = 10;
        }
    }

    public static class WindowOptions
    {
        public boolean constrainMouse = false;
        public boolean warnBeforeClosing = true;
        public boolean infoBar = false;

        /** Use {@link BaseWindow#fullscreen} to determine real fullscreen status */
        public boolean fullscreen = false;
        public double width = 1400;
        public double height = 940;
    }

    public static class SoundOptions
    {
        public float soundVolume = 1f;
        public float musicVolume = 0.5f;
        public boolean enableLayeredMusic = true;
    }

    public static class MultiplayerOptions
    {
        public PartyHostOptions partyHost = new PartyHostOptions();
        public ServerOptions server = new ServerOptions();
        public boolean chatFilter = true;
        public boolean autoReady = false;

        public static class ServerOptions
        {
            public int port = 8080;
            public String lastParty = "";
            public String lastOnlineServer = "";
            public boolean showIP = true;
            public boolean enableIPConnections = true;
        }

        public static class PartyHostOptions
        {
            public int botPlayerCount = 0;
            public double partyStartTime = 400;
            public boolean disablePartyFriendlyFire = false;
            public Anticheat anticheat = Anticheat.off;
        }
    }

    public static class MiscellaneousOptions
    {
        public boolean autoStart = true;
        public boolean fullStats = true;
        public boolean previewCrusades = true;
        public boolean circularHotbar = false;
        public int networkRate = 60;
        public ExtensionOptions extension = new ExtensionOptions();

        public static class ExtensionOptions
        {
            public boolean enableExtensions = false;
            public boolean autoLoadExtensions = true;
        }
    }

    public static class SpeedrunningOptions
    {
        public boolean showSpeedrunTimer = false;
        public boolean showBestTime = false;
        public Deterministic deterministicMode = Deterministic.off;
    }

    public static class OtherOptions
    {
        public boolean enableVibrations = true;
        public boolean agreedToWorkshopAgreement = false;
    }
}
