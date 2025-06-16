package tanks.generator;

import tanks.Game;
import tanks.gui.screen.ScreenPartyHost;

import java.util.ArrayList;
import java.util.Random;

public class LevelGeneratorRandom extends LevelGenerator
{
	public static final LevelGeneratorRandom instance = new LevelGeneratorRandom();

	// Generation parameters
	public Random random;
	public double size;
	public int width, height, walls, time, numLights;
	public double randomNum, amountWalls, amountTanks;
	public double colorR, colorG, colorB;
	public double directLight, shadowLight;
	public boolean dark;

	// Terrain features
	public double heavyTerrain, bouncyWeight, noBounceWeight;
	public boolean bouncy, nobounce;

	// Beat blocks
	public boolean beatBlocks;
	public double beatBlocksWeight;
	public ArrayList<Integer> beatBlocksKinds;

	// Environmental features
	public boolean shrubs, mud, ice, snow, boostPanels, explosives;
	public int shrubCount, mudCount, iceCount, snowCount, boostCount, numExplosives;

	// Teleporters
	public boolean teleporters;
	public int numTeleporters, teleporterGroups;

	// Generation arrays
	public int[][] teleporterArray;
	public boolean[][] solid, cells;
	public double[][] cellWeights;
	public ArrayList<Integer[]> startPointsH, startPointsV;
	public int[] tankX, tankY, playerTankX, playerTankY;

	// Direction arrays for BFS
	public static final int[] dirX = {-1, 1, 0, 0};
	public static final int[] dirY = {0, 0, -1, 1};

	public static String generateLevelString()
	{
		return instance.generate(-1);
	}

	public static String generateLevelString(int seed)
	{
		return instance.generate(seed);
	}

	@Override
	public String generate(int seed)
	{
		configureSettings(seed);
		StringBuilder s = new StringBuilder();
		generateLevel(s);

		if (!validateConnectivity())
            return generate(seed);

		return s.toString();
	}

	public void generateLevel(StringBuilder s)
	{
		addLevelDetails(s);
		initializeArrays();

		generateWalls(s);
		generateTerrain(s);
		generateBoostPanels(s);
		generateExplosives(s);
		generateLights(s);
		generateTeleporters(s);

		s.append("|");

		generateTanks(s);
	}

	public void configureSettings(int seed)
	{
		initializeRandom(seed);
		calculateLevelSize();
		calculateDimensions();

		randomNum = random.nextDouble();
		amountWalls = 12 * size * size;
		amountTanks = 8 * size * size;
		walls = (int) (randomNum * amountWalls + 4);

		calculateColors();
		calculateLighting();
		dark = directLight < 50;
		numLights = (int) (walls / 5 + random.nextDouble() * 6 + 1);

		calculateTime();
		configureTerrainFeatures();
		configureBeatBlocks();
		configureEnvironmentalFeatures();
		configureTeleporters();
		configureExplosives();
	}

	public void addLevelDetails(StringBuilder s)
	{
		s.append("level\n{").append(width).append(",").append(height).append(",").append((int) colorR).append(",")
			.append((int) colorG).append(",").append((int) colorB).append(",20,20,20,").append(time).append(",")
			.append((int) directLight).append(",").append((int) shadowLight).append("|");
	}

	public void initializeRandom(int seed)
	{
		if (seed != -1)
			random = new Random(seed);
		else
			random = new Random();
	}

	public void calculateLevelSize()
	{
		size = Game.levelSize;

		if (random.nextDouble() < 0.3)
			size *= 2;

		if (Game.players.size() > 10)
			size *= 2;

		if (Game.players.size() > 40)
			size *= 2;

		if (random.nextDouble() < 0.3)
			size *= 2;
	}

	public void calculateDimensions()
	{
		height = (int)(18 * size);
		width = (int)(28 * size);
	}

	public void calculateColors()
	{
		int shade = 185;

		if (random.nextDouble() < 0.2)
			shade = (int) (random.nextDouble() * 60);

		colorR = (int)(random.nextDouble() * 50) + shade;
		colorG = (int)(random.nextDouble() * 50) + shade;
		colorB = (int)(random.nextDouble() * 50) + shade;
	}

	public void calculateLighting()
	{
		directLight = 100;
		double shadeFactor = 0.5;

		if (random.nextDouble() < 0.2)
		{
			directLight *= random.nextDouble() * 1.25;
		}

		if (random.nextDouble() < 0.2)
			shadeFactor = random.nextDouble() * 0.6 + 0.2;

		shadowLight = directLight * shadeFactor;
	}

