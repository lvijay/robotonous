package com.lvijay.robotonous.test;

import static com.lvijay.robotonous.test.MockRobot.Event.DELAY;
import static com.lvijay.robotonous.test.MockRobot.Event.KEY_PRESS;
import static com.lvijay.robotonous.test.MockRobot.Event.KEY_RELEASE;
import static com.lvijay.robotonous.test.MockRobot.Event.MOUSE_MOVE;
import static com.lvijay.robotonous.test.MockRobot.Event.MOUSE_PRESS;
import static com.lvijay.robotonous.test.MockRobot.Event.MOUSE_RELEASE;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MockRobot extends Robot {
    public static enum Event {
        MOUSE_MOVE,
        MOUSE_PRESS,
        MOUSE_RELEASE,
        KEY_PRESS,
        KEY_RELEASE,
        DELAY,
        ;
    }

    public static final record Pair(Event event, String detail) {
        public Pair(Event event, int keycode) {
            this(event, "" + keycode);
        }

        @Override
        public String toString() {
            if (event.name().startsWith("KEY")) {
                int keycode = Integer.parseInt(detail);
                if ((keycode >= '0' && keycode <= '9')
                        || (keycode >= 'A' && keycode <= 'Z')
                        || (keycode >= 'a' && keycode <= 'z')) {
                    return "'" + Character.toString((char) keycode) + "'";
                }
                if (Character.isJavaIdentifierPart(keycode)) {
                    return "«" + Character.toString((char) keycode) + "," + keycode + "»";
                }
            }

            return String.format("<%s %s>", event(), detail());
        }
    }

    private final List<Pair> records = new ArrayList<>();

    public List<Pair> records() { return Collections.unmodifiableList(records); }

    public void clearRecords() { records.clear(); }

    public MockRobot() throws AWTException {
        super();
    }

    // Overridden methods

    @Override
    public synchronized void mouseMove(int x, int y) {
        records.add(new Pair(MOUSE_MOVE, String.format("(%d,%d)", x, y)));
    }

    @Override
    public synchronized void mousePress(int buttons) {
        records.add(new Pair(MOUSE_PRESS, Integer.toString(buttons)));
    }

    @Override
    public synchronized void mouseRelease(int buttons) {
        records.add(new Pair(MOUSE_RELEASE, Integer.toString(buttons)));
    }

    @Override
    public synchronized void keyPress(int keycode) {
        records.add(new Pair(KEY_PRESS, keycode));
    }

    @Override
    public synchronized void keyRelease(int keycode) {
        records.add(new Pair(KEY_RELEASE, keycode));
    }

    @Override
    public void delay(int ms) {
        records.add(new Pair(DELAY, ms));
    }
}
