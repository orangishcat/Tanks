package tanks.obstacle;

import tanks.Direction;
import tanks.tank.Tank;

public class Face implements Comparable<Face>
{
    public double startX, startY, endX, endY;

    public final Direction direction;
    public boolean solidTank, solidBullet;
    public boolean valid = true;

    public ISolidObject owner;
    public boolean lastValid;

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
        update(x1, y1, x2, y2, true, tank, bullet);
    }

    public int compareTo(Face f)
    {
        int cx = Double.compare(this.startX, f.startX);
        int cy = Double.compare(this.startY, f.startY);

        if (this.direction.isNonZeroX())
            return cx != 0 ? cx : cy;
        return cy != 0 ? cy : cx;
    }

    public void update(double x1, double y1, double x2, double y2, boolean valid, boolean tank, boolean bullet)
    {
        this.startX = x1;
        this.startY = y1;
        this.endX = x2;
        this.endY = y2;
        this.lastValid = this.valid;
        this.valid = valid;
        this.solidTank = tank;
        this.solidBullet = bullet;

        validate();
    }

    public void validate()
    {
        if (!valid || (startX == endX && startY == endY))
            return;

        if (this.direction.isNonZeroY())
        {
            if (this.startX == this.endX)
                throw new RuntimeException("Face has zero width: " + this);
            if (this.startY != this.endY)
                throw new RuntimeException("Face is not horizontal: " + this);
        }
        else
        {
            if (this.startY == this.endY)
                throw new RuntimeException("Face has zero height: " + this);
            if (this.startX != this.endX)
                throw new RuntimeException("Face is not vertical: " + this);
        }
    }

    public String toString()
    {
        String ownerName = this.owner instanceof Obstacle ? ((Obstacle) this.owner).name : this.owner instanceof Tank ? ((Tank) this.owner).name : this.owner != null ? this.owner.getClass().getSimpleName() : "null";
        if (this.direction.isNonZeroY())
            return String.format("%.1f-%.1f %.1f  %s", this.startX, this.endX, this.startY, ownerName);
        else
            return String.format("%.1f %.1f-%.1f  %s", this.startX, this.startY, this.endY, ownerName);
    }
}
