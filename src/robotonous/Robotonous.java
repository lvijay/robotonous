package robotonous;

import static de.tudresden.inf.lat.jsexp.SexpFactory.newAtomicSexp;
import static de.tudresden.inf.lat.jsexp.SexpFactory.newNonAtomicSexp;
import static de.tudresden.inf.lat.jsexp.SexpFactory.parse;
import static java.awt.event.InputEvent.getMaskForButton;
import static java.awt.event.KeyEvent.VK_0;
import static java.awt.event.KeyEvent.VK_1;
import static java.awt.event.KeyEvent.VK_2;
import static java.awt.event.KeyEvent.VK_3;
import static java.awt.event.KeyEvent.VK_4;
import static java.awt.event.KeyEvent.VK_5;
import static java.awt.event.KeyEvent.VK_6;
import static java.awt.event.KeyEvent.VK_7;
import static java.awt.event.KeyEvent.VK_8;
import static java.awt.event.KeyEvent.VK_9;
import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_ALT;
import static java.awt.event.KeyEvent.VK_BACK_QUOTE;
import static java.awt.event.KeyEvent.VK_BACK_SLASH;
import static java.awt.event.KeyEvent.VK_BACK_SPACE;
import static java.awt.event.KeyEvent.VK_CLOSE_BRACKET;
import static java.awt.event.KeyEvent.VK_COMMA;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_EQUALS;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_F1;
import static java.awt.event.KeyEvent.VK_F10;
import static java.awt.event.KeyEvent.VK_F11;
import static java.awt.event.KeyEvent.VK_F12;
import static java.awt.event.KeyEvent.VK_F2;
import static java.awt.event.KeyEvent.VK_F3;
import static java.awt.event.KeyEvent.VK_F4;
import static java.awt.event.KeyEvent.VK_F5;
import static java.awt.event.KeyEvent.VK_F6;
import static java.awt.event.KeyEvent.VK_F7;
import static java.awt.event.KeyEvent.VK_F8;
import static java.awt.event.KeyEvent.VK_F9;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_META;
import static java.awt.event.KeyEvent.VK_MINUS;
import static java.awt.event.KeyEvent.VK_OPEN_BRACKET;
import static java.awt.event.KeyEvent.VK_PERIOD;
import static java.awt.event.KeyEvent.VK_QUOTE;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_SEMICOLON;
import static java.awt.event.KeyEvent.VK_SHIFT;
import static java.awt.event.KeyEvent.VK_SLASH;
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.awt.event.KeyEvent.VK_TAB;
import static java.awt.event.KeyEvent.VK_UP;
import static java.awt.event.MouseEvent.BUTTON1;
import static java.awt.event.MouseEvent.BUTTON2;
import static java.awt.event.MouseEvent.BUTTON3;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Map.entry;
import static java.util.stream.Collectors.toList;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

import de.tudresden.inf.lat.jsexp.Sexp;
import de.tudresden.inf.lat.jsexp.SexpFactory;
import de.tudresden.inf.lat.jsexp.SexpList;
import de.tudresden.inf.lat.jsexp.SexpParserException;

public class Robotonous {
    public static void main(String[] args)
            throws IOException, SexpParserException, AWTException {
        int delaySeconds = 5;

        var robot = new java.awt.Robot();
        robot.setAutoDelay(80);

        for (int i = delaySeconds; i > 0; --i) {
            System.out.println("starting in..." + i);
            robot.delay(1000);
        }

        var actions = Arrays.stream(args)
                .map(Paths::get)
                .map(path -> {
                        try (var reader = Files.newBufferedReader(path, US_ASCII)) {
                            return parse(reader);
                        } catch (IOException | SexpParserException e) {
                            throw new IllegalStateException(e);
                        }
                    })
                .map(cmds -> toActions(normalize(cmds)))
                .flatMap(cmds -> cmds.stream())
                .collect(toList());

        actions
                .forEach(axn -> axn.execute(robot));
    }

    private static Sexp normalize(Sexp commands) {
        if (commands.isAtomic()) {
            var cmd = commands.toString();
            var len = cmd.length();

            if ((cmd.charAt(0) == '"' && cmd.charAt(len - 1) == '"')
                    || (cmd.charAt(0) == '|' && cmd.charAt(len - 1) == '|')) {
                cmd = cmd.substring(1, len - 1);
                return newAtomicSexp(cmd);
            }

            return commands;
        }

        var nsexp = SexpFactory.newNonAtomicSexp();

        for (var cmd : commands) {
            nsexp.add(normalize(cmd));
        }

        return nsexp;
    }

