package com.lvijay.robotonous;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.lvijay.robotonous.test.MockClipboard;
import com.lvijay.robotonous.test.MockRobot;
import com.lvijay.robotonous.test.MockRobot.Event;
import com.lvijay.robotonous.test.MockRobot.Pair;

public class TestRobotonous {
    public static void main(String[] args) throws AWTException {
        var testRobotonous = new TestRobotonous();

        runTests(testRobotonous);
    }

    private static final void runTests(TestRobotonous instance) {
        List<Method> testMethods = Arrays.stream(TestRobotonous.class.getDeclaredMethods())
                .filter(m -> m.getName().startsWith("test"))
                .filter(m -> Modifier.isPrivate(m.getModifiers()))
                .map(m -> { m.setAccessible(true); return m; })
                .collect(Collectors.toList());

        for (Method m : testMethods) {
            try {
                m.invoke(instance);
            } catch (InvocationTargetException e) {
                e.getCause().printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Error e) {
                e.printStackTrace();
            }
        }
    }

    private final MockRobot robot;
    private final Robotonous robotonous;

    public TestRobotonous() throws AWTException {
        this.robot = new MockRobot();
        this.robotonous = new Robotonous(robot, new MockClipboard(robot));
    }

    @SuppressWarnings("unused")
    private void testActions() {
        this.robot.clearRecords();
        robotonous.type("#3a\tm⑳");
        List<Pair> records = this.robot.records();

        assert records.equals(List.of(
                new Pair(Event.KEY_PRESS, "" + KeyEvent.VK_SHIFT),
                new Pair(Event.KEY_PRESS, "" + KeyEvent.VK_3),
                new Pair(Event.KEY_RELEASE, "" + KeyEvent.VK_3),
                new Pair(Event.KEY_RELEASE, "" + KeyEvent.VK_SHIFT),
                new Pair(Event.KEY_PRESS, "" + KeyEvent.VK_3),
                new Pair(Event.KEY_RELEASE, "" + KeyEvent.VK_3),
                new Pair(Event.KEY_PRESS, "" + KeyEvent.VK_A),
                new Pair(Event.KEY_RELEASE, "" + KeyEvent.VK_A),
                new Pair(Event.KEY_PRESS, "" + KeyEvent.VK_TAB),
                new Pair(Event.KEY_RELEASE, "" + KeyEvent.VK_TAB),
                new Pair(Event.KEY_PRESS, "" + KeyEvent.VK_M),
                new Pair(Event.KEY_RELEASE, "" + KeyEvent.VK_M),
                new Pair(Event.DELAY, "2000")
            ));
    }

    @SuppressWarnings("unused")
    private void testPaste() {
        this.robot.clearRecords();
        robotonous.type("〈abcdEFGhij!@#$)<>…“”‘’〉");
        List<Pair> records = this.robot.records();

        assert records.equals(List.of(
                new Pair(Event.PASTE, "abcdEFGhij!@#$)<>…“”‘’")
            ));
    }
}
