package tanks.gui.screen;

import basewindow.InputCodes;
import basewindow.InputPoint;
import basewindow.transformation.RotationAboutPoint;
import basewindow.transformation.ScaleAboutPoint;
import basewindow.transformation.Translation;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.bullet.BulletArc;
import tanks.generator.LevelGeneratorVersus;
import tanks.gui.*;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.hotbar.Hotbar;
import tanks.hotbar.ItemBar;
import tanks.item.Item;
import tanks.item.ItemBullet;
import tanks.item.ItemRemote;
import tanks.minigames.Minigame;
import tanks.network.Client;
import tanks.network.ConnectedPlayer;
import tanks.network.event.*;
import tanks.obstacle.Face;
import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleStackable;
import tanks.tank.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class ScreenGame extends Screen implements IHiddenChatboxScreen, IPartyGameScreen
{
	public static double sensitivity = 0.2;

	public boolean playing = false;
	public boolean paused = false;
	public boolean savedRemainingTanks = false;

	public boolean playingReplay = false;

	public boolean shopScreen = false;
	public boolean npcShopScreen = false;
	public boolean buildsScreen = false;

	public double slant = 0;

	public double deadTime = 0;
	public double deadTimeToSpectate = 400;

	public static boolean finishedQuick = false;
	public static boolean finished = false;
	public static double finishTimer = 100;
	public static double finishTimerMax = 100;
	public double finishQuickTimer = 0;

	public double age = 0;

	public boolean cancelCountdown = false;
	public String name = null;

	public static boolean newItemsNotification = false;
	public static boolean newBuildsNotification = false;

	public static String lastShop = "";
	public static String lastBuilds = "";

	public ArrayList<Item.ShopItem> shop = new ArrayList<>();
	public ArrayList<TankPlayer.ShopTankBuild> builds = new ArrayList<>();
	public boolean screenshotMode = false;

	public Tutorial tutorial;

	public boolean ready = false;
	public double readyNameSpacing = 10;
	public double lastNewReadyName = readyNameSpacing;
	public int readyNamesCount = 0;
	public int prevReadyNames = 0;
	public ArrayList<ConnectedPlayer> readyPlayers = new ArrayList<>();
	public ArrayList<ConnectedPlayer> eliminatedPlayers = new ArrayList<>();
	public OverlayPlayerRankings rankingsOverlay = new OverlayPlayerRankings(this);
	public boolean showRankings = false;
	public double rankingsTimeIntro = -280;
	public double rankingsTime = rankingsTimeIntro;
	public boolean isVersus = false;

	public static boolean versus = false;
	public String title = "";
	public String subtitle = "";

	public long introMusicEnd;
	public long introBattleMusicEnd;
	public long introResultsMusicEnd;

	public RotationAboutPoint slantRotation;
	public Translation slantTranslation;

	public Tank spectatingTank = null;

	public double readyPanelCounter = 0;
	public double playCounter = 0;

	public double timeRemaining;
	public double timePassed = 0;
	public static double lastTimePassed = 0;

	public double prevCursorX;
	public double prevCursorY;

	public double shrubberyScale = 0.25;

	public ScreenInfo overlay = null;
	public Minimap minimap = new Minimap();

	public HashSet<String> prevTankMusics = new HashSet<>();
	public HashSet<String> tankMusics = new HashSet<>();
	protected boolean musicStarted = false;
	protected float pausedMusicPos = 0;

	public boolean zoomPressed, zoomScrolled;

	public static boolean fcZoomPressed = false;
	public static double fcZoom, fcTargetZoom;
	public double fcZoomLastTap, fcPitch;
	public boolean selectedArcBullet = false;
	public double fcArcAim;

	public static boolean controlPlayer = false;
	public boolean freecam = false;
	public double x, y, z, yaw, pitch, pitchAdd, roll;

	public boolean playedIntro = false;

	protected static String[] ready_musics =
			{"piano.ogg", "synth.ogg", "bass-guitar.ogg", "drum.ogg", "beep.ogg",
					"bass.ogg", "cello.ogg", "chime.ogg", "drum2.ogg", "drum3.ogg",
					"drum4.ogg", "echo-piano.ogg", "pizzicato-violin.ogg", "strings.ogg", "viola-beep.ogg",
					"violin.ogg", "violin-beep.ogg"};
	protected static int[][] intro_order = {{0, 1, 2, 3, 4},  {6, 8, 12, 14},  {5, 9, 11, 13, 16},  {7, 10, 15}};
	protected ArrayList<Integer> playingReadyMusics = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4));
	protected float lastReadyMusicTime = -2;
	protected int readyMusicIterations = 0;
	protected int specialReadyMusicIterationsLeft = 0;
	public String specialReadyMusic = null;

    public ObjectArrayList<Button> pauseMenuButtons = new ObjectArrayList<>();

	@SuppressWarnings("unchecked")
	public ArrayList<IDrawable>[] drawables = (ArrayList<IDrawable>[])(new ArrayList[10]);

	Button play = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 50, 350, 40, "Play", () ->
	{
		playing = true;
		Game.playerTank.setBufferCooldown(null, 20);
	}
	);

	Button readyButton = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 50, 350, 40, "Ready", () ->
	{
		if (ScreenPartyLobby.isClient)
			Game.eventsOut.add(new EventPlayerReady());
		else
		{
			ScreenPartyHost.readyPlayers.add(Game.player);
			Game.eventsOut.add(new EventUpdateReadyPlayers(ScreenPartyHost.readyPlayers));
		}
		ready = true;
	}
	);

	Button startNow = new Button( 200, Drawing.drawing.interfaceSizeY - 50, 350, 40, "Start now", () ->
	{
		if (ScreenPartyHost.isServer)
		{
			for (Player p: Game.players)
			{
				if (!ScreenPartyHost.readyPlayers.contains(p) && ScreenPartyHost.includedPlayers.contains(p.clientID))
					ScreenPartyHost.readyPlayers.add(p);
			}

			Game.eventsOut.add(new EventUpdateReadyPlayers(ScreenPartyHost.readyPlayers));
		}
		ready = true;
	}
	);

	Button enterShop = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 110, 350, 40, "Shop", new Runnable()
	{
		@Override
		public void run()
		{
			if (shopList != null)
			{
				newItemsNotification = false;
				cancelCountdown = true;
				shopScreen = true;
			}
		}
	}, "New items available in shop!"
	);

	Button viewBuilds = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 110, 350, 40, "Change tank build", () ->
	{
		newBuildsNotification = false;
		cancelCountdown = true;
		buildsScreen = true;
	}, "New builds available!");

	Button pause = new Button(0, -1000, 70, 70, "", this::pause);

	Button zoom = new Button(0, -1000, 70, 70, "", () ->
	{
		Panel.autoZoom = false;
		Panel.zoomTarget = -1;
		Drawing.drawing.movingCamera = !Drawing.drawing.movingCamera;
	});

	Button zoomAuto = new Button(0, -1000, 70, 70, "", () ->
	{
		Panel.autoZoom = !Panel.autoZoom;
		if (!Panel.autoZoom)
			Panel.zoomTarget = -1;
	});

	Button resume = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Resume", this::unpause);

	Button resumeLowerPos = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace, this.objWidth, this.objHeight, "Resume", this::unpause);

	Button closeMenu = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Close menu", this::unpause);

	Button closeMenuLowerPos = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace, this.objWidth, this.objHeight, "Close menu", this::unpause);

	Button closeMenuClient = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace / 2, this.objWidth, this.objHeight, "Close menu", this::unpause);

	Button newLevel = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace / 2, this.objWidth, this.objHeight, "Generate new level", () ->
	{
		playing = false;
		paused = false;

		if (ScreenPartyHost.isServer)
		{
			ready = false;
			readyButton.enabled = true;
			cancelCountdown = true;
			ScreenPartyHost.readyPlayers.clear();
			ScreenPartyHost.includedPlayers.clear();
		}

		if (versus)
		{
			Game.cleanUp();
			new Level(LevelGeneratorVersus.generateLevelString()).loadLevel();
		}
		else
		{
			Game.cleanUp();
			Game.loadRandomLevel();
		}

		Game.startTime = Game.currentLevel.startTime;
		Game.screen = new ScreenGame();
	}
	);

	Button restart = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace / 2, this.objWidth, this.objHeight, "Restart level", () ->
	{
		playing = false;
		paused = false;

		if (ScreenPartyHost.isServer)
		{
			ready = false;
			readyButton.enabled = true;
			cancelCountdown = true;
			ScreenPartyHost.readyPlayers.clear();
			ScreenPartyHost.includedPlayers.clear();
		}

		Game.silentCleanUp();

		if (!(Game.currentLevel instanceof Minigame))
		{
			Level level = new Level(Game.currentLevelString);
			level.loadLevel();
		}
		else
		{
			try
			{
				Game.currentLevel = Game.currentLevel.getClass().getConstructor().newInstance();
				Game.currentLevel.loadLevel();
			}
			catch (Exception e)
			{
				Game.exitToCrash(e);
			}
		}

		Game.screen = new ScreenGame(name);
	}
	);

	Button restartLowerPos = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart level", () ->
            restart.function.run()
	);

	Button restartTutorial = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart tutorial", () ->
	{
		Game.silentCleanUp();
		new Tutorial().loadTutorial(ScreenInterlevel.tutorialInitial, Game.game.window.touchscreen);
	}
	);

	Button edit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace / 2, this.objWidth, this.objHeight, "Edit the level", () ->
	{
		Game.cleanUp();
		ScreenLevelEditor s = new ScreenLevelEditor(name, Game.currentLevel);
		Game.loadLevel(Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + name), s);
		Game.screen = s;
	}
	);

	Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Quit", () ->
	{
		Game.cleanUp();
		Panel.panel.zoomTimer = 0;
		Game.screen = new ScreenPlaySingleplayer();
	}
	);

	Button quitHigherPos = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Quit", () ->
	{
		Game.cleanUp();
		Panel.panel.zoomTimer = 0;
		Game.screen = new ScreenPlaySingleplayer();
		ScreenInterlevel.tutorial = false;
	}
	);

	Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Back to my levels", () ->
	{
		Game.cleanUp();
		System.gc();
		Panel.panel.zoomTimer = 0;

		if (ScreenInterlevel.fromMinigames)
			Game.screen = new ScreenMinigames();
		else
			Game.screen = new ScreenPlaySavedLevels();

		ScreenInterlevel.fromSavedLevels = false;
		ScreenInterlevel.fromMinigames = false;

		if (ScreenPartyHost.isServer)
		{
			ScreenPartyHost.readyPlayers.clear();
			ScreenPartyHost.includedPlayers.clear();
			Game.eventsOut.add(new EventReturnToLobby());
		}
	}
	);

	public void exitQuickPlay()
	{
		Game.cleanUp();
		System.gc();
		Panel.panel.zoomTimer = 0;

		Level level = new Level(Game.currentLevelString);
		level.loadLevel(ScreenInterlevel.fromQuickPlay);
		Game.screen = (Screen) ScreenInterlevel.fromQuickPlay;

		ScreenInterlevel.fromSavedLevels = false;
		ScreenInterlevel.fromMinigames = false;
		ScreenInterlevel.fromQuickPlay = null;

		if (ScreenPartyHost.isServer)
		{
			ScreenPartyHost.readyPlayers.clear();
			ScreenPartyHost.includedPlayers.clear();
		}
	}


	Button backToQuickPlay = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Back", () ->
	{
		exitQuickPlay();

		if (ScreenPartyHost.isServer)
			Game.eventsOut.add(new EventReturnToLobby());
	}
	);

	Button quitPartyGame = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Back to party", () ->
	{
		Game.cleanUp();
		System.gc();
		Panel.panel.zoomTimer = 0;
		Game.screen = ScreenPartyHost.activeScreen;
		ScreenPartyHost.readyPlayers.clear();
		ScreenPartyHost.includedPlayers.clear();
		Game.eventsOut.add(new EventReturnToLobby());
		versus = false;
	}
	);

	Button exitParty = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace / 2, this.objWidth, this.objHeight, "Leave party", () ->
	{
		Game.cleanUp();
		System.gc();
		Panel.panel.zoomTimer = 0;
		Drawing.drawing.playSound("leave.ogg");
		ScreenPartyLobby.isClient = false;
		Game.screen = new ScreenJoinParty();

		Client.handler.close();

		ScreenPartyLobby.connections.clear();
	}
	);

	Button quitCrusade = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Quit", () ->
	{
		Crusade.currentCrusade.quit();
		Game.cleanUp();
		Panel.panel.zoomTimer = 0;
		Game.screen = new ScreenPlaySingleplayer();
	}
			, "Note! You will lose a life for quitting---in the middle of a level------Your crusade progress will be saved.");

	Button quitCrusadeFinalLife = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Quit", () ->
	{
		Crusade.currentCrusade.quit();
		Game.cleanUp();
		Panel.panel.zoomTimer = 0;
		Game.screen = new ScreenPlaySingleplayer();
	}
			, "Note! You will lose a life for quitting---in the middle of a level------Since you do not have any other lives left,---your progress will be lost!");

	Button restartCrusade = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart the level", () ->
	{
		playing = false;
		paused = false;

		if (!finishedQuick)
		{
			for (int i = 0; i < Game.movables.size(); i++)
			{
				if (Game.movables.get(i) instanceof IServerPlayerTank && !Game.movables.get(i).destroy)
					((IServerPlayerTank) Game.movables.get(i)).getPlayer().remainingLives--;
			}
		}

		if (ScreenPartyHost.isServer)
		{
			ready = false;
			readyButton.enabled = true;
			cancelCountdown = true;
			ScreenPartyHost.readyPlayers.clear();
			ScreenPartyHost.includedPlayers.clear();
		}

		Crusade.currentCrusade.recordPerformance(ScreenGame.lastTimePassed, false);

		Crusade.currentCrusade.retry = true;

		this.saveRemainingTanks();

		Crusade.currentCrusade.saveHotbars();
		Crusade.currentCrusade.crusadePlayers.get(Game.player).saveCrusade();
		Game.silentCleanUp();

		Crusade.currentCrusade.loadLevel();
		ScreenGame s = new ScreenGame(Crusade.currentCrusade);
		s.name = name;
		Game.screen = s;
	}
			, "Note! You will lose a life for restarting!");

	Button restartCrusadeFinalLife = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart the level",
			"You can't restart the level because---you have only one life left!");

	Button quitCrusadeParty = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Back to party", () ->
	{
		Crusade.currentCrusade.retry = true;
		Crusade.currentCrusade.quit();
		Panel.panel.zoomTimer = 0;
		Game.cleanUp();

		Game.screen = ScreenPartyHost.activeScreen;
		ScreenPartyHost.readyPlayers.clear();
		ScreenPartyHost.includedPlayers.clear();
		Game.eventsOut.add(new EventReturnToLobby());
	}
			, "Note! All players will lose a life for---quitting in the middle of a level.");


	Button restartCrusadeParty = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart the level", () ->
	{
		if (!finishedQuick)
		{
			for (int i = 0; i < Game.movables.size(); i++)
			{
				if (Game.movables.get(i) instanceof IServerPlayerTank && !Game.movables.get(i).destroy)
					((IServerPlayerTank) Game.movables.get(i)).getPlayer().remainingLives--;
			}
		}

		playing = false;
		paused = false;

		ready = false;
		readyButton.enabled = true;
		cancelCountdown = true;

		Crusade.currentCrusade.recordPerformance(ScreenGame.lastTimePassed, false);

		Crusade.currentCrusade.retry = true;

		this.saveRemainingTanks();

		Panel.panel.zoomTimer = 0;
		Game.silentCleanUp();
		System.gc();
		ScreenPartyHost.readyPlayers.clear();
		ScreenPartyHost.includedPlayers.clear();

		Crusade.currentCrusade.loadLevel();
		Game.screen = new ScreenGame(Crusade.currentCrusade);
	}
			, "Note! All players will lose a life for---restarting in the middle of a level.");

	Button restartCrusadePartyFinalLife = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Restart the level",
			"You can't restart the level because---nobody has more than one life left!");

	Button quitCrusadePartyFinalLife = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Back to party", () ->
	{
		Crusade.currentCrusade.retry = true;
		Crusade.crusadeMode = false;
		Crusade.currentCrusade = null;

		Panel.panel.zoomTimer = 0;
		Game.cleanUp();
		System.gc();
		Game.screen = ScreenPartyHost.activeScreen;
		ScreenPartyHost.readyPlayers.clear();
		ScreenPartyHost.includedPlayers.clear();
		Game.eventsOut.add(new EventReturnToLobby());
	}
			, "Note! All players will lose a life for---quitting in the middle of a level.------Since nobody has any other lives left,---the crusade will end!");


	public static double shopOffset = -25;

	Button exitShop = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300 + shopOffset, 350, 40, "Exit shop", () ->
	{
		shopScreen = false;
		npcShopScreen = false;
		buildsScreen = false;
	}

	);

	public ArrayList<Button> shopItemButtons = new ArrayList<>();
	public ArrayList<Button> playerBuildButtons = new ArrayList<>();

	public ButtonList shopList;
	public ButtonList playerBuildsList;

	public ButtonList npcShopList = new ButtonList(new ArrayList<>(), 0, 0, (int) shopOffset, -30);
	public double gameAge = 0;
	public boolean playSounds = true;
	public boolean shouldExit = true;

	public ScreenGame()
	{
		Game.player.hotbar.resetTimers();
		eliminatedPlayers.clear();
		this.selfBatch = false;
		this.enableMargins = !Game.followingCam;

		introMusicEnd = Long.parseLong(Game.game.fileManager.getInternalFileContents("/music/ready_music_intro_length.txt").get(0));
		introBattleMusicEnd = Long.parseLong(Game.game.fileManager.getInternalFileContents("/music/battle_intro_length.txt").get(0));
		introResultsMusicEnd = Long.parseLong(Game.game.fileManager.getInternalFileContents("/music/finished_music_intro_length.txt").get(0));

		if (Game.framework == Game.Framework.libgdx)
			introBattleMusicEnd -= 40;

		this.drawDarkness = false;

		Game.clouds.clear();
		for (int i = 0; i < (Game.currentSizeX + Game.currentSizeY) / 2; i++)
			Game.clouds.add(new Cloud((Math.random() - 0.5) * Game.currentSizeX * 1.5 * Game.tile_size, (Math.random() - 0.5) * Game.currentSizeY * 1.5 * Game.tile_size));

		if (Game.currentLevel instanceof Minigame && !(Game.currentLevel instanceof Tutorial))
		{
			ScreenInterlevel.fromMinigames = true;
			ScreenInterlevel.fromSavedLevels = false;
			back.setText("Back to minigames");
		}
		else
		{
			ScreenInterlevel.fromMinigames = false;
			ModAPI.menuGroup.clear();
		}

		Game.startTime = Game.currentLevel.startTime;
		ScreenGame.lastTimePassed = 0;

		if (ScreenPartyHost.isServer || ScreenPartyLobby.isClient)
		{
			this.music = "waiting_music.ogg";
			cancelCountdown = true;
		}

		ScreenGame.finishTimer = ScreenGame.finishTimerMax;

		for (int i = 0; i < this.drawables.length; i++)
            this.drawables[i] = new ArrayList<>();

		slantRotation = new RotationAboutPoint(Game.game.window, 0, 0, 0, 0, 0.5, -1);
		slantTranslation = new Translation(Game.game.window, 0, 0, 0);

		if (ScreenPartyLobby.isClient && !this.shop.isEmpty())
            this.viewBuilds.posY -= 60;

		if (!Crusade.crusadeMode)
		{
			boolean shop = false;
			boolean startingItems = false;

			if (!Game.currentLevel.shop.isEmpty())
			{
				shop = true;
				this.initShop(Game.currentLevel.shop);
			}

			this.initBuilds(Game.currentLevel.playerBuilds);

			for (TankPlayer.ShopTankBuild b: Game.currentLevel.playerBuilds)
			{
				if (b.price > 0)
				{
					shop = true;
					break;
				}
			}

			if (!Game.currentLevel.startingItems.isEmpty())
			{
				startingItems = true;
			}

			for (Player p: Game.players)
			{
				p.hotbar.itemBar = new ItemBar(p);
				p.hotbar.itemBar.showItems = false;
				p.hotbar.enabledCoins = false;

				p.ownedBuilds = new HashSet<>();
				p.ownedBuilds.add(Game.currentLevel.playerBuilds.get(0).name);

				if (startingItems)
				{
					for (Item.ItemStack<?> i: Game.currentLevel.startingItems)
						p.hotbar.itemBar.addItem(i);

					p.hotbar.itemBar.showItems = true;
				}

				if (shop)
				{
					p.hotbar.enabledCoins = true;
					p.hotbar.coins = Game.currentLevel.startingCoins;
					p.hotbar.itemBar.showItems = true;
					Game.eventsOut.add(new EventUpdateCoins(p));
				}

				if (Game.currentLevel instanceof Minigame && ((Minigame) Game.currentLevel).showItems)
					p.hotbar.itemBar.showItems = true;

				if (p != Game.player)
				{
					Game.eventsOut.add(new EventSetupHotbar(p));
				}
			}

			this.botShopping();
		}

		if (Drawing.drawing.interfaceScaleZoom > 1)
		{
			startNow.sizeX *= 0.7;
			startNow.posX -= 20;
		}

		if (Game.currentLevel != null && Game.currentLevel.timed)
            this.timeRemaining = Game.currentLevel.timer;

		addPauseMenuButtons();

		if (ScreenPartyHost.isServer)
		{
			for (Player p : Game.botPlayers)
			{
				if (ScreenPartyHost.includedPlayers.contains(p.clientID))
					ScreenPartyHost.readyPlayers.add(p);
			}

			Game.eventsOut.add(new EventUpdateReadyPlayers(ScreenPartyHost.readyPlayers));
		}
	}

	public ScreenGame(String s)
	{
		this();
		this.name = s;
		addPauseMenuButtons();
	}

	public ScreenGame(Crusade c)
	{
		this();
		ArrayList<Item.ShopItem> shop = c.getShop();
		this.initShop(shop);
		this.initBuilds(c.getBuildsShop());
		for (int i = 0; i < this.shop.size(); i++)
            Game.currentLevel.itemNumbers.put(this.shop.get(i).itemStack.item.name, i + 1);
		addPauseMenuButtons();
	}

	public void botShopping()
	{
        if (this.shop.isEmpty())
            return;

        for (Player p : Game.botPlayers)
        {
            int j = 0;
            for (int i = 0; i < 5 && j < 100; i++)
            {
                int n = (int) (Math.random() * this.shop.size());
                Item.ShopItem si = this.shop.get(n);
                if (p.hotbar.coins >= si.price && p.hotbar.itemBar.addItem(si.itemStack))
                {
                    p.hotbar.coins -= si.price;
                    if (!Crusade.crusadeMode)
                        i--;

                    j++;
                }
            }
        }
    }

	public void pause()
	{
		this.paused = true;
		this.pausedMusicPos = Game.game.window.soundPlayer.getMusicPos();

		if (Game.followingCam)
			Game.game.window.setCursorPos(Panel.windowWidth / 2, Panel.windowHeight / 2);

		Game.game.window.setShowCursor(!Panel.showMouseTarget);
	}

	public void unpause()
	{
		this.paused = false;
		Game.playerTank.setBufferCooldown(null, 20);
		updateMousePos();

		Game.game.window.setShowCursor(!Game.followingCam && !Panel.showMouseTarget);

		if (Game.currentLevel.synchronizeMusic && !(ScreenPartyHost.isServer || ScreenPartyLobby.isClient) && playing)
			Game.game.window.soundPlayer.setMusicPos(this.pausedMusicPos);
	}

	public void initShop(ArrayList<Item.ShopItem> shop)
	{
		this.shop = shop;

		if (!shop.isEmpty())
			this.viewBuilds.posY -= 60;

		for (int i = 0; i < this.shop.size(); i++)
		{
			final int j = i;
			Item.ShopItem item = this.shop.get(j);
			if (item.itemStack.item instanceof ItemRemote)
				continue;

			Button b = new Button(0, 0, 350, 40, item.itemStack.item.name, () ->
			{
				int pr = shop.get(j).price;
				if (Game.player.hotbar.coins >= pr)
				{
					if (Game.player.hotbar.itemBar.addItem(shop.get(j).itemStack))
						Game.player.hotbar.coins -= pr;
				}
			}
			);

			int p = item.price;

			if (p == 0)
				b.setSubtext("Free!");
			else if (p == 1)
				b.setSubtext("1 coin");
			else
				b.setSubtext("%d coins", p);

			this.shopItemButtons.add(b);

			Game.eventsOut.add(new EventAddShopItem(i, item.itemStack.item.name, b.rawSubtext, p, item.itemStack.item.icon));
		}

		if (Crusade.crusadeMode)
			this.botShopping();

		this.initializeShopList();

		Game.eventsOut.add(new EventSortShopButtons());
	}

	public void initBuilds(ArrayList<TankPlayer.ShopTankBuild> builds)
	{
		if (!lastBuilds.equals(builds.toString()))
			newBuildsNotification = true;

		lastBuilds = builds.toString();

		this.builds = builds;
		for (int i = 0; i < builds.size(); i++)
		{
			TankPlayer.ShopTankBuild t = builds.get(i);
			TankPlayable display = t.clonePropertiesTo(new TankPlayer().setPlayerColor());
			ButtonObject b = getButtonObject(i, display, t);
			this.playerBuildButtons.add(b);
		}

		this.playerBuildsList = new ButtonList(this.playerBuildButtons, 0, 0, 0, 0);
		this.playerBuildsList.buttonWidth = 75;
		this.playerBuildsList.buttonHeight = 75;
		this.playerBuildsList.buttonXSpace = 100;
		this.playerBuildsList.buttonYSpace = 100;
		this.playerBuildsList.horizontalLayout = true;
		this.playerBuildsList.columns = 10;
		this.playerBuildsList.rows = 3;
		this.playerBuildsList.yOffset = -30;
		this.playerBuildsList.controlsYOffset = 40;

		this.playerBuildsList.sortButtons();
	}

	private static ButtonObject getButtonObject(int i, TankPlayable display, TankPlayer.ShopTankBuild t)
	{
        return new ButtonObject(display, 0, 0, 75, 75, () ->
		{
			if (ScreenPartyLobby.isClient)
				Game.eventsOut.add(new EventPlayerSetBuild(i));
			else
			{
				boolean success = false;
				if (Game.player.ownedBuilds.contains(t.name))
					success = true;
				else if (Game.player.hotbar.coins >= t.price)
				{
					Game.player.ownedBuilds.add(t.name);
					Game.player.hotbar.coins -= t.price;
					success = true;
				}

				if (success)
				{
					t.clonePropertiesTo(Game.playerTank);
					Game.player.buildName = t.name;
				}
			}
		}, t.description);
	}

	public void initializeShopList()
	{
		StringBuilder s = new StringBuilder();
		for (Button b: this.shopItemButtons)
			s.append(b.text);

		if (!lastShop.contentEquals(s))
			newItemsNotification = true;

		lastShop = s.toString();

		this.shopList = new ButtonList(this.shopItemButtons, 0, 0, (int) shopOffset, -30);

		if (ScreenPartyLobby.isClient && !this.shop.isEmpty())
			this.viewBuilds.posY -= 60;
	}

	@Override
	public void setupLights()
	{
		setupGameLights();
	}

	public static void setupGameLights()
	{
		for (Obstacle o: Game.obstacles)
		{
			if (o instanceof IDrawableLightSource && ((IDrawableLightSource) o).lit())
			{
				double[] l = ((IDrawableLightSource) o).getLightInfo();
				l[0] = Drawing.drawing.gameToAbsoluteX(o.posX, 0);
				l[1] = Drawing.drawing.gameToAbsoluteY(o.posY, 0);
				l[2] = (o instanceof ObstacleStackable ? ((ObstacleStackable) o).startHeight : 25) * Drawing.drawing.scale;
				Panel.panel.lights.add(l);
			}
		}

		for (Movable o: Game.movables)
		{
			if (o instanceof IDrawableLightSource && ((IDrawableLightSource) o).lit())
			{
				double[] l = ((IDrawableLightSource) o).getLightInfo();
				l[0] = Drawing.drawing.gameToAbsoluteX(o.posX, 0);
				l[1] = Drawing.drawing.gameToAbsoluteY(o.posY, 0);
				l[2] = (o.posZ + 25) * Drawing.drawing.scale;
				Panel.panel.lights.add(l);
			}
		}
    }

    public static void handleRemoveObstacles()
	{
		for (Obstacle o: Game.removeObstacles)
		{
			if (o instanceof IAvoidObject)
				IAvoidObject.avoidances.remove(o);

			o.removed = true;
			Drawing.drawing.terrainRenderer.remove(o);

			if (o.shouldUpdate())
				Game.updateObstacles.remove(o);

			int x = (int) (o.posX / Game.tile_size);
			int y = (int) (o.posY / Game.tile_size);

			if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
			{
				Game.removeObstacle(o);
				Game.removeSurfaceObstacle(o);
			}

			for (Obstacle o1 : o.getNeighbors())
				o1.onNeighborUpdate();

			Game.obstacles.remove(o);
		}

		Game.removeObstacles.clear();
	}

	public void updateFollowingCam()
	{
		if (Game.playerTank == null || Game.playerTank.destroy)
			return;

		ItemBar b = Game.player.hotbar.itemBar;
		selectedArcBullet = b.selected > -1 && b.selected < 5 && b.slots[b.selected].item instanceof ItemBullet && ((ItemBullet) b.slots[b.selected].item).bullet instanceof BulletArc;

		Game.playerTank.angle += (Drawing.drawing.getInterfaceMouseX() - prevCursorX) * sensitivity / 150;
		Game.game.window.setCursorLocked(true);
		if (Game.game.input.tilt.isPressed())
			fcPitch += (Drawing.drawing.getInterfaceMouseY() - this.prevCursorY) * sensitivity * 5e-4;
		else if (selectedArcBullet)
			fcArcAim += (this.prevCursorY - Drawing.drawing.getInterfaceMouseY()) * (sensitivity * 3);
		fcPitch = Math.max(0, Math.min(0.5, fcPitch));
		Game.game.window.setCursorPos(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2);
		updateMousePos();

		if (selectedArcBullet)
		{
			Bullet bullet = ((ItemBullet) b.slots[b.selected].item).bullet;
			fcArcAim = Math.max(bullet.getRangeMin(), Math.min(bullet.getRangeMax() > 0 ? bullet.getRangeMax() : Double.MAX_VALUE, fcArcAim));
		}

		fcZoomPressed = Game.game.input.fcZoom.isPressed();
		if (Math.abs(fcTargetZoom - fcZoom) < 0.05)
			fcZoom = fcTargetZoom;
		else
			fcZoom += (fcTargetZoom - fcZoom) / 10;

		if (fcZoomPressed)
		{
			if (Game.game.input.fcZoom.isValid())
			{
				if (System.currentTimeMillis() - fcZoomLastTap < 500)
					fcTargetZoom = 0;

				fcZoomLastTap = System.currentTimeMillis();
				Game.game.input.fcZoom.invalidate();
			}

			if (Game.game.window.validScrollUp && fcTargetZoom < 0.9)
			{
				fcTargetZoom += 0.1;
				Game.game.window.validScrollUp = false;
			}

			if (Game.game.window.validScrollDown && fcTargetZoom > -0.9)
			{
				fcTargetZoom -= 0.1;
				Game.game.window.validScrollDown = false;
			}
		}
	}

	private void updateMousePos()
	{
		this.prevCursorX = Drawing.drawing.getInterfaceMouseX();
		this.prevCursorY = Drawing.drawing.getInterfaceMouseY();
	}

	public void togglePerspective()
	{
		if (Game.game.window.shift)
            Game.perspectiveID--;
		else if (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_CONTROL))
			Game.perspectiveID = 0;
		else
			Game.perspectiveID++;

		Game.perspectiveID = (Game.perspectiveID + 4) % 4;

		switch (Game.perspectiveID)
		{
			case 0:
				Game.angledView = false;
				Game.followingCam = false;
				Game.firstPerson = false;
				break;
			case 1:
				Game.angledView = true;
				Game.followingCam = false;
				Game.firstPerson = false;
				break;
			case 2:
				Game.angledView = false;
				Game.followingCam = true;
				Game.firstPerson = false;
				break;
			case 3:
				Game.angledView = false;
				Game.followingCam = true;
				Game.firstPerson = true;
				break;
		}

		if (Game.followingCam)
		{
			Drawing.drawing.movingCamera = true;
			Panel.autoZoom = false;
			Panel.zoomTarget = -1;
		}

		this.enableMargins = !Game.followingCam;
		Game.game.input.perspective.invalidate();
	}

	public ObjectArrayList<Team> aliveTeams = new ObjectArrayList<>(), fullyAliveTeams = new ObjectArrayList<>();

	@Override
	public void update()
	{
		if (ScreenPartyHost.isServer && this.shop.isEmpty() && Game.autoReady && !this.ready)
			this.readyButton.function.run();

		if (Game.game.window.pressedKeys.contains(InputCodes.KEY_F7))
		{
			Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_F7);
			controlPlayer = !controlPlayer;
		}

		if (Game.game.input.zoom.isValid())
		{
			zoomScrolled = false;
			zoomPressed = true;
			Game.game.input.zoom.invalidate();
			spectatingTank = null;
		}

		if (Game.game.input.freecam.isValid() && Game.game.window.shift)
		{
			Game.game.input.freecam.invalidate();
			freecam = !freecam;

			if (freecam)
			{
				controlPlayer = false;
				x = -0.08;
				y = 0.22;
				z = -0.07;
				yaw = -0.4;
				pitchAdd = 1;
				roll = -0.27;
			}
		}

		if (playing && !paused)
		{
			if (Game.game.input.zoomIn.isPressed())
			{
				if (Panel.autoZoom)
					Panel.zoomTarget = Panel.panel.zoomTimer;

				Panel.autoZoom = false;
				zoomScrolled = true;
				Drawing.drawing.movingCamera = true;

				if (Panel.zoomTarget == -1)
					Panel.zoomTarget = Panel.panel.zoomTimer;

				Game.game.window.validScrollUp = false;
				Panel.zoomTarget = Math.min(1, Panel.zoomTarget + 0.02 * Panel.frameFrequency * Drawing.drawing.unzoomedScale);
				displayZoomMessage();
			}

			if (Game.game.input.zoomOut.isPressed())
			{
				if (Panel.autoZoom)
					Panel.zoomTarget = Panel.panel.zoomTimer;

				Panel.autoZoom = false;
				zoomScrolled = true;
				Drawing.drawing.movingCamera = true;

				if (Panel.zoomTarget == -1)
					Panel.zoomTarget = Panel.panel.zoomTimer;

				Game.game.window.validScrollDown = false;
				Panel.zoomTarget = Math.max(0, Panel.zoomTarget - 0.02 * Panel.frameFrequency * Drawing.drawing.unzoomedScale);
				displayZoomMessage();
			}

			if (Game.playerTank != null && !Game.playerTank.destroy && Panel.autoZoom)
				Panel.zoomTarget = Game.playerTank.getAutoZoom();

			if (spectatingTank != null && !spectatingTank.destroy && Panel.autoZoom)
				Panel.zoomTarget = spectatingTank.getAutoZoom();
		}

        Tank tank = Game.playerTank;
        if (tank == null || tank.destroy || finishedQuick)
            fcPitch = Math.max(0, fcPitch - 0.02 * Panel.frameFrequency);

		if (Game.game.input.perspective.isValid())
			togglePerspective();

		if (Game.game.input.zoom.isPressed() && playing)
		{
			if (Panel.autoZoom)
				Panel.zoomTarget = Panel.panel.zoomTimer;

			Panel.autoZoom = false;

			if (Game.game.window.validScrollUp)
			{
				zoomScrolled = true;
				Drawing.drawing.movingCamera = true;

				if (Panel.zoomTarget == -1)
					Panel.zoomTarget = Panel.panel.zoomTimer;

				Game.game.window.validScrollUp = false;
				Panel.zoomTarget = Math.min(1, Panel.zoomTarget + 0.1 * Drawing.drawing.unzoomedScale);
			}

			if (Game.game.window.validScrollDown)
			{
				zoomScrolled = true;
				Drawing.drawing.movingCamera = true;

				if (Panel.zoomTarget == -1)
					Panel.zoomTarget = Panel.panel.zoomTimer;

				Game.game.window.validScrollDown = false;
				Panel.zoomTarget = Math.max(0, Panel.zoomTarget - 0.1 * Drawing.drawing.unzoomedScale);
			}

			displayZoomMessage();
		}
		else if (zoomPressed)
		{
			if (!zoomScrolled)
			{
				Drawing.drawing.movingCamera = !Drawing.drawing.movingCamera;
				Panel.zoomTarget = -1;
				spectatingTank = null;
			}

			zoomPressed = false;
		}

		if (Game.game.input.zoomAuto.isValid() && playing)
		{
			if (Panel.autoZoom)
				Panel.zoomTarget = Panel.panel.zoomTimer;

			Game.game.input.zoomAuto.invalidate();
			Panel.autoZoom = !Panel.autoZoom;
			Panel.currentMessage = new ScreenElement.CenterMessage("Auto zoom: " + (Panel.autoZoom ? "on" : "off"), 100);
		}

		Game.player.hotbar.update();
		minimap.update();

		this.showDefaultMouse = !(Game.followingCam && playing && !paused);

		String prevMusic = this.music;
		this.music = null;
		this.musicID = null;

		if (this.playCounter >= 0 && this.playing)
		{
			if (!this.playedIntro)
			{
				this.playedIntro = true;

				if (playSounds)
				{
					if (Game.currentLevel instanceof Minigame && ((Minigame) Game.currentLevel).customIntroMusic)
						Drawing.drawing.playSound(((Minigame) Game.currentLevel).introMusic, 1f, true);
					else if (Game.currentLevel != null && Game.currentLevel.timed)
						Drawing.drawing.playSound("battle_timed_intro.ogg", 1f, true);
					else if (Level.isDark())
						Drawing.drawing.playSound("battle_night_intro.ogg", 1f, true);
					else
						Drawing.drawing.playSound("battle_intro.ogg", 1f, true);
				}

				if (Game.currentLevel.beatBlocks > 0 && Game.enableLayeredMusic)
				{
					Drawing.drawing.playSound("beatblocks/beat_blocks_intro.ogg", 1f, true);

					if ((Game.currentLevel.beatBlocks & 1) != 0)
						Drawing.drawing.playSound("beatblocks/beat_beeps_1_intro.ogg", 1f, true);

					if ((Game.currentLevel.beatBlocks & 2) != 0)
						Drawing.drawing.playSound("beatblocks/beat_beeps_2_intro.ogg", 1f, true);

					if ((Game.currentLevel.beatBlocks & 4) != 0)
						Drawing.drawing.playSound("beatblocks/beat_beeps_4_intro.ogg", 1f, true);

					if ((Game.currentLevel.beatBlocks & 8) != 0)
						Drawing.drawing.playSound("beatblocks/beat_beeps_8_intro.ogg", 1f, true);
				}
			}

			if (!(Game.currentLevel.synchronizeMusic && paused))
				this.playCounter += Panel.frameFrequency;
		}

		if (this.playCounter * 10 >= introBattleMusicEnd)
		{
			Panel.forceRefreshMusic = playSounds;
			this.playCounter = -2;
		}

		if (this.playCounter < 0)
		{
			if (this.showRankings)
			{
				if (rankingsTime * 10 < introResultsMusicEnd)
				{
					this.music = null;
					this.musicID = null;

					double r = this.rankingsTime;
					if (isVersus && this.rankingsTime <= this.rankingsTimeIntro)
					{
						boolean lose = false;
						for (Movable m: Game.movables)
						{
							if (m instanceof Tank && !m.destroy && m != Game.playerTank && !Team.isAllied(m, Game.playerTank))
							{
								lose = true;
								break;
							}
						}

						if (lose)
							Drawing.drawing.playSound("lose.ogg", 1f, true);
					}

					this.rankingsTime += Panel.frameFrequency;

					if (r < 0 && this.rankingsTime >= 0)
						Drawing.drawing.playSound("finished_music_intro.ogg", 1f, true);
				}
				else
				{
					this.music = "finished_music.ogg";
					this.musicID = "versus_results";
				}
			}
			else if (!finishedQuick)
			{
				if (Game.currentLevel != null && Game.currentLevel.timed)
				{
					if (this.paused || Game.playerTank == null || Game.playerTank.destroy)
						this.music = "battle_timed_paused.ogg";
					else
						this.music = "battle_timed.ogg";

					this.musicID = "battle_timed";
				}
				else
				{
					if (this.paused || Game.playerTank == null || Game.playerTank.destroy)
						this.music = "battle_paused.ogg";
					else if (Level.isDark())
						this.music = "battle_night.ogg";
					else
						this.music = "battle.ogg";

					this.musicID = "battle";

					if (Level.isDark())
						this.musicID = "battle_night";
				}
			}

			if (!this.musicStarted)
			{
				this.musicStarted = true;
				prevMusic = this.music;
				Panel.panel.playScreenMusic(0);
			}

			this.prevTankMusics.clear();
			this.prevTankMusics.addAll(this.tankMusics);
			this.tankMusics.clear();

			boolean dead = Game.currentLevel instanceof Minigame && ((Minigame) Game.currentLevel).removeMusicWhenDead && Game.playerTank != null && Game.playerTank.destroy;

			if (!this.paused && !showRankings)
			{
				if (!Game.currentLevel.timed && !dead)
				{
					for (Movable m : Game.movables)
                        if (m instanceof Tank && !m.destroy)
                            this.tankMusics.addAll(((Tank) m).musicTracks);
				}

				if (Game.currentLevel.beatBlocks > 0)
				{
					if (!dead)
						this.tankMusics.add("beatblocks/beat_blocks.ogg");

					if ((Game.currentLevel.beatBlocks & 1) != 0)
						this.tankMusics.add("beatblocks/beat_beeps_1.ogg");

					if ((Game.currentLevel.beatBlocks & 2) != 0)
						this.tankMusics.add("beatblocks/beat_beeps_2.ogg");

					if ((Game.currentLevel.beatBlocks & 4) != 0)
						this.tankMusics.add("beatblocks/beat_beeps_4.ogg");

					if ((Game.currentLevel.beatBlocks & 8) != 0)
						this.tankMusics.add("beatblocks/beat_beeps_8.ogg");
				}
			}

			if (playSounds)
			{
				for (String m : this.prevTankMusics)
				{
					if (!this.tankMusics.contains(m))
						Drawing.drawing.removeSyncedMusic(m, 500);
				}

				for (String m : this.tankMusics)
				{
					if (!this.prevTankMusics.contains(m))
					{
						if (this.playCounter == -2 && m.startsWith("beatblocks/"))
							Drawing.drawing.addSyncedMusic(m, Game.musicVolume, true, 0);
						else
							Drawing.drawing.addSyncedMusic(m, Game.musicVolume, true, 500);
					}
				}
			}

			this.playCounter = -1;
		}

		if (finishedQuick)
		{
			this.finishQuickTimer += Panel.frameFrequency;

			if (!isVersus)
			{
				this.musicID = null;

				if (!(Game.currentLevel instanceof Minigame && ((Minigame) Game.currentLevel).disableEndMusic))
				{
					if (Panel.win && this.finishQuickTimer >= 75)
						this.music = "waiting_win.ogg";

					if (!Panel.win && this.finishQuickTimer >= 150)
						this.music = "waiting_lose.ogg";
				}
			}
		}

		if (Game.game.input.pause.isValid())
		{
			if (shopScreen || npcShopScreen || buildsScreen)
			{
				shopScreen = false;
				npcShopScreen = false;
				buildsScreen = false;
			}
			else
			{
				if (this.paused)
					this.unpause();
				else
					this.pause();
			}

			if (Game.followingCam)
				Game.game.window.setCursorPos(Panel.windowWidth / 2, Panel.windowHeight / 2);

			if (this.paused)
			{
				Game.game.window.setCursorLocked(false);
				Game.game.window.setShowCursor(!Panel.showMouseTarget);
			}
			else
			{
				Game.game.window.setCursorLocked(Game.followingCam);

				if (Game.followingCam)
					Game.game.window.setShowCursor(false);
				else
					Game.game.window.setShowCursor(!Panel.showMouseTarget);
			}

			Game.game.input.pause.invalidate();
		}

		if (Game.game.input.hidePause.isValid())
		{
			this.screenshotMode = !this.screenshotMode;
			Game.game.input.hidePause.invalidate();
		}

		if (!finished)
		{
			if (Obstacle.draw_size == 0)
				Drawing.drawing.playSound("level_start.ogg");

			Obstacle.draw_size = Math.min(Game.tile_size, Obstacle.draw_size + Panel.frameFrequency);
		}

		if (freecam && !(paused && !screenshotMode) && !Game.game.window.pressedKeys.contains(InputCodes.KEY_F) && Game.screen == this)
			updateFreecam();

		if (npcShopScreen)
		{
			Game.player.hotbar.hidden = false;
			Game.player.hotbar.hideTimer = 100;

			this.exitShop.update();
			this.npcShopList.update();
		}

		if (paused)
		{
			if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
			{
				if (!playing && Game.startTime >= 0)
					this.updateSingleplayerWaitingMusic();

				this.updateMusic(prevMusic);
			}

			if (!this.screenshotMode)
			{
				if (this.overlay != null)
					this.overlay.update();
				else
				    for (Button b : pauseMenuButtons)
                        b.update();
			}

			if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
                return;

			Game.game.window.validPressedKeys.clear();
			Game.game.window.pressedKeys.clear();

			Game.game.window.validPressedButtons.clear();
			Game.game.window.pressedButtons.clear();

			Game.game.window.validScrollUp = false;
			Game.game.window.validScrollDown = false;

			if (Game.game.window.touchscreen)
			{
				TankPlayer.controlStick.activeInput = -1;
				TankPlayer.controlStick.inputIntensity = 0;
				TankPlayer.controlStick.update();

				for (InputPoint p : Game.game.window.touchPoints.values())
				{
					p.valid = false;
					p.tag = "backgroundscreen";
				}
			}
		}
		else if (Game.game.window.touchscreen && !shopScreen)
		{
			boolean vertical = Drawing.drawing.interfaceScale * Drawing.drawing.interfaceSizeY >= Game.game.window.absoluteHeight - Drawing.drawing.statsHeight;
			double vStep = 0;
			double hStep = 0;

			if (vertical)
				vStep = 100;
			else
				hStep = 100;

			pause.posX = (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
					+ Drawing.drawing.interfaceSizeX - 50 - Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale;
			pause.posY = -((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2 + 50;
			pause.update();

			zoom.posX = pause.posX - hStep;
			zoom.posY = pause.posY + vStep;

			zoomAuto.posX = zoom.posX - hStep;
			zoomAuto.posY = zoom.posY + vStep;

			if (Drawing.drawing.enableMovingCamera)
			{
				zoom.update();

				if (!Panel.autoZoom)
					zoomAuto.update();
			}

			if (playing)
			{
				TankPlayer.controlStick.mobile = TankPlayer.controlStickMobile;
				TankPlayer.controlStick.snap = TankPlayer.controlStickSnap;
				TankPlayer.controlStick.update();
			}
		}

		if (!playing && Game.startTime >= 0)
		{
			if (shopScreen || buildsScreen)
			{
				Game.player.hotbar.hidden = false;
				Game.player.hotbar.hideTimer = 100;

				this.exitShop.update();

				if (this.buildsScreen)
				{
					if (Game.player.tank != null)
					{
						for (int i = 0; i < this.builds.size(); i++)
						{
							Button b = this.playerBuildsList.buttons.get(i);
							TankPlayer.ShopTankBuild t = this.builds.get(i);

							int p = t.price;

							((ButtonObject)b).showText = true;

							String prefix = p > Game.player.hotbar.coins ? "\u00A7255127127255" : "";
							if (Game.player.ownedBuilds.contains(t.name))
								b.setText("Owned");
							else if (p == 0)
								b.setText("Free!");
							else if (p == 1)
								b.setText("%s1 coin", prefix);
							else
								b.setText("%s%d coins", prefix, p);

							b.enabled = !t.name.equals(Game.player.buildName);
						}
					}
					this.playerBuildsList.update();
				}
				else if (this.shopScreen)
				{
					for (int i = 0; i < this.shop.size(); i++)
					{
						this.shopItemButtons.get(i).enabled = this.shop.get(i).price <= Game.player.hotbar.coins;
					}

					this.shopList.update();
				}

				if (ScreenPartyHost.isServer || ScreenPartyLobby.isClient)
				{
					this.music = "waiting_music.ogg";
					this.musicID = null;
				}
				else
				{
					this.music = "ready/silence.ogg";
					this.musicID = "ready";
				}

				if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
                    this.updateSingleplayerWaitingMusic();
			}
			else
			{
				if ((ScreenPartyHost.isServer || ScreenPartyLobby.isClient || Game.autostart) && !cancelCountdown)
					Game.startTime -= Panel.frameFrequency;

				if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
				{
					play.update();

					if (Game.game.input.play.isValid())
					{
						play.function.run();
						Game.game.input.play.invalidate();
					}

					this.updateSingleplayerWaitingMusic();
				}
				else
				{
					if (this.cancelCountdown)
					{
						readyButton.enabled = !this.ready;

						if (this.ready)
						{
							if (this.readyPanelCounter * 10 >= introMusicEnd)
							{
								this.music = "ready/silence.ogg";
								this.musicID = "ready";
							}
							else
							{
								if (this.readyPanelCounter == 0)
									Drawing.drawing.playSound("ready_music_intro.ogg", 1f, true);

								this.music = null;
								this.musicID = null;
							}

							this.readyPanelCounter += Panel.frameFrequency;
							readyButton.setText("Waiting... (%d/%d)");
						}
						else
						{
							readyButton.setText("Ready (%d/%d)");
							this.music = "waiting_music.ogg";
							this.musicID = null;
						}

						if (ScreenPartyHost.isServer)
						{
							if (!ScreenPartyHost.includedPlayers.contains(Game.clientID))
							{
								readyButton.setText("Spectating... (%d/%d)");
								readyButton.enabled = false;
							}

							readyButton.setTextArgs(ScreenPartyHost.readyPlayers.size(), ScreenPartyHost.includedPlayers.size());
						}
						else
						{
							if (!ScreenPartyLobby.includedPlayers.contains(Game.clientID))
							{
								readyButton.setText("Spectating... (%d/%d)");
								readyButton.enabled = false;
							}

							readyButton.setTextArgs(ScreenPartyLobby.readyPlayers.size(), ScreenPartyLobby.includedPlayers.size());
						}
					}
					else
					{
						if (this.readyPanelCounter * 10 >= introMusicEnd)
						{
							this.music = "ready/shaker.ogg";
							this.musicID = "ready";
						}
						else
						{
							if (this.readyPanelCounter == 0)
								Drawing.drawing.playSound("ready_music_intro.ogg", 1f, true);

							this.music = null;
							this.musicID = null;
						}

						this.readyPanelCounter += Panel.frameFrequency;
						readyButton.enabled = false;
						readyButton.setText("Starting in %d", ((int)(Game.startTime / 100) + 1));
					}

					readyButton.update();

					if (Game.game.input.play.isValid() && readyButton.enabled)
					{
						readyButton.function.run();
						Game.game.input.play.invalidate();
					}
				}

				if (!this.shopItemButtons.isEmpty() && readyButton.enabled)
					enterShop.update();

				if (this.playerBuildButtons.size() > 1 && readyButton.enabled)
					viewBuilds.update();

				if (ScreenPartyHost.isServer && this.cancelCountdown)
				{
					startNow.update();
				}

				TankPlayer.controlStick.mobile = TankPlayer.controlStickMobile;
				TankPlayer.controlStick.snap = TankPlayer.controlStickSnap;
				TankPlayer.controlStick.update();
			}
		}
		else
		{
			if (Game.currentLevel instanceof Minigame)
				((Minigame) Game.currentLevel).update();

			if (Game.screen instanceof ILevelPreviewScreen)
				return;

			playing = true;
			this.age += Panel.frameFrequency;

			if ((!freecam || controlPlayer) && Game.followingCam)
				updateFollowingCam();

			aliveTeams.clear();
			fullyAliveTeams.clear();

			Obstacle.draw_size = Math.min(Game.tile_size, Obstacle.draw_size);

			for (Effect e : Game.effects)
				e.update();

			for (Cloud c : Game.clouds)
				c.update();

            for (Movable m : Game.movables)
                m.preUpdate();

			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable m = Game.movables.get(i);

				if (m.skipNextUpdate)
				{
					m.skipNextUpdate = false;
					continue;
				}

				if (m instanceof IAvoidObject)
					IAvoidObject.avoidances.add(((IAvoidObject) m));

				m.update();
			}

			if (Game.currentLevel != null)
			{
				Game.currentLevel.beatBlocks = 0;
				Game.currentLevel.synchronizeMusic = false;
			}

			for (Obstacle o : Game.updateObstacles)
                o.update();

			for (Obstacle o : Game.checkUpdateObstacles)
			{
				if (o.shouldUpdate())
					Game.updateObstacles.add(o);
				else
					Game.updateObstacles.remove(o);
			}
			Game.checkUpdateObstacles.clear();

			for (Effect e : Game.tracks)
				e.update();

			Game.player.hotbar.update();

			for (Movable m: Game.movables)
			{
				if (m instanceof Crate)
					m = ((Crate) m).tank;

				if (m instanceof Tank && ((Tank)m).mandatoryKill)
				{
					Team t;

					if (Game.playerTank != null && m instanceof IServerPlayerTank && !Team.isAllied(m, Game.playerTank))
					{
						if (!isVersus)
							Game.eventsOut.add(new EventSetLevelVersus());
						isVersus = true;
					}

					if (m.team == null)
					{
						if (m instanceof IServerPlayerTank)
							t = new Team(((IServerPlayerTank) m).getPlayer().clientID.toString());
						else if (m instanceof TankRemote && ((TankRemote) m).tank instanceof TankPlayer)
							t = new Team(((TankPlayer) ((TankRemote) m).tank).player.clientID.toString());
						else
							t = new Team("*");
					}
					else
						t = m.team;

					if (!aliveTeams.contains(t))
						aliveTeams.add(t);

					if (!fullyAliveTeams.contains(t) && !m.destroy)
						fullyAliveTeams.add(t);
				}
			}

			if (!finishedQuick)
			{
				this.timePassed += Panel.frameFrequency;
				lastTimePassed = this.timePassed;

				if (Crusade.crusadeMode)
					Crusade.currentCrusade.timePassed += Panel.frameFrequency;
			}

			if (Game.currentLevel != null && Game.currentLevel.timed)
			{
				if (!finishedQuick)
				{
					int seconds = (int) (timeRemaining / 100 + 0.5);
					int secondHalves = (int) (timeRemaining / 50);

					this.timeRemaining -= Panel.frameFrequency;

					playTimerTick(seconds, secondHalves, timeRemaining);
				}

				if (this.timeRemaining <= 0)
				{
					this.saveRemainingTanks();

					boolean found = false;
					for (int i = 0; i < Game.movables.size(); i++)
					{
						Movable m = Game.movables.get(i);

						if (!m.destroy)
							found = true;

						m.destroy = true;

						if (m instanceof Tank)
							((Tank) m).health = 0;
					}

					if (found)
						Drawing.drawing.playGlobalSound("leave.ogg");
				}
			}

			boolean done = fullyAliveTeams.size() <= 1;

			if (Game.currentLevel instanceof Minigame)
			{
				if (((Minigame) Game.currentLevel).customLevelEnd)
					done = ((Minigame) Game.currentLevel).levelEnded();

				if (done)
					((Minigame) Game.currentLevel).onLevelEndQuick();
			}

			if (Game.screen == this && done)
			{
				if (!ScreenGame.finishedQuick)
				{
					Panel.forceRefreshMusic = playSounds;

					if (Game.playerTank != null && (fullyAliveTeams.contains(Game.playerTank.team) || (!fullyAliveTeams.isEmpty() && fullyAliveTeams.get(0).name.equals(Game.clientID.toString()))))
					{
						if (Crusade.crusadeMode && !Crusade.currentCrusade.respawnTanks)
						{
							restartCrusade.enabled = false;
							restartCrusadeParty.enabled = false;
						}

						if (!ScreenPartyLobby.isClient && playSounds)
						{
							Drawing.drawing.playSound("win.ogg", 1.0f, true);
							Panel.win = true;
						}
					}
					else
					{
						if (!ScreenPartyLobby.isClient && playSounds)
						{
							if (!isVersus)
								Drawing.drawing.playSound("lose.ogg", 1.0f, true);

							Panel.win = Game.currentLevel instanceof Minigame && ((Minigame) Game.currentLevel).noLose;
						}
					}

					String s = "**";

					if (!fullyAliveTeams.isEmpty())
						s = fullyAliveTeams.get(0).name;

					if (ScreenPartyHost.isServer)
						Game.eventsOut.add(new EventLevelFinishedQuick(s));
				}

				ScreenGame.finishedQuick = true;
				TankPlayer.shootStickHidden = false;
			}

			if (aliveTeams.size() <= 1 && done)
			{
				if (ScreenPartyHost.isServer && !ScreenGame.finished)
					Game.eventsOut.add(new EventLevelFinished());

				ScreenGame.finished = true;
				Game.bulletLocked = true;

				if (isVersus && ScreenPartyHost.isServer)
				{
					for (Movable m : Game.movables)
					{
						if (!m.destroy && m instanceof IServerPlayerTank && this.eliminatedPlayers.size() < ScreenPartyHost.includedPlayers.size())
						{
							this.eliminatedPlayers.add(new ConnectedPlayer(((IServerPlayerTank) m).getPlayer()));
							Game.eventsOut.add(new EventUpdateEliminatedPlayers(eliminatedPlayers));
						}
					}
				}

				if (ScreenGame.finishTimer > 0)
				{
					ScreenGame.finishTimer -= Panel.frameFrequency;
					if (ScreenGame.finishTimer < 0)
						ScreenGame.finishTimer = 0;
				}
				else
				{
					boolean noMovables = true;

					for (int m = 0; m < Game.movables.size(); m++)
					{
						Movable mo = Game.movables.get(m);
						if (mo instanceof tanks.bullet.Bullet || mo instanceof Mine)
						{
							noMovables = false;
							mo.destroy = true;
						}
					}

					int includedPlayers = 0;

					if (ScreenPartyHost.isServer)
						includedPlayers = ScreenPartyHost.includedPlayers.size();
					else if (ScreenPartyLobby.isClient)
						includedPlayers = ScreenPartyLobby.includedPlayers.size();
					if (Game.effects.isEmpty() && noMovables && !(isVersus && ((finishQuickTimer < introResultsMusicEnd / 10.0 - rankingsTimeIntro) || (rankingsOverlay.namesCount != includedPlayers))))
					{
						if (Game.followingCam)
							Game.game.window.setCursorPos(Panel.windowWidth / 2, Panel.windowHeight / 2);

						if (Obstacle.draw_size == Game.tile_size)
							Drawing.drawing.playSound("level_end.ogg");

						Obstacle.draw_size = Math.max(0, Obstacle.draw_size - Panel.frameFrequency);

						this.saveRemainingTanks();

						for (Movable m: Game.movables)
							m.destroy = true;

						if (Obstacle.draw_size <= 0)
						{
							Panel.levelPassed = false;

							for (int i = 0; i < Game.players.size(); i++)
							{
								if (Game.players.get(i) != null && Game.players.get(i).tank != null && aliveTeams.contains(Game.players.get(i).tank.team) || (!aliveTeams.isEmpty() && aliveTeams.get(0).name.equals(Game.players.get(i).clientID.toString())))
								{
									Panel.levelPassed = true;

									if (Crusade.crusadeMode)
										Panel.winlose = "Battle cleared!";

									break;
								}
							}

							if (Game.playerTank != null)
							{
								if (aliveTeams.contains(Game.playerTank.team) || (!aliveTeams.isEmpty() && aliveTeams.get(0).name.equals(Game.clientID.toString())))
								{
									if (Crusade.crusadeMode)
										Panel.winlose = "Battle cleared!";
									else
										Panel.winlose = "Victory!";

									if (!ScreenPartyLobby.isClient)
										Panel.win = true;
								}
								else
								{
									if (Crusade.crusadeMode)
										Panel.winlose = "Battle failed!";
									else
										Panel.winlose = "You were destroyed!";

									if (!ScreenPartyLobby.isClient)
										Panel.win = (Game.currentLevel instanceof Minigame && ((Minigame) Game.currentLevel).noLose);
								}

								if (Game.currentLevel instanceof Minigame)
									((Minigame) Game.currentLevel).onLevelEnd(Panel.win);
							}
							else if (!ScreenPartyLobby.isClient)
								Panel.win = false;

							if (Crusade.crusadeMode)
								Crusade.currentCrusade.saveHotbars();

							if (ScreenPartyHost.isServer)
							{
								Game.silentCleanUp();

								String s = "**";

								if (!aliveTeams.isEmpty())
									s = aliveTeams.get(0).name;

								ScreenPartyHost.readyPlayers.clear();
								Game.eventsOut.add(new EventLevelExit(s));

								if (Crusade.crusadeMode)
								{
									Crusade.currentCrusade.levelFinished(Panel.levelPassed);

									EventReturnToCrusade e = new EventReturnToCrusade(Crusade.currentCrusade);

									e.execute();
									Game.eventsOut.add(e);

									if (Crusade.currentCrusade.win || Crusade.currentCrusade.lose)
										Game.eventsOut.add(new EventShowCrusadeStats());

									for (int i = 0; i < Game.players.size(); i++)
                                        Game.eventsOut.add(new EventUpdateRemainingLives(Game.players.get(i)));
								}
								else if (ScreenInterlevel.fromQuickPlay != null)
									exitQuickPlay();
								else
									Game.exitToInterlevel();

								System.gc();
							}
							else if (shouldExit && Game.currentLevel != null && !Game.currentLevel.remote)
							{
								if (ScreenInterlevel.fromQuickPlay != null)
									exitQuickPlay();
								else if (name != null)
									Game.exitToEditor(name);
								else
									Game.exitToInterlevel();
							}
						}
					}
				}
			}
			else
				Game.bulletLocked = false;
		}

		if (spectatingTank != null && spectatingTank.destroy)
			spectatingTank = null;

		if (!Game.game.window.touchscreen)
		{
			double mx = Drawing.drawing.getInterfaceMouseX();
			double my = Drawing.drawing.getInterfaceMouseY();

			boolean handled = checkMouse(mx, my, Game.game.window.validPressedButtons.contains(InputCodes.MOUSE_BUTTON_1));

			if (handled)
				Game.game.window.validPressedButtons.remove((Integer) InputCodes.MOUSE_BUTTON_1);
		}
		else
		{
			for (int i: Game.game.window.touchPoints.keySet())
			{
				InputPoint p = Game.game.window.touchPoints.get(i);

				if (p.tag.isEmpty())
				{
					double mx = Drawing.drawing.toGameCoordsX(Drawing.drawing.getInterfacePointerX(p.x));
					double my = Drawing.drawing.getInterfacePointerY(p.y);

					boolean handled = checkMouse(mx, my, p.valid);

					if (handled)
						p.tag = "spectate";
				}
			}
		}

		if (playing && !paused && !finishedQuick)
            this.shrubberyScale = Math.min(this.shrubberyScale + Panel.frameFrequency / 200, 1);

		if (playing && !paused)
			gameAge += Panel.frameFrequency;

		if (finishedQuick)
            this.shrubberyScale = Math.max(this.shrubberyScale - Panel.frameFrequency / 200, 0.25);

		this.updateMusic(prevMusic);

		for (Movable m : Game.removeMovables)
		{
            for (Chunk chunk : m.getTouchingChunks())
                chunk.removeMovable(m);

            if (m instanceof IAvoidObject)
				IAvoidObject.avoidances.remove(m);
		}

		Game.movables.removeAll(Game.removeMovables);
		Game.clouds.removeAll(Game.removeClouds);
		ModAPI.menuGroup.removeAll(ModAPI.removeMenus);

		if (Game.clouds.size() < 10)
			Game.clouds.add(new Cloud(0, Math.random() * Game.currentSizeY * Game.tile_size));

		handleRemoveObstacles();

		for (Effect e: Game.removeEffects)
		{
			if (e.state == Effect.State.removed)
			{
				e.state = Effect.State.recycle;
				Game.effects.remove(e);
				Game.recycleEffects.add(e);
			}
		}

		Game.effects.addAll(Game.addEffects);
		Game.addEffects.clear();

		for (Effect e: Game.removeTracks)
		{
			if (e.state == Effect.State.removed)
			{
				e.state = Effect.State.recycle;
				Game.tracks.remove(e);
				Game.recycleEffects.add(e);
			}
		}

		Game.removeMovables.clear();
		Game.removeObstacles.clear();
		Game.removeEffects.clear();
		Game.removeTracks.clear();
		Game.removeClouds.clear();
		ModAPI.removeMenus.clear();

		if (this.tutorial != null)
            this.tutorial.update();
	}

	private static void displayZoomMessage()
	{
		Panel.currentMessage = new ScreenElement.CenterMessage("Zoom: %.0f", 100, Panel.zoomTarget >= 0 ? Panel.zoomTarget * 100 : (Drawing.drawing.movingCamera ? 100 : 0));
	}

	public static void playTimerTick(int seconds, int secondHalves, double timeRemaining)
	{
		int newSeconds = (int) (timeRemaining / 100 + 0.5);
		int newSecondHalves = (int) (timeRemaining / 50);

		if (seconds <= 5)
		{
			if (newSecondHalves < secondHalves)
				Drawing.drawing.playSound("tick.ogg", 2f, 0.5f);
		}
		else if (newSeconds < seconds && seconds <= 10)
			Drawing.drawing.playSound("tick.ogg", 2f, 0.5f);

		if (seconds > newSeconds && (newSeconds == 10 || newSeconds == 30 || newSeconds == 60))
			Drawing.drawing.playSound("timer.ogg");
	}

	public void addPauseMenuButtons()
    {
		pauseMenuButtons.clear();
        addButtons(pauseMenuButtons,
                ScreenPartyLobby.isClient ? new Button[]{closeMenuClient, exitParty} : null,
                ScreenPartyHost.isServer ? getServerButtons() : null,
                name != null ? new Button[]{resume, edit, restart, quit} : null,
                (ScreenInterlevel.fromSavedLevels || ScreenInterlevel.fromMinigames) ? new Button[]{resumeLowerPos, restartLowerPos, ScreenInterlevel.fromQuickPlay != null ? backToQuickPlay : back} : null,
                ScreenInterlevel.tutorialInitial ? new Button[]{resumeLowerPos, restartTutorial} : null,
                ScreenInterlevel.tutorial ? new Button[]{resumeLowerPos, restartTutorial, quitHigherPos} : null,
                Crusade.crusadeMode ? getCrusadeButtons() : null,
				Game.screen instanceof ScreenAutomatedTests ? new Button[]{resumeLowerPos, quitHigherPos} : null
        );
		if (pauseMenuButtons.isEmpty())
			addButtons(pauseMenuButtons, new Button[]{resume, newLevel, restart, quit});
    }

    private Button[] getServerButtons()
    {
        if (ScreenInterlevel.fromSavedLevels || ScreenInterlevel.fromMinigames)
        {
            return new Button[]{closeMenuLowerPos, restartLowerPos, back};
        }
        else if (Crusade.crusadeMode)
        {
            return new Button[]{closeMenuLowerPos, restartCrusadePartyFinalLife,
					(finishedQuick && Panel.win) || !Crusade.currentCrusade.finalLife() ? quitCrusadeParty : quitCrusadePartyFinalLife};
        }
        else
            return new Button[]{closeMenu, newLevel, restart, quitPartyGame};
    }

    private Button[] getCrusadeButtons()
    {
        if (Crusade.currentCrusade.finalLife())
            return new Button[]{restartCrusadeFinalLife, quitCrusadeFinalLife, resumeLowerPos};
        else
            return new Button[]{restartCrusade, quitCrusade, resumeLowerPos};
    }

    private static void addButtons(ObjectArrayList<Button> buttons, Button[]... buttonGroups)
    {
        for (Button[] group : buttonGroups)
            if (group != null)
				buttons.addElements(0, group);
    }

	public void updateMusic(String prevMusic)
	{
		if (this.music == null && prevMusic != null)
			Panel.forceRefreshMusic = playSounds;

        if (this.music != null && !this.music.equals(prevMusic))
			Panel.forceRefreshMusic = playSounds;

		if (this.musicID != null && this.musicID.equals("ready"))
		{
			float pos = Game.game.window.soundPlayer.getMusicPos();
			if (this.lastReadyMusicTime == -1)
			{
				for (int m: this.playingReadyMusics)
				{
					Drawing.drawing.addSyncedMusic("ready/" + ready_musics[m], Game.musicVolume, true, 0);
				}
			}

			int fadeTime = 6000;

			// ready music loops
			if (pos < this.lastReadyMusicTime)
			{
				int toAdd = (int) (Math.random() * 6) + 4;
				int toRemove = (int) (Math.random() * this.playingReadyMusics.size() * 0.8);

				if (readyMusicIterations + 1 < intro_order.length)
				{
					toAdd = intro_order[readyMusicIterations + 1].length;
					toRemove = 0;
				}

				if (Math.random() < 0.01 && specialReadyMusicIterationsLeft <= 0)
				{
					specialReadyMusicIterationsLeft = 3;
					for (int m: this.playingReadyMusics)
					{
						Drawing.drawing.removeSyncedMusic("ready/" + ready_musics[m], fadeTime);
					}

					int num = 1;
					int r = (int) (Math.random() * 10);
					if (r == 0)
						num = 4;
					else if (r == 1)
						num = 5;
					else if (r < 10)
						num = 3;

					this.specialReadyMusic = "ready_music_" + num + ".ogg";
					Drawing.drawing.addSyncedMusic(this.specialReadyMusic, Game.musicVolume, true, fadeTime);
				}

				this.readyMusicIterations++;

				if (specialReadyMusicIterationsLeft > 0)
				{
					this.specialReadyMusicIterationsLeft--;

					if (this.specialReadyMusicIterationsLeft == 0)
					{
						Drawing.drawing.removeSyncedMusic(this.specialReadyMusic, fadeTime);
						for (int m: this.playingReadyMusics)
						{
							Drawing.drawing.addSyncedMusic("ready/" + ready_musics[m], Game.musicVolume, true, fadeTime);
						}
					}
				}
				else
				{
                    int added = 0;
					for (int i = 0; i < toAdd; i++)
					{
						int m = (int) (Math.random() * ready_musics.length);
						if (readyMusicIterations < intro_order.length)
							m = intro_order[readyMusicIterations][i];

						if (!playingReadyMusics.contains(m) && (m < 15 || this.playingReadyMusics.size() > 8))
						{
							added++;
							playingReadyMusics.add(m);
							Drawing.drawing.addSyncedMusic("ready/" + ready_musics[m], Game.musicVolume, true, fadeTime);
						}
					}

					for (int i = 0; i < toRemove; i++)
					{
						int m = Math.max(0, (int) (Math.random() * playingReadyMusics.size() - added));

						if (playingReadyMusics.get(m) < 5 && Math.random() < 0.5)
							continue;

						Drawing.drawing.removeSyncedMusic("ready/" + ready_musics[playingReadyMusics.get(m)], fadeTime);
						playingReadyMusics.remove(m);
					}
				}
			}

			if (this.lastReadyMusicTime == -2)
				this.lastReadyMusicTime = -1;
			else
				this.lastReadyMusicTime = pos;
		}
	}

	public void updateSingleplayerWaitingMusic()
	{
		if (ScreenInterlevel.tutorialInitial)
			return;

		if (this.readyPanelCounter * 10 >= introMusicEnd)
		{
			this.music = "ready/shaker.ogg";
			this.musicID = "ready";

			if (this.paused || this.shopScreen || this.buildsScreen)
				this.music = "ready/silence.ogg";
		}
		else
		{
			if (this.readyPanelCounter == 0)
				Drawing.drawing.playSound("ready_music_intro.ogg", 1f, true);

			this.music = null;
			this.musicID = null;
		}

		this.readyPanelCounter += Panel.frameFrequency;
	}

	public boolean checkMouse(double mx, double my, boolean valid)
	{
		if (!valid)
			return false;

		double x = Drawing.drawing.toGameCoordsX(mx);
		double y = Drawing.drawing.toGameCoordsY(my);

		if ((Game.playerTank == null || Game.playerTank.destroy) && !ScreenGame.finishedQuick && Drawing.drawing.unzoomedScale < Drawing.drawing.interfaceScale)
		{
            for (Movable m : Game.getMovablesInRadius(x, y, 100))
            {
                if (!(m instanceof Tank) || m.destroy || ((Tank) m).hidden)
                    continue;

                this.spectatingTank = (Tank) m;

                if (Panel.panel.zoomTimer > 0)
                {
                    Drawing.drawing.lastSwitchedPlayerX = Drawing.drawing.lastPlayerX;
                    Drawing.drawing.lastSwitchedPlayerY = Drawing.drawing.lastPlayerY;
                    Drawing.drawing.spectateTransitionTime = Drawing.drawing.spectateTransitionTimeBase;
                }

                Panel.panel.pastPlayerX.clear();
                Panel.panel.pastPlayerY.clear();
                Panel.panel.pastPlayerTime.clear();

                Panel.panel.pastPlayerX.add(Drawing.drawing.lastPlayerX);
                Panel.panel.pastPlayerY.add(Drawing.drawing.lastPlayerY);
                Panel.panel.pastPlayerTime.add(Panel.panel.age - Drawing.drawing.getTrackOffset());

                Drawing.drawing.movingCamera = true;
                return true;
            }
        }

		return false;
	}

	public void drawSpectateOverlay()
	{
		double x = Drawing.drawing.getMouseX();
		double y = Drawing.drawing.getMouseY();

		if (deadTime > deadTimeToSpectate)
			deadTime = deadTimeToSpectate;

		if (ScreenGame.finishedQuick)
			deadTime -= Panel.frameFrequency;

		if ((Game.playerTank == null || Game.playerTank.destroy) && !ScreenGame.finishedQuick && Drawing.drawing.unzoomedScale < Drawing.drawing.interfaceScale)
		{
			deadTime += Panel.frameFrequency;
			for (Movable m: Game.movables)
			{
				if (m instanceof Tank && !m.destroy && !((Tank) m).hidden && spectatingTank != m)
				{
					double dx = x - m.posX;
					double dy = y - m.posY;

					double ix = Drawing.drawing.gameToInterfaceCoordsX(m.posX);
					double iy = Drawing.drawing.gameToInterfaceCoordsY(m.posY);

					if (dx * dx + dy * dy < Math.pow(((Tank) m).size + Game.tile_size / 2, 2))
					{
						Drawing.drawing.setColor(((Tank) m).colorR, ((Tank) m).colorG, ((Tank) m).colorB);
						Mine.drawRange2D(m.posX, m.posY, ((Tank) m).size + Game.tile_size / 2);
						Drawing.drawing.setColor(255, 255, 255, 255, 255);
						Drawing.drawing.drawInterfaceImage("icons/shown.png", ix, iy, Game.tile_size, Game.tile_size);

						if (Level.isDark())
							Drawing.drawing.setColor(255, 255, 255, 255, 255);
						else
							Drawing.drawing.setColor(0, 0, 0);

						Drawing.drawing.setInterfaceFontSize(12);
						Drawing.drawing.drawInterfaceText(ix, iy - Game.tile_size, "Click to spectate tank");

						break;
					}
				}
			}


			if (spectatingTank != null)
			{
				double ix = Drawing.drawing.gameToInterfaceCoordsX(spectatingTank.posX);
				double iy = Drawing.drawing.gameToInterfaceCoordsY(spectatingTank.posY);

				Drawing.drawing.setColor(spectatingTank.colorR, spectatingTank.colorG, spectatingTank.colorB, 127);
				Drawing.drawing.drawInterfaceImage("cursor.png", ix, iy, Game.tile_size * 3, Game.tile_size * 3);
			}

			double posX = Drawing.drawing.interfaceSizeX / 2;
			double posY = Drawing.drawing.getInterfaceEdgeY(true) - 100 - (100 - Game.player.hotbar.percentHidden) * 0.5;

			double frac = Math.max(0, (deadTime / deadTimeToSpectate - 0.5) * 2);

			Drawing.drawing.setColor(0, 0, 0, 127 * frac);
			Drawing.drawing.drawPopup(posX, posY, 300, 80);
			Drawing.drawing.setColor(255, 255, 255, 255 * frac);

			if (spectatingTank == null)
			{
				Drawing.drawing.setInterfaceFontSize(15);
				Drawing.drawing.displayInterfaceText(posX, posY, "Click a tank to spectate it!");
			}
			else
			{
				Drawing.drawing.setInterfaceFontSize(15);
				Drawing.drawing.displayInterfaceText(posX, posY - 15, Game.game.input.zoom.getInputs() + ": Stop spectating");

				if (Panel.panel.zoomTimer <= 0)
					Drawing.drawing.displayInterfaceText(posX, posY - 0, "%s: Zoom in", Game.game.input.zoomIn.getInputs());
				else if (Panel.panel.zoomTimer >= 1)
					Drawing.drawing.displayInterfaceText(posX, posY - 0, "%s: Zoom out", Game.game.input.zoomOut.getInputs());
				else
					Drawing.drawing.displayInterfaceText(posX, posY - 0, "%s/%s: Zoom in/out", Game.game.input.zoomIn.getInputs(), Game.game.input.zoomOut.getInputs());

				if (Panel.autoZoom)
					Drawing.drawing.displayInterfaceText(posX, posY + 15,  "%s: Lock zoom", Game.game.input.zoomAuto.getInputs());
				else
					Drawing.drawing.displayInterfaceText(posX, posY + 15,  "%s: Automatic zoom", Game.game.input.zoomAuto.getInputs());
			}
		}
	}

	public void setPerspective()
	{
        if (Game.angledView)
		{
			if (!Game.game.window.drawingShadow)
			{
				if (this.playing && (!this.paused || ScreenPartyHost.isServer || ScreenPartyLobby.isClient) && !ScreenGame.finished)
					slant = Math.min(1, slant + 0.01 * Panel.frameFrequency);
				else if (ScreenGame.finished)
					slant = Math.max(0, slant - 0.01 * Panel.frameFrequency);
			}

			this.slantRotation.pitch = this.slant * -Math.PI / 16;
			this.slantTranslation.y = -this.slant * 0.05;

			if (!Game.followingCam)
			{
				Game.game.window.transformations.add(this.slantTranslation);
				Game.game.window.transformations.add(this.slantRotation);
			}

			Game.game.window.loadPerspective();
		}

		if (Game.followingCam && Game.framework == Game.Framework.lwjgl && !Game.game.window.drawingShadow)
		{
			double frac = Panel.panel.zoomTimer;

			Game.game.window.clipMultiplier = 1;
			Game.game.window.clipDistMultiplier = 100;

			if (freecam)
				Game.game.window.transformations.add(new Translation(Game.game.window, x, y, z));

			Tank t = focusedTank();

			if (!Game.firstPerson)
			{
				Game.game.window.transformations.add(new RotationAboutPoint(Game.game.window, 0, 0, freecam ? 0 : (frac * ((t.angle + Math.PI * 3 / 2) % (Math.PI * 2) - Math.PI)), 0, -Drawing.drawing.statsHeight / Game.game.window.absoluteHeight / 2, 0));
				Game.game.window.transformations.add(new Translation(Game.game.window, 0, 0.1 * frac, 0));
				Game.game.window.transformations.add(new RotationAboutPoint(Game.game.window, 0, -Math.PI * 0.35 * frac + fcPitch, 0, fcPitch * 3, fcPitch * 3, -1));
				Game.game.window.transformations.add(new Translation(Game.game.window, 0, 0, 0.5 * frac));
            }
			else
			{
				Game.game.window.transformations.add(new RotationAboutPoint(Game.game.window, 0, 0, frac * ((t.angle + Math.PI * 3 / 2) % (Math.PI * 2) - Math.PI), 0, -Drawing.drawing.statsHeight / Game.game.window.absoluteHeight / 2, 0));
				Game.game.window.transformations.add(new Translation(Game.game.window, 0, 0.1 * frac, 0));
				Game.game.window.transformations.add(new RotationAboutPoint(Game.game.window, 0, -Math.PI * 0.5 * frac, 0, 0, 0, -1));
				Game.game.window.transformations.add(new Translation(Game.game.window, 0, 0.0575 * frac, 0.9 * frac));
            }

            if (fcZoom > 0)
                Game.game.window.transformations.add(new ScaleAboutPoint(Game.game.window, 1, 1, fcZoom + 1, 0, 0, 0));

            if (freecam)
				Game.game.window.transformations.add(new RotationAboutPoint(Game.game.window, yaw, pitch, roll, 0, 0, 0));

			Game.game.window.loadPerspective();
		}
	}

	@Override
	public void draw()
	{
        this.setPerspective();

		double mul = 1;
		if (Game.angledView)
			mul = 2;

		if (!Game.followingCam)
		{
			Drawing.drawing.setColor(174, 92, 16);
			Drawing.drawing.fillShadedInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2,
					mul * Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale, mul * Game.game.window.absoluteHeight / Drawing.drawing.interfaceScale);
		}

		Drawing drawing = Drawing.drawing;
		drawGame();

		for (Cloud c : Game.clouds)
			c.draw();

		if (Chunk.debug && !Game.game.window.drawingShadow)
		{
			for (Movable m : Game.movables)
			{
                Drawing.drawing.setColor(255, 255, 255);
                Drawing.drawing.drawText(m.posX, m.posY, m.getSize(), m.getTouchingChunks().size() + "");
            }
		}

		if (Game.drawFaces)
		{
			for (Chunk c : Chunk.chunkList)
			{
                for (Face f : c.faces.topFaces)
                {
                    if (shouldHide(f)) continue;
                    drawing.setColor(150, 50, 50);
                    drawing.fillRect(0.5 * (f.endX + f.startX), f.startY, f.endX - f.startX, 5);
                }

                for (Face f : c.faces.bottomFaces)
                {
                    if (shouldHide(f)) continue;
                    drawing.setColor(255, 50, 50);
                    drawing.fillRect(0.5 * (f.endX + f.startX), f.startY, f.endX - f.startX, 5);
                }

                for (Face f : c.faces.leftFaces)
                {
                    if (shouldHide(f)) continue;
                    drawing.setColor(50, 50, 150);
                    drawing.fillRect(f.startX, 0.5 * (f.endY + f.startY), 5, f.endY - f.startY);
                }

                for (Face f : c.faces.rightFaces)
                {
                    if (shouldHide(f)) continue;
                    drawing.setColor(50, 50, 255);
                    drawing.fillRect(f.startX, 0.5 * (f.endY + f.startY), 5, f.endY - f.startY);
                }
            }
		}

		Drawing.drawing.setColor(0, 0, 0, 127);

		if (Panel.darkness > 0)
		{
			Drawing.drawing.setColor(0, 0, 0, Math.max(0, Panel.darkness));
			Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth, Game.game.window.absoluteHeight - Drawing.drawing.statsHeight);
		}

		drawSpectateOverlay();

		if (!(paused && screenshotMode) && Game.player.hotbar.enabledItemBar)
            Game.player.hotbar.itemBar.drawOverlay();

		Chunk.drawDebugStuff();

		Game.game.window.transformations.clear();
		Game.game.window.loadPerspective();

		if (Game.game.window.touchscreen)
		{
			TankPlayer.controlStick.draw();

			if (TankPlayer.shootStickEnabled && !TankPlayer.shootStickHidden)
				TankPlayer.shootStick.draw();

			if (TankPlayer.shootStickEnabled)
			{
				double size = TankPlayer.mineButton.sizeX * Obstacle.draw_size / Game.tile_size;
				Drawing.drawing.setColor(255, 127, 0, 64);
				Drawing.drawing.fillInterfaceOval(TankPlayer.mineButton.posX, TankPlayer.mineButton.posY, size, size);

				Drawing.drawing.setColor(255, 255, 0, 64);
				Drawing.drawing.fillInterfaceOval(TankPlayer.mineButton.posX, TankPlayer.mineButton.posY, size * 0.8, size * 0.8);
			}

		}

		if (npcShopScreen)
		{
			Drawing.drawing.setColor(127, 178, 228, 64);
			Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);

			Drawing.drawing.setInterfaceFontSize(this.titleSize);

			if (Level.isDark())
				Drawing.drawing.setColor(255, 255, 255);
			else
				Drawing.drawing.setColor(0, 0, 0);

			Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - 210 + shopOffset, "Shop");

			this.exitShop.draw();

			this.npcShopList.draw();
		}

		if (isVersus && ((Game.playerTank != null && Game.playerTank.destroy) || finishedQuick))
			this.showRankings = true;

		if (this.showRankings && rankingsTime * 10 >= 0)
			this.rankingsOverlay.draw();

		if (!playing)
		{
			if (Crusade.crusadeMode)
			{
				if (Level.isDark())
					Drawing.drawing.setColor(255, 255, 255, 127);
				else
					Drawing.drawing.setColor(0, 0, 0, 127);

				Drawing.drawing.setInterfaceFontSize(100);
				Drawing.drawing.displayInterfaceText(this.centerX, this.centerY, "Battle %d", (Crusade.currentCrusade.currentLevel + 1));

				if (Crusade.currentCrusade != null && Crusade.currentCrusade.showNames)
				{
					Drawing.drawing.setInterfaceFontSize(50);
					Drawing.drawing.drawInterfaceText(this.centerX, this.centerY + 75, Crusade.currentCrusade.levels.get(Crusade.currentCrusade.currentLevel).levelName.replace("_", " "));
				}
			}

			if (!title.isEmpty())
			{
				if (Level.isDark())
					Drawing.drawing.setColor(255, 255, 255, 127);
				else
					Drawing.drawing.setColor(0, 0, 0, 127);

				Drawing.drawing.setInterfaceFontSize(100);
				Drawing.drawing.drawInterfaceText(this.centerX, this.centerY, title);
			}

			if (!subtitle.isEmpty())
			{
				if (Level.isDark())
					Drawing.drawing.setColor(255, 255, 255, 127);
				else
					Drawing.drawing.setColor(0, 0, 0, 127);

				Drawing.drawing.setInterfaceFontSize(50);
				Drawing.drawing.drawInterfaceText(this.centerX, this.centerY + 75, subtitle);
			}

			if (shopScreen || buildsScreen)
			{
				Drawing.drawing.setColor(127, 178, 228, 64);
				Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);

				Drawing.drawing.setColor(0, 0, 0, 127);
				Drawing.drawing.drawPopup(this.centerX, this.centerY + shopOffset + 50, 1100, 650);

				if (shopScreen)
					exitShop.setText("Exit shop");
				else if (buildsScreen)
					exitShop.setText("Done");

				this.exitShop.draw();

				Drawing.drawing.setColor(255, 255, 255);
				Drawing.drawing.setInterfaceFontSize(this.titleSize);

				if (this.buildsScreen)
				{
					Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 210 + shopOffset, "Tank builds");
					this.playerBuildsList.manualDarkMode = true;
					this.playerBuildsList.draw();
				}
				else
				{
					Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 210 + shopOffset, "Shop");
					this.shopList.manualDarkMode = true;
					this.shopList.draw();

					for (int i = Math.min((this.shopList.page + 1) * this.shopList.rows * this.shopList.columns, shopItemButtons.size()) - 1; i >= this.shopList.page * this.shopList.rows * this.shopList.columns; i--)
					{
						Button b = this.shopItemButtons.get(i);
						b.draw();
						Drawing.drawing.setColor(255, 255, 255);
						Drawing.drawing.drawInterfaceImage(this.shop.get(i).itemStack.item.icon, b.posX - 135, b.posY, 40, 40);
					}
				}
			}
			else
			{
				if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
					play.draw();
				else
				{
					if (ScreenPartyHost.isServer)
					{
						readyPlayers.clear();

						for (Player p : ScreenPartyHost.readyPlayers)
							readyPlayers.add(p.getConnectedPlayer());
					}
					else
						readyPlayers = ScreenPartyLobby.readyPlayers;

					double s = Game.startTime;

					if (cancelCountdown)
						s = 400;

					double extraWidth = (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2;
					double height = (Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale;

					Drawing.drawing.setColor(0, 0, 0, Math.max(0, 127 * Math.min(1, (readyPanelCounter * 10) / 200) * Math.min(s / 25, 1)));
					Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX + extraWidth / 2, Drawing.drawing.interfaceSizeY / 2, extraWidth, height);
					Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX - Math.min(readyPanelCounter * 10, 200), Drawing.drawing.interfaceSizeY / 2,
							Math.min(readyPanelCounter * 20, 400), height);

					double c = readyPanelCounter - 35;

					double opacity = Math.max(Math.min(s / 25, 1) * 255, 0);
					if (c > 0)
					{
						Drawing.drawing.setColor(255, 255, 255, opacity);
						Drawing.drawing.setInterfaceFontSize(this.titleSize);

						Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX - 200, 50, "Ready players:");
					}

					int includedPlayers = 0;

					if (ScreenPartyHost.isServer)
						includedPlayers = ScreenPartyHost.includedPlayers.size();
					else if (ScreenPartyLobby.isClient)
						includedPlayers = ScreenPartyLobby.includedPlayers.size();

					double spacing = readyNameSpacing;
					if (!cancelCountdown)
						spacing = Math.min(readyNameSpacing, Math.max(Game.currentLevel.startTime - 50.0f, 0) / (includedPlayers + 1));

					if (readyPlayers.size() < readyNamesCount)
						readyNamesCount = readyPlayers.size();

					while (readyPlayers.size() > readyNamesCount && c > lastNewReadyName + spacing)
					{
						lastNewReadyName = lastNewReadyName + spacing;
						readyNamesCount++;
					}

					int slots = (int) ((Drawing.drawing.interfaceSizeY - 200) / 40) - 1;
					int base = 0;

					if (readyNamesCount >= includedPlayers)
						slots++;

					if (readyNamesCount > slots)
						base = readyNamesCount - slots;

					for (int i = 0; i < readyPlayers.size(); i++)
					{
						if (i < readyNamesCount)
						{
							Drawing.drawing.setColor(255, 255, 255, opacity);

							if (i >= base)
							{
								ConnectedPlayer cp = readyPlayers.get(i);

								String name;
								if (Game.enableChatFilter)
									name = Game.chatFilter.filterChat(cp.username);
								else
									name = cp.username;

								Drawing.drawing.setBoundedInterfaceFontSize(this.textSize, 250, name);
								Drawing.drawing.setColor(cp.teamColorR, cp.teamColorG, cp.teamColorB, opacity);
								Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX - 200, 40 * (i - base) + 100, name);
								Tank.drawTank(Drawing.drawing.interfaceSizeX - 240 - Drawing.drawing.getStringWidth(name) / 2, 40 * (i - base) + 100, cp.colorR, cp.colorG, cp.colorB, cp.colorR2, cp.colorG2, cp.colorB2, cp.colorR3, cp.colorG3, cp.colorB3, opacity / 255 * 25);
							}
						}
					}

					if (c >= 0)
					{
						Drawing.drawing.setColor(255, 255, 255, Math.min(s / 25, 1) * 127);
						Drawing.drawing.setInterfaceFontSize(this.textSize);

						for (int i = readyNamesCount; i < Math.min(includedPlayers, slots); i++)
						{
							Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX - 200, 40 * i + 100, "Waiting...");
						}

						int extra = includedPlayers - Math.max(readyNamesCount, slots);
						if (extra > 0)
						{
							if (extra == 1)
								Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX - 200, 40 * slots + 100, "Waiting...");
							else
								Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX - 200, 40 * slots + 100, "%d waiting...", extra);
						}
					}

					if (prevReadyNames != readyNamesCount)
					{
						Drawing.drawing.playSound("bullet_explode.ogg", 1.0f + readyNamesCount * 1.0f / includedPlayers);
					}

					prevReadyNames = readyNamesCount;

					readyButton.draw();
				}

				if (!this.shopItemButtons.isEmpty() && this.readyButton.enabled)
				{
					enterShop.enableHover = newItemsNotification;
					enterShop.fullInfo = true;
					enterShop.draw();

					if (newItemsNotification)
						drawExclamation(enterShop);
				}

				if (this.playerBuildButtons.size() > 1 && readyButton.enabled)
				{
					viewBuilds.enableHover = newBuildsNotification;
					viewBuilds.fullInfo = true;
					viewBuilds.draw();

					if (newBuildsNotification)
						drawExclamation(viewBuilds);
				}

				if (ScreenPartyHost.isServer && this.cancelCountdown)
					startNow.draw();

				if ((ScreenPartyHost.isServer || ScreenPartyLobby.isClient || Game.autostart) && !cancelCountdown)
				{
					Drawing.drawing.setColor(127, 127, 127);
					Drawing.drawing.fillInterfaceRect(play.posX, play.posY + play.sizeY / 2 - 5, play.sizeX * 32 / 35, 3);
					Drawing.drawing.setColor(255, 127, 0);
					Drawing.drawing.fillInterfaceProgressRect(play.posX, play.posY + play.sizeY / 2 - 5, play.sizeX * 32 / 35, 3, Math.max(Game.startTime / Game.currentLevel.startTime, 0));

					if (Game.glowEnabled)
					{
						Drawing.drawing.fillInterfaceGlow(play.posX + ((Game.startTime / Game.currentLevel.startTime - 0.5) * (play.sizeX * 32 / 35)), play.posY + play.sizeY / 2 - 5, 20, 20);
					}
				}
			}
		}

		if (!paused && Game.game.window.touchscreen && !shopScreen)
		{
			pause.draw();
			Drawing.drawing.setColor(255, 255, 255);
			Drawing.drawing.drawInterfaceImage("icons/pause.png", pause.posX, pause.posY, 40, 40);

			if (Drawing.drawing.enableMovingCamera)
			{
				zoom.draw();

				if (!Panel.autoZoom)
					zoomAuto.draw();

				Drawing.drawing.setColor(255, 255, 255);
				if (Drawing.drawing.movingCamera)
					Drawing.drawing.drawInterfaceImage("icons/zoom_out.png", zoom.posX, zoom.posY, 40, 40);
				else
					Drawing.drawing.drawInterfaceImage("icons/zoom_in.png", zoom.posX, zoom.posY, 40, 40);

				if (!Panel.autoZoom)
					Drawing.drawing.drawInterfaceImage("icons/zoom_auto.png", zoomAuto.posX, zoomAuto.posY, 40, 40);
			}
		}

		if (!(paused && screenshotMode))
		{
			Game.player.hotbar.draw();

			if (Hotbar.circular)
				Game.player.hotbar.drawCircle();

			if (((Game.showSpeedrunTimer || Game.showBestTime) && !(paused && screenshotMode) && !(Game.currentLevel instanceof Minigame && ((Minigame) Game.currentLevel).hideSpeedrunTimer)) || (Game.currentLevel instanceof Minigame && ((Minigame) Game.currentLevel).forceSpeedrunTimer))
				SpeedrunTimer.draw();

			minimap.draw();
		}

		if (Game.deterministicMode && !ScreenPartyLobby.isClient)
		{
			if (Level.isDark() || (Game.screen instanceof IDarkScreen && Panel.win && Game.effectsEnabled))
				Drawing.drawing.setColor(255, 255, 255, 127);
			else
				Drawing.drawing.setColor(0, 0, 0, 127);

			double posX = Drawing.drawing.interfaceSizeX + (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2 - Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale - 50;
			double posY = -((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2 + 50;

			if (Drawing.drawing.interfaceScaleZoom > 1)
				posX -= 50;

			Drawing.drawing.setInterfaceFontSize(24);

			if (Game.deterministic30Fps)
				Drawing.drawing.drawInterfaceText(posX, posY, "Deterministic mode (30 FPS)", true);
			else
				Drawing.drawing.drawInterfaceText(posX, posY, "Deterministic mode (60 FPS)", true);
		}

		if (Game.currentLevel instanceof Minigame)
			((Minigame) Game.currentLevel).draw();

		if (paused && !screenshotMode)
		{
			if (ScreenGame.finishedQuick)
			{
				quitCrusade.enableHover = false;
				quitCrusadeParty.enableHover = false;

				quitCrusadeFinalLife.enableHover = false;
				quitCrusadePartyFinalLife.enableHover = false;

				restartCrusade.enableHover = false;
				restartCrusadeParty.enableHover = false;

				restartCrusadeFinalLife.enableHover = false;
				restartCrusadePartyFinalLife.enableHover = false;
			}

			Drawing.drawing.setColor(127, 178, 228, 64);
			Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);

			for (Button b : pauseMenuButtons)
                b.draw();

			Drawing.drawing.setInterfaceFontSize(this.titleSize);
			Drawing.drawing.setColor(0, 0, 0);

			if (Level.isDark())
				Drawing.drawing.setColor(255, 255, 255);

			if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
				Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Game paused");
			else
				Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Game menu");
		}

		if (this.overlay != null)
			this.overlay.draw();

		Drawing.drawing.setInterfaceFontSize(this.textSize);

		if (Game.showPathfinding)
		{
			Drawing.drawing.setColor(255, 255, 255);
			Drawing.drawing.setInterfaceFontSize(16);
			Drawing.drawing.drawInterfaceText(Drawing.drawing.getInterfaceEdgeX(true) - 10, Drawing.drawing.getInterfaceEdgeY(true) - Drawing.drawing.fontSize * 36, "Operations: " + TankAIControlled.Tile.tilesChecked + "/" + Game.currentSizeX * Game.currentSizeY, true);
		}
	}

	public void drawGame()
	{
		drawGame(drawables, this);
	}

	public static void drawGame(ArrayList<IDrawable>[] drawables, ScreenGame game)
	{
		Game.screen.drawDefaultBackground();
		drawDrawables(drawables, game);
	}

	public static void drawDrawables(ArrayList<IDrawable>[] drawables, ScreenGame game)
	{
		for (Movable m: Game.movables)
		{
			drawables[m.drawLevel].add(m);

			if (m.showName)
				drawables[m.nameTag.drawLevel].add(m.nameTag);
		}

		for (Obstacle o : Game.obstacles)
		{
			if (!o.batchDraw || !Game.enable3d)
				drawables[o.drawLevel].add(o);
		}

		for (Effect e: Game.effects)
			drawables[e.drawLevel].add(e);

		for (int i = 0; i < drawables.length; i++)
		{
			for (IDrawable d: drawables[i])
			{
				if (d != null)
					d.draw();
			}

			if (Game.glowEnabled)
			{
				for (IDrawable d: drawables[i])
				{
					if (d instanceof IDrawableWithGlow && ((IDrawableWithGlow) d).isGlowEnabled())
						((IDrawableWithGlow) d).drawGlow();
				}
			}

			if (game != null) drawUI(i, game);
			drawables[i].clear();
		}
	}

	private static void drawUI(int i, ScreenGame g)
	{
		if (i != 9) return;

		if (Game.game.window.touchscreen)
		{
			TankPlayer.controlStick.draw();

			if (TankPlayer.shootStickEnabled && !TankPlayer.shootStickHidden)
				TankPlayer.shootStick.draw();
		}


		if (Game.playerTank != null && Game.playerTank.showTouchCircle())
		{
			Drawing.drawing.setColor(255, 127, 0, 63);
			Drawing.drawing.fillInterfaceOval(Drawing.drawing.gameToInterfaceCoordsX(Game.playerTank.posX),
					Drawing.drawing.gameToInterfaceCoordsY(Game.playerTank.posY),
					Game.playerTank.getTouchCircleSize(), Game.playerTank.getTouchCircleSize());
		}

		if (Game.playerTank != null && !Game.game.window.drawingShadow)
		{
			if (Level.isDark())
				Drawing.drawing.setColor(255, 255, 255, 50);
			else
				Drawing.drawing.setColor(0, 0, 0, 50);

			if (Game.playerTank.getDrawLifespan() > 0)
				Mine.drawRange2D(Game.playerTank.posX, Game.playerTank.posY, Game.playerTank.getDrawLifespan());

			if (Game.playerTank.getDrawRangeMin() > 0)
				Mine.drawRange2D(Game.playerTank.posX, Game.playerTank.posY, Game.playerTank.getDrawRangeMin(), true);

			if (Game.playerTank.getDrawRangeMax() > 0)
				Mine.drawRange2D(Game.playerTank.posX, Game.playerTank.posY, Game.playerTank.getDrawRangeMax());

			Game.playerTank.setDrawRanges(-1, -1, -1, true);
		}

		if (Game.playerTank != null && !Game.playerTank.destroy && Game.screen instanceof ScreenGame && !((ScreenGame) Game.screen).playing && Game.movables.contains(Game.playerTank))
		{
			double s = Game.startTime;

			if (g.cancelCountdown)
				s = 400;

			Game.playerTank.drawSpinny(s);
		}

		if (Game.playerTank != null && !Game.playerTank.destroy && Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).playing && Game.movables.contains(Game.playerTank) && Game.playerTank.invulnerabilityTimer > 0)
		{
			Game.playerTank.drawSpinny(Game.playerTank.invulnerabilityTimer);
		}

		if (Game.followingCam && Panel.panel.zoomTimer > 0.2 && g.selectedArcBullet)
		{
			Tank t = focusedTank();
			double x = t.posX + Math.cos(t.angle) * g.fcArcAim;
			double y = t.posY + Math.sin(t.angle) * g.fcArcAim;
			Drawing.drawing.setColor(t.colorR, t.colorG, t.colorB);
			Drawing.drawing.drawImage(t.angle + Math.PI / 4, "cursor.png",x, y, Math.max(5, Game.sampleObstacleHeight(x, y)) + 5, 100, 100);
		}
	}

	public static boolean shouldHide(Face f)
	{
		return (f.owner instanceof Tank && (((Tank) f.owner).canHide && ((Tank) f.owner).hidden)) || (f.owner instanceof TankAIControlled && ((TankAIControlled) f.owner).invisible);
	}

	public static Tank focusedTank()
	{
		ScreenGame g = getInstance();
		if (g == null || Game.playerTank == null)
			return Game.playerTank;

		if (Game.playerTank.destroy && g.spectatingTank != null)
			return g.spectatingTank;

		return Game.playerTank;
	}

	public static ScreenGame getInstance()
	{
		if (Game.screen instanceof ScreenGame)
			return (ScreenGame) Game.screen;
		if (Game.screen instanceof IGameOverlayScreen)
			return ((IGameOverlayScreen) Game.screen).getGameScreen();
		return null;
	}

	public static boolean isUpdatingGame()
	{
		ScreenGame g = getInstance();
		if (g == null)
			return false;
		return !g.paused || ScreenPartyHost.isServer || ScreenPartyLobby.isClient;
	}

	public void updateFreecam()
	{
		if (controlPlayer)
			return;

		if (!Game.followingCam || focusedTank() == null || focusedTank().destroy || finishedQuick)
			freecam = false;

		int fwd = Game.game.input.moveUp.isPressed() ? 1 : 0;
		int bwd = Game.game.input.moveDown.isPressed() ? -1 : 0;
		int left = Game.game.input.moveLeft.isPressed() ? -1 : 0;
		int right = Game.game.input.moveRight.isPressed() ? 1 : 0;
		double speed = Game.game.window.pressedKeys.contains(InputCodes.KEY_R) && fwd != 0 ? 2 : 1;
		boolean up = Game.game.window.pressedKeys.contains(InputCodes.KEY_SPACE);
		boolean down = Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT);

		if (up && down)
			speed *= 0.5;

		if (fwd == 0 && bwd == 0 && left == 0 && right == 0)
			speed = 0;

		double angle = yaw + Math.PI / 2;
		double strafe = fwd + bwd == 0 ? 1 : (fwd + bwd == 1 ? 0.5 : -0.5);

		if (fwd + bwd == -1) angle -= Math.PI;
		if (left + right == 1) angle += Math.PI / 2 * strafe;
		else if (left + right == -1) angle -= Math.PI / 2 * strafe;

		x += Math.cos(angle) * speed * (Panel.frameFrequency / 150);
		y += Math.sin(angle) * speed * (Panel.frameFrequency / 150);

		yaw = (yaw + (Drawing.drawing.getInterfaceMouseX() - prevCursorX) / 1000) % (Math.PI * 2);
		pitchAdd = (pitchAdd + (Drawing.drawing.getInterfaceMouseY() - prevCursorY) / 1000) % (Math.PI * 2);
		roll = 0.5 * Math.sin(yaw);
		pitchAdd = Math.max(-Math.PI / 2, Math.min(Math.PI / 2, pitchAdd));
		pitch = 0.5 * -Math.cos(Math.PI / 3 * yaw) + pitchAdd;

		if (down)
			z += Panel.frameFrequency / 200;

		if (up)
			z -= Panel.frameFrequency / 200;

		Game.game.window.setCursorPos(Panel.windowWidth / 2, Panel.windowHeight / 2);
		updateMousePos();
	}

	@Override
	public void onFocusChange(boolean focused)
	{
		if (!focused && Panel.pauseOnDefocus && ((Game.autostart && !cancelCountdown) || (playing && !shopScreen && !npcShopScreen)))
			paused = true;
	}

	public void saveRemainingTanks()
	{
		if (!savedRemainingTanks && Crusade.crusadeMode && Crusade.currentCrusade != null)
		{
			Crusade.currentCrusade.livingTankIDs.clear();
			for (Movable m : Game.movables)
			{
				if (m instanceof Tank && !m.destroy && ((Tank) m).crusadeID >= 0)
					Crusade.currentCrusade.livingTankIDs.add(((Tank) m).crusadeID);
			}
		}
		savedRemainingTanks = true;
	}

	public void onPlayerDeath(Player p)
	{
		int remaining = 0;
		for (Movable m: Game.movables)
		{
			if (m instanceof IServerPlayerTank && !m.destroy)
				remaining++;
		}

		if (isVersus && ScreenPartyHost.isServer)
		{
			String s = "\u00A7255060000255%s was eliminated. %d players remain.";
			if (remaining == 1)
				s = "\u00A7255060000255%s was eliminated. %d player remains.";

			ChatMessage m = new ChatMessage(tanks.translation.Translation.translate(s, p.username, remaining));
			ScreenPartyHost.chat.add(0, m);
			Game.eventsOut.add(new EventChat(m.rawMessage));
			Drawing.drawing.playGlobalSound("hit_chain.ogg", (float) Math.pow(2, remaining * 1.0 / (ScreenPartyHost.includedPlayers.size() - 1) * -2 + 2), 0.5f);
		}
	}

	public void drawExclamation(Button b)
	{
		Drawing drawing = Drawing.drawing;
		Button.drawGlow(b.posX - b.sizeX / 2 + b.sizeY / 2, b.posY + 2.5 + 1, b.sizeY * 3 / 4, b.sizeY * 3 / 4, 0.6, 0, 0, 0, 100, false);
		drawing.setInterfaceFontSize(this.textSize / Drawing.drawing.interfaceScaleZoom);
		drawing.setColor(255, 127, 0);
		drawing.fillInterfaceOval(b.posX - b.sizeX / 2 + b.sizeY / 2, b.posY, b.sizeY * 3 / 4, b.sizeY * 3 / 4);
		drawing.setColor(255, 255, 255);
		Drawing.drawing.drawInterfaceText(b.posX - b.sizeX / 2 + b.sizeY / 2 + 0.5, b.posY, "!");
	}

	@Override
	public double getOffsetX()
	{
		return Drawing.drawing.getPlayerOffsetX();
	}

	@Override
	public double getOffsetY()
	{
		return Drawing.drawing.getPlayerOffsetY();
	}

	@Override
	public double getScale()
	{
		return Drawing.drawing.scale * (1 - Panel.panel.zoomTimer) + Drawing.drawing.interfaceScale * Panel.panel.zoomTimer;
	}

}
