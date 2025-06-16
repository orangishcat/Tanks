package tanks.generator;

import tanks.Game;
import tanks.item.Item;
import tanks.translation.Translation;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class LevelGeneratorVersus extends LevelGeneratorRandom
{
	public static final LevelGeneratorVersus instance = new LevelGeneratorVersus();

	public static String generateLevelString() {
		return instance.generate(-1);
	}

	@Override
	public String generate(int seed)
	{
		configureSettings(seed);
		StringBuilder s = new StringBuilder();
		addShopItems(s);
		generateLevel(s);

		if (!validateVersusConnectivity())
            return generate(seed);

		return s.toString();
	}

	public void calculateColors()
	{
		int shade = 185;

		if (random.nextDouble() < 0.2)
			shade = 30;

		colorR = (int)(random.nextDouble() * 50) + shade;
		colorG = (int)(random.nextDouble() * 50) + shade;
		colorB = (int)(random.nextDouble() * 50) + shade;
	}

	@Override
	public void calculateTime()
	{
		time = (int) (random.nextDouble() * 24 + 12 * size) * 5;

		if (random.nextDouble() > 0.2)
			time = 0;
	}

	public void addShopItems(StringBuilder s)
	{
		s.append("coins\n50\nshop\n");
		ArrayList<String> items = Game.game.fileManager.getInternalFileContents("/items/items.tanks");

		for (String si: items)
		{
			Item.ItemStack<?> i = Item.ItemStack.fromString(null, si);
			int price;

			switch (i.item.name)
			{
				case "Fire bullet":
                case "Block":
                case "Mini bullet":
                case "Artillery shell":
                    price = 5;
					break;
				case "Bouncy fire bullet":
                case "Dark fire bullet":
                case "Booster":
                case "Explosive bullet":
                case "Freezing bullet":
                    price = 10;
					break;
				case "Mega mine":
                case "Homing bullet":
                case "Healing ray":
                    price = 25;
					break;
				case "Zap":
                case "Mega bullet":
                    price = 15;
					break;
				case "Shield":
					price = 50;
					break;
                case "Flamethrower":
					price = 4;
					break;
                case "Air":
					price = 8;
					break;
                default:
					continue;
			}

			i.item.name = Translation.translate(i.item.name);
			Item.ShopItem shopItem = new Item.ShopItem(i);
			shopItem.price = price;
			s.append(shopItem).append("\n");
		}
	}

	public void generateTanks(StringBuilder s)
	{
		int numPlayers = Game.players.size();
		int[] playerX = new int[numPlayers - 1];
		int[] playerY = new int[numPlayers - 1];
		int firstPlayerX = 0;
		int firstPlayerY = 0;

		for (int i = 0; i < numPlayers; i++)
		{
			int angle = (int) (random.nextDouble() * 4);
			int x = (int) (random.nextDouble() * (width));
			int y = (int) (random.nextDouble() * (height));

			int attempts1 = 0;
			while (cells[x][y] && attempts1 < 100)
			{
				attempts1++;
				x = (int) (random.nextDouble() * (width));
				y = (int) (random.nextDouble() * (height));
			}

			int bound = calculateVersusPlayerBound(numPlayers);

			for (int a = -bound; a <= bound; a++)
				for (int j = -bound; j <= bound; j++)
					cells[Math.max(0, Math.min(width - 1, x+a))][Math.max(0, Math.min(height - 1, y+j))] = true;

			s.append(x).append("-").append(y).append("-");
			s.append("player");
			s.append("-").append(angle);

			if (i == 0)
			{
				firstPlayerX = x;
				firstPlayerY = y;
			}
			else
			{
				playerX[i - 1] = x;
				playerY[i - 1] = y;
			}

			if (i == numPlayers - 1)
			{
				s.append("|ally-true}");
			}
			else
			{
				s.append(",");
			}
		}

		// Store tank positions for validation
		playerTankX = new int[]{firstPlayerX};
		playerTankY = new int[]{firstPlayerY};
		tankX = playerX;
		tankY = playerY;
	}

	public int calculateVersusPlayerBound(int numTanks)
	{
		if (numTanks < 4)
			return 8;
		else if (numTanks < 6)
			return 4;
		else if (numTanks < 10)
			return 3;
		else if (numTanks < 20)
			return 2;
		else if (numTanks < 56)
			return 1;
		else
			return 0;
	}

	public boolean validateVersusConnectivity()
	{
		ArrayDeque<Integer> queueX = new ArrayDeque<>();
		ArrayDeque<Integer> queueY = new ArrayDeque<>();

		queueX.add(playerTankX[0]);
		queueY.add(playerTankY[0]);

		while (!queueX.isEmpty())
		{
			int x = queueX.remove();
			int y = queueY.remove();

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

		// Check if all other player tanks are reachable
		for (int i = 0; i < tankX.length; i++)
		{
			if (!solid[tankX[i]][tankY[i]])
				return false;
		}

		return true;
	}
}
