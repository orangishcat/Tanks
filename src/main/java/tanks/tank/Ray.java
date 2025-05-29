package tanks.tank;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.gui.screen.ScreenGame;
import tanks.obstacle.Face;
import tanks.obstacle.ISolidObject;
import tanks.obstacle.Obstacle;

import java.util.Arrays;

public class Ray
{
	public static int chunksAdded;
	/** Caches the chunks to avoid creating new temp objects */
	public static Chunk[] chunkCache = new Chunk[40];
	/** Caches the ray to avoid creating new temp objects */
	public static Ray cacheRay = new Ray();

	public double size = 10;
	public double tankHitSizeMul = 1;

	public int bounces, bouncyBounces = 100;
	public double posX, posY, vX, vY, angle;
	public double startX, startY;

	public int maxChunkCheck = 12;

	public boolean enableBounciness = true;
	public boolean asBullet = true;
	public boolean ignoreTanks = false, ignoreBullets = true;
	public boolean ignoreDestructible = false;
	public boolean ignoreShootThrough = false;

	public boolean trace = Game.traceAllRays;
	public boolean dotted = false;

	public double speed = 10;

	public double age = 0;
	public int traceAge;

	public Tank tank, targetTank;
	public double targetTankSizeMul = 0;

	public DoubleArrayList bounceX = new DoubleArrayList();
	public DoubleArrayList bounceY = new DoubleArrayList();

	public double targetX, targetY;
	public boolean acquiredTarget = false;

	/** Should be consumed immediately via getTarget or getDist. Otherwise, use {@linkplain #copy()}  */
	public static Ray newRay(double x, double y, double angle, int bounces, Tank tank)
	{
		return newRay(x, y, angle, bounces, tank, 10);
	}

	/** Should be consumed immediately via getTarget or getDist. Otherwise, use {@linkplain #copy()}  */
	public static Ray newRay(double x, double y, double angle, int bounces, Tank tank, double speed)
	{
		return cacheRay.set(x, y, angle, bounces, tank, speed);
	}

	public Ray copy()
	{
		return new Ray().set(posX, posY, angle, bounces, tank, speed);
	}

	private Ray() {}

	public Ray set(double x, double y, double angle, int bounces, Tank tank, double speed)
	{
		this.vX = speed * Math.cos(angle);
		this.vY = speed * Math.sin(angle);
		this.angle = angle;

		this.posX = this.startX = x;
		this.posY = this.startY = y;
		this.bounces = bounces;
		this.bouncyBounces = 100;
		setSize(10).setMaxChunks(12);

		this.trace = Game.traceAllRays;
		this.dotted = false;
		this.enableBounciness = true;
		this.ignoreTanks = false;
		this.ignoreBullets = true;
		this.asBullet = true;
		this.ignoreDestructible = false;
		this.ignoreShootThrough = false;

		this.acquiredTarget = false;
		this.tank = tank;

		this.bounceX.clear();
		this.bounceY.clear();

		return this;
	}

	public Movable getTarget(double mul, Tank targetTank)
	{
		this.targetTank = targetTank;
		this.targetTankSizeMul = mul;
		return this.getTarget();
	}

	public Ray setAsExplosive(boolean explosive)
	{
		this.ignoreDestructible = explosive;
		this.ignoreShootThrough = explosive;
		return this;
	}

	public Ray setBouncyBounces(int bouncyBounces)
	{
		this.bouncyBounces = bouncyBounces;
		return this;
	}

	public Ray setAsBullet(boolean testBulletCollision)
	{
		this.asBullet = testBulletCollision;
		return this;
	}

	public Ray setMaxChunks(int maxChunks)
	{
		this.maxChunkCheck = maxChunks;
		return this;
	}

	@SuppressWarnings("unused")
    public Ray setMaxDistance(double distance)
	{
		setMaxChunks((int) (distance / Game.tile_size / Chunk.chunkSize + 1));
		return this;
	}

	public Ray setSize(double size)
	{
		this.size = size;
		return this;
	}

	public Ray moveOut(double amount)
	{
		this.posX += this.vX * amount;
		this.posY += this.vY * amount;
		return this;
	}

	/** Result of dynamic collisions */
	public static Result dynamic = new Result();
	/** Result of static collisions */
	public static Result stat = new Result();