	public void calculateTime()
	{
		time = (int) (randomNum * amountTanks + 4) * 5;

		if (random.nextDouble() > 0.2)
			time = 0;
		else
			time += (int) (45 * (size / Game.levelSize - 1));
	}

	public void configureTerrainFeatures()
	{
		heavyTerrain = 1;

		if (random.nextDouble() < 0.2)
			heavyTerrain *= 2;

		if (random.nextDouble() < 0.2)
			heavyTerrain *= 2;

		if (random.nextDouble() < 0.2)
			heavyTerrain *= 4;

		bouncy = random.nextDouble() < 0.2;
		bouncyWeight = random.nextDouble() * 0.5 + 0.2;

		nobounce = random.nextDouble() < 0.2;
		noBounceWeight = random.nextDouble() * 0.5 + 0.2;
	}

	public void configureBeatBlocks()
	{
		beatBlocks = random.nextDouble() < 0.2;
		beatBlocksWeight = random.nextDouble() * 0.5 + 0.2;
		beatBlocksKinds = new ArrayList<>();
		double br = random.nextDouble();
		if (br < 0.5)
			beatBlocksKinds.add(0);
		else if (br < 0.7)
			beatBlocksKinds.add(1);
		else if (br < 0.8)
			beatBlocksKinds.add(2);
		else if (br < 0.85)
		{
			beatBlocksKinds.add(0);
			beatBlocksKinds.add(1);
		}
		else if (br < 0.9)
		{
			beatBlocksKinds.add(0);
			beatBlocksKinds.add(2);
		}
		else if (br < 0.95)
		{
			beatBlocksKinds.add(1);
			beatBlocksKinds.add(2);
		}
		else
		{
			beatBlocksKinds.add(0);
			beatBlocksKinds.add(1);
			beatBlocksKinds.add(2);
		}

		if (random.nextDouble() < 0.05)
			beatBlocksKinds.add(3);
	}

	public void configureEnvironmentalFeatures()
	{
		shrubs = random.nextDouble() < 0.2;
		shrubCount = (int) (walls + random.nextDouble() * 4 - 2);

		mud = random.nextDouble() < 0.2;
		mudCount = (int) (walls + random.nextDouble() * 4 - 2);

		ice = random.nextDouble() < 0.2;
		iceCount = (int) (walls + random.nextDouble() * 4 - 2);

		snow = random.nextDouble() < 0.2;
		snowCount = (int) (walls + random.nextDouble() * 4 - 2);

		boostPanels = random.nextDouble() < 0.2;
		boostCount = (int) (walls + random.nextDouble() * 4 - 2);
	}

	public void configureTeleporters()
	{
		teleporters = random.nextDouble() < 0.1;
		numTeleporters = walls / 5 + 2;
		teleporterGroups = (int) ((numTeleporters - 1) * 0.5 * random.nextDouble()) + 1;
	}

	public void configureExplosives()
	{
		explosives = random.nextDouble() < 0.2;
		numExplosives = (int) (walls / 5 + random.nextDouble() * 4 + 1);
	}

	public void initializeArrays()
	{
		teleporterArray = new int[width][height];
		solid = new boolean[width][height];
		cells = new boolean[width][height];
		cellWeights = new double[width][height];
		startPointsH = new ArrayList<>();
		startPointsV = new ArrayList<>();

		for (int i = 0; i < teleporterArray.length; i++)
		{
			for (int j = 0; j < teleporterArray[0].length; j++)
			{
				teleporterArray[i][j] = -1;
			}
		}

		for (int i = 0; i < width; i++)
		{
			for (int j = 0; j < height; j++)
			{
				cellWeights[i][j] = 1;
			}
		}
	}

	public void generateWalls(StringBuilder s)
	{
		int vertical = 2;
		int horizontal = 2;

		for (int i = 0; i < walls; i++)
		{
			int l = 1 + (int) Math.max(1, (random.nextDouble() * (Math.min(height, width) - 3)));

			String[] wallType = determineWallType();
			String type = wallType[0];
			boolean passable = Boolean.parseBoolean(wallType[1]);

			if (random.nextDouble() * (vertical + horizontal) < horizontal)
			{
				generateHorizontalWall(l, type, passable, s, vertical++);
			}
			else
			{
				generateVerticalWall(l, type, passable, s, horizontal++);
			}

			if (i < walls - 1)
			{
				if (!s.toString().endsWith(","))
					s.append(",");
			}
		}
	}

