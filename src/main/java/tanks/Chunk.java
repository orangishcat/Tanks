package tanks;

import basewindow.IBatchRenderableObject;
import tanks.obstacle.Obstacle;

public class Chunk
{
    public static Chunk everything;     // will be replaced later

    public static Chunk getChunk(int tileX, int tileY)
    {
        return everything;
    }

    public static Tile getTile(double tileX, double tileY)
    {
        return getTile(((int) (tileX / Game.tile_size)), ((int) (tileY / Game.tile_size)));
    }

    /** @return The tile at the given position, or null if the position is out of bounds. Expects tile coordinates. */
    public static Tile getTile(int tileX, int tileY)
    {
        if (tileX < 0 || tileX >= Game.currentSizeX || tileY < 0 || tileY >= Game.currentSizeY)
            return null;
        return Game.tiles[tileX][tileY];
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

    public static class Tile implements IBatchRenderableObject
    {
        public Obstacle obstacle, surfaceObstacle, extraObstacle;
        public double colR, colG, colB, depth, lastHeight;

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
            return obstacle != null && obstacle.bulletCollision;
        }

        public boolean unbreakable()
        {
            return obstacle != null && !obstacle.shouldShootThrough;
        }
    }
}
