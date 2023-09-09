package com.lvijay.robotonous;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.awt.Robot;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import com.lvijay.robotonous.asides.Alongside;

public class Robotonous {
    private final SpecialKeys keys;
    private final KeyEventSequencer sequencer;
    private final Robot robot;
    private final Clipboard clipboard;
    private final List<Action> pasteAction;
    private final ExecutorService threadpool;
    private final Map<String, Alongside> sides;
    private final Queue<Future<Void>> futures;

    public Robotonous(
            SpecialKeys keys,
            KeyEventSequencer sequencer,
            Robot robot,
            Clipboard clipboard,
            ExecutorService threadpool,
            Map<String, Alongside> sides) {
        this.keys = keys;
        this.sequencer = sequencer;
        this.robot = robot;
        this.clipboard = clipboard;
        this.pasteAction = toActions(
                keys.keyAction()
                + keys.pasteChord()
                + keys.keyAction());
        this.threadpool = threadpool;
        this.sides = Map.copyOf(sides);
        this.futures = new LinkedList<>();
    }

    public List<Action> toActions(String s) {
        List<Action> actions = new ArrayList<>(s.length());

        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);

            try {
                if (c == keys.keyAction()) {
                    ++i;
                    int start = i;
                    int end = s.indexOf(keys.keyAction(), start);

                    int[] chordKeys = s.substring(start, end)
                            .chars()
                            .mapToObj(ch -> sequencer.toKeyEvent((char) ch))
                            .flatMapToInt(axn -> IntStream.of(axn.arguments()))
                            .toArray();
                    actions.add(new Action(chordKeys));

                    i = end;
                } else if (c == keys.keyCopy()) { // add contents to system clipboard
                    int start = ++i;
                    int end = s.indexOf(keys.keyCopy(), start);
                    String pasteContents = s.substring(start, end);
                    int[] pastechars = pasteContents.chars()
                            .toArray();
                    actions.add(new Action(Event.PASTE, pastechars));
                    i = end;
                } else if (c == keys.keyCommentLine()) { // ignore until end of line
                    int end = s.indexOf('\n', i);
                    i = end;
                } else if (c == keys.keyAsideInit()) {
                    int start = ++i;
                    int end = s.indexOf(keys.keyAsideInit(), start);
                    String commandString = s.substring(start, end);
                    int[] command = commandString.chars().toArray();

                    actions.add(new Action(Event.ASIDE_INIT, command));
                    i = end;
                } else if (c == keys.keyAsideWait()) {
                    long initCounts = actions.stream()
                            .filter(a -> a.event() == Event.ASIDE_INIT)
                            .count();
                    long waitCounts = actions.stream()
                            .filter(a -> a.event() == Event.ASIDE_WAIT)
                            .count();

                    if (initCounts <= waitCounts) {
                        throw new IllegalArgumentException("More waits than inits");
                    }

                    actions.add(new Action(Event.ASIDE_WAIT));
                } else {
                    actions.add(sequencer.toKeyEvent(c));
                }
            } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                System.err.printf("Exception at index %d c=%c%n", i, c);
                throw e;
            }
        }

        return actions;
    }

    public void execute(List<Action> actions) throws Exception {
        for (Action action : actions) {
            System.out.println("Executing " + action);
            switch (action.event()) {
                case DELAY -> delay(action.arguments());
                case PASTE -> copyPaste(action.arguments());
                case TYPE -> chord(action.arguments());
                case ASIDE_INIT -> asideInit(action.arguments());
                case ASIDE_WAIT -> asideWait();
            };
        }
    }

    void chord(int[] keyEvents) {
        IntStream.of(keyEvents)
                .forEach(ke -> robot.keyPress(ke));

        IntStream.iterate(keyEvents.length - 1, i -> i >= 0, i -> i - 1)
                .map(i -> keyEvents[i])
                .forEach(ke -> robot.keyRelease(ke));
    }

    void delay(int[] delays) {
        IntStream.of(delays)
                .map(d -> d * 100) // to milliseconds
                .forEach(robot::delay);
    }

    void copyPaste(int[] chord) throws Exception {
        var contents = new StringBuilder(chord.length - 1);
        Arrays.stream(chord, 0, chord.length)
                .forEach(kc -> contents.append((char) kc));
        var pasteContents = new StringSelection(contents.toString());
        clipboard.setContents(pasteContents, null);
        execute(pasteAction);
    }

    void asideInit(int[] arguments) {
        byte[] stringData = new byte[arguments.length];
        IntStream.range(0, arguments.length)
                .forEach(i -> stringData[i] = (byte) arguments[i]);
        var str = new String(stringData, UTF_8);
        int arg0end = str.indexOf(' ');
        var asideName = str.substring(0, arg0end);
        var asideArgs = str.substring(arg0end + 1);
        var alongside = sides.get(asideName);

        submit(alongside, asideArgs);
    }

    void asideWait() throws Exception {
        Future<Void> head = futures.remove();

        head.get(); // indefinite wait
    }

    private void submit(Alongside alongside, String args) {
        Future<Void> future = threadpool.submit(() -> {
            try {
                alongside.execute(args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, (Void) null);

        futures.add(future);
    }
}
