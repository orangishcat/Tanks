package tanks.gui.screen;

import basewindow.BaseFile;
import com.codedisaster.steamworks.SteamMatchmaking;
import tanks.*;
import tanks.gui.Button;
import tanks.tank.*;
import tanks.translation.Translation;

import java.io.*;
import java.util.Date;

public class ScreenOptions extends Screen
{
	public static final String onText = "\u00A7000200000255on";
	public static final String offText = "\u00A7200000000255off";

	public static boolean alwaysDebug = false;

	TankPlayer preview = new TankPlayer(0, 0, 0);

	public ScreenOptions()
	{
		this.music = "menu_options.ogg";
		this.musicID = "menu";

		if (!Game.game.window.soundsEnabled)
		{
			soundOptions.enabled = false;
			soundOptions.setHoverText("Sound is disabled because there---are no sound devices connected------To use sound, connect a sound---device and restart the game");
			soundOptions.enableHover = true;
		}

		this.openFolder.image = "icons/folder.png";
		this.openFolder.imageSizeX = 30;
		this.openFolder.imageSizeY = 30;
		this.openFolder.imageXOffset = -145;
	}

	Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () ->
	{
		saveOptions(Game.homedir);

		if (ScreenPartyHost.isServer)
			Game.screen = ScreenPartyHost.activeScreen;
		else if (ScreenPartyLobby.isClient)
			Game.screen = new ScreenPartyLobby();
		else
			Game.screen = new ScreenTitle();
	}
	);


	Button multiplayerOptions = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Multiplayer options", () -> Game.screen = new ScreenOptionsMultiplayer());

	Button graphicsOptions = new Button(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Graphics options", () -> Game.screen = new ScreenOptionsGraphics());

	Button soundOptions = new Button(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Sound options", () -> Game.screen = new ScreenOptionsSound());

	Button inputOptions = new Button(this.centerX - this.objXSpace / 2, this.centerY, this.objWidth, this.objHeight, "Input options", () ->
	{
		if (Game.game.window.touchscreen)
			Game.screen = new ScreenOptionsInputTouchscreen();
		else
			Game.screen = ScreenOverlayControls.lastControlsScreen;
	});

	Button personalize = new Button(this.centerX, this.centerY - this.objYSpace * 2.4, this.objWidth * 1.5, this.objHeight * 2, "", () ->
	{
		if (ScreenPartyHost.isServer || ScreenPartyLobby.isClient)
			Game.screen = new ScreenOptionsPlayerColor();
		else
			Game.screen = new ScreenOptionsPersonalize();
	});

	Button interfaceOptions = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 0, this.objWidth, this.objHeight, "Window options", () -> Game.screen = new ScreenOptionsWindow());
	Button interfaceOptionsMobile = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 0, this.objWidth, this.objHeight, "Interface options", () -> Game.screen = new ScreenOptionsWindowMobile());

	Button speedrunOptions = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Speedrunning options", () -> Game.screen = new ScreenOptionsSpeedrun());

	Button miscOptions = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Miscellaneous options", () -> Game.screen = new ScreenOptionsMisc());

	Button openFolder = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Open game folder", () ->
	{
		Game.game.fileManager.openFileManager(Game.homedir + Game.directoryPath);
	});


	@Override
	public void update()
	{
		soundOptions.update();

		if (Game.framework == Game.Framework.libgdx)
			interfaceOptionsMobile.update();
		else
			interfaceOptions.update();

		speedrunOptions.update();

		graphicsOptions.update();
		inputOptions.update();
		multiplayerOptions.update();
		personalize.update();

		miscOptions.update();
		openFolder.update();

		back.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();
		back.draw();
		miscOptions.draw();
		multiplayerOptions.draw();
		inputOptions.draw();
		graphicsOptions.draw();
		speedrunOptions.draw();
		openFolder.draw();

		if (Game.framework == Game.Framework.libgdx)
			interfaceOptionsMobile.draw();
		else
			interfaceOptions.draw();

		soundOptions.draw();
		personalize.draw();

		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Options");

		if (Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, Game.player.username) / Drawing.drawing.interfaceScale > personalize.sizeX - 240)
			Drawing.drawing.setInterfaceFontSize(this.titleSize * (personalize.sizeX - 240) / (Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, Game.player.username) / Drawing.drawing.interfaceScale));

		if (Game.player.color.red + Game.player.color.green + Game.player.color.blue >= 380 && Game.player.username.length() >= 1)
		{
			Drawing.drawing.setColor(127, 127, 127);
			double s = Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, Game.player.username) / Drawing.drawing.interfaceScale;
			double z = this.objHeight / 40;
			Drawing.drawing.fillInterfaceRect(personalize.posX, personalize.posY + personalize.sizeY * 0.1, s, z * 40);
			Drawing.drawing.fillInterfaceOval(personalize.posX - (s) / 2, personalize.posY + personalize.sizeY * 0.1, z * 40, z * 40);
			Drawing.drawing.fillInterfaceOval(personalize.posX + (s) / 2, personalize.posY + personalize.sizeY * 0.1, z * 40, z * 40);
		}

		preview.drawForInterface(personalize.posX - personalize.sizeX / 2 + personalize.sizeY * 0.7, personalize.posY, objHeight / 40);

		Drawing.drawing.setColor(Game.player.color2);
		Drawing.drawing.drawInterfaceText(personalize.posX + 2, personalize.posY + personalize.sizeY * 0.1 + 2, Game.player.username);
		Drawing.drawing.setColor(Game.player.color3);
		Drawing.drawing.drawInterfaceText(personalize.posX + 1, personalize.posY + personalize.sizeY * 0.1 + 1, Game.player.username);
		Drawing.drawing.setColor(Game.player.color);
		Drawing.drawing.drawInterfaceText(personalize.posX, personalize.posY + personalize.sizeY * 0.1, Game.player.username);

		if (Game.player.username.length() < 1)
		{
			Drawing.drawing.setColor(127, 127, 127);
			Drawing.drawing.displayInterfaceText(personalize.posX, personalize.posY + personalize.sizeY * 0.1, "Pick a username...");
		}

		Drawing.drawing.setInterfaceFontSize(this.titleSize * 0.65);
		Drawing.drawing.setColor(80, 80, 80);
		Drawing.drawing.displayInterfaceText(personalize.posX, personalize.posY - personalize.sizeY * 0.3, "My profile");
	}

	public static void initOptions(String homedir)
	{
		String path = homedir + Game.optionsPath;

		try
		{
			Game.game.fileManager.getFile(path).create();
		}
		catch (IOException e)
		{
			Game.logger.println (new Date().toString() + " (syserr) file permissions are broken! cannot initialize options file.");
			System.exit(1);
		}

		saveOptions(homedir);
	}

	public static void saveOptions(String homedir)
	{
		String path = homedir + Game.optionsPath;

		try
		{
			boolean fullscreen = Game.options.window.fullscreen;

			if (Game.game.window != null)
				fullscreen = Game.game.window.fullscreen;

			BaseFile f = Game.game.fileManager.getFile(path);
			f.startWriting();
			f.println("# This file stores game settings that you have set");
			f.println("username=" + Game.player.username);
			f.println("fancy_terrain=" + Game.options.graphics.fancyTerrain);
			f.println("effects=" + Game.options.graphics.effect.particleEffects);
			f.println("effect_multiplier=" + (int) Math.round(Game.options.graphics.effect.particlePercentage * 100));
			f.println("bullet_trails=" + (Game.options.graphics.bulletTrails != GameOptions.BulletTrails.off));
			f.println("fancy_bullet_trails=" + (Game.options.graphics.bulletTrails == GameOptions.BulletTrails.fancy));
			f.println("glow=" + Game.options.graphics.glowEnabled);
			f.println("3d=" + Game.options.graphics.enable3d);
			f.println("3d_ground=" + Game.options.graphics.enable3dBg);
			f.println("shadows_enabled=" + Game.options.graphics.shadow.shadowsEnabled);
			f.println("shadow_quality=" + Game.options.graphics.shadow.shadowQuality);
			f.println("vsync=" + Game.options.graphics.vsync);
			f.println("max_fps=" + Game.options.graphics.maxFps);
			f.println("antialiasing=" + Game.options.graphics.antialiasing);
			f.println("perspective=" + ScreenOptionsGraphics.viewNo);
			f.println("preview_crusades=" + Game.options.misc.previewCrusades);
			f.println("tank_textures=" + Game.options.graphics.tankTextures);
			f.println("xray_bullets=" + Game.options.graphics.xrayBullets);
			f.println("circular_hotbar=" + Game.options.misc.circularHotbar);
			f.println("mouse_target=" + Panel.showMouseTarget);
			f.println("mouse_target_height=" + Panel.showMouseTargetHeight);
			f.println("constrain_mouse=" + Game.options.window.constrainMouse);
			f.println("fullscreen=" + fullscreen);
			f.println("vibrations=" + Game.options.other.enableVibrations);
			f.println("mobile_joystick=" + TankPlayer.controlStickMobile);
			f.println("snap_joystick=" + TankPlayer.controlStickSnap);
			f.println("dual_joystick=" + TankPlayer.shootStickEnabled);
			f.println("sound=" + Game.soundsEnabled);
			f.println("sound_volume=" + Game.options.sound.soundVolume);
			f.println("music=" + Game.musicEnabled);
			f.println("music_volume=" + Game.options.sound.musicVolume);
			f.println("layered_music=" + Game.options.sound.enableLayeredMusic);
			f.println("auto_start=" + Game.options.misc.autoStart);
			f.println("full_stats=" + Game.options.misc.fullStats);
			f.println("timer=" + Game.options.speedrun.showSpeedrunTimer);
			f.println("best_run=" + Game.options.speedrun.showBestTime);
			f.println("deterministic=" + (Game.options.speedrun.deterministicMode != GameOptions.Deterministic.off));
			f.println("deterministic_30fps=" + (Game.options.speedrun.deterministicMode == GameOptions.Deterministic._30fps));
			f.println("warn_before_closing=" + Game.options.window.warnBeforeClosing);
			f.println("info_bar=" + Game.options.window.infoBar);
			f.println("port=" + Game.options.multiplayer.server.port);
			f.println("last_party=" + Game.options.multiplayer.server.lastParty);
			f.println("last_online_server=" + Game.options.multiplayer.server.lastOnlineServer);
			f.println("show_ip=" + Game.options.multiplayer.server.showIP);
			f.println("allow_ip_connections=" + Game.options.multiplayer.server.enableIPConnections);
			f.println("steam_visibility=" + Game.steamVisibility.ordinal());
			f.println("chat_filter=" + Game.options.multiplayer.chatFilter);
			f.println("auto_ready=" + Game.options.multiplayer.autoReady);
			f.println("anticheat=" + TankPlayerRemote.checkMotion);
			f.println("anticheat_weak=" + TankPlayerRemote.weakTimeCheck);
			f.println("disable_party_friendly_fire=" + Game.options.multiplayer.partyHost.disablePartyFriendlyFire);
			f.println("party_countdown=" + Game.options.multiplayer.partyHost.partyStartTime);
			f.println("party_bots=" + Game.options.multiplayer.partyHost.botPlayerCount);
			f.println("tank_secondary_color=" + Game.player.enableSecondaryColor);
			f.println("tank_tertiary_color=" + Game.player.enableTertiaryColor);
			f.println("tank_red=" + Game.player.color.red);
			f.println("tank_green=" + Game.player.color.green);
			f.println("tank_blue=" + Game.player.color.blue);
			f.println("tank_red_2=" + Game.player.color2.red);
			f.println("tank_green_2=" + Game.player.color2.green);
			f.println("tank_blue_2=" + Game.player.color2.blue);
			f.println("tank_red_3=" + Game.player.color3.red);
			f.println("tank_green_3=" + Game.player.color3.green);
			f.println("tank_blue_3=" + Game.player.color3.blue);
			f.println("translation=" + (Translation.currentTranslation == null ? "null" : Translation.currentTranslation.fileName));
			f.println("agreed_steam_workshop=" + Game.options.other.agreedToWorkshopAgreement);
			f.println("last_version=" + Game.lastVersion);
			f.println("enable_extensions=" + Game.options.misc.extension.enableExtensions);
			f.println("auto_load_extensions=" + Game.options.misc.extension.autoLoadExtensions);
			f.println("debug_mode=" + alwaysDebug);
			f.stopWriting();
		}
		catch (FileNotFoundException e)
		{
			Game.exitToCrash(e);
		}
	}

	public static void loadOptions(String homedir)
	{
		String path = homedir + Game.optionsPath;

		try
		{
			BaseFile f = Game.game.fileManager.getFile(path);
			f.startReading();
			while (f.hasNextLine())
			{
				String line = f.nextLine();
				String[] optionLine = line.split("=");

				if (optionLine[0].charAt(0) == '#')
				{
					continue;
				}

				switch (optionLine[0].toLowerCase())
				{
					case "username":
						if (optionLine.length >= 2)
							Game.player.username = optionLine[1];
						else
							Game.player.username = "";
						break;
					case "fancy_terrain":
						Game.options.graphics.fancyTerrain = Boolean.parseBoolean(optionLine[1]);
						break;
					case "effects":
						Game.options.graphics.effect.particleEffects = Boolean.parseBoolean(optionLine[1]);
						break;
					case "effect_multiplier":
						Game.options.graphics.effect.particlePercentage = Integer.parseInt(optionLine[1]) / 100.0;
						break;
					case "bullet_trail_quality":
                        Game.options.graphics.bulletTrails = GameOptions.BulletTrails.valueOf(optionLine[1]);
                        break;
					case "glow":
						Game.options.graphics.glowEnabled = Boolean.parseBoolean(optionLine[1]);
						break;
					case "3d":
						Game.options.graphics.enable3d = Boolean.parseBoolean(optionLine[1]);
						break;
					case "3d_ground":
						Game.options.graphics.enable3dBg = Boolean.parseBoolean(optionLine[1]);
						break;
					case "shadows_enabled":
						Game.options.graphics.shadow.shadowsEnabled = Boolean.parseBoolean(optionLine[1]);
						break;
					case "shadow_quality":
						Game.options.graphics.shadow.shadowQuality = Integer.parseInt(optionLine[1]);
						break;
					case "vsync":
						Game.options.graphics.vsync = Boolean.parseBoolean(optionLine[1]);
						break;
					case "max_fps":
						Game.options.graphics.maxFps = Integer.parseInt(optionLine[1]);
						break;
					case "antialiasing":
						Game.options.graphics.antialiasing = Boolean.parseBoolean(optionLine[1]);
						break;
					case "mouse_target":
						Panel.showMouseTarget = Boolean.parseBoolean(optionLine[1]);
						break;
					case "mouse_target_height":
						Panel.showMouseTargetHeight = Boolean.parseBoolean(optionLine[1]);
						break;
					case "constrain_mouse":
						Game.options.window.constrainMouse = Boolean.parseBoolean(optionLine[1]);
						break;
					case "vibrations":
						Game.options.other.enableVibrations = Boolean.parseBoolean(optionLine[1]);
						break;
					case "mobile_joystick":
						TankPlayer.controlStickMobile = Boolean.parseBoolean(optionLine[1]);
						break;
					case "snap_joystick":
						TankPlayer.controlStickSnap = Boolean.parseBoolean(optionLine[1]);
						break;
					case "dual_joystick":
						TankPlayer.setShootStick(Boolean.parseBoolean(optionLine[1]));
						break;
					case "sound":
						Game.soundsEnabled = Boolean.parseBoolean(optionLine[1]);
						break;
					case "music":
						Game.musicEnabled = Boolean.parseBoolean(optionLine[1]);
						break;
					case "layered_music":
						Game.options.sound.enableLayeredMusic = Boolean.parseBoolean(optionLine[1]);
						break;
					case "sound_volume":
						Game.options.sound.soundVolume = Float.parseFloat(optionLine[1]);
						break;
					case "music_volume":
						Game.options.sound.musicVolume =  Float.parseFloat(optionLine[1]);
						break;
					case "auto_start":
						Game.options.misc.autoStart = Boolean.parseBoolean(optionLine[1]);
						break;
					case "full_stats":
						Game.options.misc.fullStats = Boolean.parseBoolean(optionLine[1]);
						break;
					case "timer":
						Game.options.speedrun.showSpeedrunTimer = Boolean.parseBoolean(optionLine[1]);
						break;
					case "best_run":
						Game.options.speedrun.showBestTime = Boolean.parseBoolean(optionLine[1]);
						break;
                    case "deterministic":
						Game.options.speedrun.deterministicMode = GameOptions.Deterministic.valueOf(optionLine[1]);
						break;
					case "info_bar":
						Drawing.drawing.showStats(Boolean.parseBoolean(optionLine[1]));
						break;
					case "warn_before_closing":
						Game.options.window.warnBeforeClosing = Boolean.parseBoolean(optionLine[1]);
						break;
					case "perspective":
						ScreenOptionsGraphics.viewNo = Integer.parseInt(optionLine[1]);
						switch (ScreenOptionsGraphics.viewNo)
						{
							case 0:
								Game.options.graphics.angledView = false;
								Game.options.debug.followingCam = false;
								Game.options.debug.firstPerson = false;
								break;
							case 1:
								Game.options.graphics.angledView = true;
								Game.options.debug.followingCam = false;
								Game.options.debug.firstPerson = false;
								break;
							case 2:
								Game.options.graphics.angledView = false;
								Game.options.debug.followingCam = true;
								Game.options.debug.firstPerson = false;
								break;
							case 3:
								Game.options.graphics.angledView = false;
								Game.options.debug.followingCam = true;
								Game.options.debug.firstPerson = true;
						}
						break;
					case "tank_textures":
						Game.options.graphics.tankTextures = Boolean.parseBoolean(optionLine[1]);
						break;
					case "xray_bullets":
						Game.options.graphics.xrayBullets = Boolean.parseBoolean(optionLine[1]);
						break;
					case "circular_hotbar":
						Game.options.misc.circularHotbar = Boolean.parseBoolean(optionLine[1]);
						break;
					case "preview_crusades":
						Game.options.misc.previewCrusades = Boolean.parseBoolean(optionLine[1]);
						break;
					case "fullscreen":
						Game.options.window.fullscreen = Boolean.parseBoolean(optionLine[1]);
						break;
					case "port":
						Game.options.multiplayer.server.port = Integer.parseInt(optionLine[1]);
						break;
					case "last_party":
						if (optionLine.length >= 2)
							Game.options.multiplayer.server.lastParty = optionLine[1];
						else
							Game.options.multiplayer.server.lastParty = "";
						break;
					case "last_online_server":
						if (optionLine.length >= 2)
							Game.options.multiplayer.server.lastOnlineServer = optionLine[1];
						else
							Game.options.multiplayer.server.lastOnlineServer = "";
						break;
					case "show_ip":
						Game.options.multiplayer.server.showIP = Boolean.parseBoolean(optionLine[1]);
						break;
					case "allow_ip_connections":
						Game.options.multiplayer.server.enableIPConnections = Boolean.parseBoolean(optionLine[1]);
						break;
					case "steam_visibility":
						Game.steamVisibility = SteamMatchmaking.LobbyType.values()[Integer.parseInt(optionLine[1])];
						break;
					case "chat_filter":
						Game.options.multiplayer.chatFilter = Boolean.parseBoolean(optionLine[1]);
						break;
					case "auto_ready":
						Game.options.multiplayer.autoReady = Boolean.parseBoolean(optionLine[1]);
						break;
					case "anticheat":
						TankPlayerRemote.checkMotion = Boolean.parseBoolean(optionLine[1]);
						break;
					case "anticheat_weak":
						TankPlayerRemote.weakTimeCheck = Boolean.parseBoolean(optionLine[1]);
						break;
					case "disable_party_friendly_fire":
						Game.options.multiplayer.partyHost.disablePartyFriendlyFire = Boolean.parseBoolean(optionLine[1]);
						break;
					case "party_countdown":
						Game.options.multiplayer.partyHost.partyStartTime = Double.parseDouble(optionLine[1]);
						break;
					case "party_bots":
						Game.options.multiplayer.partyHost.botPlayerCount = Integer.parseInt(optionLine[1]);
						break;
					case "tank_secondary_color":
						Game.player.enableSecondaryColor = Boolean.parseBoolean(optionLine[1]);
						break;
					case "tank_tertiary_color":
						Game.player.enableTertiaryColor = Boolean.parseBoolean(optionLine[1]);
						break;
					case "tank_red":
						Game.player.color.red = Double.parseDouble(optionLine[1]);
						break;
					case "tank_green":
						Game.player.color.green = Double.parseDouble(optionLine[1]);
						break;
					case "tank_blue":
						Game.player.color.blue = Double.parseDouble(optionLine[1]);
						break;
					case "tank_red_2":
						Game.player.color2.red = Double.parseDouble(optionLine[1]);
						break;
					case "tank_green_2":
						Game.player.color2.green = Double.parseDouble(optionLine[1]);
						break;
					case "tank_blue_2":
						Game.player.color2.blue = Double.parseDouble(optionLine[1]);
						break;
					case "tank_red_3":
						Game.player.color3.red = Double.parseDouble(optionLine[1]);
						break;
					case "tank_green_3":
						Game.player.color3.green = Double.parseDouble(optionLine[1]);
						break;
					case "tank_blue_3":
						Game.player.color3.blue = Double.parseDouble(optionLine[1]);
						break;
					case "translation":
						Translation.setCurrentTranslation(optionLine[1]);
						break;
					case "last_version":
						Game.lastVersion = optionLine[1];
						break;
					case "enable_extensions":
						Game.options.misc.extension.enableExtensions = Boolean.parseBoolean(optionLine[1]);
						break;
					case "auto_load_extensions":
						Game.options.misc.extension.autoLoadExtensions = Boolean.parseBoolean(optionLine[1]);
						break;
					case "agreed_steam_workshop":
						Game.options.other.agreedToWorkshopAgreement = Boolean.parseBoolean(optionLine[1]);
						break;
					case "debug_mode":
						alwaysDebug = Boolean.parseBoolean(optionLine[1]);
						if (alwaysDebug)
							Game.debug = true;
						break;
				}
			}

			f.stopReading();

			if (Game.framework == Game.Framework.libgdx)
				Panel.showMouseTarget = false;

			if (!Game.soundsEnabled)
				Game.options.sound.soundVolume = 0;

			if (!Game.musicEnabled)
				Game.options.sound.musicVolume = 0;


			if (TankPlayerRemote.weakTimeCheck)
				TankPlayerRemote.anticheatMaxTimeOffset = TankPlayerRemote.anticheatStrongTimeOffset;
			else
				TankPlayerRemote.anticheatMaxTimeOffset = TankPlayerRemote.anticheatWeakTimeOffset;

			if (!Game.player.enableTertiaryColor)
			{
				Turret.setTertiary(Game.player.color, Game.player.color2, Game.player.color3);
			}
		}
		catch (Exception e)
		{
			Game.logger.println (new Date().toString() + " Options file is nonexistent or broken, using default:");
			e.printStackTrace(Game.logger);
			System.err.println("Failed to load options!");
			e.printStackTrace();
		}
	}
}