	public Movable getTarget()
	{
		double remainder = 0;
		acquiredTarget = true;

		if (testInsideObstacle(posX, posY))
			return null;

		if (!ignoreTanks)
		{
			Chunk c = Chunk.getChunk(posX, posY);
			if (c == null)
				return null;

			for (Movable m : c.movables)
			{
				if (m instanceof Tank && m != this.tank)
				{
					Tank t = (Tank) m;
					if (this.posX + this.size / 2 >= t.posX - t.size / 2 &&
							this.posX - this.size / 2 <= t.posX + t.size / 2 &&
							this.posY + this.size / 2 >= t.posY - t.size / 2 &&
							this.posY - this.size / 2 <= t.posY + t.size / 2)
						return t;
				}
			}
		}

		boolean firstBounce = this.targetTank == null;

		while (this.bounces >= 0 && this.bouncyBounces >= 0)
		{
			Result result;
			Chunk current = Chunk.getChunk(posX, posY);
			if (current == null)
				return null;

			checkFaceList(current, dynamic, Ray::dynamicFaces, firstBounce);
			checkFaceList(current, stat, Ray::staticFaces, firstBounce);

			if (dynamic.collisionFace != null && stat.collisionFace != null)
			{
				result = Movable.sqDistBetw(dynamic.collisionX, dynamic.collisionY, posX, posY) <
						Movable.sqDistBetw(stat.collisionX, stat.collisionY, posX, posY) ? dynamic : stat;
			}
			else
				result = dynamic.collisionFace != null ? dynamic : stat;

			if (result == null)
				return null;

			this.age += result.t;

			firstBounce = false;

			if (result.collisionFace != null)
			{
				if (trace && ScreenGame.isUpdatingGame())
				{
					double dx = result.collisionX - posX, dy = result.collisionY - posY;
					double steps = (Math.sqrt((Math.pow(dx, 2) + Math.pow(dy, 2)) / (1 + Math.pow(this.vX, 2) + Math.pow(this.vY, 2))) + 1);

					if (dotted)
						steps /= 2;

					double s;
					for (s = remainder; s <= steps; s++)
					{
						double x = posX + dx * s / steps;
						double y = posY + dy * s / steps;

						this.traceAge++;

						double frac = 1 / (1 + this.traceAge / 100.0);
						double z = this.tank.size / 2 + this.tank.turretSize / 2 * frac + (Game.tile_size / 4) * (1 - frac);

						if (Game.screen instanceof ScreenGame && !ScreenGame.finished)
							Game.effects.add(Effect.createNewEffect(x, y, z, Effect.EffectType.ray));
					}

					remainder = s - steps;
				}

				this.posX = result.collisionX;
				this.posY = result.collisionY;

				if (Chunk.debug && trace)
					Game.effects.add(Effect.createNewEffect(posX, posY, 50, Effect.EffectType.piece).setColor(0, 150, 0));

				ISolidObject obj = result.collisionFace.owner;
				if (obj instanceof Movable)
				{
					this.targetX = result.collisionX;
					this.targetY = result.collisionY;
					bounceX.add(result.collisionX);
					bounceY.add(result.collisionY);

					return (Movable) obj;
				}

                if (obj instanceof Obstacle && ((Obstacle) obj).bouncy)
                    this.bouncyBounces--;
                else if (obj instanceof Obstacle && !((Obstacle) obj).allowBounce)
                    this.bounces = -1;
                else
                    this.bounces--;

                bounceX.add(result.collisionX);
				bounceY.add(result.collisionY);

				if (this.bounces >= 0)
				{
					if (result.corner)
					{
						this.vX = -this.vX;
						this.vY = -this.vY;
					}
					else if (result.collisionFace.horizontal)
						this.vY = -this.vY;
					else
						this.vX = -this.vX;

					this.angle = Movable.getPolarDirection(this.vX, this.vY);
				}
			}
			else
				return null;
		}

		return null;
	}

	/** Lambdas with parameter(s) are anonymous classes, which create temp objects. We don't want that. */
	public static Chunk.FaceList dynamicFaces(Chunk c)
	{
		return c.faces;
	}

	/** Lambdas with parameter(s) are anonymous classes, which create temp objects. Temp objects bad. */
	public static Chunk.FaceList staticFaces(Chunk c)
	{
		return c.staticFaces;
	}