    private static final Map<String, CommandHandler> COMMANDS = Map.of(
            ":text",       new TextHandler(),
            ":line",       new LineHandler(),
            ":mouseclick", new MouseClickHandler(),
            ":mousemove",  new MouseMoveHandler(),
            ":delay",      new DelayHandler()
            );

    static List<RobotAction> toActions(Sexp sexp) {
        if (sexp.isAtomic()) {
            // throw?
            throw new IllegalStateException("¯\\ ̲(ツ) ̲/¯");
        }

        Sexp cmdEl = car(sexp);
        Sexp cdr = cdr(sexp);

        if (!cmdEl.isAtomic()) {
            throw new IllegalArgumentException("Expected command got " + cmdEl);
        }

        var cmd = cmdEl.toString();
        if (!COMMANDS.containsKey(cmd)) {
            throw new IllegalArgumentException("Expected command got " + cmdEl);
        }

        var actions = COMMANDS.get(cmd)
                .handle(cdr, (s, ignored) -> toActions(s));

        return actions;
    }

    static abstract class RobotAction {
        public abstract void execute(Robot robot);
    }

    static final class DelayAction extends RobotAction {
        private final int delayMs;
        public DelayAction(int delayMs) { this.delayMs = delayMs; }
        @Override public void execute(Robot robot) { robot.delay(delayMs); }
        @Override public String toString() { return "#DelayAction(" + delayMs + ")"; }
    }

    static final class KeyPressAction extends RobotAction {
        private final int keycode;
        public KeyPressAction(int keycode) { this.keycode = keycode; }
        @Override public void execute(Robot robot) { robot.keyPress(keycode); }
        @Override public String toString() { return "#KeyPressAction(" + keycode + ")"; }
    }

    static final class KeyReleaseAction extends RobotAction {
        private final int keycode;
        public KeyReleaseAction(int keycode) { this.keycode = keycode; }
        @Override public void execute(Robot robot) { robot.keyRelease(keycode); }
        @Override public String toString() { return "#KeyRelesAction(" + keycode + ")"; }
    }

    static final class MousePressAction extends RobotAction {
        private final int button;
        public MousePressAction(int button) { this.button = button; }
        @Override public void execute(Robot robot) { robot.mousePress(button); }
    }

    static final class MouseReleaseAction extends RobotAction {
        private final int button;
        public MouseReleaseAction(int button) { this.button = button; }
        @Override public void execute(Robot robot) { robot.mouseRelease(button); }
    }

    static final class MouseMoveAction extends RobotAction {
        private final int x, y;
        public MouseMoveAction(int x, int y) { this.x = x; this.y = y; }
        @Override public void execute(Robot robot) { robot.mouseMove(x, y); }
    }

    /*
     * Handlers
     */
    static interface CommandHandler {
        List<RobotAction> handle(Sexp sexp, CommandHandler parent);
    }

    static class DelayHandler implements CommandHandler {
        @Override @SuppressWarnings("unused")
        public List<RobotAction> handle(Sexp sexp, CommandHandler parent) {
            var delaySexp = car(sexp);
            int delayMs = Integer.parseInt(delaySexp.toString());

            return List.of(new DelayAction(delayMs));
        }
    }

    static class TextHandler implements CommandHandler {
        private static final Map<String, Integer> SPECIALS = Map.ofEntries(
                entry(":ctrl",      VK_CONTROL),
                entry(":alt",       VK_ALT),
                entry(":meta",      VK_META),
                entry(":shift",     VK_SHIFT),
                entry(":left",      VK_LEFT),
                entry(":right",     VK_RIGHT),
                entry(":up",        VK_UP),
                entry(":down",      VK_DOWN),
                entry(":backspace", VK_BACK_SPACE),
                entry(":space",     VK_SPACE),
                entry(":enter",     VK_ENTER),
                entry(":escape",    VK_ESCAPE),
                entry(":f1",        VK_F1),
                entry(":f2",        VK_F2),
                entry(":f3",        VK_F3),
                entry(":f4",        VK_F4),
                entry(":f5",        VK_F5),
                entry(":f6",        VK_F6),
                entry(":f7",        VK_F7),
                entry(":f8",        VK_F8),
                entry(":f9",        VK_F9),
                entry(":f10",       VK_F10),
                entry(":f11",       VK_F11),
                entry(":f12",       VK_F12)
                );

