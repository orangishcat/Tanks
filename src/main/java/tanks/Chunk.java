package tanks;

import basewindow.IBatchRenderableObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import tanks.obstacle.Face;
import tanks.obstacle.ISolidObject;
import tanks.obstacle.Obstacle;

import java.util.Comparator;
import java.util.Objects;
import java.util.Random;

@SuppressWarnings("UnusedReturnValue")
public class Chunk implements Comparable<Chunk>
{
    public static Level defaultLevel = new Level("{28,18|,|,}");
    public static final Chunk zeroChunk = new Chunk();
    public static boolean debug = false;

    public static Int2ObjectOpenHashMap<Chunk> chunks = new Int2ObjectOpenHashMap<>();
    public static ObjectArrayList<Chunk> chunkList = new ObjectArrayList<>();
    private static final ObjectArrayList<Chunk> chunkCache = new ObjectArrayList<>();
    public static int chunkSize = 8;

    public final Level level;
    public final int chunkX, chunkY;
    public Face[] borderFaces = new Face[4];
    public final ObjectOpenHashSet<Obstacle> obstacles = new ObjectOpenHashSet<>();
    public final ObjectOpenHashSet<Movable> movables = new ObjectOpenHashSet<>();

    /** Stores faces of Movables, which are updated every frame */
    public final FaceList faces = new FaceList();
    public final Tile[][] tileGrid = new Tile[chunkSize][chunkSize];

    public Chunk compareTo;

    /** The variable that caches the previous call to {@link Chunk#getChunk} */
    private static Chunk prevChunk;

    public Chunk(Level l, Random r, int x, int y)
    {
        this.chunkX = x;
        this.chunkY = y;
        this.level = l;

        for (int i = 0; i < chunkSize; i++)
        {
            for (int j = 0; j < chunkSize; j++)
            {
                if (tileGrid[i][j] == null)
                    tileGrid[i][j] = setTileColor(l, r, new Tile());
            }
        }
    }

    private Chunk()
    {
        this.level = null;
        this.chunkX = 0;
        this.chunkY = 0;
    }

    /** Iterates in a diamond shape (like BFS) outwards until the manhattan distance traveled is >= maxChunks.
     * @return the chunks within the range */
    public static ObjectArrayList<Chunk> iterateOutwards(int tileX, int tileY, int maxChunks)
    {
        chunkCache.clear();
        queue.clear();
        visited.clear();

        Chunk start = Chunk.getChunk(tileX, tileY);

        if (start != null)
        {
            queue.enqueue(start);

            while (!queue.isEmpty())
            {
                Chunk c = queue.dequeue();
                for (int i = 0; i < 4; i++)
                {
                    int newX = c.chunkX + Direction.X[i];
                    int newY = c.chunkY + Direction.Y[i];
                    Chunk next = Chunk.getChunkCoords(newX, newY);
                    if (next != null && start.manhattanDist(next) < maxChunks && visited.add(next))
                    {
                        chunkCache.add(next);
                        queue.enqueue(next);
                    }
                }
            }
        }

        return chunkCache;
    }


    public static int[] x1 = {0, 1, 0, 0}, x2 = {1, 1, 1, 0}, y1 = {0, 0, 1, 0}, y2 = {0, 1, 1, 1};

    /**
     * Adds a level border on the specified side of the chunk, where rays will collide off of.
     *
     * @param dir The side of the chunk to add the border on
     * @param l The level to get the border coordinates from
     */
    public void addBorderFace(Direction dir, Level l)
    {
        int side = dir.index();
        Face f = new Face(null,
                convert(chunkX + x1[side], l, true),
                convert(chunkY + y1[side], l, false),
                convert(chunkX + x2[side], l, true),
                convert(chunkY + y2[side], l, false),
                dir, true, true);
        borderFaces[side] = f;
        faces.getSide(dir.opposite().index()).add(f);
    }

    /** Helper to convert chunk coordinates to game coordinates and clamp it to the level size. */
    private static double convert(int chunk, Level l, boolean isX)
    {
        return Math.max(isX ? l.startX : l.startY, Math.min(isX ? l.sizeX : l.sizeY, chunk * Chunk.chunkSize)) * Game.tile_size;
    }

    public static boolean initialized()
    {
        return !Chunk.chunkList.isEmpty();
    }

