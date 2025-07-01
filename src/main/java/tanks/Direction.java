package tanks;

public enum Direction
{
    up, right, down, left;

    public static final int[] X = {1, -1, 0, 0}, Y = {0, 0, 1, -1};

    private static final Direction[] values = Direction.values();

    public boolean isHorizontal()
    {
        return this == right || this == left;
    }

    public boolean isVertical()
    {
        return this == up || this == down;
    }

    public int index()
    {
        return this.ordinal();
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
