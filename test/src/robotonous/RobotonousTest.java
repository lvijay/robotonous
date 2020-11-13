package robotonous;

import static de.tudresden.inf.lat.jsexp.SexpFactory.parse;
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

public class RobotonousTest {
    public static void main(String[] args)
            throws IOException, SexpParserException, AWTException {
        {
            var robot = new robotonous.test.FakeRobot();
            var test = new RobotonousTest(robot);
            var expression = parseSexp("(:mouseclick ($left ($right)))");
            var commands = toActions(expression);
            List<Action> expected = List.of(
                    newAction("mousePress", BUTTON1_DOWN_MASK),
                    newAction("mousePress", BUTTON2_DOWN_MASK),
                    newAction("mouseRelease", BUTTON1_DOWN_MASK),
                    newAction("mouseRelease", BUTTON2_DOWN_MASK));

            test.test("simultaneous mouse clicks", commands, expected);
        }

        {
            var robot = new robotonous.test.FakeRobot();
            var test = new RobotonousTest(robot);
            var commands = toActions(parseSexp("(:type abCD)"));
            List<Action> expected = List.of(
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

            test.test("type some alphabets",
                    commands, expected);
        }

        {
            var robot = new robotonous.test.FakeRobot();
            var test = new RobotonousTest(robot);
            var expression = parseSexp("(:type a (:delay 3141) ($ctrl ($alt $enter) $left))");
            var commands = toActions(expression);
            List<Action> expected = List.of(
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

            test.test("type some control keys",
                    commands, expected);
        }
    }

    private final FakeRobot robot;

    public RobotonousTest(FakeRobot robot) { this.robot = robot; }

    private void test(String testname,
            List<RobotAction> actions,
            List<Action> expected) {
        actions
                .forEach(axn -> axn.execute(robot));

        List<Action> actual = robot.actions();

        if (!expected.equals(actual)) {
            String message = "test " + testname + " failed.";

            message += String.format("%nExpected: %s%nActual  : %s", expected, actual);

            throw new IllegalStateException(message);
        }
    }

    private static Sexp parseSexp(String content)
            throws SexpParserException {
        try (var reader = new StringReader(content)) {
            return parse(reader);
        } catch (IOException e) {
            throw new RuntimeException("unreachable", e);
        }
    }

    private static Action newAction(String name, Object... args) {
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