    /** Iterates in a diamond shape (like BFS) outwards until the manhattan distance traveled is >= maxChunks.
     * @return the chunks within the range */
    public static ObjectArrayList<Chunk> iterateOutwards(double posX, double posY, int maxChunks)
    {
        return iterateOutwards((int) (posX / Game.tile_size), (int) (posY / Game.tile_size), maxChunks);
    }

    static ObjectArrayFIFOQueue<Chunk> queue = new ObjectArrayFIFOQueue<>();
    static ObjectOpenHashSet<Chunk> visited = new ObjectOpenHashSet<>();

    public static double chunkToPixel(double chunkPos)
    {
        return chunkPos * Chunk.chunkSize * Game.tile_size;
    }

    public int manhattanDist(Chunk other)
    {
        return Math.abs(chunkX - other.chunkX) + Math.abs(chunkY - other.chunkY);
    }

    public void addMovable(Movable m)
    {
        addMovable(m, true);
    }

    public void addMovable(Movable m, boolean refresh)
    {
        if (m == null)
            return;

        movables.add(m);
        if (refresh)
            faces.addFaces(m);
    }

    public void removeMovable(Movable m)
    {
        if (m == null)
            return;

        movables.remove(m);
        faces.removeFaces(m);
    }

    public void addObstacle(Obstacle o)
    {
        addObstacle(o, true);
    }

    public void addObstacle(Obstacle o, boolean refresh)
    {
        if (o == null)
            return;

        obstacles.add(o);
        if (refresh)
            faces.addFaces(o);
    }

    public void removeObstacle(Obstacle o)
    {
        if (o == null)
            return;

        obstacles.remove(o);
        faces.removeFaces(o);
    }

    public static ObjectArrayList<Chunk> getChunksInRange(double x1, double y1, double x2, double y2)
    {
        return getChunksInRange((int) (x1 / Game.tile_size), (int) (y1 / Game.tile_size),
                (int) (x2 / Game.tile_size), (int) (y2 / Game.tile_size));
    }

    public static ObjectArrayList<Chunk> getChunksInRange(int tx1, int ty1, int tx2, int ty2)
    {
        int x1 = tx1 / chunkSize, y1 = ty1 / chunkSize, x2 = tx2 / chunkSize, y2 = ty2 / chunkSize;
        chunkCache.clear();
        for (Chunk c : chunkList)
        {
            if (Game.isOrdered(true, x1, c.chunkX, x2)
                    && Game.isOrdered(true, y1, c.chunkY, y2))
                chunkCache.add(c);
        }
        return chunkCache;
    }

    public static Tile runIfTilePresent(int tileX, int tileY, Consumer<Tile> tc)
    {
        Tile t = getTile(tileX, tileY);
        if (t != null)
            tc.accept(t);
        return t;
    }

    public static Tile runIfTilePresent(double posX, double posY, Consumer<Tile> tc)
    {
        Tile t = getTile(posX, posY);
        if (t != null)
            tc.accept(t);
        return t;
    }

    public static <K> K getIfPresent(int tileX, int tileY, Function<Tile, K> func)
    {
        Tile t = getTile(tileX, tileY);
        if (t == null) return null;
        return func.apply(t);
    }

    @SuppressWarnings("unused")
    public static <K> K getIfPresent(double posX, double posY, Function<Tile, K> func)
    {
        Tile t = getTile(posX, posY);
        if (t == null) return null;
        return func.apply(t);
    }

    /** Expects all pixel coordinates. */
    public static ObjectArrayList<Chunk> getChunksInRadius(double x1, double y1, double radius)
    {
        return getChunksInRadius((int) (x1 / Game.tile_size), (int) (y1 / Game.tile_size), (int) (radius / Game.tile_size));
    }

    /** Expects all tile coordinates. */
    public static ObjectArrayList<Chunk> getChunksInRadius(int tx1, int ty1, int radius)
    {
        chunkCache.clear();
        double x1 = (double) tx1 / chunkSize, y1 = (double) ty1 / chunkSize, cRad = Math.ceil((double) radius / chunkSize) + 1;
        for (Chunk chunk : chunkList)
        {
            if ((chunk.chunkX - x1) * (chunk.chunkX - x1) +
                    (chunk.chunkY - y1) * (chunk.chunkY - y1) <= cRad * cRad)
                chunkCache.add(chunk);
        }
        return chunkCache;
    }

