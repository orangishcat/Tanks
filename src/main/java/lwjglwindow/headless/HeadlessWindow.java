package lwjglwindow.headless;

import basewindow.*;
import basewindow.transformation.Matrix4;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;

public class HeadlessWindow extends BaseWindow
{
    public HeadlessWindow()
    {
        super("headless", 0, 0, 0, null, null, null, false, false);
    }
    public void run() {}
    public void setShowCursor(boolean show) {}
    public void setCursorLocked(boolean locked) {}
    public void setCursorPos(double x, double y) {}
    public void setFullscreen(boolean enabled) {}
    public void setOverrideLocations(ArrayList<String> loc, BaseFileManager fileManager) {}
    public void setIcon(String icon) {}
    public void setColor(double r, double g, double b, double a, double glow) {}
    public void setColor(double r, double g, double b, double a) {}
    public void setColor(double r, double g, double b) {}
    public void setUpPerspective() {}
    public void applyTransformations() {}
    public void loadPerspective() {}
    public void clearDepth() {}
    public void setWindowTitle(String s) {}
    public String getClipboard()
    {
        return "";
    }
    public void setClipboard(String s) {}
    public void setVsync(boolean enable) {}
    public ArrayList<Character> getRawTextKeys()
    {
        return null;
    }
    public String getKeyText(int key)
    {
        return "";
    }
    public String getTextKeyText(int key)
    {
        return "";
    }
    public int translateKey(int key)
    {
        return 0;
    }
    public int translateTextKey(int key)
    {
        return 0;
    }
    public void transform(double[] matrix) {}
    public void transform(Matrix4 matrix) {}
    public void calculateBillboard() {}
    public double getEdgeBounds()
    {
        return 0;
    }
    public void createImage(String image, InputStream in) {}
    public void setUpscaleImages(boolean upscaleImages) {}
    public void setTextureCoords(double u, double v) {}
    public void setTexture(String image) {}
    public void stopTexture() {}
    public void addVertex(double x, double y, double z) {}
    public void addVertex(double x, double y) {}
    public void openLink(URL url) throws Exception {}
    public void setResolution(int x, int y) {}
    public void setShadowQuality(double quality) {}
    public double getShadowQuality()
    {
        return 0;
    }
    public void setLighting(double light, double glowLight, double shadow, double glowShadow) {}
    public void setMaterialLights(float[] ambient, float[] diffuse, float[] specular, double shininess) {}
    public void setMaterialLights(float[] ambient, float[] diffuse, float[] specular, double shininess, double minBound, double maxBound, boolean enableNegative) {}
    public void disableMaterialLights() {}
    public void setCelShadingSections(float sections) {}
    public void createLights(ArrayList<double[]> lights, double scale) {}
    public void addMatrix() {}
    public void removeMatrix() {}
    public void setMatrixProjection() {}
    public void setMatrixModelview() {}
    public float[] getTransformedMouse()
    {
        return new float[0];
    }
    public float[] getTransformedMouse(double x, double y)
    {
        return new float[0];
    }
    public ModelPart createModelPart()
    {
        return null;
    }
    public ModelPart createModelPart(Model model, ArrayList<ModelPart.Shape> shapes, Model.Material material)
    {
        return null;
    }
    public PosedModel createPosedModel(Model m)
    {
        return null;
    }
    public BaseShapeBatchRenderer createStaticBatchRenderer(ShaderGroup shader, boolean color, String texture, boolean normal, int vertices)
    {
        return null;
    }
    public BaseShapeBatchRenderer createShapeBatchRenderer()
    {
        return null;
    }
    public BaseShapeBatchRenderer createShapeBatchRenderer(ShaderGroup shader)
    {
        return null;
    }
    public BaseShaderUtil getShaderUtil(ShaderProgram p)
    {
        return null;
    }
    public String screenshot(String dir, boolean async) throws IOException
    {
        return "";
    }
    public void setForceModelGlow(boolean glow) {}
}
