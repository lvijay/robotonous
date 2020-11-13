package robotonous;

import static java.awt.event.InputEvent.BUTTON1_DOWN_MASK;
import static java.awt.event.InputEvent.BUTTON2_DOWN_MASK;
import static robotonous.Robotonous.toActions;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import de.tudresden.inf.lat.jsexp.Sexp;
import de.tudresden.inf.lat.jsexp.SexpParserException;
import robotonous.Robotonous.RobotAction;
import robotonous.test.FakeRobot;
import robotonous.test.FakeRobot.Action;

public final class RobotonousTest {
    static public void main(String[] args)
            throws IOException, SexpParserException, AWTException {
        {
            var expression = parseSexp("(:mouseclick ($left ($right)))");

            new RobotonousTest(new FakeRobot())
                    .test("simultaneous mouse clicks",
                            toActions(expression),
                            newAction("mousePress", BUTTON1_DOWN_MASK),
                            newAction("mousePress", BUTTON2_DOWN_MASK),
                            newAction("mouseRelease", BUTTON1_DOWN_MASK),
                            newAction("mouseRelease", BUTTON2_DOWN_MASK));
        }

        {
            new RobotonousTest(new FakeRobot())
                    .test("type some alphabets",
                            toActions(parseSexp("(:type abCD)")),
                            newAction("keyPress",   KeyEvent.VK_A),
                            newAction("keyRelease", KeyEvent.VK_A),
                            newAction("keyPress",   KeyEvent.VK_B),
                            newAction("keyRelease", KeyEvent.VK_B),
                            newAction("keyPress",   KeyEvent.VK_SHIFT),
                            newAction("keyPress",   KeyEvent.VK_C),
                            newAction("keyRelease", KeyEvent.VK_C),
                            newAction("keyRelease", KeyEvent.VK_SHIFT),
                            newAction("keyPress",   KeyEvent.VK_SHIFT),
                            newAction("keyPress",   KeyEvent.VK_D),
                            newAction("keyRelease", KeyEvent.VK_D),
                            newAction("keyRelease", KeyEvent.VK_SHIFT));
        }

        {
            var expression = parseSexp("(:type a (:delay 3141) ($ctrl ($alt $enter) $left))");

            new RobotonousTest(new FakeRobot())
                    .test("type some control keys",
                            toActions(expression),
                            newAction("keyPress",   KeyEvent.VK_A),
                            newAction("keyRelease", KeyEvent.VK_A),
                            newAction("delay",      3141),
                            newAction("keyPress",   KeyEvent.VK_CONTROL),
                            newAction("keyPress",   KeyEvent.VK_ALT),
                            newAction("keyPress",   KeyEvent.VK_ENTER),
                            newAction("keyRelease", KeyEvent.VK_ENTER),
                            newAction("keyRelease", KeyEvent.VK_ALT),
                            newAction("keyPress",   KeyEvent.VK_LEFT),
                            newAction("keyRelease", KeyEvent.VK_LEFT),
                            newAction("keyRelease", KeyEvent.VK_CONTROL));
        }

        {
            var expression = parseSexp("(:type |(|)");

            new RobotonousTest(new FakeRobot())
                    .test("type some special characters",
                            toActions(expression),
                            newAction("keyPress",   KeyEvent.VK_SHIFT),
                            newAction("keyPress",   KeyEvent.VK_0),
                            newAction("keyRelease", KeyEvent.VK_0),
                            newAction("keyRelease", KeyEvent.VK_SHIFT));
        }
    }

    private final FakeRobot robot;

    public RobotonousTest(FakeRobot robot) { this.robot = robot; }

    private void test(String testname,
            List<RobotAction> actions,
            Action... expected) {
        actions
                .forEach(axn -> axn.execute(robot));

        List<Action> actual = robot.actions();
        List<Action> expctd = Arrays.asList(expected);

        if (!actual.equals(expctd)) {
            String message = "test " + testname + " failed.";

            message += String.format("%nExpected: %s%nActual  : %s", expctd, actual);

            throw new IllegalStateException(message);
        }
    }

    static private Sexp parseSexp(String content)
            throws SexpParserException {
        try (var reader = new StringReader(content)) {
            return Robotonous.parse(reader);
        } catch (IOException e) {
            throw new RuntimeException("unreachable", e);
        }
    }

    static private Action newAction(String name, Object... args) {
        if (args == null) {
            return new Action(name);
        }

        String[] strargs = Arrays.stream(args)
                .map(v -> {
                    if (v instanceof Integer) {
                        int i = ((Integer) v).intValue();
                        return Character.isLetterOrDigit(i)
                                ? Character.toString(i)
                                : "" + i;
                    }
                    return v.toString();
                })
                .toArray(String[]::new);

        return new Action(name, strargs);
    }
}