	public String[] determineWallType()
	{
		String type = "";
		boolean passable = true;

		if (bouncy && random.nextDouble() < bouncyWeight)
			type = "-bouncy";
		else if (beatBlocks && random.nextDouble() < beatBlocksWeight)
		{
			type = "-beat-" + (int) ((beatBlocksKinds.get((int) (random.nextDouble() * beatBlocksKinds.size())) + random.nextDouble()) * 2);
			passable = true;
		}
		else if (nobounce && random.nextDouble() < noBounceWeight)
		{
			type = "-nobounce";
			passable = false;
		}
		else if (random.nextDouble() < 0.5)
		{
			type = "-hard";
			passable = false;
		}
		else if (random.nextDouble() < 0.25)
		{
			type = "-hole";
			passable = false;
		}
		else if (random.nextDouble() < 0.25)
		{
			type = "-breakable";
			passable = true;
		}

		return new String[]{type, String.valueOf(passable)};
	}

	public void generateHorizontalWall(int l, String type, boolean passable, StringBuilder s, int vertical)
	{
		int x = 0, y = 0, xEnd = 0, yEnd = 0, rand;
		Integer[] sp = null;

		if (random.nextDouble() < 0.25 || startPointsH.isEmpty())
		{
			for (int in = 0; in < 50; in++)
			{
				boolean chosen = false;

				int attempts = 0;
				while (!chosen && attempts < 100)
				{
					attempts++;

					x = (int) (random.nextDouble() * (width - l));
					y = (int) (random.nextDouble() * (height));
					xEnd = x + l;
					yEnd = y;

					double weight = 0;
					for (int x1 = x; x1 <= xEnd; x1++)
					{
						weight += cellWeights[x1][y];
					}
					weight /= (xEnd - x + 1);

					if (random.nextDouble() < weight)
						chosen = true;
				}

				boolean stop = false;

				for (int x1 = x - 2; x1 <= xEnd + 2; x1++)
				{
					for (int y1 = y - 2; y1 <= yEnd + 2; y1++)
					{
						if (cells[Math.max(0, Math.min(width-1, x1))][Math.max(0, Math.min(height-1, y1))])
						{
							stop = true;
							break;
						}
					}

					if (stop)
						break;
				}

				if (!stop)
					break;
			}
		}
		else
		{
			rand = (int) (random.nextDouble() * startPointsH.size());
			x = startPointsH.get(rand)[0] + 1;
			y = startPointsH.get(rand)[1];
			xEnd = x + l + 1;
			yEnd = y;
			sp = startPointsH.remove(rand);

			if ((random.nextDouble() < 0.5 && x > 1) || x >= width)
			{
				xEnd -= l + 2;
				x -= l + 2;
			}
		}

		x = Math.max(x, 0);
		xEnd = Math.min(xEnd, width - 1);

		if (sp == null || sp[0] != x || sp[1] != y)
			startPointsV.add(new Integer[]{x, y});

		if (sp == null || sp[0] != xEnd || sp[1] != yEnd)
			startPointsV.add(new Integer[]{xEnd, yEnd});

		boolean started = false, stopped = false;

		for (int z = x; z <= xEnd; z++)
		{
			if (!cells[z][y])
			{
				if (!started)
				{
					if (stopped)
					{
						s.append("-").append(y).append(type).append(",");
						stopped = false;
					}

					s.append(z).append("...");
					started = true;
				}

				cells[z][y] = true;
				solid[z][y] = solid[z][y] || !passable;
			}
			else
			{
				if (started)
				{
					started = false;
					stopped = true;
					s.append(z - 1);
				}
			}
		}

		if (started)
			s.append(xEnd);

		if (started || stopped)
			s.append("-").append(y).append(type);

		for (int j = Math.max(0, x - 5); j <= Math.min(xEnd + 5, width - 1); j++)
		{
			for (int k = Math.max(0, y - 5); k <= Math.min(yEnd + 5, height - 1); k++)
			{
				cellWeights[j][k] /= 2;
			}
		}
	}

