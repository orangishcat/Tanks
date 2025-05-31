package tanks.replay.testing.tests;

import tanks.Panel;
import tanks.replay.testing.Test;
import tanks.replay.testing.TestType;

public class TestPerformance extends Test
{
    public double totalMemory = 0, totalAlloc = 0, totalTime, lastMemory = 0;
    public int samples = 0;
    public long lastTime = System.currentTimeMillis();
    public TestFunction allocFunc, totalFunc, fpsFunc;

    public double allocAvg = 12, memoryAvg = 65, fpsAvg = 40;

    public TestPerformance()
    {
        super("performance_test", TestType.LEVEL);
        this.name = "Performance Test";
        this.maximumTime = 10 * 100;

        fpsFunc = expectAtEnd(() -> samples > 0 && samples * 0.6 >= fpsAvg);
        allocFunc = expectAtEnd(() -> samples > 0 && totalAlloc / samples / 1024 / 1024 <= allocAvg);
        totalFunc = expectAtEnd(() -> samples > 0 && totalMemory / samples / 1024 / 1024 <= memoryAvg);
    }

    @Override
    public boolean update()
    {
        if (Panel.panel.ageFrames % 6 == 0)
        {
            long fm = Runtime.getRuntime().freeMemory();
            long time = System.currentTimeMillis();

            totalMemory += Runtime.getRuntime().totalMemory();
            totalAlloc += Math.max(0, fm - lastMemory) / (time - lastTime) * 1000;
            totalTime += time - lastTime;
            lastMemory = fm;
            samples++;
            Panel.setTickSprint(false);
            lastTime = time;
        }

        return super.update();
    }

    @Override
    public boolean testPassed()
    {
        double ar = totalAlloc / samples / 1024 / 1024;
        double tm = totalMemory / samples / 1024 / 1024;
        double fps = samples * 0.6;
        fpsFunc.setName(String.format("FPS: %.1f fps %s %.1f fps", fps, fps <= fpsAvg ? "<=" : " >", fpsAvg));
        allocFunc.setName(String.format("Allocation rate: %.1f MB/s %s %.1f MB/s", ar, ar <= allocAvg ? "<=" : ">", allocAvg));
        totalFunc.setName(String.format("Total memory: %.1f MB %s %.1f MB", tm, tm <= memoryAvg ? "<=" : " >", memoryAvg));
        return super.testPassed();
    }
}