    public static Tile setTileColor(Level l, Random r, Tile t)
    {
        t.colR = l.colorR + (Game.fancyTerrain ? r.nextDouble() * l.colorVarR : 0);
        t.colG = l.colorG + (Game.fancyTerrain ? r.nextDouble() * l.colorVarG : 0);
        t.colB = l.colorB + (Game.fancyTerrain ? r.nextDouble() * l.colorVarB : 0);
        t.depth = Game.fancyTerrain && Game.enable3dBg ? r.nextDouble() * 10 : 0;
        return t;
    }

    public static void update()
    {

    }

    public static void reset()
    {
        populateChunks(defaultLevel);
    }

    /**
     * @param pix Coordinate in pixels
     * @return The tile position relative to the top left corner of the chunk the coordinate is in
     */
    public static int pixelToPosInChunk(double pix)
    {
        return  (int) (pix / Game.tile_size) % chunkSize;
    }

    public void removeExtraIfEquals(Obstacle o)
    {
        int x = pixelToPosInChunk(o.posX);
        int y = pixelToPosInChunk(o.posY);

        if (Objects.equals(tileGrid[x][y].extraObstacle, o))
        {
            tileGrid[x][y].extraObstacle = null;
            removeObstacle(o);
        }
    }

    public void removeSurfaceIfEquals(Obstacle o)
    {
        int x = pixelToPosInChunk(o.posX);
        int y = pixelToPosInChunk(o.posY);

        if (Objects.equals(tileGrid[x][y].surfaceObstacle, o))
        {
            tileGrid[x][y].surfaceObstacle = null;
            removeObstacle(o);
        }
    }

    /** Automatically converts to chunk coordinates. */
    public Tile getChunkTile(int posX, int posY)
    {
        if (posX < 0 || posY < 0)
            return null;

        return tileGrid[posX % chunkSize][posY % chunkSize];
    }

    public void removeObstacleIfEquals(Obstacle o)
    {
        int x = pixelToPosInChunk(o.posX);
        int y = pixelToPosInChunk(o.posY);

        if (Objects.equals(tileGrid[x][y].obstacle, o))
        {
            tileGrid[x][y].obstacle = null;
            if (tileGrid[x][y].surfaceObstacle != null)
            {
                tileGrid[x][y].obstacle = tileGrid[x][y].surfaceObstacle;
                tileGrid[x][y].surfaceObstacle = null;
            }

            removeObstacle(o);
        }
    }

    public void setObstacle(int x, int y, Obstacle o)
    {
        if (o.startHeight >= 1)
            return;

        Tile t = tileGrid[x][y];
        Obstacle o1 = t.obstacle;
        if (!Obstacle.canPlaceOn(o.type, t))
            return;

        if (o.type == Obstacle.ObstacleType.extra)
            t.extraObstacle = o;
        else if (o1 != null && o1.type == Obstacle.ObstacleType.ground)
            t.surfaceObstacle = o1;
        t.obstacle = o;
    }

    /** Expects tile coordinates. */
    public static Tile getTile(int tileX, int tileY)
    {
        Chunk c = getChunk(tileX, tileY);
        if (c == null)
            return null;
        return c.getChunkTile(tileX, tileY);
    }

    /** Expects pixel coordinates. */
    public static Tile getTile(double posX, double posY)
    {
        Chunk c = getChunk(posX, posY);
        if (c == null)
            return null;
        return c.getChunkTile(posX, posY);
    }

    /** Automatically converts to tile coordinates and then chunk coordinates. */
    public Tile getChunkTile(double posX, double posY)
    {
        if (posX < 0 || posX >= Game.currentSizeX * Game.tile_size || posY < 0 || posY >= Game.currentSizeY * Game.tile_size)
            return null;

        return tileGrid[pixelToPosInChunk(posX)][pixelToPosInChunk(posY)];
    }

    public static double addCoords(double chunk, double tile)
    {
        return chunk * chunkSize + tile;
    }

    public static void drawDebugStuff()
    {
        if (!debug)
            return;

        Drawing.drawing.setColor(255, 255, 0, 128);

        for (Chunk c : chunkList)
            Drawing.drawing.drawRect(addCoords(c.chunkX, chunkSize / 2.) * Game.tile_size, addCoords(c.chunkY, chunkSize / 2.) * Game.tile_size,
                    chunkSize * Game.tile_size, chunkSize * Game.tile_size, 1);
    }

    @Override
    public int compareTo(Chunk o)
    {
        Chunk chunkToCompare = compareTo != null ? compareTo : zeroChunk;
        return Integer.compare(this.manhattanDist(chunkToCompare), o.manhattanDist(chunkToCompare)) | this.manhattanDist(o);
    }

