package tanks;

public enum Direction
{
    up, right, down, left, upLeft, upRight, downLeft, downRight;

    public static final int[] X = {0, 1, 0, -1, 0, 1, -1, 0, 1, -1}, Y = {-1, 0, 1, 0, 1, 0, 0, -1, 1, -1};

    private static final Direction[] values = Direction.values();

    public boolean isNonZeroX()
    {
        return x() != 0;
    }

    public boolean isNonZeroY()
    {
        return y() != 0;
    }

    public boolean isDiagonal()
    {
        return x() != 0 && y() != 0;
    }

    public Direction opposite()
    {
        int offset = ordinal() >= 4 ? 4 : 0;
        return values[(this.ordinal() - offset + 2) % 4 + offset];
    }

    public int index()
    {
        return this.ordinal();
    }

    public int x()
    {
        return X[this.ordinal()];
    }

    public int y()
    {
        return Y[this.ordinal()];
    }

    /**
     * Returns the direction with the specified index.
     * 0 = up, 1 = right, 2 = down, 3 = left
     * @param i the index of the direction
     * @return the direction with the specified index
     * */
    public static Direction fromIndex(int i)
    {
        return values[i];
    }
}
