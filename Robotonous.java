package com.lvijay.robotonous;

import static java.awt.event.KeyEvent.VK_0;
import static java.awt.event.KeyEvent.VK_ALT;
import static java.awt.event.KeyEvent.VK_BACK_QUOTE;
import static java.awt.event.KeyEvent.VK_BACK_SLASH;
import static java.awt.event.KeyEvent.VK_BACK_SPACE;
import static java.awt.event.KeyEvent.VK_CLOSE_BRACKET;
import static java.awt.event.KeyEvent.VK_COMMA;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_DELETE;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_EQUALS;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_META;
import static java.awt.event.KeyEvent.VK_MINUS;
import static java.awt.event.KeyEvent.VK_OPEN_BRACKET;
import static java.awt.event.KeyEvent.VK_PERIOD;
import static java.awt.event.KeyEvent.VK_QUOTE;
import static java.awt.event.KeyEvent.VK_SEMICOLON;
import static java.awt.event.KeyEvent.VK_SHIFT;
import static java.awt.event.KeyEvent.VK_SLASH;
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.awt.event.KeyEvent.VK_TAB;

import java.awt.Dimension;
import java.awt.Robot;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.lvijay.robotonous.print.Formatter;
import com.lvijay.robotonous.speak.AudioClient;
import com.lvijay.robotonous.speak.AudioPlayer;

public class Robotonous {
    private final String commands;
    private final SpecialKeys keys;
    private final Robot robot;
    private final Clipboard clipboard;
    private final List<ActionTypeKeys> pasteAction;
    private final ExecutorService threadpool;
    private final AudioClient audioClient;
    private final Queue<Future<Void>> futures;
    private final List<Action<?>> actions;
    private final PrintStream printTo;

    public Robotonous(
            String commands,
            SpecialKeys keys,
            Robot robot,
            Clipboard clipboard,
            ExecutorService threadpool,
            AudioClient audioClient,
            PrintStream printTo) {
        this.commands = commands;
        this.keys = keys;
        this.robot = robot;
        this.clipboard = clipboard;
        this.pasteAction = toActions(
                keys.keyAction().open()
                + keys.chordPaste()
                + keys.keyAction().close())
                        .stream()
                        .map(v -> (ActionTypeKeys) v)
                        .toList();
        this.threadpool = threadpool;
        this.audioClient = audioClient;
        this.futures = new LinkedList<>();
        this.actions = new ArrayList<>();
        this.printTo = printTo;
    }

    public void init() {
        actions.addAll(toActions(commands));
    }

    public void execute() {
        actions.stream()
                .forEach(Action::perform);
    }

    private List<Action<?>> toActions(String s) {
        List<Action<?>> actions = new ArrayList<>(s.length());
        int speakCount = 0;
        int waitCount = 0;
        boolean mouseLeftActive = false;
        boolean mouseRightActive = false;
        int lastDelay = 0;

        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);