    public static class FaceList
    {
        /**
         * dynamic x, static y
         */
        public final ObjectAVLTreeSet<Face> topFaces = new ObjectAVLTreeSet<>();
        /**
         * dynamic x, static y
         */
        public final ObjectAVLTreeSet<Face> bottomFaces = new ObjectAVLTreeSet<>(Comparator.reverseOrder());
        /**
         * static x, dynamic y
         */
        public final ObjectAVLTreeSet<Face> leftFaces = new ObjectAVLTreeSet<>();
        /**
         * static x, dynamic y
         */
        public final ObjectAVLTreeSet<Face> rightFaces = new ObjectAVLTreeSet<>(Comparator.reverseOrder());

        public void addFaces(ISolidObject s)
        {
            if (s.disableRayCollision())
                return;

            Face[] faces = s.getFaces();
            for (int i = 0; i < 4; i++)
            {
                if (faces[i].valid)
                    getSide(i).add(faces[i]);
            }
        }

        public void removeFaces(ISolidObject s)
        {
            if (s.disableRayCollision())
                return;

            Face[] faces = s.getFaces();
            for (int i = 0; i < 4; i++)
            {
                if (faces[i].lastValid)
                    getSide(i).remove(faces[i]);
            }
        }

        public ObjectAVLTreeSet<Face> getSide(int side)
        {
            switch (side)
            {
                case 0:
                    return topFaces;
                case 1:
                    return rightFaces;
                case 2:
                    return bottomFaces;
                case 3:
                    return leftFaces;
                default:
                    throw new RuntimeException("Invalid side: " + side);
            }
        }

        public void clear()
        {
            topFaces.clear();
            bottomFaces.clear();
            leftFaces.clear();
            rightFaces.clear();
        }
    }

    public static class Tile implements IBatchRenderableObject
    {
        public Obstacle obstacle, surfaceObstacle, extraObstacle;
        public double colR, colG, colB, depth;

        /** Whether a tank has spawned on this tile (used during level creation only) */
        public boolean tankSolid;

        public double height()
        {
            return obstacle != null ? obstacle.getTileHeight() : -1000;
        }

        public double edgeDepth()
        {
            return obstacle != null ? obstacle.getEdgeDrawDepth() : 0;
        }

        public double groundHeight()
        {
            return obstacle != null ? obstacle.getGroundHeight() : 0;
        }

        public boolean solid()
        {
            return obstacle != null && obstacle.tankCollision;
        }
    }

    public static void initialize()
    {
        populateChunks(defaultLevel);
    }

    public static void populateChunks(Level l)
    {
        populateChunks(l, true);
    }

    public static void populateChunks(Level l, boolean clear)
    {
        if (clear)
        {
            chunks.clear();
            chunkList.clear();
            prevChunk = null;
        }

        int startX = l.startX / chunkSize, startY = l.startY / chunkSize;
        int sX = l.sizeX / chunkSize + 1, sY = l.sizeY / chunkSize + 1;
        Random r = new Random(l.tilesRandomSeed);

        for (int x = 0; x < sX; x++)
            for (int y = 0; y < sY; y++)
                addChunk(x + startX, y + startY, new Chunk(l, r, x, y));
    }

    /** Expects pixel coordinates. */
    public static Chunk getChunk(double posX, double posY)
    {
        return getChunk((int) (posX / Game.tile_size), (int) (posY / Game.tile_size));
    }

    /** Expects tile coordinates. */
    public static Chunk getChunk(int tileX, int tileY)
    {
        return getChunkCoords(tileX / Chunk.chunkSize, tileY / Chunk.chunkSize);
    }

    /** Expects chunk coordinates. */
    public static Chunk getChunkCoords(int chunkX, int chunkY)
    {
        if (prevChunk != null && prevChunk.chunkX == chunkX && prevChunk.chunkY == chunkY)
            return prevChunk;

        Chunk c = chunks.get(encodeChunkCoords(chunkX, chunkY));
        if (c != null)
            prevChunk = c;
        return c;
    }

    public static Chunk addChunk(int chunkX, int chunkY, Chunk c)
    {
        chunkList.add(c);
        return chunks.put(encodeChunkCoords(chunkX, chunkY), c);
    }

    public static int f(int i)
    {
        return 1664525 * i + 1013904223;
    }

    public static int encodeChunkCoords(int chunkX, int chunkY)
    {
        return f(f(chunkX) + chunkY);
    }
}
