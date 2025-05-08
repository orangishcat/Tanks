package tanks.replay.testing;

import tanks.Game;
import tanks.network.event.EventRunTest;
import tanks.network.event.EventStartTest;

import java.util.Arrays;

public class TestRunner
{
    public Test current;
    public int pos;
    public TestState[] testStates = new TestState[Test.registry.size()];
    {
        Arrays.fill(testStates, TestState.notRun);
    }

    public boolean started;
    public int passedCases = 0;

    public enum TestState {passed, failed, running, notRun}

    public static void reset()
    {
        Test.runner = new TestRunner();
    }

    public static void startTests()
    {
        reset();
        Test.runner.started = true;
        Test.registry.refreshCache();
        Game.eventsOut.add(new EventStartTest());
    }

    public TestState getState(int ind)
    {
        if (pos - 1 == ind && started && !(current != null && current.finished))
            return TestState.running;
        return testStates[ind];
    }

    public boolean updateTest()
    {
        if (current == null)
            return true;

        return current.update();
    }

    public void loadTest(int i)
    {
        pos = i + 1;
        retryTest();
    }

    public void retryTest()
    {
        if (pos > Test.registry.size())
            return;

        current = Test.registry.loadTest(pos - 1);
        started = true;
        Game.eventsOut.add(new EventRunTest(pos - 1));
    }

    public void runNextTest()
    {
        pos++;
        retryTest();
    }

    public boolean currentTestPassed()
    {
        if (current == null || pos > testStates.length)
            return true;

        boolean b = current.testPassed();
        testStates[pos - 1] = b ? TestState.passed : TestState.failed;
        updatePassed();
        return b;
    }

    public void updatePassed()
    {
        passedCases = 0;
        for (TestState s : testStates)
            if (s == TestState.passed)
                passedCases++;
    }
}