	public void checkFaceList(Chunk current, Result result, Function<Chunk, Chunk.FaceList> faceList, boolean firstBounce)
	{
		if (current == null)
			return;

		double collisionX = -1, collisionY = -1;
		int totalChunksChecked = 0;

		chunkCheck:
		for (int chunksChecked = 0; chunksChecked < maxChunkCheck; chunksChecked++)
		{
			double moveXBase = Chunk.chunkSize * Game.tile_size * Math.cos(angle);
			double moveYBase = Chunk.chunkSize * Game.tile_size * Math.sin(angle);
			double moveX = moveXBase * chunksChecked, moveXPrev = moveXBase * Math.max(0, chunksChecked - 1);
			double moveY = moveYBase * chunksChecked, moveYPrev = moveYBase * Math.max(0, chunksChecked - 1);

			chunksAdded = 0;

			// move forward one chunk in the ray's direction
			Chunk mid = chunksChecked > 0 ? Chunk.getChunk(posX + moveX, posY + moveY) : current;
			// add current chunk and chunk in front
			addChunks(current, mid);

			// if the ray moved diagonally, add the chunks on the sides
			if (mid == null || current.manhattanDist(mid) > 1)
				addChunks(current,
						Chunk.getChunk(posX + moveXPrev, posY + moveY),
						Chunk.getChunk(posX + moveX, posY + moveYPrev)
				);

			if (chunksAdded == 0)
				break;

			// sort the chunks by distance from the ray
			Arrays.sort(chunkCache, 0, chunksAdded);

			for (int i = 0; i < chunksAdded; i++)
			{
				Chunk chunk = chunkCache[i];
				if (chunk == null)
					continue;

				totalChunksChecked++;

				if (Chunk.debug && trace)
				{
					// displays the order of chunks checked and locations that the ray checked
					Game.effects.add(Effect.createNewEffect(
							(chunk.chunkX + 0.5) * Chunk.chunkSize * Game.tile_size + (totalChunksChecked * 5),
							(chunk.chunkY + 0.5) * Chunk.chunkSize * Game.tile_size,
							150, Effect.EffectType.chain, 90
					).setRadius(totalChunksChecked));

					Game.effects.add(Effect.createNewEffect(posX + moveX, posY + moveY, 20, Effect.EffectType.laser));

					if (mid == null || current.manhattanDist(mid) > 1)
					{
						Game.effects.add(Effect.createNewEffect(posX, posY + moveY, 20, Effect.EffectType.obstaclePiece));
						Game.effects.add(Effect.createNewEffect(posX + moveX, posY, 20, Effect.EffectType.piece));
					}
				}

				checkCollisionIn(result, faceList.apply(chunk), firstBounce, collisionX, collisionY);
				collisionX = result.collisionX;
				collisionY = result.collisionY;

				if (result.collisionFace != null)
					break chunkCheck;
			}
		}
	}

	public boolean testInsideObstacle(double x, double y)
	{
		return isInsideObstacle(x - size / 2, y - size / 2) ||
				isInsideObstacle(x + size / 2, y - size / 2) ||
				isInsideObstacle(x + size / 2, y + size / 2) ||
				isInsideObstacle(x - size / 2, y + size / 2);
	}

	public void checkCollisionIn(Result result, Chunk.FaceList faceList, boolean firstBounce, double collisionX, double collisionY)
	{
		Face collisionFace = null;
		double t = Double.MAX_VALUE;
		boolean corner = false;

		if (vX > 0)
		{
			// rays moving right will not collide with faces to the left of the ray
			for (Face f : faceList.leftFaces.tailSet(DummyFace.face.set(false, this.posX - size / 2)))
			{
				double size = this.size;

				if (f.owner instanceof Movable)
					size *= tankHitSizeMul;
				if (f.owner != null && f.owner == targetTank)
					size *= targetTankSizeMul;

				if (passesThrough(f))
					continue;

				if (f.startX < this.posX + size / 2 || !collision(f) || (f.owner == this.tank && firstBounce))
					continue;

				double y = (f.startX - size / 2 - this.posX) * vY / vX + this.posY;
				if (y >= f.startY - size / 2 && y <= f.endY + size / 2)
				{
					t = (f.startX - size / 2 - this.posX) / vX;
					collisionX = f.startX - size / 2;
					collisionY = y;
					collisionFace = f;
					break;
				}
			}
		}
		else if (vX < 0)
		{
			// rays moving left will not collide with faces to the right of the ray
			for (Face f : faceList.rightFaces.tailSet(DummyFace.face.set(false, this.posX + size / 2)))
			{
				double size = this.size;

				if (f.owner instanceof Movable)
					size *= tankHitSizeMul;

				if (passesThrough(f))
					continue;

				if (f.startX > this.posX - size / 2 || !collision(f) || (f.owner == this.tank && firstBounce))
					continue;

				double y = (f.startX + size / 2 - this.posX) * vY / vX + this.posY;
				if (y >= f.startY - size / 2 && y <= f.endY + size / 2)
				{
					t = (f.startX + size / 2 - this.posX) / vX;
					collisionX = f.startX + size / 2;
					collisionY = y;
					collisionFace = f;
					break;
				}
			}
		}

		if (vY > 0)
		{
			// rays moving down will not collide with faces above the ray
			for (Face f : faceList.topFaces.tailSet(DummyFace.face.set(true, this.posY - size / 2)))
			{
				double size = this.size;

				if (f.owner instanceof Movable)
					size *= tankHitSizeMul;

				if (passesThrough(f))
					continue;

				if (f.startY < this.posY + size / 2 || !collision(f) || (f.owner == this.tank && firstBounce))
					continue;

				double x = (f.startY - size / 2 - this.posY) * vX / vY + this.posX;
				if (x >= f.startX - size / 2 && x <= f.endX + size / 2)
				{
					double t1 = (f.startY - size / 2 - this.posY) / vY;

					if (t1 == t)
						corner = true;
					else if (t1 < t)
					{
						collisionX = x;
						collisionY = f.startY - size / 2;
						collisionFace = f;
						t = t1;
					}
					break;
				}
			}
		}
		else if (vY < 0)
		{
			// rays moving up will not collide with faces below the ray
			for (Face f : faceList.bottomFaces.tailSet(DummyFace.face.set(true, this.posY + size / 2)))
			{
				double size = this.size;

				if (f.owner instanceof Movable)
					size *= tankHitSizeMul;

				if (passesThrough(f))
					continue;

				if (f.startY > this.posY - size / 2 || !collision(f) || (f.owner == this.tank && firstBounce))
					continue;

				double x = (f.startY + size / 2 - this.posY) * vX / vY + this.posX;
				if (x >= f.startX - size / 2 && x <= f.endX + size / 2)
				{
					double t1 = (f.startY + size / 2 - this.posY) / vY;

					if (t1 == t)
						corner = true;
					else if (t1 < t)
					{
						collisionX = x;
						collisionY = f.startY + size / 2;
						collisionFace = f;
						t = t1;
					}
					break;
				}
			}
		}

		result.set(t, collisionX, collisionY, collisionFace, corner);
	}