	public void generateVerticalWall(int l, String type, boolean passable, StringBuilder s, int horizontal)
	{
		int x = 0, y = 0, xEnd = 0, yEnd = 0, rand;
		Integer[] sp = null;

		if (random.nextDouble() < 0.25 || startPointsV.isEmpty())
		{
			for (int in = 0; in < 50; in++)
			{
				boolean chosen = false;

				int attempts = 0;
				while (!chosen && attempts < 100)
				{
					attempts++;

					x = (int) (random.nextDouble() * (width));
					y = (int) (random.nextDouble() * (height - l));
					xEnd = x;
					yEnd = y + l;

					double weight = 0;
					for (int y1 = y; y1 <= yEnd; y1++)
					{
						weight += cellWeights[x][y1];
					}
					weight /= (yEnd - y + 1);

					if (random.nextDouble() < weight)
						chosen = true;
				}

				boolean stop = false;

				for (int x1 = x - 2; x1 <= xEnd + 2; x1++)
				{
					for (int y1 = y - 2; y1 <= yEnd + 2; y1++)
					{
						if (cells[Math.max(0, Math.min(width - 1, x1))][Math.max(0, Math.min(height - 1, y1))])
						{
							stop = true;
							break;
						}
					}

					if (stop)
						break;
				}

				if (!stop)
					break;
			}
		}
		else
		{
			rand = (int) (random.nextDouble() * startPointsV.size());
			x = startPointsV.get(rand)[0];
			y = startPointsV.get(rand)[1] + 1;
			xEnd = x;
			yEnd = y + l + 1;
			sp = startPointsV.remove(rand);

			if ((random.nextDouble() < 0.5 && y > 1) || y >= height)
			{
				yEnd -= l + 2;
				y -= l + 2;
			}
		}

		y = Math.max(y, 0);
		yEnd = Math.min(yEnd, height - 1);

		if (sp == null || sp[0] != x || sp[1] != y)
			startPointsH.add(new Integer[]{x, y});

		if (sp == null || sp[0] != xEnd || sp[1] != yEnd)
			startPointsH.add(new Integer[]{xEnd, yEnd});

		boolean started = false, stopped = false;

		for (int z = y; z <= yEnd; z++)
		{
			if (!cells[x][z])
			{
				if (!started)
				{
					if (stopped)
					{
						s.append(type).append(",");
						stopped = false;
					}

					s.append(x).append("-").append(z).append("...");
					started = true;
				}

				cells[x][z] = true;
				solid[x][z] = solid[x][z] || !passable;
			}
			else
			{
				if (started)
				{
					s.append(z - 1);
					started = false;
					stopped = true;
				}
			}
		}

		if (started)
			s.append(yEnd);

		if (started || stopped)
			s.append(type);

		for (int j = Math.max(0, x - 5); j <= Math.min(xEnd + 5, width - 1); j++)
		{
			for (int k = Math.max(0, y - 5); k <= Math.min(yEnd + 5, height - 1); k++)
			{
				cellWeights[j][k] /= 2;
			}
		}
	}

	public void generateTerrain(StringBuilder s)
	{
		if (shrubs)
			generateShrubs(s);

		if (mud)
			generateMud(s);

		if (ice)
			generateIce(s);

		if (snow)
			generateSnow(s);
	}

	public void generateShrubs(StringBuilder s)
	{
		for (int j = 0; j < shrubCount; j++)
		{
			int x = (int) (random.nextDouble() * width);
			int y = (int) (random.nextDouble() * height);

			for (int i = 0; i < (random.nextDouble() * 20 + 4) * heavyTerrain; i++)
			{
				if (x < width && y < height && x >= 0 && y >= 0 && !cells[x][y])
				{
					cells[x][y] = true;

					if (!s.toString().endsWith(","))
						s.append(",");

					s.append(x).append("-").append(y).append("-shrub");
				}

				double rand = random.nextDouble();

				if (rand < 0.25)
					x++;
				else if (rand < 0.5)
					x--;
				else if (rand < 0.75)
					y++;
				else
					y--;
			}
		}
	}

	public void generateMud(StringBuilder s)
	{
		for (int j = 0; j < mudCount; j++)
		{
			int x = (int) (random.nextDouble() * width);
			int y = (int) (random.nextDouble() * height);

			for (int i = 0; i < (random.nextDouble() * 20 + 4) * heavyTerrain; i++)
			{
				if (x < width && y < height && x >= 0 && y >= 0 && !cells[x][y])
				{
					cells[x][y] = true;

					if (!s.toString().endsWith(","))
						s.append(",");

					s.append(x).append("-").append(y).append("-mud");
				}

				double rand = random.nextDouble();

				if (rand < 0.25)
					x++;
				else if (rand < 0.5)
					x--;
				else if (rand < 0.75)
					y++;
				else
					y--;
			}
		}
	}

