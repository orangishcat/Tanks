package lwjglwindow.headless;

import basewindow.*;

public class HeadlessFontRenderer extends BaseFontRenderer
{
    public HeadlessFontRenderer(BaseWindow h)
    {
        super(h);
    }

    @Override
    public boolean supportsChar(char c)
    {
        return false;
    }

    @Override
    public void drawString(double x, double y, double z, double sX, double sY, String s, boolean depth)
    {

    }

    @Override
    public void drawString(double x, double y, double z, double sX, double sY, String s)
    {

    }

    @Override
    public void drawString(double x, double y, double sX, double sY, String s)
    {

    }

    @Override
    public double getStringSizeX(double sX, String s)
    {
        return 0;
    }

    @Override
    public double getStringSizeY(double sY, String s)
    {
        return 0;
    }

    @Override
    public void addFont(String imageFile, String chars, int[] charSizes)
    {

    }
}