            try {
                if (c == keys.keyAction().open()) {
                    ++i;
                    int start = i;
                    int end = s.indexOf(keys.keyAction().close(), start);

                    int[] chordKeys = s.substring(start, end)
                            .chars()
                            .mapToObj(ch -> toKeyEvent((char) ch))
                            .flatMapToInt(axn -> IntStream.of(axn.arg))
                            .toArray();
                    actions.add(new ActionTypeKeys(chordKeys));

                    i = end;
                } else if (c == keys.keyCopy().open()) { // add contents to system clipboard
                    int start = ++i;
                    int end = s.indexOf(keys.keyCopy().close(), start);
                    String pasteContents = s.substring(start, end);

                    actions.add(new ActionPaste(pasteContents));
                    i = end;
                } else if (c == keys.keyCommentLine()) { // ignore until end of line
                    int end = s.indexOf('\n', i);
                    i = end;
                } else if (c == keys.keySpeak().open()) {
                    ++speakCount;
                    int start = ++i;
                    int end = s.indexOf(keys.keySpeak().close(), start);
                    String speakContent = s.substring(start, end);
                    var player = audioClient.toAudioPlayer(speakContent);

                    if (speakCount - waitCount > 1) {
                        throw new IllegalArgumentException("Not enough waits.");
                    }

                    actions.add(new ActionSpeak(player));
                    i = end;
                } else if (c == keys.keyPrint().open()) {
                    int start = ++i;
                    int end = s.indexOf(keys.keyPrint().close(), start);
                    var printContent = s.substring(start, end);
                    var formatter = Formatter.fromString(printContent);
                    var format = formatter.format();
                    var delay = formatter.delay() == -1 ? lastDelay : formatter.delay();
                    var content = formatter.contents();

                    actions.add(new ActionBashFormatPrint(format, delay, content));
                    lastDelay = delay;
                    i = end;
                } else if (c == keys.keySpeakWait()) {
                    ++waitCount;

                    if (speakCount < waitCount) {
                        throw new IllegalArgumentException("More waits than inits");
                    }

                    actions.add(new ActionSpeakWait());
                } else if (c == keys.mouseLeft().open()) {
                    if (mouseLeftActive) {
                        throw new IllegalArgumentException("Mouse Left already pressed");
                    }
                    mouseLeftActive = true;
                    actions.add(new ActionMouseAction(MouseEvent.BUTTON1, mouseLeftActive));
                } else if (c == keys.mouseLeft().close()) {
                    if (!mouseLeftActive) {
                        throw new IllegalArgumentException("Mouse Left not pressed");
                    }
                    mouseLeftActive = false;
                    actions.add(new ActionMouseAction(MouseEvent.BUTTON1, mouseLeftActive));
                } else if (c == keys.mouseRight().open()) {
                    if (mouseRightActive) {
                        throw new IllegalArgumentException("Mouse Right already pressed");
                    }
                    mouseRightActive = true;
                    actions.add(new ActionMouseAction(MouseEvent.BUTTON3, mouseRightActive));
                } else if (c == keys.mouseRight().close()) {
                    if (!mouseRightActive) {
                        throw new IllegalArgumentException("Mouse Right not pressed");
                    }
                    mouseRightActive = false;
                    actions.add(new ActionMouseAction(MouseEvent.BUTTON3, mouseRightActive));
                } else if (c == keys.mouseMove().open()) {
                    int start = ++i;
                    int end = s.indexOf(keys.mouseMove().close(), start);
                    String dimensions = s.substring(start, end);
                    var xy = dimensions.split(",");
                    int x = Integer.parseInt(xy[0]);
                    int y = Integer.parseInt(xy[1]);
                    if (xy.length != 2) {
                        throw new IllegalArgumentException("unexpected dimensions " + dimensions);
                    }

                    actions.add(new ActionMouseMove(x, y));
                    i = end;
                } else if (c >= '①' && c <= '⑳') { // 9312...9331
                    int diff = c - '①';
                    int val = diff + 1;
                    actions.add(new ActionDelay(val));
                } else if (c >= '㉑' && c <= '㉟') { // 12881...12895
                    int diff = c - '㉑';
                    int val = diff + 21;
                    actions.add(new ActionDelay(val));
                } else if (c >= '㊱' && c <= '㊿') { // 12977...12991
                    int diff = c - '㊱';
                    int val = diff + 36;
                    actions.add(new ActionDelay(val));
                } else {
                    actions.add(toKeyEvent(c));
                }
            } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                System.err.printf("Exception at index %d c=%c%n", i, c);
                throw e;
            } catch (IOException e) {
                System.err.printf("Exception %s at index %d c=%c%n", e, i, c);
                throw new IllegalStateException(e);
            }
        }

        if (mouseLeftActive || mouseRightActive) {
            throw new IllegalStateException("Mouse clicked but not released");
        }

        return actions;
    }

    ActionTypeKeys toKeyEvent(char c) {
        if (c >= 'a' && c <= 'z') {
            int diff = c - 'a';
            return new ActionTypeKeys(KeyEvent.VK_A + diff);
        }

        if (c >= '0' && c <= '9') {
            int diff = c - '0';
            return new ActionTypeKeys(VK_0 + diff);
        }

        if (c >= 'A' && c <= 'Z') {
            var lc = Character.toLowerCase(c);
            var typeEvent = toKeyEvent(lc);
            int[] arguments = typeEvent.arg;
            return new ActionTypeKeys(VK_SHIFT, arguments[0]);
        }

        if (c == keys.keyControl())   { return new ActionTypeKeys(VK_CONTROL);    }
        if (c == keys.keyAlt())       { return new ActionTypeKeys(VK_ALT);        }
        if (c == keys.keyMeta())      { return new ActionTypeKeys(VK_META);       }
        if (c == keys.keyShift())     { return new ActionTypeKeys(VK_SHIFT);      }
        if (c == keys.keyBackspace()) { return new ActionTypeKeys(VK_BACK_SPACE); }
        if (c == keys.keyDelete())    { return new ActionTypeKeys(VK_DELETE);     }
        if (c == keys.keyEscape())    { return new ActionTypeKeys(VK_ESCAPE);     }
        if (c == keys.keyNewline())   { return new ActionTypeKeys(VK_ENTER);      }
        if (c == keys.keyTab())       { return new ActionTypeKeys(VK_TAB);        }

        return switch (c) {
            case '\n'-> new ActionTypeKeys(VK_ENTER);
            case '\t'-> new ActionTypeKeys(VK_TAB);
            case ' ' -> new ActionTypeKeys(VK_SPACE);
            case '!' -> new ActionTypeKeys(VK_SHIFT, toKeyEvent('1').arg[0]);
            case '@' -> new ActionTypeKeys(VK_SHIFT, toKeyEvent('2').arg[0]);
            case '#' -> new ActionTypeKeys(VK_SHIFT, toKeyEvent('3').arg[0]);
            case '$' -> new ActionTypeKeys(VK_SHIFT, toKeyEvent('4').arg[0]);
            case '%' -> new ActionTypeKeys(VK_SHIFT, toKeyEvent('5').arg[0]);
            case '^' -> new ActionTypeKeys(VK_SHIFT, toKeyEvent('6').arg[0]);
            case '&' -> new ActionTypeKeys(VK_SHIFT, toKeyEvent('7').arg[0]);
            case '*' -> new ActionTypeKeys(VK_SHIFT, toKeyEvent('8').arg[0]);
            case '(' -> new ActionTypeKeys(VK_SHIFT, toKeyEvent('9').arg[0]);
            case ')' -> new ActionTypeKeys(VK_SHIFT, toKeyEvent('0').arg[0]);
            case '\''-> new ActionTypeKeys(VK_QUOTE);
            case '"' -> new ActionTypeKeys(VK_SHIFT, VK_QUOTE);
            case ',' -> new ActionTypeKeys(VK_COMMA);
            case '<' -> new ActionTypeKeys(VK_SHIFT, VK_COMMA);
            case ';' -> new ActionTypeKeys(VK_SEMICOLON);
            case ':' -> new ActionTypeKeys(VK_SHIFT, VK_SEMICOLON);
            case '=' -> new ActionTypeKeys(VK_EQUALS);
            case '+' -> new ActionTypeKeys(VK_SHIFT, VK_EQUALS);
            case '.' -> new ActionTypeKeys(VK_PERIOD);
            case '>' -> new ActionTypeKeys(VK_SHIFT, VK_PERIOD);
            case '/' -> new ActionTypeKeys(VK_SLASH);
            case '?' -> new ActionTypeKeys(VK_SHIFT, VK_SLASH);
            case '[' -> new ActionTypeKeys(VK_OPEN_BRACKET);
            case '{' -> new ActionTypeKeys(VK_SHIFT, VK_OPEN_BRACKET);
            case ']' -> new ActionTypeKeys(VK_CLOSE_BRACKET);
            case '}' -> new ActionTypeKeys(VK_SHIFT, VK_CLOSE_BRACKET);
            case '-' -> new ActionTypeKeys(VK_MINUS);
            case '_' -> new ActionTypeKeys(VK_SHIFT, VK_MINUS);
            case '\\'-> new ActionTypeKeys(VK_BACK_SLASH);
            case '|' -> new ActionTypeKeys(VK_SHIFT, VK_BACK_SLASH);
            case '`' -> new ActionTypeKeys(VK_BACK_QUOTE);
            case '~' -> new ActionTypeKeys(VK_SHIFT, VK_BACK_QUOTE);
            default ->
                throw new IllegalArgumentException("Unknown character: " + c);
        };
    }

    // sealed is really overkill here
    private abstract sealed class Action<T>
            permits
                    ActionDelay,
                    ActionTypeKeys,
                    ActionPaste,
                    ActionSpeak,
                    ActionSpeakWait,
                    ActionBashFormatPrint,
                    ActionMouseAction,
                    ActionMouseMove
    {
        public final T arg;
        public Action(T arg) { this.arg = arg; }
        public abstract void perform();
        @Override
        public String toString() {
            return String.format("<%s %s>", getClass().getSimpleName(), arg);
        }
    }

    private final class ActionDelay extends Action<Integer> {
        public ActionDelay(int arg) { super(Integer.valueOf(arg * 100)); } // to millis
        @Override
        public void perform() { robot.delay(arg.intValue()); }
        @Override
        public String toString() { return String.format("<Delay %dms>", arg); }
    }

    private final class ActionTypeKeys extends Action<int[]> {
        public ActionTypeKeys(int... keyEvents) { super(keyEvents); }

        @Override
        public void perform() {
            int[] keyEvents = arg;
            IntStream.of(keyEvents)
                    .forEach(ke -> robot.keyPress(ke));

            IntStream.iterate(keyEvents.length - 1, i -> i >= 0, i -> i - 1)
                    .map(i -> keyEvents[i])
                    .forEach(ke -> robot.keyRelease(ke));
        }

        @Override
        public String toString() {
            String typedChars = Arrays.stream(arg)
                    .mapToObj(i -> switch (i) {
                            case VK_SHIFT -> "SHIFT";
                            case VK_CONTROL -> "CTRL";
                            case VK_ALT -> "ALT";
                            case VK_META -> "META";
                            default -> {
                                var name = Character.getName((char) i);
                                if (name.startsWith("LATIN CAPITAL LETTER ")) {
                                    yield ("" + name.charAt(name.length() - 1)).toLowerCase();
                                }
                                yield name;
                            }
                    })
                    .collect(Collectors.joining(" "));
            return String.format("<Type %s>", typedChars);
        }
    }

    private final class ActionPaste extends Action<String> {
        public ActionPaste(String arg) {
            super(arg);
        }

        @Override
        public void perform() {
            var pasteContents = new StringSelection(arg);
            clipboard.setContents(pasteContents, null);
            pasteAction.forEach(Action::perform);
        }

        @Override
        public String toString() {
            return String.format("<Paste %s>", arg);
        }
    }

    private final class ActionSpeak extends Action<AudioPlayer> {
        private ActionSpeak(AudioPlayer player) {
            super(player);
        }

        @Override
        public void perform() {
            var future = threadpool.<Void>submit(this.arg::play, null);
            futures.add(future);
        }
    }

    private final class ActionSpeakWait extends Action<Void> {
        public ActionSpeakWait() { super((Void) null); }

        @Override
        public void perform() {
            try {
                var future = futures.remove();
                future.get(); // wait indefinitely for speaking to end.
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final class ActionBashFormatPrint extends Action<Void> {
        private final String format;
        private final int delay;
        private final String contents;

        private ActionBashFormatPrint(String format, int delay, String contents) {
            super((Void) null);

            this.format = format;
            this.delay = delay;
            this.contents = contents;
        }
        private String prefix() {
            return "\033[" + format + "m";
        }

        private String reset() {
            return "\033[0m";
        }

        @Override
        public void perform() {
            printTo.print(prefix());
            for (char c : contents.toCharArray()) {
                printTo.print(c);
                robot.delay(delay);
            }
            printTo.print(reset());
        }
    }

    private final class ActionMouseAction extends Action<Integer> {
        private final Runnable action;

        private ActionMouseAction(int value, boolean press) {
            super(InputEvent.getMaskForButton(value));

            this.action = press ?
                () -> robot.mousePress(arg.intValue())
                : () -> robot.mouseRelease(arg.intValue());
        }

        @Override
        public void perform() {
            action.run();
        }
    }

    private final class ActionMouseMove extends Action<Dimension> {
        public ActionMouseMove(int x, int y) {
            super(new Dimension(x, y));
        }

        @Override
        public void perform() {
            robot.mouseMove(arg.width, arg.height);
        }
    }
}
