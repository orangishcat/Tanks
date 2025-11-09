package tanks.rendering;

import basewindow.IBatchRenderableObject;

public class HeadlessTerrainRenderer extends TerrainRenderer
{
    @Override
    protected void initializeShaders()
    {
        // Skip shader initialization when running headless
    }

    @Override
    public void addBox(IBatchRenderableObject o, double x, double y, double z, double sX, double sY, double sZ, byte options, boolean out)
    {
    }

    @Override
    public void addBoxWithCenter(IBatchRenderableObject o, double x, double y, double z, double sX, double sY, double sZ, byte options, boolean alternate, float cx, float cy, float cz)
    {
    }

    @Override
    public void remove(IBatchRenderableObject o)
    {
    }

    @Override
    public void reset()
    {
        this.renderers.clear();
        this.renderersByObj.clear();
        this.outOfBoundsRenderers.clear();
        this.staged = false;
        this.bgStaged = false;
        this.stagedCount = 0;
        this.totalObjectsCount = 0;
        this.hasContinuationed = false;
        this.allowPartialLoading = false;
    }

    @Override
    public void draw()
    {
    }
}
