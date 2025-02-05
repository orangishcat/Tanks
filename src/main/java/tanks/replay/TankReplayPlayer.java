package tanks.replay;

import tanks.tank.Tank;
import tanks.tank.TankRemote;

public class TankReplayPlayer extends TankRemote
{
    public TankReplayPlayer(Tank t)
    {
        super(t);
        networkID = t.networkID;
        isRemote = false;
        invulnerable = false;
    }
}