	public void generateIce(StringBuilder s)
	{
		for (int j = 0; j < iceCount; j++)
		{
			int x = (int) (random.nextDouble() * width);
			int y = (int) (random.nextDouble() * height);

			for (int i = 0; i < (random.nextDouble() * 40 + 8) * heavyTerrain; i++)
			{
				if (x < width && y < height && x >= 0 && y >= 0 && !cells[x][y])
				{
					cells[x][y] = true;

					if (!s.toString().endsWith(","))
						s.append(",");

					s.append(x).append("-").append(y).append("-ice");
				}

				double rand = random.nextDouble();

				if (rand < 0.25)
					x++;
				else if (rand < 0.5)
					x--;
				else if (rand < 0.75)
					y++;
				else
					y--;
			}
		}
	}

	public void generateSnow(StringBuilder s)
	{
		for (int j = 0; j < snowCount; j++)
		{
			int x = (int) (random.nextDouble() * width);
			int y = (int) (random.nextDouble() * height);

			for (int i = 0; i < (random.nextDouble() * 40 + 8) * heavyTerrain; i++)
			{
				if (x < width && y < height && x >= 0 && y >= 0 && !cells[x][y])
				{
					cells[x][y] = true;

					if (!s.toString().endsWith(","))
						s.append(",");

					s.append(x).append("-").append(y).append("-snow");
				}

				double rand = random.nextDouble();

				if (rand < 0.25)
					x++;
				else if (rand < 0.5)
					x--;
				else if (rand < 0.75)
					y++;
				else
					y--;
			}
		}
	}

	public void generateBoostPanels(StringBuilder s)
	{
		if (boostPanels)
		{
			for (int j = 0; j < boostCount; j++)
			{
				int x1 = (int) (random.nextDouble() * width);
				int y1 = (int) (random.nextDouble() * height);

				int panelSize = (int)(random.nextDouble() * 3) + 1;

				for (int x = x1; x < x1 + panelSize; x++)
				{
					for (int y = y1; y < y1 + panelSize; y++)
					{
						if (x < width && y < height && x >= 0 && y >= 0 && !cells[x][y])
						{
							cells[x][y] = true;

							if (!s.toString().endsWith(","))
								s.append(",");

							s.append(x).append("-").append(y).append("-boostpanel");
						}
					}
				}
			}
		}
	}

	public void generateExplosives(StringBuilder s)
	{
		if (explosives)
		{
			for (int j = 0; j < numExplosives; j++)
			{
				int x = (int) (random.nextDouble() * width);
				int y = (int) (random.nextDouble() * height);

				if (x < width && y < height && x >= 0 && y >= 0 && !cells[x][y])
				{
					cells[x][y] = true;

					if (!s.toString().endsWith(","))
						s.append(",");

					s.append(x).append("-").append(y).append("-explosive");
				}
			}
		}
	}

	public void generateLights(StringBuilder s)
	{
		if (dark)
		{
			for (int j = 0; j < numLights; j++)
			{
				int x = (int) (random.nextDouble() * width);
				int y = (int) (random.nextDouble() * height);

				if (x < width && y < height && x >= 0 && y >= 0 && !cells[x][y])
				{
					cells[x][y] = true;

					if (!s.toString().endsWith(","))
						s.append(",");

					s.append(x).append("-").append(y).append("-light-").append((int)(random.nextDouble() * 5 + 1) / 2.0);
				}
			}
		}
	}

	public void generateTeleporters(StringBuilder s)
	{
		if (teleporters)
		{
			int n = numTeleporters;
			int groupProgress = 0;

			while (n > 0)
			{
				int x = (int) (random.nextDouble() * width);
				int y = (int) (random.nextDouble() * height);

				if (!cells[x][y])
				{
					for (int i = Math.max(x - 2, 0); i <= Math.min(x + 2, width - 1); i++)
						for (int j = Math.max(y - 2, 0); j <= Math.min(y + 2, height - 1); j++)
							cells[i][j] = true;

					if (!s.toString().endsWith(","))
						s.append(",");

					int id = groupProgress / 2;

					if (n == 1)
						id = (groupProgress - 1) / 2;

					groupProgress++;

					if (id >= teleporterGroups)
						id = (int) (random.nextDouble() * teleporterGroups);

					s.append(x).append("-").append(y).append("-teleporter");
					teleporterArray[x][y] = id;

					if (id != 0)
						s.append("-").append(id);

					n--;
				}
			}
		}
	}

