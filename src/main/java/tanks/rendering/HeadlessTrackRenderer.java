package tanks.rendering;

import basewindow.IBatchRenderableObject;

public class HeadlessTrackRenderer extends TrackRenderer
{
    @Override
    public void addRect(IBatchRenderableObject o, double x, double y, double z, double width, double height, double rotation)
    {
    }

    @Override
    public void remove(IBatchRenderableObject o)
    {
    }

    @Override
    public void reset()
    {
        this.tiles = null;
        this.renderers.clear();
        this.renderersByObj.clear();
    }

    @Override
    public void draw()
    {
    }
}
