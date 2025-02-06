package tanks.replay.tests;

import tanks.replay.tests.test.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class TestRegistry
{
    private final ArrayList<Class<? extends Test>> tests = new ArrayList<>();
    private final ArrayList<Test> cache = new ArrayList<>();

    public TestRegistry()
    {
        registerTests();
    }

    public void registerTests()
    {
        registerTest(TestBullet.class);
        registerTest(TestExplosion.class);
        registerTest(TestAttribute.class);
        registerTest(TestSupportTank.class);
        registerTest(TestPathfinding.class);
    }

    public void registerTest(Class<? extends Test> t)
    {
        tests.add(t);
        cache.add(createTest(t));
    }

    public void refreshCache()
    {
        int i = 0;
        for (Class<? extends Test> test : tests)
            cache.set(i++, createTest(test));
    }

    private static Test createTest(Class<? extends Test> test)
    {
        try
        {
            return test.getConstructor().newInstance();
        }
        catch (InstantiationException | NoSuchMethodException | InvocationTargetException |
               IllegalAccessException e)
        {
            throw new RuntimeException("Failed to initialize replay", e);
        }
    }

    public int size()
    {
        return tests.size();
    }

    public Test loadTest(int i)
    {
        if (cache.get(i).loaded)
            cache.set(i, createTest(tests.get(i)));

        Test t = getTest(i);
        t.onLoad();
        return t;
    }

    public Test getTest(int i)
    {
        return cache.get(i);
    }
}