        @Override
        public List<RobotAction> handle(Sexp text, CommandHandler parent) {
            var actions = new ArrayList<RobotAction>(text.getLength());

            if (text.isAtomic()) {
                return toTypeActions(text.toString());
            }

            for (Sexp el : text) {
                if (el.isAtomic()) {
                    var typeActions = toTypeActions(el.toString());

                    actions.addAll(typeActions);
                } else {
                    // two cases
                    // case 1: it contains a subcommand of text (:shift 7) or
                    // else  : it is something all together new (:mousemove 10 20)
                    var subcmdsexp = car(el);
                    var subcmd = subcmdsexp.toString();

                    if (SPECIALS.containsKey(subcmd)) {
                        int keycode = SPECIALS.get(subcmd).intValue();

                        actions.add(new KeyPressAction(keycode));

                        try {
                            var subTextTyping = handle(cdr(el), parent);

                            actions.addAll(subTextTyping);
                        } finally {
                            actions.add(new KeyReleaseAction(keycode));
                        }
                    } else {
                        actions.addAll(parent.handle(el, parent));
                    }
                }
            }

            return actions;
        }

        private List<RobotAction> toTypeActions(String cmd) {
            // two cases
            // case 1: it is a subcommand [eg :left :backspace] or
            // else  : it is string with individual characters to type
            var keys = new ArrayList<int[]>(cmd.length());

            if (SPECIALS.containsKey(cmd)) {
                keys.add(new int[] { SPECIALS.get(cmd).intValue() });
            } else {
                for (int i = 0; i < cmd.length(); ++i) {
                    keys.add(key(cmd.charAt(i)));
                }
            }

            List<RobotAction> keyPresses = keys.stream()
                    .map(this::toRobotAction)
                    .flatMap(List::stream)
                    .collect(toList());

            return keyPresses;
        }

        private int[] key(char c) {
            int shiftKey = SPECIALS.get(":shift").intValue();
            if (c >= 'a' && c <= 'z') {
                return new int[] { VK_A + (Character.toUpperCase(c) - 'A') };
            }
            if (c >= 'A' && c <= 'Z') {
                return new int[] { shiftKey, VK_A + (Character.toUpperCase(c) - 'A') };
            }
            if (c >= '0' && c <= '9') {
                return new int[] { c };
            }
            switch (c) {
            case '\n': return new int[] { VK_ENTER };
            case '\t': return new int[] { VK_TAB };
            case ' ':  return new int[] { VK_SPACE };
            case '-':  return new int[] { VK_MINUS };
            case '_':  return new int[] { shiftKey, VK_MINUS };
            case '=':  return new int[] { VK_EQUALS };
            case '+':  return new int[] { shiftKey, VK_EQUALS };
            case '`':  return new int[] { VK_BACK_QUOTE };
            case '~':  return new int[] { shiftKey, VK_BACK_SLASH };
            case ',':  return new int[] { VK_COMMA };
            case '<':  return new int[] { shiftKey, VK_COMMA };
            case '.':  return new int[] { VK_PERIOD };
            case '>':  return new int[] { shiftKey, VK_PERIOD };
            case '/':  return new int[] { VK_SLASH };
            case '?':  return new int[] { shiftKey, VK_SLASH };
            case ';':  return new int[] { VK_SEMICOLON };
            case ':':  return new int[] { shiftKey, VK_SEMICOLON };
            case '\'': return new int[] { VK_QUOTE };
            case '"':  return new int[] { shiftKey, VK_QUOTE };
            case '[':  return new int[] { VK_OPEN_BRACKET };
            case '{':  return new int[] { shiftKey, VK_OPEN_BRACKET };
            case ']':  return new int[] { VK_CLOSE_BRACKET };
            case '}':  return new int[] { shiftKey, VK_CLOSE_BRACKET };
            case '\\': return new int[] { VK_BACK_SLASH };
            case '|':  return new int[] { shiftKey, VK_BACK_SLASH };

            case '!':  return new int[] { shiftKey, VK_1 };
            case '@':  return new int[] { shiftKey, VK_2 };
            case '#':  return new int[] { shiftKey, VK_3 };
            case '$':  return new int[] { shiftKey, VK_4 };
            case '%':  return new int[] { shiftKey, VK_5 };
            case '^':  return new int[] { shiftKey, VK_6 };
            case '&':  return new int[] { shiftKey, VK_7 };
            case '*':  return new int[] { shiftKey, VK_8 };
            case '(':  return new int[] { shiftKey, VK_9 };
            case ')':  return new int[] { shiftKey, VK_0 };

            default: throw new IllegalArgumentException("cannot handle " + c);
            }
        }

