package tanks.obstacle;

import tanks.Direction;

public class Face implements Comparable<Face>
{
    public double startX, startY, endX, endY;

    public final Direction direction;
    public boolean solidTank, solidBullet;
    public boolean valid = true;

    public ISolidObject owner;

    public Face(ISolidObject o, Direction direction, boolean tank, boolean bullet)
    {
        this.owner = o;
        this.direction = direction;
        this.solidTank = tank;
        this.solidBullet = bullet;
    }

    public Face(ISolidObject o, double x1, double y1, double x2, double y2, Direction direction, boolean tank, boolean bullet)
    {
        this(o, direction, tank, bullet);
        this.startX = x1;
        this.startY = y1;
        this.endX = x2;
        this.endY = y2;
    }

    public int compareTo(Face f)
    {
        int cx = Double.compare(this.startX, f.startX);
        int cy = Double.compare(this.startY, f.startY);

        if (!this.direction.isHorizontal())
            return cx != 0 ? cx : cy;
        return cy != 0 ? cy : cx;
    }

    public void update(double x1, double y1, double x2, double y2)
    {
        this.startX = x1;
        this.startY = y1;
        this.endX = x2;
        this.endY = y2;
    }

    public String toString()
    {
        if (this.direction.isHorizontal())
            return this.startX + "-" + this.endX + " " + this.startY;
        else
            return this.startX + " " + this.startY + "-" + this.endY;
    }
}
