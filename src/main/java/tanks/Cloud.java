package tanks;

import java.util.ArrayList;

public class Cloud implements IDrawable
{
    public ArrayList<Double> posX = new ArrayList<>();
    public ArrayList<Double> posY = new ArrayList<>();
    public double posZ = Math.random() * 100 + 500;
    public double size = Math.random() * 300 + 100;

    public Cloud(double x, double y)
    {
        int parts = (int) (Math.random() * 5) + 1;
        for (int i = 0; i < parts; i++)
        {
            this.posX.add(x + Math.random() * 100);
            this.posY.add(y + Math.random() * 100);
        }
    }

    @Override
    public void draw()
    {
        if (!Game.followingCam || !Game.enable3d || !Drawing.drawing.movingCamera)
            return;

        for (int i = 0; i < this.posY.size(); i++)
        {
            Drawing.drawing.setColor(255 * Level.currentLightIntensity, 255 * Level.currentLightIntensity, 255 * Level.currentLightIntensity, 128);
            Drawing.drawing.fillBox(this.posX.get(i), this.posY.get(i), this.posZ, size, size, 30, (byte) 0);
        }
    }

    public void update()
    {
        int i = 0;
        for (double x : this.posX)
            posX.set(i++, x + Panel.frameFrequency / 2);

        if (this.posX.get(0) < -0.5 * Game.currentSizeX * Game.tile_size || this.posX.get(0) > Game.currentSizeX * 1.5 * Game.tile_size)
            Game.removeClouds.add(this);
    }
}
