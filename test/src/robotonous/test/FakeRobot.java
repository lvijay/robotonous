package robotonous.test;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FakeRobot extends Robot {
    public static final class Action {
        private final String name;
        private final String[] args;
        public Action(String name, String... args) {
            this.name = name;
            if (args == null) {
                this.args = new String[0];
            } else {
                this.args = args;
            }
        }
        @Override public String toString() {
            if (args != null) {
                return Arrays.stream(args)
                        .map(v -> v == null ? "null" : v.toString())
                        .collect(Collectors.joining(", ", name + "(", ")"));
            }
            return name + "()";
        }
        @Override public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (!(o instanceof Action)) {
                return false;
            }
            Action that = (Action) o;
            // ignore args
            return Objects.equals(this.name, that.name);
        }
        @Override public int hashCode() {
            return 0;
        }
    }

    private final List<Action> actions = new ArrayList<>();

    public FakeRobot() throws AWTException {
    }

    @Override
    public synchronized void mouseMove(int x, int y) {
        actions.add(new Action("mouseMove", x + "", y + ""));
    }

    @Override
    public synchronized void mousePress(int buttons) {
        actions.add(new Action("mousePress", buttons + ""));
    }

    @Override
    public synchronized void mouseRelease(int buttons) {
        actions.add(new Action("mouseRelease", buttons + ""));
    }

    @Override
    public synchronized void mouseWheel(int wheelAmt) {
        actions.add(new Action("mouseWheel", wheelAmt + ""));
    }

    @Override
    public synchronized void keyPress(int keycode) {
        actions.add(new Action("keyPress", Character.isLetterOrDigit(keycode)
                ? Character.toString(keycode)
                : "" + keycode));
    }

    @Override
    public synchronized void keyRelease(int keycode) {
        actions.add(new Action("keyRelease", Character.isLetterOrDigit(keycode)
                ? Character.toString(keycode)
                : "" + keycode));
    }

    @Override
    public synchronized Color getPixelColor(int x, int y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized BufferedImage createScreenCapture(Rectangle screenRect) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized MultiResolutionImage createMultiResolutionScreenCapture(Rectangle screenRect) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized boolean isAutoWaitForIdle() {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized void setAutoWaitForIdle(boolean isOn) {
        actions.add(new Action("setAutoWaitForIdle", "" + isOn));
    }

    @Override
    public synchronized int getAutoDelay() {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized void setAutoDelay(int ms) {
        actions.add(new Action("setAutoDelay", "" + ms));
    }

    @Override
    public synchronized void delay(int ms) {
        actions.add(new Action("delay", "" + ms));
    }

    @Override
    public synchronized void waitForIdle() {
        actions.add(new Action("waitForIdle"));
    }

    @Override
    public synchronized String toString() {
        return actions.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public List<Action> actions() {
        return List.copyOf(actions);
    }
}
