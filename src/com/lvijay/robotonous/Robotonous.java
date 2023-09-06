package com.lvijay.robotonous;

import java.awt.Robot;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Robotonous {
    private final SpecialKeys keys;
    private final KeyEventSequencer sequencer;
    private final Robot robot;
    private final Clipboard clipboard;
    private final List<Action> pasteAction;

    public Robotonous(
            SpecialKeys keys,
            KeyEventSequencer sequencer,
            Robot robot,
            Clipboard clipboard) {
        this.keys = keys;
        this.sequencer = sequencer;
        this.robot = robot;
        this.clipboard = clipboard;
        this.pasteAction = toActions(
                keys.keyAction()
                + keys.pasteChord()
                + keys.keyAction());
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
                    ++i;
                    int start = i;
                    int end = s.indexOf(keys.keyCopy(), start);
                    String pasteContents = s.substring(start, end);
                    int[] pastechars = pasteContents.chars()
                            .toArray();
                    actions.add(new Action(Event.PASTE, pastechars));
                    i = end;
                } else if (c == keys.keyCommentLine()) { // ignore until end of line
                    int end = s.indexOf('\n', i);
                    i = end;
                } else {
                    actions.add(sequencer.toKeyEvent(c));
                }
            } catch (IllegalArgumentException e) {
                System.err.printf("Exception at index %d%n", i);
                throw e;
            }
        }

        return actions;
    }

    public void execute(List<Action> actions) {
        for (Action action : actions) {
            System.out.println("Executing " + action);
            switch (action.event()) {
                case DELAY -> delay(action.arguments());
                case PASTE -> copyPaste(action.arguments());
                case TYPE -> chord(action.arguments());
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

    void copyPaste(int[] chord) {
        var contents = new StringBuilder(chord.length - 1);
        Arrays.stream(chord, 0, chord.length)
                .forEach(kc -> contents.append((char) kc));
        var pasteContents = new StringSelection(contents.toString());
        clipboard.setContents(pasteContents, null);
        execute(pasteAction);
    }
}
