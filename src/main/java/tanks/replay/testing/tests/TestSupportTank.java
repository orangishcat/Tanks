package tanks.replay.testing.tests;

import tanks.Game;
import tanks.Movable;
import tanks.replay.testing.Test;
import tanks.replay.testing.TestType;
import tanks.tank.TankAIControlled;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TestSupportTank extends Test
{
    public TestSupportTank()
    {
        super("support_tank_test", TestType.LEVEL);
        this.name = "Support tank test";
        this.maximumTime = 50;

        HashMap<String, Boolean> hashMap = new LinkedHashMap<>();
        hashMap.put("illusion", false);
        hashMap.put("decoy", false);
        hashMap.put("electric", false);
        hashMap.put("last_stand", false);
        hashMap.put("medic", true);
        hashMap.put("gold", true);
        hashMap.put("boost_kb", false);

        for (Map.Entry<String, Boolean> e : hashMap.entrySet())
        {
            String s = e.getKey() + " is" + (e.getValue() ? "" : " not") + " a support tank, ";
            expectAtEnd(() ->
            {
                for (Movable m : Game.movables)
                    if (m instanceof TankAIControlled && ((TankAIControlled) m).name.equals(e.getKey()) && ((TankAIControlled) m).isSupportTank() == e.getValue())
                        return true;
                return false;
            }).setPassMessage(s + "as expected")
                    .setFailMessage(s + "but function returned " + e.getValue());
        }
    }
}
