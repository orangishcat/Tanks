package lwjglwindow;

import basewindow.BaseFontRenderer;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class FontRenderer extends BaseFontRenderer
{
    public static class FontInfo
    {
        public String chars;
        public int[] charSizes;
        public String image;
        public float size = 16; // how many characters fit per horizontal line
        public int hSpace = 2; // spacing between rows, increase this to 2 for antialiasing to prevent weird artifacts
        public Int2IntOpenHashMap charIndexMap = new Int2IntOpenHashMap();

        public FontInfo(String image, String chars, int[] charSizes)
        {
            this.image = image;
            this.chars = chars;
            this.charSizes = charSizes;

            for (int i = 0; i < chars.length(); i++)
                charIndexMap.put(chars.charAt(i), i);
        }
    }

    private final List<FontInfo> fontInfos = new ArrayList<>();
    private final FontInfo defaultFont;

    public FontRenderer(LWJGLWindow h, String defaultFontFile)
    {
        super(h);

        defaultFont = new FontInfo(defaultFontFile,
            " !\"#$%&'()*+,-./" +
                "0123456789:;<=>?" +
                "@ABCDEFGHIJKLMNO" +
                "PQRSTUVWXYZ[\\]^_" +
                "'abcdefghijklmno" +
                "pqrstuvwxyz{|}~`" +
                "âăîşţàçæèéêëïôœù" +
                "úûüÿáíóñ¡¿äöå",
            new int[]{
                3, 2, 4, 5, 5, 6, 5, 2, 3, 3, 4, 5, 1, 5, 1, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 1, 1, 5, 5, 5, 5,
                7, 5, 5, 5, 5, 5, 5, 5, 5, 3, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 3, 5, 3, 5, 5,
                2, 5, 5, 5, 5, 5, 4, 5, 5, 1, 5, 4, 2, 5, 5, 5,
                5, 5, 5, 5, 3, 5, 5, 5, 5, 5, 5, 4, 1, 4, 6, 2,
                5, 5, 5, 5, 3, 5, 5, 7, 5, 5, 5, 5, 3, 5, 7, 5,
                5, 5, 5, 5, 5, 3, 5, 5, 3, 5, 5, 5, 5
            });

        fontInfos.add(defaultFont);
    }

    /**
     * Add a new font to the renderer.
     *
     * @param imageFile The image file path.
     * @param chars     The characters to include in the font.
     * @param charSizes The width of each character in (pixels / 4).
     */
    public void addFont(String imageFile, String chars, int[] charSizes)
    {
        fontInfos.add(new FontInfo(imageFile, chars, charSizes));
    }

    public boolean supportsChar(char c)
    {
        for (FontInfo font : fontInfos)
        {
            if (font.charIndexMap.containsKey(c))
            {
                return true;
            }
        }
        return false;
    }

    protected FontInfo findFontForChar(char c)
    {
        for (FontInfo font : fontInfos)
        {
            if (font.charIndexMap.containsKey(c))
                return font;
        }
        return defaultFont;
    }

    protected int drawChar(double x, double y, double z, double sX, double sY, char c, boolean depthtest)
    {
        FontInfo font = findFontForChar(c);
        int i = font.charIndexMap.getOrDefault(c, '?');

        int col = (int) (i % font.size);
        int row = (int) (i / font.size);
        int width = font.charSizes[i];

        if (this.drawBox)
        {
            this.window.shapeRenderer.drawRect(x, y, sX * width * 2, sY * 32);
            this.window.shapeRenderer.drawRect(x, y + sY * 16, sX * width * 2, sY * 16);
            this.window.shapeRenderer.drawRect(x + sX * width * 2, y, sX * width * 2, sY * 32);
            this.window.shapeRenderer.drawRect(x + sX * width * 2, y + sY * 16, sX * width * 2, sY * 16);
        }

        this.window.shapeRenderer.drawImage(x, y - sY * 16, z, sX * 32 * font.size, sY * 32 * font.size,
            col / font.size, (row * font.hSpace) / font.size,
            (col + width / 8f) / font.size, (row * font.hSpace + 2) / font.size,
            font.image, false, depthtest);
        return width;
    }

    public void drawString(double x, double y, double z, double sX, double sY, String s)
    {
        drawString(x, y, z, sX, sY, s, true);
    }

    public void drawString(double x, double y, double z, double sX, double sY, String s, boolean depth)
    {
        if (window.drawingShadow)
            return;

        if (depth)
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        else
            GL11.glDisable(GL11.GL_DEPTH_TEST);

        double opacity = this.window.colorA;

        double curX = x;

        double r0 = this.window.colorR;
        double g0 = this.window.colorG;
        double b0 = this.window.colorB;
        double a0 = this.window.colorA;

        for (int i = 0; i < s.length(); i++)
        {
            if (s.charAt(i) == '\u00C2')
                continue;

            if (s.charAt(i) == '\u00A7')
            {
                if (s.charAt(i + 1) == 'r')
                {
                    i++;
                    this.window.setColor(r0 * 255, g0 * 255, b0 * 255, a0 * 255);
                }
                else
                    i = handleColorChar(s, opacity, i);
            }
            else
                curX += (drawChar(curX, y, z, sX, sY, s.charAt(i), true) + 1) * sX * 4;
        }

        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    public void drawString(double x, double y, double sX, double sY, String s)
    {
        if (window.drawingShadow)
            return;

        double curX = x;
        double opacity = this.window.colorA;

        double r0 = this.window.colorR;
        double g0 = this.window.colorG;
        double b0 = this.window.colorB;
        double a0 = this.window.colorA;

        for (int i = 0; i < s.length(); i++)
        {
            if (s.charAt(i) == '\u00C2')
                continue;

            if (s.charAt(i) == '\u00A7')
            {
                if (s.charAt(i + 1) == 'r')
                {
                    i++;
                    this.window.setColor(r0 * 255, g0 * 255, b0 * 255, a0 * 255);
                }
                else
                    i = handleColorChar(s, opacity, i);
            }
            else
                curX += (drawChar(curX, y, 0, sX, sY, s.charAt(i), false) + 1) * sX * 4;
        }
    }

    public int handleColorChar(String s, double opacity, int i)
    {
        if (s.length() <= i + 12)
            return i;

        try
        {
            int r = Integer.parseInt(s.charAt(i + 1) + "" + s.charAt(i + 2) + s.charAt(i + 3));
            int g = Integer.parseInt(s.charAt(i + 4) + "" + s.charAt(i + 5) + s.charAt(i + 6));
            int b = Integer.parseInt(s.charAt(i + 7) + "" + s.charAt(i + 8) + s.charAt(i + 9));
            int a = Integer.parseInt(s.charAt(i + 10) + "" + s.charAt(i + 11) + s.charAt(i + 12));
            this.window.setColor(r, g, b, a * opacity);
        }
        catch (Exception e)
        {
            return i;
        }

        i += 12;
        return i;
    }

    public double getStringSizeX(double sX, String s)
    {
        double w = 0;

        for (int i = 0; i < s.length(); i++)
        {
            if (s.charAt(i) == '\u00C2')
                continue;
            else if (s.charAt(i) == '\u00A7')
            {
                if (s.length() <= i + 1)
                    continue;

                if (s.charAt(i + 1) == 'r')
                {
                    i++;
                    continue;
                }

                if (s.length() <= i + 12)
                    continue;

                i += 12;
            }
            else
            {
                FontInfo font = findFontForChar(s.charAt(i));
                int index = font.charIndexMap.getOrDefault(s.charAt(i), '?');
                w += (font.charSizes[index] + 1) * sX * 4;
            }
        }

        return Math.max(w - sX * 4, 0);
    }

    public double getStringSizeY(double sY, String s)
    {
        return (sY * 32);
    }
}
