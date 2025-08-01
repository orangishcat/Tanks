package tanks;

import basewindow.BaseFile;
import basewindow.BaseFileManager;
import basewindow.BaseWindow;
import basewindow.ShaderGroup;
import com.codedisaster.steamworks.SteamMatchmaking;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import tanks.bullet.*;
import tanks.extension.Extension;
import tanks.extension.ExtensionRegistry;
import tanks.generator.LevelGenerator;
import tanks.generator.LevelGeneratorRandom;
import tanks.gui.Button;
import tanks.gui.ChatFilter;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.input.InputBindings;
import tanks.gui.screen.*;
import tanks.gui.screen.leveleditor.OverlayEditorMenu;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.gui.screen.leveleditor.selector.*;
import tanks.hotbar.Hotbar;
import tanks.hotbar.ItemBar;
import tanks.item.Item;
import tanks.item.ItemBullet;
import tanks.item.ItemMine;
import tanks.item.ItemShield;
import tanks.minigames.ArcadeBeatBlocks;
import tanks.minigames.ArcadeClassic;
import tanks.minigames.CastleRampage;
import tanks.minigames.Minigame;
import tanks.network.Client;
import tanks.network.NetworkEventMap;
import tanks.network.SteamNetworkHandler;
import tanks.network.SynchronizedList;
import tanks.network.event.*;
import tanks.network.event.online.*;
import tanks.obstacle.*;
import tanks.registry.*;
import tanks.rendering.ShaderGroundIntro;
import tanks.rendering.ShaderGroundOutOfBounds;
import tanks.rendering.ShaderTracks;
import tanks.tank.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class Game
{
	public enum Framework {lwjgl, libgdx}
	public static Framework framework;

	public static final double tile_size = 50;

	public static UUID computerID;
	public static final UUID clientID = UUID.randomUUID();

	public static final int absoluteDepthBase = 1000;

	public static ArrayList<Movable> movables = new ArrayList<>();
	public static ArrayList<Obstacle> obstacles = new ArrayList<>();
	public static ArrayList<Effect> effects = new ArrayList<>();
	public static ArrayList<Effect> tracks = new ArrayList<>();
	public static ArrayList<Cloud> clouds = new ArrayList<>();
	public static SynchronizedList<Player> players = new SynchronizedList<>();

	public static ArrayList<Player> botPlayers = new ArrayList<>();
	public static int botPlayerCount = 0;

	/**
	 * Obstacles that need to change how they look next frame
	 */
	public static HashSet<Obstacle> redrawObstacles = new HashSet<>();

	/**
	 * Ground tiles that need to be redrawn due to obstacles being added/removed over them
	 */
	public static class GroundTile
	{
		public int x;
		public int y;
		public GroundTile(int x, int y)
		{
			this.x = x;
			this.y = y;
		}

		@Override
		public boolean equals(Object o)
		{
			return o instanceof GroundTile && ((GroundTile) o).x == this.x && ((GroundTile) o).y == this.y;
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(x, y);
		}
	}

	public static HashSet<GroundTile> redrawGroundTiles = new HashSet<>();

	public static Player player;

	public static ArrayList<Obstacle> updateObstacles = new ArrayList<>();
	public static ArrayList<Obstacle> checkUpdateObstacles = new ArrayList<>();

	public static HashSet<Movable> removeMovables = new HashSet<>();
	public static HashSet<Obstacle> removeObstacles = new HashSet<>();
	public static HashSet<Effect> removeEffects = new HashSet<>();
	public static HashSet<Effect> removeTracks = new HashSet<>();
	public static HashSet<Cloud> removeClouds = new HashSet<>();

	public static ArrayList<Effect> addEffects = new ArrayList<>();
	public static Queue<Effect> recycleEffects = new LinkedList<>();

	public static final SynchronizedList<INetworkEvent> eventsOut = new SynchronizedList<>();
	public static final SynchronizedList<INetworkEvent> eventsIn = new SynchronizedList<>();

	public static Team playerTeam = new Team("ally");
	public static Team enemyTeam = new Team("enemy");

	public static Team playerTeamNoFF = new Team("ally", false);
	public static Team enemyTeamNoFF = new Team("enemy", false);

	/** Use this if you want to spawn a mine not allied with any tank, or such*/
	public static Tank dummyTank;

	public static int currentSizeX = 28, currentSizeY = 18;
	public static int tileOffsetX = 0, tileOffsetY = 0;
	public static double bgResMultiplier = 1;


	//Remember to change the version in android's build.gradle and ios's robovm.properties
	//Versioning has moved to version.txt
	public static String version = "Tanks v-1.-1.-1";

    public static final int network_protocol = 58;
	public static boolean debug = false;
	public static boolean traceAllRays = false;
	public static boolean showTankIDs = false;
	public static boolean drawAutoZoom = false;
	public static final boolean cinematic = false;

	public static long steamLobbyInvite = -1;

	public static String lastVersion = "Tanks v0";

	public static int port = 8080;

	public static String lastParty = "";
	public static String lastOnlineServer = "";
	public static boolean showIP = true;
	public static boolean enableIPConnections = true;
	public static SteamMatchmaking.LobbyType steamVisibility = SteamMatchmaking.LobbyType.Private;

	public static boolean agreedToWorkshopAgreement = false;

	public static double levelSize = 1;

	public static TankPlayer playerTank;

	public static boolean bulletLocked = false;

	public static boolean vsync = true;
	public static int maxFPS = 0;
	public static int networkRate = 20;

	public static boolean enable3d = true;
	public static boolean enable3dBg = true;
	public static boolean angledView = false;
	public static boolean xrayBullets = true;

	public static boolean followingCam = false;
	public static boolean firstPerson = false;

	public static boolean fancyLights = false;

	public static boolean tankTextures = true;

	public static boolean soundsEnabled = true;
	public static boolean musicEnabled = true;

	public static boolean antialiasing = false;

	public static boolean enableVibrations = true;

	public static boolean enableChatFilter = true;
	public static boolean nameInMultiplayer = true;

	public static boolean showPathfinding = false;
	public static boolean drawFaces = false;
	public static boolean immutableFaces = false;
	public static boolean showSpeedrunTimer = false;
	public static boolean showBestTime = false;

	public static boolean previewCrusades = true;

	public static boolean deterministicMode = false;
	public static boolean deterministic30Fps = false;
	public static int seed = 0;

	public static boolean invulnerable = false;

	public static boolean warnBeforeClosing = true;

	public static String crashMessage = "Why would this game ever even crash anyway?";
	public static String crashLine = "What, did you think I was a bad programmer? smh";

	public static long crashTime = 0;

	//public static boolean autoMinimapEnabled = true;
	//public static float defaultZoom = 1.5f;

    public static double[] color = new double[3];

    public static Screen screen;
	public static Screen prevScreen;

	public static String ip = "";

	public static boolean fancyTerrain = true;
	public static boolean effectsEnabled = true;
	public static boolean bulletTrails = true;
	public static boolean fancyBulletTrails = true;
	public static boolean glowEnabled = true;

	public static double effectMultiplier = 1;

	public static boolean shadowsEnabled = Game.framework != Framework.libgdx;
	public static int shadowQuality = 10;

	public static boolean autostart = true;
	public static boolean autoReady = false;
	public static double startTime = 400;
	public static boolean fullStats = true;

	public static boolean constrainMouse = false;

	public static double partyStartTime = 400;
	public static boolean disablePartyFriendlyFire = false;

	public static Screen lastOfflineScreen = null;

	public static RegistryTank registryTank = new RegistryTank();
	public static RegistryBullet registryBullet = new RegistryBullet();
	public static RegistryObstacle registryObstacle = new RegistryObstacle();
	public static RegistryItem registryItem = new RegistryItem();
	public static RegistryGenerator registryGenerator = new RegistryGenerator();
	public static RegistryModelTank registryModelTank = new RegistryModelTank();
	public static RegistryMinigame registryMinigame = new RegistryMinigame();
	public static RegistryMetadataSelectors registryMetadataSelectors = new RegistryMetadataSelectors();

	public HashMap<Class<? extends ShaderGroup>, ShaderGroup> shaderInstances = new HashMap<>();
	public ShaderGroundIntro shaderIntro;
	public ShaderGroundOutOfBounds shaderOutOfBounds;
	public ShaderTracks shaderTracks;

	public static boolean enableExtensions = false;
	public static boolean autoLoadExtensions = true;
	public static ExtensionRegistry extensionRegistry = new ExtensionRegistry();

	public static Extension[] extraExtensions = null;
	public static int[] extraExtensionOrder;

	public BaseWindow window;

	public BaseFileManager fileManager;

	public static Level currentLevel = null;
	public static String currentLevelString = "";

	/** 0: Birds-eye<br>
	 * 1: Angled<br>
	 * 2: Third person<br>
	 * 3: First person */
	public static int perspectiveID = 0;

	public static ChatFilter chatFilter = new ChatFilter();

	public LinkedHashMap<String, InputBindingGroup> inputBindings = new LinkedHashMap<>();
	public InputBindings input;

	public static PrintStream logger = System.err;

	public static String directoryPath = "/.tanks";

	public static final String logPath = directoryPath + "/logfile.txt";
	public static final String extensionRegistryPath = directoryPath + "/extensions.txt";
	public static final String optionsPath = directoryPath + "/options.txt";
	public static final String controlsPath = directoryPath + "/controls.txt";
	public static final String tutorialPath = directoryPath + "/tutorial.txt";
	public static final String uuidPath = directoryPath + "/uuid";
	public static final String levelDir = directoryPath + "/levels";
	//public static final String modLevelDir = directoryPath + "/modlevels/";
	public static final String crusadeDir = directoryPath + "/crusades";
	public static final String savedCrusadePath = directoryPath + "/crusades/progress/";
	public static final String itemDir = directoryPath + "/items";
	public static final String tankDir = directoryPath + "/tanks";
	public static final String buildDir = directoryPath + "/builds";
	public static final String replaysDir = directoryPath + "/replays/";
	public static final String extensionDir = directoryPath + "/extensions/";
	public static final String crashesPath = directoryPath + "/crashes/";
	public static final String screenshotsPath = directoryPath + "/screenshots/";

	public static final String resourcesPath = directoryPath + "/resources/";
	public static final String languagesPath = resourcesPath + "languages/";

	public static float soundVolume = 1f;
	public static float musicVolume = 0.5f;
	public static boolean enableLayeredMusic = true;

	public static boolean isOnlineServer;
	public static boolean connectedToOnline = false;

	public static SteamNetworkHandler steamNetworkHandler;
	public boolean runningCallbacks = false;
	public Throwable callbackException = null;

	public static String homedir;
	public static Game game = new Game();

	/**
	Note: this is not used by the game to determine fullscreen status<br>
	It is simply a value defined before<br>
	Refer to {@link BaseWindow#fullscreen Game.game.window.fullscreen} for true fullscreen status<br>
	Value is set before {@code Game.game.window} is initialized
	*/
	public boolean fullscreen = false;

	private Game()
	{
		Game.game = this;
		input = new InputBindings();
	}

	public static void registerEvents()
	{
		NetworkEventMap.register(EventSendClientDetails.class);
		NetworkEventMap.register(EventPing.class);
		NetworkEventMap.register(EventConnectionSuccess.class);
		NetworkEventMap.register(EventKick.class);
		NetworkEventMap.register(EventAnnounceConnection.class);
		NetworkEventMap.register(EventChat.class);
		NetworkEventMap.register(EventPlayerChat.class);
		NetworkEventMap.register(EventLoadLevel.class);
		NetworkEventMap.register(EventEnterLevel.class);
		NetworkEventMap.register(EventSetLevelVersus.class);
		NetworkEventMap.register(EventLevelFinishedQuick.class);
		NetworkEventMap.register(EventLevelFinished.class);
		NetworkEventMap.register(EventLevelExit.class);
		NetworkEventMap.register(EventReturnToLobby.class);
		NetworkEventMap.register(EventBeginCrusade.class);
		NetworkEventMap.register(EventReturnToCrusade.class);
		NetworkEventMap.register(EventShowCrusadeStats.class);
		NetworkEventMap.register(EventLoadCrusadeHotbar.class);
		NetworkEventMap.register(EventSetupHotbar.class);
		NetworkEventMap.register(EventAddShopItem.class);
		NetworkEventMap.register(EventSortShopButtons.class);
		NetworkEventMap.register(EventPurchaseItem.class);
		NetworkEventMap.register(EventPurchaseBuild.class);
		NetworkEventMap.register(EventSetItem.class);
		NetworkEventMap.register(EventSetItemBarSlot.class);
		NetworkEventMap.register(EventLoadItemBarSlot.class);
		NetworkEventMap.register(EventUpdateTankAbility.class);
		NetworkEventMap.register(EventUpdateCoins.class);
		NetworkEventMap.register(EventPlayerReady.class);
		NetworkEventMap.register(EventPlayerAutoReady.class);
		NetworkEventMap.register(EventPlayerAutoReadyConfirm.class);
		NetworkEventMap.register(EventPlayerSetBuild.class);
		NetworkEventMap.register(EventPlayerRevealBuild.class);
		NetworkEventMap.register(EventUpdateReadyPlayers.class);
		NetworkEventMap.register(EventUpdateEliminatedPlayers.class);
		NetworkEventMap.register(EventUpdateRemainingLives.class);
		NetworkEventMap.register(EventBeginLevelCountdown.class);
		NetworkEventMap.register(EventTankUpdate.class);
		NetworkEventMap.register(EventTankControllerUpdateS.class);
		NetworkEventMap.register(EventTankControllerUpdateC.class);
		NetworkEventMap.register(EventTankControllerUpdateAmmunition.class);
		NetworkEventMap.register(EventTankControllerAddVelocity.class);
		NetworkEventMap.register(EventTankPlayerCreate.class);
		NetworkEventMap.register(EventTankCreate.class);
		NetworkEventMap.register(EventTankCustomCreate.class);
		NetworkEventMap.register(EventTankSpawn.class);
		NetworkEventMap.register(EventAirdropTank.class);
		NetworkEventMap.register(EventTankUpdateHealth.class);
		NetworkEventMap.register(EventTankRemove.class);
		NetworkEventMap.register(EventShootBullet.class);
		NetworkEventMap.register(EventBulletBounce.class);
		NetworkEventMap.register(EventBulletUpdate.class);
		NetworkEventMap.register(EventBulletDestroyed.class);
		NetworkEventMap.register(EventBulletInstantWaypoint.class);
		NetworkEventMap.register(EventBulletAddAttributeModifier.class);
		NetworkEventMap.register(EventBulletStunEffect.class);
		NetworkEventMap.register(EventBulletUpdateTarget.class);
		NetworkEventMap.register(EventBulletReboundIndicator.class);
		NetworkEventMap.register(EventAddObstacleBullet.class);
		NetworkEventMap.register(EventLayMine.class);
		NetworkEventMap.register(EventMineRemove.class);
		NetworkEventMap.register(EventMineChangeTimer.class);
		NetworkEventMap.register(EventExplosion.class);
		NetworkEventMap.register(EventTankTeleport.class);
		NetworkEventMap.register(EventTankUpdateVisibility.class);
		NetworkEventMap.register(EventTankUpdateColor.class);
		NetworkEventMap.register(EventTankTransformPreset.class);
		NetworkEventMap.register(EventTankTransformCustom.class);
		NetworkEventMap.register(EventTankCharge.class);
		NetworkEventMap.register(EventTankMimicTransform.class);
		NetworkEventMap.register(EventTankMimicLaser.class);
		NetworkEventMap.register(EventTankAddAttributeModifier.class);
		NetworkEventMap.register(EventCreateFreezeEffect.class);
		NetworkEventMap.register(EventObstacleDestroy.class);
		NetworkEventMap.register(EventObstacleHit.class);
		NetworkEventMap.register(EventObstacleShrubberyBurn.class);
		NetworkEventMap.register(EventObstacleSnowMelt.class);
		NetworkEventMap.register(EventObstacleBoostPanelEffect.class);
		NetworkEventMap.register(EventPlaySound.class);
		NetworkEventMap.register(EventSendTankColors.class);
		NetworkEventMap.register(EventUpdateTankColors.class);
		NetworkEventMap.register(EventShareLevel.class);
		NetworkEventMap.register(EventShareCrusade.class);
		NetworkEventMap.register(EventItemDrop.class);
		NetworkEventMap.register(EventItemPickup.class);
		NetworkEventMap.register(EventItemDropDestroy.class);
		NetworkEventMap.register(EventStatusEffectBegin.class);
		NetworkEventMap.register(EventStatusEffectDeteriorate.class);
		NetworkEventMap.register(EventStatusEffectEnd.class);
		NetworkEventMap.register(EventArcadeHit.class);
		NetworkEventMap.register(EventArcadeRampage.class);
		NetworkEventMap.register(EventClearMovables.class);
		NetworkEventMap.register(EventArcadeFrenzy.class);
		NetworkEventMap.register(EventArcadeEnd.class);
		NetworkEventMap.register(EventArcadeBonuses.class);
		NetworkEventMap.register(EventStartTest.class);
		NetworkEventMap.register(EventRunTest.class);

		NetworkEventMap.register(EventSendOnlineClientDetails.class);
		NetworkEventMap.register(EventSilentDisconnect.class);
		NetworkEventMap.register(EventNewScreen.class);
		NetworkEventMap.register(EventSetScreen.class);
		NetworkEventMap.register(EventAddShape.class);
		NetworkEventMap.register(EventAddText.class);
		NetworkEventMap.register(EventAddButton.class);
		NetworkEventMap.register(EventAddTextBox.class);
		NetworkEventMap.register(EventAddMenuButton.class);
		NetworkEventMap.register(EventAddUUIDTextBox.class);
		NetworkEventMap.register(EventSetMusic.class);
		NetworkEventMap.register(EventRemoveShape.class);
		NetworkEventMap.register(EventRemoveText.class);
		NetworkEventMap.register(EventRemoveButton.class);
		NetworkEventMap.register(EventRemoveTextBox.class);
		NetworkEventMap.register(EventRemoveMenuButton.class);
		NetworkEventMap.register(EventSetPauseScreenTitle.class);
		NetworkEventMap.register(EventPressedButton.class);
		NetworkEventMap.register(EventSetTextBox.class);
		NetworkEventMap.register(EventUploadLevel.class);
		NetworkEventMap.register(EventSendLevelToDownload.class);
		NetworkEventMap.register(EventCleanUp.class);
	}

	public static void registerObstacle(Class<? extends Obstacle> obstacle, String name)
	{
		if (Game.registryObstacle.getEntry(name).obstacle == ObstacleUnknown.class)
			new RegistryObstacle.ObstacleEntry(Game.registryObstacle, obstacle, name);
	}

	public static void registerTank(Class<? extends Tank> tank, String name, double weight)
	{
		if (Game.registryTank.getEntry(name).tank == TankUnknown.class)
			new RegistryTank.TankEntry(Game.registryTank, tank, name, weight);
	}

	public static void registerTank(Class<? extends Tank> tank, String name, double weight, boolean isBoss)
	{
		if (Game.registryTank.getEntry(name).tank == TankUnknown.class)
			new RegistryTank.TankEntry(Game.registryTank, tank, name, weight, isBoss);
	}

	public static void registerBullet(Class<? extends Bullet> bullet, String name, String icon)
	{
		new RegistryBullet.BulletEntry(Game.registryBullet, bullet, name, icon);
	}

	public static void registerItem(Class<? extends Item> item, String name, String image)
	{
		new RegistryItem.ItemEntry(Game.registryItem, item, name, image);
	}

	public static void registerGenerator(Class<? extends LevelGenerator> generator, String name)
	{
		try
		{
			Game.registryGenerator.generators.put(name, generator.getConstructor().newInstance());
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}
	}

	public static void registerTankModel(String dir)
	{
		Game.registryModelTank.registerFullModel(dir);
	}

	public static void registerTankSkin(TankModels.TankSkin s)
	{
		Game.registryModelTank.registerSkin(s);
	}

	public static void registerMinigame(Class<? extends Minigame> minigame, String name, String desc)
	{
		registryMinigame.minigames.put(name, minigame);
		registryMinigame.minigameDescriptions.put(name, desc);
	}

	public static void registerMetadataSelector(String name, Class<? extends MetadataSelector> selector)
	{
		registryMetadataSelectors.metadataSelectors.put(name, selector);
	}

	public static void initScript()
	{
		version = "Tanks v" + Game.readVersionFromFile();
		player = new Player(clientID, "");
		Game.players.add(player);

		Drawing.initialize();
		Panel.initialize();
		Game.exitToTitle();

		Hotbar.toggle = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 20, 150, 40, "", () -> Game.player.hotbar.persistent = !Game.player.hotbar.persistent);

		steamNetworkHandler = new SteamNetworkHandler();
		steamNetworkHandler.load();

		registerEvents();
		DefaultItems.initialize();

		registerObstacle(ObstacleStackable.class, "normal");
		registerObstacle(ObstacleIndestructible.class, "hard");
		registerObstacle(ObstacleHole.class, "hole");
		registerObstacle(ObstacleBouncy.class, "bouncy");
		registerObstacle(ObstacleNoBounce.class, "nobounce");
		registerObstacle(ObstacleBreakable.class, "breakable");
		registerObstacle(ObstacleExplosive.class, "explosive");
		registerObstacle(ObstacleLight.class, "light");
		registerObstacle(ObstacleShrubbery.class, "shrub");
		registerObstacle(ObstacleMud.class, "mud");
		registerObstacle(ObstacleIce.class, "ice");
		registerObstacle(ObstacleSnow.class, "snow");
//		registerObstacle(ObstacleLava.class, "lava");
		registerObstacle(ObstacleBoostPanel.class, "boostpanel");
		registerObstacle(ObstacleTeleporter.class, "teleporter");
		registerObstacle(ObstacleBeatBlock.class, "beat");
		registerObstacle(ObstacleGroundPaint.class, "paint");
//		registerObstacle(ObstacleText.class, "text");

		registerTank(TankDummy.class, "dummy", 0);
		registerTank(TankBrown.class, "brown", 1);
		registerTank(TankGray.class, "gray", 1);
		registerTank(TankMint.class, "mint", 1.0 / 2);
		registerTank(TankYellow.class, "yellow", 1.0 / 2);
		registerTank(TankMagenta.class, "magenta", 1.0 / 3);
		registerTank(TankRed.class, "red", 1.0 / 6);
		registerTank(TankGreen.class, "green", 1.0 / 10);
		registerTank(TankPurple.class, "purple", 1.0 / 10);
		registerTank(TankBlue.class, "blue", 1.0 / 4);
		registerTank(TankWhite.class, "white", 1.0 / 10);
		registerTank(TankCyan.class, "cyan", 1.0 / 4);
		registerTank(TankOrange.class, "orange", 1.0 / 4);
		registerTank(TankMaroon.class, "maroon", 1.0 / 4);
		registerTank(TankMustard.class, "mustard", 1.0 / 4);
		registerTank(TankMedic.class, "medic", 1.0 / 4);
		registerTank(TankOrangeRed.class, "orangered", 1.0 / 4);
		registerTank(TankGold.class, "gold", 1.0 / 4);
		registerTank(TankDarkGreen.class, "darkgreen", 1.0 / 10);
		registerTank(TankBlack.class, "black", 1.0 / 10);
		registerTank(TankMimic.class, "mimic", 1.0 / 4);
		registerTank(TankLightBlue.class, "lightblue", 1.0 / 8);
		registerTank(TankPink.class, "pink", 1.0 / 12);
		registerTank(TankMini.class, "mini", 0);
		registerTank(TankSalmon.class, "salmon", 1.0 / 10);
		registerTank(TankLightPink.class, "lightpink", 1.0 / 10);
		registerTank(TankBoss.class, "boss", 1.0 / 40, true);

		registerBullet(Bullet.class, Bullet.bullet_class_name, "bullet_normal.png");
		registerBullet(BulletInstant.class, BulletInstant.bullet_class_name, "bullet_laser.png");
		registerBullet(BulletGas.class, BulletGas.bullet_class_name, "bullet_flame.png");
		registerBullet(BulletArc.class, BulletArc.bullet_class_name, "bullet_arc.png");
		registerBullet(BulletBlock.class, BulletBlock.bullet_class_name, "bullet_block.png");
		registerBullet(BulletAirStrike.class, BulletAirStrike.bullet_class_name, "bullet_fire.png");

		registerItem(ItemBullet.class, ItemBullet.item_class_name, "bullet_normal.png");
		registerItem(ItemMine.class, ItemMine.item_class_name, "mine.png");
		registerItem(ItemShield.class, ItemShield.item_class_name, "shield.png");

		registerMinigame(ArcadeClassic.class, "Arcade mode", "A gamemode which gets crazier as you---destroy more tanks.------Featuring a score mechanic, unlimited---lives, a time limit, item drops, and---end-game bonuses!");
		registerMinigame(ArcadeBeatBlocks.class, "Beat arcade mode", "Arcade mode but with beat blocks!");
		registerMinigame(CastleRampage.class, "Rampage trial", "Beat the level as fast as you can---with unlimited lives and rampages!");
//		registerMinigame(TeamDeathmatch.class, "Team deathmatch", "something");

		registerMetadataSelector(SelectorStackHeight.selector_name, SelectorStackHeight.class);
		registerMetadataSelector(SelectorGroupID.selector_name, SelectorGroupID.class);
		registerMetadataSelector(SelectorBeatPattern.selector_name, SelectorBeatPattern.class);
		registerMetadataSelector(SelectorRotation.selector_name, SelectorRotation.class);
		registerMetadataSelector(SelectorTeam.selector_name, SelectorTeam.class);
		registerMetadataSelector(SelectorLuminosity.selector_name, SelectorLuminosity.class);
		registerMetadataSelector(SelectorColor.selector_name, SelectorColor.class);
		registerMetadataSelector(SelectorColorAndNoise.selector_name, SelectorColorAndNoise.class);

		homedir = System.getProperty("user.home");

		if (Game.framework == Framework.libgdx)
			homedir = "";

		BaseFile directoryFile = game.fileManager.getFile(homedir + directoryPath);
		if (!directoryFile.exists() && Game.framework != Framework.libgdx)
		{
			directoryFile.mkdirs();
			try
			{
				game.fileManager.getFile(homedir + logPath).create();
				Game.logger = new PrintStream(new FileOutputStream(homedir + logPath, true));
			}
			catch (IOException e)
			{
				e.printStackTrace();
				System.exit(1);
			}
		}

		BaseFile extensionRegistryFile = game.fileManager.getFile(homedir + extensionRegistryPath);
		if (!extensionRegistryFile.exists())
            extensionRegistry.initRegistry();

		BaseFile levelsFile = game.fileManager.getFile(homedir + levelDir);
		if (!levelsFile.exists())
            levelsFile.mkdirs();

		BaseFile crusadesFile = game.fileManager.getFile(homedir + crusadeDir);
		if (!crusadesFile.exists())
            crusadesFile.mkdirs();

		BaseFile savedCrusadesProgressFile = game.fileManager.getFile(homedir + savedCrusadePath + "/internal");
		if (!savedCrusadesProgressFile.exists())
            savedCrusadesProgressFile.mkdirs();

		BaseFile itemsFile = game.fileManager.getFile(homedir + itemDir);
		if (!itemsFile.exists())
            itemsFile.mkdirs();

		BaseFile tanksFile = game.fileManager.getFile(homedir + tankDir);
		if (!tanksFile.exists())
            tanksFile.mkdirs();

		BaseFile extensionsFile = game.fileManager.getFile(homedir + extensionDir);
		if (!extensionsFile.exists())
            extensionsFile.mkdirs();

		BaseFile replaysFile = game.fileManager.getFile(homedir + replaysDir);
		if (!replaysFile.exists())
			replaysFile.mkdirs();

		BaseFile screenshotsFile = game.fileManager.getFile(homedir + screenshotsPath);
		if (!screenshotsFile.exists())
		{
			screenshotsFile.mkdirs();
		}

		BaseFile uuidFile = game.fileManager.getFile(homedir + uuidPath);
		if (!uuidFile.exists())
		{
			try
			{
				uuidFile.create();
				uuidFile.startWriting();
				uuidFile.println(UUID.randomUUID().toString());
				uuidFile.println("IMPORTANT: This file contains an ID unique to your computer.");
				uuidFile.println("The file can be used by online services. Deleting or modifying");
				uuidFile.println("the file or its contents can cause loss of online data.");
				uuidFile.stopWriting();
			}
			catch (IOException e)
			{
				e.printStackTrace();
				System.exit(1);
			}
		}

		try
		{
			uuidFile.startReading();
			Game.computerID = UUID.fromString(uuidFile.nextLine());
			uuidFile.stopReading();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}

		try
		{
			Game.logger = new PrintStream(new FileOutputStream (homedir + logPath, true));
		}
		catch (FileNotFoundException e)
		{
			Game.logger = System.err;
			Game.logger.println(new Date() + " (syswarn) logfile not found despite existence of tanks directory! using stderr instead.");
		}

		BaseFile optionsFile = Game.game.fileManager.getFile(Game.homedir + Game.optionsPath);
		if (!optionsFile.exists())
		{
			ScreenOptions.initOptions(Game.homedir);
		}

		ScreenOptions.loadOptions(Game.homedir);

		extensionRegistry.loadRegistry();

		if (extraExtensions != null)
		{
			for (int i = 0; i < extraExtensions.length; i++)
			{
				if (extraExtensionOrder != null && i < extraExtensionOrder.length)
					extensionRegistry.extensions.add(extraExtensionOrder[i], extraExtensions[i]);
				else
					extensionRegistry.extensions.add(extraExtensions[i]);
			}
		}

		if (!enableExtensions && extraExtensions != null)
		{
			System.err.println("Notice: The game has been launched from Tanks.launchWithExtensions() with extensions in options.txt disabled. Only extensions provided to launchWithExtensions() will be used.");
		}

		for (Extension e: extensionRegistry.extensions)
			e.setUp();

		for (RegistryTank.TankEntry e: registryTank.tankEntries)
			e.initialize();

		game.input.file = game.fileManager.getFile(Game.homedir + Game.controlsPath);
		game.input.load();
	}

	public static void postInitScript()
	{
		ArrayList<String> overrideLocations = new ArrayList<>();
		overrideLocations.add(Game.homedir + Game.resourcesPath);
		Game.game.window.setOverrideLocations(overrideLocations, Game.game.fileManager);
	}

	public static void createModels()
	{
		TankModels.initialize();

		BulletBlock.block = Drawing.drawing.getModel("/models/cube/");
	}

	/**
	 * Adds a tank to the game's movables list and generates/registers a network ID for it.
	 * Use this if you want to add computer-controlled tanks if you are not connected to a server.
	 *
	 * @param tank the tank to add
	 */
	public static void addTank(Tank tank)
	{
		if (tank instanceof TankPlayer || tank instanceof TankPlayerRemote || tank instanceof TankRemote)
			Game.exitToCrash(new RuntimeException("Invalid tank added with Game.addTank(" + tank + ")"));

		tank.registerNetworkID();
		Game.movables.add(tank);
		Game.eventsOut.add(new EventTankCreate(tank));
	}

	/**
	 * Adds a tank to the game's movables list and generates/registers a network ID for it after it was spawned by another tank.
	 * Use this if you want to spawn computer-controlled tanks from another tank if you are not connected to a server.
	 *
	 * @param tank the tank to add
	 * @param parent the tank that is spawning the tank
	 */
	public static void spawnTank(Tank tank, Tank parent)
	{
		tank.registerNetworkID();
		Game.movables.add(tank);
		Game.eventsOut.add(new EventTankSpawn(tank, parent));
	}

	/**
	 * Adds a tank to the game's movables list and generates/registers a network ID for it.
	 * Use this if you want to add computer-controlled tanks if you are not connected to a server.
	 */
	public static void addPlayerTank(Player player, double x, double y, double angle, Team t)
	{
		addPlayerTank(player, x, y, angle, t, 0);
	}

	public static void addPlayerTank(Player player, double x, double y, double angle, Team t, double drawAge)
	{
		int id = Tank.nextFreeNetworkID();
		EventTankPlayerCreate e = new EventTankPlayerCreate(player, x, y, angle, t, id, drawAge);
		Game.eventsOut.add(e);
		e.execute();
	}

	public static void addObstacle(Obstacle o)
	{
		addObstacle(o, true);
	}

	public static void addObstacle(Obstacle o, boolean refresh)
	{
		o.removed = false;
		Game.obstacles.add(o);
		o.postOverride();

		if (o instanceof IAvoidObject)
			IAvoidObject.avoidances.add((IAvoidObject) o);

		if (o.startHeight > 0)
			return;

		Chunk c = Chunk.getChunk(o.posX, o.posY);
		if (c != null)
			c.addObstacle(o, refresh);

		if (refresh)
		{
			redraw(o);
			Game.redrawObstacles.add(o);
		}

		o.afterAdd();

		for (Obstacle o1 : o.getNeighbors())
			o1.onNeighborUpdate();
	}

	public static boolean usernameInvalid(String username)
	{
		if (username.length() > 25)
			return true;

		for (int i = 0; i < username.length(); i++)
		{
			if (!"abcdefghijklmnopqrstuvwxyz1234567890_".contains(username.toLowerCase().substring(i, i+1)))
			{
				return true;
			}
		}

		return false;
	}

	public static void removePlayer(UUID id)
	{
		for (int i = 0; i < Game.players.size(); i++)
		{
			if (Game.players.get(i).clientID.equals(id))
			{
				Game.players.remove(i);
				i--;
			}
		}
	}

	public static String timeInterval(long time1, long time2)
	{
		return timeInterval(time1, time2, false);
	}

	public static String timeInterval(long time1, long time2, boolean seconds)
	{
		long secs = (time2 - time1) / 1000;
		long mins = secs / 60;
		long hours = mins / 60;
		long days = hours / 24;

		if (days > 7)
			return days + "d";
		else if (days > 0)
			return days + "d " + hours % 24 + "h";
		else if (hours > 0)
			return hours % 24 + "h " + mins % 60 + "m";
		else if (mins > 0)
			return mins % 60 + "m";
		else if (seconds)
			return secs + "s";
		else
			return "less than 1m";
	}

	public static String formatString(String s)
	{
		if (s.isEmpty())
			return s;
		else if (s.length() == 1)
			return s.toUpperCase();
		else
			return Character.toUpperCase(s.charAt(0)) + s.substring(1).replace("-", " ").replace("_", " ").toLowerCase();
	}

	public static void exitToInterlevel()
	{
		Minigame m = null;
		if (Game.currentLevel instanceof Minigame)
			m = (Minigame) Game.currentLevel;

		silentCleanUp();

		if (m == null)
		{
			if (ScreenPartyHost.isServer)
				screen = new ScreenPartyInterlevel();
			else
				screen = new ScreenInterlevel();
		}
		else
			m.loadInterlevelScreen();
	}

	public static void exitToEditor(String name)
	{
		silentCleanUp();

		ScreenLevelEditor s = new ScreenLevelEditor(name, Game.currentLevel);
		Game.loadLevel(game.fileManager.getFile(Game.homedir + levelDir + "/" + name), s);
		s.paused = true;

		OverlayEditorMenu m = new OverlayEditorMenu(s, s);
		m.showTime = true;
		Game.screen = m;
	}

	public static void exitToCrash(Throwable e)
	{
		while (e instanceof GameCrashedException)
			e = ((GameCrashedException) e).originalException;

		if (Game.game.runningCallbacks)
			Game.game.callbackException = e;

		throw new GameCrashedException(e);
	}

	protected static void displayCrashScreen(Throwable e)
	{
		System.gc();

		e.printStackTrace();

		if (ScreenPartyHost.isServer && ScreenPartyHost.server != null)
			ScreenPartyHost.server.close("The party has ended because the host crashed");

		if (ScreenPartyLobby.isClient || Game.connectedToOnline)
			Client.handler.close();

		ScreenPartyLobby.connections.clear();

		ScreenPartyHost.isServer = false;
		ScreenPartyLobby.isClient = false;

		try
		{
			if (Game.screen instanceof ScreenLevelEditor)
				((ScreenLevelEditor) Game.screen).save();
		}
		catch (Exception ignored) {}

		cleanUp();

		Game.crashMessage = e.toString();
		Game.crashLine = "Unable to locate crash line. Please check the crash report for more info.";

		for (StackTraceElement se: e.getStackTrace())
		{
			String s = se.toString();
			if (s.startsWith("tanks") || (s.contains(".") && s.split("\\.")[0].endsWith("window")))
			{
				Game.crashLine = "at " + s;
				break;
			}
		}

		Game.crashTime = System.currentTimeMillis();
		Game.logger.println(new Date() + " (syserr) the game has crashed! below is a crash report, good luck:");
		e.printStackTrace(Game.logger);

		if (!(Game.screen instanceof ScreenCrashed))
		{
			try
			{
				BaseFile dir = Game.game.fileManager.getFile(Game.homedir + Game.crashesPath);
				if (!dir.exists())
					dir.mkdirs();

				BaseFile f = Game.game.fileManager.getFile(Game.homedir + Game.crashesPath + Game.crashTime + ".crash");
				f.create();

				f.startWriting();
				f.println("Tanks crash report: " + Game.version + " - " + new Date() + "\n");

				f.println(e.toString());
				for (StackTraceElement el: e.getStackTrace())
				{
					f.println("at " + el.toString());
				}

				f.println("\nSystem properties:");
				Properties p = System.getProperties();
				for (Object s: p.keySet())
					f.println(s + ": " + p.get(s));

				f.stopWriting();
			}
			catch (Exception ex) {ex.printStackTrace();}
		}

		if (e instanceof OutOfMemoryError)
			screen = new ScreenOutOfMemory();
		else
			screen = new ScreenCrashed();

		try
		{
			if (Crusade.currentCrusade != null && !ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
			{
				Crusade.currentCrusade.crusadePlayers.get(Game.player).saveCrusade();
			}
		}
		catch (Exception e1)
		{
			e1.printStackTrace(Game.logger);
			e1.printStackTrace();
		}

		if (Game.game.window != null)
			Drawing.drawing.playSound("leave.ogg");
		else
			throw new RuntimeException("Failed to start game", e);
	}

	public static void resetTiles()
	{
		Drawing.drawing.setScreenBounds(Game.tile_size * 28, Game.tile_size * 18);

        tileOffsetX = 0;
        tileOffsetY = 0;

		Chunk.reset();

		Level.currentColorR = 235;
		Level.currentColorG = 207;
		Level.currentColorB = 166;

		Level.currentColorVarR = 20;
		Level.currentColorVarG = 20;
		Level.currentColorVarB = 20;

		Level.currentLightIntensity = 1.0;
		Level.currentShadowIntensity = 0.75;
	}

	public static Obstacle getObstacle(int tileX, int tileY)
	{
		Chunk.Tile t = Chunk.getTile(tileX, tileY);
		return t != null ? t.obstacle : null;
	}

	public static Obstacle getSurfaceObstacle(int tileX, int tileY)
	{
		Chunk.Tile t = Chunk.getTile(tileX, tileY);
		return t != null ? t.surfaceObstacle : null;
	}

	public static Obstacle getExtraObstacle(int tileX, int tileY)
	{
		Chunk.Tile t = Chunk.getTile(tileX, tileY);
		return t != null ? t.extraObstacle : null;
	}

	public static Obstacle getObstacle(double posX, double posY)
	{
		return getObstacle((int) (posX / Game.tile_size), (int) (posY / Game.tile_size));
	}

	public static Obstacle getSurfaceObstacle(double posX, double posY)
	{
		return getSurfaceObstacle((int) (posX / Game.tile_size), (int) (posY / Game.tile_size));
	}

	public static Obstacle getExtraObstacle(double posX, double posY)
	{
		return getExtraObstacle((int) (posX / Game.tile_size), (int) (posY / Game.tile_size));
	}

	private static final ObjectArrayList<Movable> movableOut = new ObjectArrayList<>();
	private static final ObjectArrayList<Obstacle> obstacleOut = new ObjectArrayList<>();

	/** Expects all pixel coordinates.
	 * @return all the movables within the specified range */
    public static ObjectArrayList<Movable> getMovablesInRange(double x1, double y1, double x2, double y2)
	{
		movableOut.clear();
        for (Chunk c : Chunk.getChunksInRange(x1, y1, x2, y2))
        {
            for (Movable o : c.movables)
            {
                if (Game.isOrdered(true, x1, o.posX, x2) && Game.isOrdered(true, x2, o.posY, y2))
                    movableOut.add(o);
            }
        }
        return movableOut;
	}

	/** Expects all pixel coordinates.
	 * @return all the movables within a certain radius of the position */
	public static ObjectArrayList<Movable> getMovablesInRadius(double posX, double posY, double radius)
	{
		movableOut.clear();
		for (Chunk c : Chunk.getChunksInRadius(posX, posY, radius))
            for (Movable o : c.movables)
                if (Movable.sqDistBetw(o.posX, o.posY, posX, posY) < radius * radius)
                    movableOut.add(o);
		return movableOut;
	}

	/** Expects all pixel coordinates.
	 * @return all the obstacles within the specified range */
	public static ObjectArrayList<Obstacle> getObstaclesInRange(double x1, double y1, double x2, double y2)
	{
		obstacleOut.clear();
		for (Chunk c : Chunk.getChunksInRange(x1, y1, x2, y2))
		{
			for (Obstacle o : c.obstacles)
			{
				if (Game.isOrdered(true, x1, o.posX, x2) && Game.isOrdered(true, x2, o.posY, y2))
					obstacleOut.add(o);
			}
		}
		return obstacleOut;
	}

	/** Expects all pixel coordinates.
	 * @return all the obstacles within a certain radius of the position */
	public static ObjectArrayList<Obstacle> getObstaclesInRadius(double posX, double posY, double radius)
	{
		obstacleOut.clear();
		for (Chunk c : Chunk.getChunksInRadius(posX, posY, radius))
		{
			for (Obstacle o : c.obstacles)
				if (Movable.sqDistBetw(o.posX, o.posY, posX, posY) < radius * radius)
					obstacleOut.add(o);
		}
		return obstacleOut;
	}

	public static void removeObstacle(Obstacle o)
	{
		Chunk c = Chunk.getChunk(o.posX, o.posY);
        if (c == null)
            return;

		if (o instanceof IAvoidObject)
			IAvoidObject.avoidances.remove(o);

        c.removeObstacleIfEquals(o);
		c.removeSurfaceIfEquals(o);
		c.removeExtraIfEquals(o);

		redraw(o);

		for (Obstacle o1 : o.getNeighbors())
			o1.onNeighborUpdate();

		Game.obstacles.remove(o);
    }

	public static void redraw(Obstacle o)
	{
		int x = (int) (o.posX / Game.tile_size);
		int y = (int) (o.posY / Game.tile_size);

		if (x >= 0 && y >= 0 && x < Game.currentSizeX && y < Game.currentSizeY)
			Game.redrawGroundTiles.add(new GroundTile(x, y));
	}

	public static boolean isSolid(int tileX, int tileY)
	{
		Chunk.Tile t = Chunk.getTile(tileX, tileY);
		return t != null && t.solid();
	}

	public static boolean isSolid(double posX, double posY)
	{
		Chunk.Tile t = Chunk.getTile(posX, posY);
		return t != null && t.solid();
	}

	public static double getTileHeight(double posX, double posY)
	{
		Chunk.Tile t = Chunk.getTile(posX, posY);
		return t != null ? t.height() : 0;
	}

	public static void removeSurfaceObstacle(Obstacle o)
	{
		Chunk c = Chunk.getChunk(o.posX, o.posY);
		if (c != null)
			c.removeSurfaceIfEquals(o);
		Game.obstacles.remove(o);
	}

	public static void setObstacle(double posX, double posY, Obstacle o)
	{
		Chunk c = Chunk.getChunk(posX, posY);
		if (c != null)
			c.setObstacle(Chunk.pixelToPosInChunk(posX), Chunk.pixelToPosInChunk(posY), o);
	}

	public static double sampleGroundHeight(double px, double py)
	{
		int x = (int) (px / Game.tile_size);
		int y = (int) (py / Game.tile_size);

		if (!Game.enable3dBg || !Game.enable3d || x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY)
			return 0;

		Chunk.Tile t = Chunk.getTile(x, y);
        assert t != null;
        return t.groundHeight() + t.depth;
    }

	/** @return The depth that the tile renders with; not affected by obstacles */
	public static double sampleDefaultGroundHeight(double px, double py)
	{
		int x = (int) (px / Game.tile_size);
		int y = (int) (py / Game.tile_size);

		if (px < 0)
			x--;

		if (py < 0)
			y--;

		if (!Game.fancyTerrain || !Game.enable3d || x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY)
			return 0;

		return Objects.requireNonNull(Chunk.getTile(x, y)).depth;
	}

	public static double sampleObstacleHeight(double px, double py)
	{
		int x = (int) (px / Game.tile_size);
		int y = (int) (py / Game.tile_size);

		if (px < 0)
			x--;

		if (py < 0)
			y--;

		double r;
		if (!Game.fancyTerrain || !Game.enable3d || x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY)
			r = 0;
		else
			r = Game.getTileHeight(px, py);

		return r;
	}

	public static void loadTankMusic()
	{
		if (!Game.game.window.soundsEnabled)
			return;

		ArrayList<String> music = Game.game.fileManager.getInternalFileContents("/music/tank/tank_music.txt");

		HashSet<String> loadedMusics = new HashSet<>();
		for (String s: music)
		{
			String[] sections = s.split("=");

			if (sections.length < 2)
				continue;

			String tank = sections[0];
			String[] musics = sections[1].split(",");

			for (String track: musics)
			{
				if (!loadedMusics.contains(track))
				{
					Game.game.window.soundPlayer.loadMusic("/music/" + track);
					loadedMusics.add(track);
				}

				registerTankMusic(tank, track);
			}
		}
	}

	public static void registerTankMusic(String tank, String track)
	{
		if (!Game.registryTank.tankMusics.containsKey(tank))
			Game.registryTank.tankMusics.put(tank, new HashSet<>());

		Game.registryTank.tankMusics.get(tank).add(track);
	}

	public static double[] getRainbowColor(double fraction)
    {
		fraction = ((fraction % 1.0) + 1) % 1.0;

        double col = fraction * 255 * 6;

        double r = 0;
        double g = 0;
        double b = 0;

        if (col <= 255)
        {
            r = 255;
            g = col;
            b = 0;
        }
        else if (col <= 255 * 2)
        {
            r = 255 * 2 - col;
            g = 255;
            b = 0;
        }
        else if (col <= 255 * 3)
        {
            g = 255;
            b = col - 255 * 2;
        }
        else if (col <= 255 * 4)
        {
            g = 255 * 4 - col;
            b = 255;
        }
        else if (col <= 255 * 5)
        {
            r = col - 255 * 4;
            g = 0;
            b = 255;
        }
        else if (col <= 255 * 6)
        {
            r = 255;
            g = 0;
            b = 255 * 6 - col;
        }

        return Team.setTeamColor(color, r, g, b);
    }

	public static void exitToTitle()
	{
		cleanUp();
		Panel.panel.zoomTimer = 0;
		screen = new ScreenTitle();
		System.gc();
	}

	public static void cleanUp()
	{
		resetTiles();
		Game.currentLevel = null;
		silentCleanUp();
	}

	public static void silentCleanUp()
	{
		if (Game.currentLevel != null)
			Chunk.populateChunks(Game.currentLevel);

		obstacles.clear();
		tracks.clear();
		movables.clear();
		effects.clear();
		clouds.clear();
		recycleEffects.clear();
		removeEffects.clear();
		removeTracks.clear();
		removeClouds.clear();
		updateObstacles.clear();
		checkUpdateObstacles.clear();

		IAvoidObject.avoidances.clear();

		resetNetworkIDs();

		Game.player.hotbar.coins = 0;
		Game.player.hotbar.enabledCoins = false;
		Game.player.hotbar.itemBar = new ItemBar(Game.player);
		Game.player.hotbar.itemBar.showItems = false;
		Game.player.ownedBuilds = new HashSet<>();
		Game.player.buildName = "player";

		//if (Game.game.window != null)
		//	Game.game.window.setShowCursor(false);
	}

	public static void resetNetworkIDs()
	{
		Tank.currentID = 0;
		Tank.idMap.clear();
		Tank.freeIDs.clear();

		Bullet.currentID = 0;
		Bullet.idMap.clear();
		Bullet.freeIDs.clear();

		Mine.currentID = 0;
		Mine.idMap.clear();
		Mine.freeIDs.clear();
	}

	public static boolean loadLevel(BaseFile f)
	{
		return Game.loadLevel(f, null);
	}

	public static boolean loadLevel(BaseFile f, ILevelPreviewScreen s)
	{
		StringBuilder line = new StringBuilder();
		try
		{
			f.startReading();

			while (f.hasNextLine())
			{
				line.append(f.nextLine()).append("\n");
			}

			Level l = new Level(line.substring(0, line.length() - 1));
			l.loadLevel(s);

			f.stopReading();
			return true;
		}
		catch (Exception e)
		{
			Game.screen = new ScreenFailedToLoadLevel(f.path, line.toString(), e, Game.screen);
			return false;
		}
	}

	public static int compareVersions(String v1, String v2)
	{
		String[] a = v1.substring(v1.indexOf(" v") + 2).split("\\.");
		String[] b = v2.substring(v2.indexOf(" v") + 2).split("\\.");

		for (int i = 0; i < Math.max(a.length, b.length); i++)
		{
			String a1 = "0";
			String b1 = "0";

			if (i < a.length)
				a1 = a[i];

			if (i < b.length)
				b1 = b[i];

			StringBuilder na = new StringBuilder("0");
			StringBuilder nb = new StringBuilder("0");
			StringBuilder la = new StringBuilder();
			StringBuilder lb = new StringBuilder();

			for (int j = 0; j < a1.length(); j++)
			{
				if ("0123456789".indexOf(a1.charAt(j)) != -1)
					na.append(a1.charAt(j));
				else
					la.append(a1.charAt(j));
			}

			for (int j = 0; j < b1.length(); j++)
			{
				if ("0123456789".indexOf(b1.charAt(j)) != -1)
					nb.append(b1.charAt(j));
				else
					lb.append(b1.charAt(j));
			}

			int ia = Integer.parseInt(na.toString());
			int ib = Integer.parseInt(nb.toString());

			if (ia != ib)
				return ia - ib;
			else if ((la.toString().isEmpty() || lb.toString().isEmpty()) && la.toString().length() + lb.toString().length() > 0)
				return lb.toString().length() - la.toString().length();
			else if (la.toString().length() != lb.toString().length())
				return la.toString().length() - lb.toString().length();
			else if (!la.toString().contentEquals(lb))
				return la.toString().compareTo(lb.toString());
		}

		return 0;
	}

	public static void loadRandomLevel()
	{
		loadRandomLevel(-1);
	}

	public static void loadRandomLevel(int seed)
	{
		//Level level = new Level("{28,18|4...11-6,11-0...5,17...27-6,16-3...6,0...10-11,11-11...14,16...23-11,16-12...17|3-15-player,7-3-purple2-2,20-14-green,22-3-green-2,8-8.5-brown,19-8.5-mint-2,13.5-5-yellow-1}");
		//Level level = new Level("{28,18|4...11-6,11-0...5,17...27-6,16-3...6,0...10-11,11-11...14,16...23-11,16-12...17|3-15-player,7-3-green-2,20-14-green,22-3-green-2,8-8.5-green,19-8.5-green-2,13.5-5-green-1}");

		//System.out.println(LevelGenerator.generateLevelString());
		//Game.currentLevel = "{28,18|0-17,1-16,2-15,3-14,4-13,5-12,6-11,7-10,10-7,12-5,15-2,16-1,17-0,27-0,26-1,25-2,24-3,23-4,22-5,21-6,20-7,17-10,15-12,12-15,11-16,10-17,27-17,26-16,25-15,24-14,23-13,22-12,21-11,20-10,17-7,15-5,12-2,11-1,10-0,0-0,1-1,3-3,2-2,4-4,5-5,6-6,7-7,10-10,12-12,15-15,16-16,17-17,11-11,16-11,16-6,11-6|0-8-player-0,13-8-magenta-1,14-9-magenta-3,12-10-yellow-0,15-7-yellow-2,13-0-mint-1,14-17-mint-3,27-8-mint-2,27-9-mint-2}";///LevelGenerator.generateLevelString();
		Level level = new Level(LevelGeneratorRandom.generateLevelString(seed));
		//Level level = new Level("{28,18|3...6-3...4,3...4-5...6,10...19-13...14,18...19-4...12|22-14-player,14-10-brown}");
		//Level level = new Level("{28,18|0...27-9,0...27-7|2-8-player,26-8-purple2-2}");
		level.loadLevel();
	}

	public static String readVersionFromFile()
	{
		ArrayList<String> version = Game.game.fileManager.getInternalFileContents("/version.txt");
		if (version == null)
			return "-1.-1.-1";
		else
			return version.get(0);
	}

	public static String readHashFromFile()
	{
		ArrayList<String> hash = Game.game.fileManager.getInternalFileContents("/hash.txt");
		if (hash == null)
			return "";
		else
			return hash.get(0);
	}

	public static boolean isOrdered(double a, double b, double c)
	{
		return (a < b && b < c) || (c < b && b < a);
	}

	public static boolean isOrdered(boolean orEqualTo, double a, double b, double c)
	{
		if (!orEqualTo)
			return isOrdered(a, b, c);

		return (a <= b && b <= c) || (c <= b && b <= a);
	}
}
