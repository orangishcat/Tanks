package tanks;

import tanks.obstacle.Face;
import tanks.obstacle.ISolidObject;

public abstract class SolidGameObject extends GameObject implements ISolidObject
{
    public Face[] faces;

    public abstract double getSize();

    @Override
    public Face[] getFaces()
    {
        return this.faces;
    }

    public boolean isFaceValid(Face f)
    {
        return true;
    }

    @Override
    public void updateFaces()
    {
        double s = this.getSize() / 2;

        if (this.faces == null)
        {
            this.faces = new Face[4];
            for (int i = 0; i < 4; i++)
                this.faces[i] = new Face(this, Direction.fromIndex(i), true, true);
        }

        for (int i = 0; i < 4; i++)
        {
            Face f = this.faces[i];
            f.startX = this.posX - s * Direction.X[i];
            f.endX = this.posX + s * Direction.X[i];
            f.startY = this.posY - s * Direction.Y[i];
            f.endY = this.posY + s * Direction.Y[i];
            f.valid = this.isFaceValid(f);
        }
    }
}
