package tanks.handle;

import tanks.Level;

public interface ILevelLoadHandler extends IHandle
{
    void onLevelLoad(Level l);
}