        private List<RobotAction> toRobotAction(int[] keys) {
            if (keys.length == 1) {
                return List.of(
                        new KeyPressAction(keys[0]),
                        new KeyReleaseAction(keys[0]));
            }

            List<RobotAction> presses = IntStream.range(0, keys.length)
                    .map(i -> keys[i])
                    .mapToObj(keycode -> new KeyPressAction(keycode))
                    .collect(toList());

            IntStream.range(0, keys.length)
                    .map(i -> keys[keys.length - i - 1]) // reverse order
                    .mapToObj(keycode -> new KeyReleaseAction(keycode))
                    .forEach(presses::add);

            return presses;
        }
    }

    /**
     * Line is text with a newline following it.
     */
    static final class LineHandler extends TextHandler {
        @Override
        public List<RobotAction> handle(Sexp text, CommandHandler parent) {
            var typing = super.handle(text, parent);
            var newlines = super.handle(newAtomicSexp("\n"), parent);

            var line = new ArrayList<RobotAction>(typing.size() + newlines.size());

            line.addAll(typing);
            line.addAll(newlines);

            return List.copyOf(line);
        }
    }

    static class MouseMoveHandler implements CommandHandler {
        @SuppressWarnings("unused")
        @Override
        public List<RobotAction> handle(Sexp sexp, CommandHandler parent) {
            var xposSexp = car(sexp);
            var yposSexp = car(cdr(sexp));
            int xpos = Integer.parseInt(xposSexp.toString());
            int ypos = Integer.parseInt(yposSexp.toString());

            return List.of(new MouseMoveAction(xpos, ypos));
        }
    }

    static class MouseClickHandler implements CommandHandler {
        private static final Map<String, Integer> MOUSE_EVENT = Map.of(
                ":left", getMaskForButton(BUTTON1),
                ":right", getMaskForButton(BUTTON3),
                ":middle", getMaskForButton(BUTTON2));

        @Override
        public List<RobotAction> handle(Sexp clicks, CommandHandler parent) {
            var actions = new ArrayList<RobotAction>(clicks.getLength());

            for (Sexp el : clicks) {
                // case 1: it is a subcommand [eg :left :middle] or
                // case 2: is it a mouse button?
                // else  : delegate to parent
                if (el.isAtomic()) {
                    var click = el.toString();

                    if (!MOUSE_EVENT.containsKey(click)) {
                        throw new IllegalArgumentException("cannot click " + el);
                    }

                    int button = MOUSE_EVENT.get(click).intValue();
                    actions.add(new MousePressAction(button));
                    actions.add(new MouseReleaseAction(button));
                } else {
                    var subclicksexp = car(el);
                    var subclick = subclicksexp.toString();

                    if (MOUSE_EVENT.containsKey(subclick)) {
                        int button = MOUSE_EVENT.get(subclick).intValue();

                        actions.add(new MousePressAction(button));

                        try {
                            var subClicks = handle(cdr(el), parent);

                            actions.addAll(subClicks);
                        } finally {
                            actions.add(new MouseReleaseAction(button));
                        }
                    } else {
                        actions.addAll(parent.handle(el, parent));
                    }
                }
            }

            return actions;
        }
    }

    /*
     * S-Exp utilities
     */
    private static Sexp car(Sexp sexp) {
        if (sexp.getLength() == 0) {
            return newNonAtomicSexp(); // nil
        }

        return sexp.get(0);
    }

    private static SexpList cdr(Sexp list) {
        Sexp cdr = newNonAtomicSexp();

        Iterator<Sexp> iter = list.iterator();
        @SuppressWarnings("unused")
        Sexp car = iter.next();

        while (iter.hasNext()) {
            cdr.add(iter.next());
        }

        return (SexpList) cdr;
    }
}
