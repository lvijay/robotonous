package robotonous;

import static de.tudresden.inf.lat.jsexp.SexpFactory.newAtomicSexp;
import static de.tudresden.inf.lat.jsexp.SexpFactory.newNonAtomicSexp;
import static java.awt.event.InputEvent.getMaskForButton;
import static java.awt.event.KeyEvent.*;
import static java.awt.event.MouseEvent.BUTTON1;
import static java.awt.event.MouseEvent.BUTTON2;
import static java.awt.event.MouseEvent.BUTTON3;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Map.entry;
import static java.util.stream.Collectors.toList;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
        final Reader reader;
        final boolean useRepl;
        if (args.length > 0) {
            try {
                String filename = args[0];
                reader = Files.newBufferedReader(Paths.get(filename), US_ASCII);
                useRepl = false;
            } catch (MalformedInputException e) {
                throw new IllegalArgumentException("Only ASCII encoded files supported.");
            }
        } else {
            reader = new BufferedReader(new InputStreamReader(System.in));
            useRepl = true;
        }

        var robot = new java.awt.Robot();
        robot.setAutoDelay(80);

        try (reader) {
            if (useRepl) { System.out.println("Type ^D to quit."); }
            while (true) {
                if (useRepl) { System.out.print("command>>> "); }
                try {
                    var commands = SexpFactory.parse(reader);
                    var action = toActions(normalize(commands));

                    action
                            .forEach(axn -> axn.execute(robot));
                } catch (IllegalStateException | IllegalArgumentException e) {
                    if (useRepl) {
                        System.err.println(e.getMessage());
                        continue;
                    }
                    throw e;
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        } catch (SexpParserException e) {
            if (e.getMessage().equals("Empty expression cannot be parsed.")) {
                return;
            }
            throw e;
        }
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
            ":type",       new TextHandler(),
            ":typeline",   new LineHandler(),
            ":mouseclick", new MouseClickHandler(),
            ":mousemove",  new MouseMoveHandler(),
            ":delay",      new DelayHandler()
            );

    static List<RobotAction> toActions(Sexp sexp) {
        if (sexp.isAtomic()) {
            // throw?
            throw new IllegalStateException("could not parse " + sexp);
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
    }

    static final class KeyPressAction extends RobotAction {
        private final int keycode;
        public KeyPressAction(int keycode) { this.keycode = keycode; }
        @Override public void execute(Robot robot) { robot.keyPress(keycode); }
    }

    static final class KeyReleaseAction extends RobotAction {
        private final int keycode;
        public KeyReleaseAction(int keycode) { this.keycode = keycode; }
        @Override public void execute(Robot robot) { robot.keyRelease(keycode); }
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

    private static class DelayHandler implements CommandHandler {
        @Override @SuppressWarnings("unused")
        public List<RobotAction> handle(Sexp sexp, CommandHandler parent) {
            var delaySexp = car(sexp);
            int delayMs = Integer.parseInt(delaySexp.toString());

            return List.of(new DelayAction(delayMs));
        }
    }

    private static class TextHandler implements CommandHandler {
        private static final Map<String, int[]> SPECIALS = Map.ofEntries(
                entry(":ctrl",        new int[] { VK_CONTROL              }),
                entry(":alt",         new int[] { VK_ALT                  }),
                entry(":meta",        new int[] { VK_META                 }),
                entry(":shift",       new int[] { VK_SHIFT                }),
                entry(":left",        new int[] { VK_LEFT                 }),
                entry(":right",       new int[] { VK_RIGHT                }),
                entry(":up",          new int[] { VK_UP                   }),
                entry(":down",        new int[] { VK_DOWN                 }),
                entry(":backspace",   new int[] { VK_BACK_SPACE           }),
                entry(":space",       new int[] { VK_SPACE                }),
                entry(":enter",       new int[] { VK_ENTER                }),
                entry(":home",        new int[] { VK_HOME                 }),
                entry(":end",         new int[] { VK_END                  }),
                entry(":pageup",      new int[] { VK_PAGE_UP              }),
                entry(":pagedown",    new int[] { VK_PAGE_DOWN            }),
                entry(":delete",      new int[] { VK_DELETE               }),
                entry(":doublequote", new int[] { VK_SHIFT, VK_QUOTE      }),
                entry(":pipe",        new int[] { VK_SHIFT, VK_BACK_SLASH })
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
                    // else  : it is something new all together (:mousemove 10 20)
                    var subcmdsexp = car(el);
                    var subcmd = subcmdsexp.toString();

                    if (SPECIALS.containsKey(subcmd)) {
                        int[] keycodes = SPECIALS.get(subcmd);

                        Arrays.stream(keycodes)
                                .mapToObj(KeyPressAction::new)
                                .forEach(actions::add);

                        try {
                            var subTextTyping = handle(cdr(el), parent);

                            actions.addAll(subTextTyping);
                        } finally {
                            IntStream.rangeClosed(keycodes.length - 1, 0)
                                    .map(i -> keycodes[i])
                                    .mapToObj(KeyReleaseAction::new)
                                    .forEach(actions::add);
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
                keys.add(SPECIALS.get(cmd));
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
            int shiftKey = SPECIALS.get(":shift")[0];
            if (c >= 'a' && c <= 'z') {
                return new int[] { VK_A + (Character.toUpperCase(c) - 'A') };
            }
            if (c >= 'A' && c <= 'Z') {
                return new int[] { shiftKey, VK_A + (Character.toUpperCase(c) - 'A') };
            }
            IntUnaryOperator todigit = i -> '0' + (i - '0');
            if (c >= '0' && c <= '9') {
                return new int[] { todigit.applyAsInt(c) };
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
            case '~':  return new int[] { shiftKey, VK_BACK_QUOTE };
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

            case '!':  return new int[] { shiftKey, todigit.applyAsInt('1') };
            case '@':  return new int[] { shiftKey, todigit.applyAsInt('2') };
            case '#':  return new int[] { shiftKey, todigit.applyAsInt('3') };
            case '$':  return new int[] { shiftKey, todigit.applyAsInt('4') };
            case '%':  return new int[] { shiftKey, todigit.applyAsInt('5') };
            case '^':  return new int[] { shiftKey, todigit.applyAsInt('6') };
            case '&':  return new int[] { shiftKey, todigit.applyAsInt('7') };
            case '*':  return new int[] { shiftKey, todigit.applyAsInt('8') };
            case '(':  return new int[] { shiftKey, todigit.applyAsInt('9') };
            case ')':  return new int[] { shiftKey, todigit.applyAsInt('0') };

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
                    .mapToObj(KeyPressAction::new)
                    .collect(toList());

            IntStream.range(0, keys.length)
                    .map(i -> keys[keys.length - i - 1]) // reverse order
                    .mapToObj(KeyReleaseAction::new)
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

    private static class MouseMoveHandler implements CommandHandler {
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

    private static class MouseClickHandler implements CommandHandler {
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
            return sexp; // (car nil) => nil
        }

        return sexp.get(0);
    }

    private static SexpList cdr(Sexp list) {
        SexpList cdr = (SexpList) newNonAtomicSexp();

        boolean first = true;
        for (Sexp el : list) {
            if (!first) { cdr.add(el); }
            else        { first = false; }
        }

        return cdr;
    }
}
