package lwjglwindow.headless;

import basewindow.BaseShapeRenderer;

public class HeadlessShapeRenderer extends BaseShapeRenderer
{
    public void fillOval(double x, double y, double sX, double sY) {}
    public void fillOval(double x, double y, double z, double sX, double sY, boolean depthTest) {}
    public void fillPartialOval(double x, double y, double sX, double sY, double start, double end) {}
    public void fillFacingOval(double x, double y, double z, double sX, double sY, boolean depthTest) {}
    public void fillFacingOval(double x, double y, double z, double sX, double sY, double oZ, boolean depthTest) {}
    public void fillPartialRing(double x, double y, double size, double thickness, double start, double end) {}
    public void fillPartialRing(double x, double y, double z, double size, double thickness, double start, double end) {}
    public void fillGlow(double x, double y, double sX, double sY) {}
    public void fillGlow(double x, double y, double z, double sX, double sY, boolean depthTest) {}
    public void fillFacingGlow(double x, double y, double z, double sX, double sY, boolean depthTest) {}
    public void fillGlow(double x, double y, double sX, double sY, boolean shade) {}
    public void fillGlow(double x, double y, double sX, double sY, boolean shade, boolean light) {}
    public void fillGlow(double x, double y, double z, double sX, double sY, boolean depthTest, boolean shade) {}
    public void fillGlow(double x, double y, double z, double sX, double sY, boolean depthTest, boolean shade, boolean light) {}
    public void fillFacingGlow(double x, double y, double z, double sX, double sY, boolean depthTest, boolean shade) {}
    public void fillFacingGlow(double x, double y, double z, double sX, double sY, boolean depthTest, boolean shade, boolean light) {}
    public void drawOval(double x, double y, double sX, double sY) {}
    public void drawOval(double x, double y, double z, double sX, double sY) {}
    public void fillRect(double x, double y, double sX, double sY) {}
    public void fillRect(double x, double y, double z, double sX, double sY, boolean depthTest) {}
    public void fillRoundedRect(double x, double y, double sX, double sY, double radius) {}
    public void fillBox(double x, double y, double z, double sX, double sY, double sZ, String texture) {}
    public void fillBox(double x, double y, double z, double sX, double sY, double sZ, byte options, String texture) {}
    public void fillQuad(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {}
    public void fillQuadBox(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4, double z, double sZ, byte options) {}
    public void drawRect(double x, double y, double sX, double sY) {}
    public void drawRect(double x, double y, double sX, double sY, double borderWidth) {}
    public void drawRect(double x, double y, double sX, double sY, double borderWidth, double borderRadius) {}
    public void drawImage(double x, double y, double sX, double sY, String image, boolean scaled) {}
    public void drawImage(double x, double y, double z, double sX, double sY, String image, boolean scaled) {}
    public void drawImage(double x, double y, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled) {}
    public void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled) {}
    public void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled, boolean depthtest) {}
    public void drawImage(double x, double y, double sX, double sY, String image, double rotation, boolean scaled) {}
    public void drawImage(double x, double y, double z, double sX, double sY, String image, double rotation, boolean scaled) {}
    public void drawImage(double x, double y, double sX, double sY, double u1, double v1, double u2, double v2, String image, double rotation, boolean scaled) {}
    public void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, double rotation, boolean scaled) {}
    public void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, double rotation, boolean scaled, boolean depthtest) {}
    public void setBatchMode(boolean enabled, boolean quads, boolean depth) {}
    public void setBatchMode(boolean enabled, boolean quads, boolean depth, boolean glow) {}
    public void setBatchMode(boolean enabled, boolean quads, boolean depth, boolean glow, boolean depthMask) {}
}