	public void generateTanks(StringBuilder s)
	{
		int numTanks = (int) (randomNum * amountTanks + 1);
		tankX = new int[numTanks];
		tankY = new int[numTanks];

		int numPlayers = 1;

		if (ScreenPartyHost.isServer)
			numPlayers = Game.players.size();

		playerTankX = new int[numPlayers];
		playerTankY = new int[numPlayers];

		generatePlayerTanks(numPlayers, s);
		generateEnemyTanks(numTanks, s);
	}

	public void generatePlayerTanks(int numPlayers, StringBuilder s)
	{
		for (int i = 0; i < numPlayers; i++)
		{
			int angle = (int) (random.nextDouble() * 4);
			int x = (int) (random.nextDouble() * (width));
			int y = (int) (random.nextDouble() * (height));

			int attempts2 = 0;
			while (cells[x][y] && attempts2 < 100)
			{
				attempts2++;
				x = (int) (random.nextDouble() * width);
				y = (int) (random.nextDouble() * height);
			}

			int bound = calculatePlayerBound(numPlayers);

			for (int a = -bound; a <= bound; a++)
				for (int j = -bound; j <= bound; j++)
					cells[Math.max(0, Math.min(width - 1, x+a))][Math.max(0, Math.min(height - 1, y+j))] = true;

			s.append(x).append("-").append(y).append("-");
			s.append("player");
			s.append("-").append(angle);

			playerTankX[i] = x;
			playerTankY[i] = y;

			s.append(",");
		}
	}

	public int calculatePlayerBound(int numPlayers)
	{
		if (numPlayers < 20)
			return 2;
		else if (numPlayers < 56)
			return 1;
		else
			return 0;
	}

	public void generateEnemyTanks(int numTanks, StringBuilder s)
	{
		for (int i = 0; i < numTanks; i++)
		{
			int angle = (int) (random.nextDouble() * 4);
			int x = (int) (random.nextDouble() * (width));
			int y = (int) (random.nextDouble() * (height));

			int attempts2 = 0;
			while (cells[x][y] && attempts2 < 100)
			{
				attempts2++;
				x = (int) (random.nextDouble() * (width));
				y = (int) (random.nextDouble() * (height));
			}

			for (int a = -1; a <= 1; a++)
				for (int j = -1; j <= 1; j++)
					cells[Math.max(0, Math.min(width - 1, x+a))][Math.max(0, Math.min(height - 1, y+j))] = true;

			s.append(x).append("-").append(y).append("-");
			s.append(Game.registryTank.getRandomTank(random).name);
			s.append("-").append(angle);

			tankX[i] = x;
			tankY[i] = y;

			if (i == numTanks - 1)
			{
				s.append("}");
			}
			else
			{
				s.append(",");
			}
		}
	}

	public boolean validateConnectivity()
	{
		ArrayList<Integer> queueX = new ArrayList<>();
		ArrayList<Integer> queueY = new ArrayList<>();

		queueX.add(playerTankX[0]);
		queueY.add(playerTankY[0]);

		while (!queueX.isEmpty())
		{
			int x = queueX.remove(0);
			int y = queueY.remove(0);

			solid[x][y] = true;

			// Handle teleporter connections
			if (teleporterArray[x][y] >= 0)
			{
				int id = teleporterArray[x][y];
				for (int i = 0; i < width; i++)
				{
					for (int j = 0; j < height; j++)
					{
						if (teleporterArray[i][j] == id && !(x == i && y == j) && !solid[i][j])
						{
							queueX.add(i);
							queueY.add(j);
						}
					}
				}
			}

			// Check all 4 directions using direction arrays
			for (int d = 0; d < 4; d++)
			{
				int nx = x + dirX[d];
				int ny = y + dirY[d];

				if (nx >= 0 && nx < width && ny >= 0 && ny < height && !solid[nx][ny])
				{
					queueX.add(nx);
					queueY.add(ny);
					solid[nx][ny] = true;
				}
			}
		}

		// Check if all tanks are reachable
		for (int i = 0; i < tankX.length; i++)
		{
			if (!solid[tankX[i]][tankY[i]])
				return false;
		}

		for (int i = 1; i < playerTankX.length; i++)
		{
			if (!solid[playerTankX[i]][playerTankY[i]])
				return false;
		}

		return true;
	}
}