	private boolean passesThrough(Face f)
	{
		boolean passThrough = false;
		if (f.owner instanceof Obstacle && !((Obstacle) f.owner).bouncy)
        {
            Obstacle o = (Obstacle) f.owner;
            passThrough = (this.ignoreDestructible && o.destructible) || (this.ignoreShootThrough && o.shouldShootThrough);
        }

		if ((ignoreTanks && f.owner instanceof Tank) || (ignoreBullets && f.owner instanceof Bullet))
			passThrough = true;

		return passThrough;
	}

    public static final class Result
    {
        private double t;
        private double collisionX;
        private double collisionY;
        private Face collisionFace;
        private boolean corner;

        public void set(double t, double collisionX, double collisionY, Face collisionFace, boolean corner)
        {
            this.t = t;
            this.collisionX = collisionX;
            this.collisionY = collisionY;
            this.collisionFace = collisionFace;
            this.corner = corner;
        }
    }

	public double getDist()
	{
		this.bounceX.add(this.posX);
		this.bounceY.add(this.posY);

		if (!acquiredTarget)
			this.getTarget();

		return Math.sqrt(getSquaredFinalDist());
	}

	public double getTargetDist(double mul, Tank m)
	{
		return Math.sqrt(getSquaredTargetDist(mul, m));
	}

	public double getSquaredTargetDist(double mul, Tank m)
	{
		this.bounceX.add(0, this.posX);
		this.bounceY.add(0, this.posY);

		if (this.getTarget(mul, m) != m)
			return -1;

		return getSquaredFinalDist();
	}

	private double getSquaredFinalDist()
	{
		double dist = 0;
		for (int i = 0; i < this.bounceX.size() - 1; i++)
            dist += Math.pow(this.bounceX.getDouble(i + 1) - this.bounceX.getDouble(i), 2) + Math.pow(this.bounceY.getDouble(i + 1) - this.bounceY.getDouble(i), 2);

		if (this.bounces >= 0)
			dist += Chunk.chunkToPixel(maxChunkCheck);

		return dist;
	}

	private void addChunks(Chunk compare, Chunk... chunks)
	{
		for (Chunk c : chunks)
		{
			if (c == null)
				continue;

			c.compareTo = compare;
			chunkCache[chunksAdded++] = c;
		}
	}

	public double getAngleInDirection(double x, double y)
	{
		x -= this.posX;
		y -= this.posY;

		return Movable.getPolarDirection(x, y);
	}

	public boolean isInsideObstacle(double x, double y)
	{
		Obstacle o = Game.getObstacle(x, y);
		return o != null && collision(o) && !(ignoreShootThrough && o.shouldShootThrough) && !(ignoreDestructible && o.destructible);
	}

	public boolean collision(Face f)
	{
		return asBullet ? f.solidBullet : f.solidTank;
	}

	public boolean collision(Obstacle o)
	{
		return asBullet ? o.bulletCollision : o.tankCollision;
	}

	/** Used to compare coordinates between faces */
	public static class DummyFace extends Face
	{
		/** The instance of DummyFace to reduce memory allocation */
		public static DummyFace face = new DummyFace();

		public DummyFace()
		{
			super(null, 0, 0, 0, 0, false, false, true, true);
		}

		public DummyFace set(boolean horizontal, double filter)
		{
			this.horizontal = horizontal;
			if (horizontal)
				this.startY = filter;
			else
				this.startX = filter;
			return this;
		}
	}
}
