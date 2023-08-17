package com.lvijay.robotonous;

import static java.awt.event.KeyEvent.VK_0;
import static java.awt.event.KeyEvent.VK_ALT;
import static java.awt.event.KeyEvent.VK_BACK_QUOTE;
import static java.awt.event.KeyEvent.VK_BACK_SLASH;
import static java.awt.event.KeyEvent.VK_BACK_SPACE;
import static java.awt.event.KeyEvent.VK_CLOSE_BRACKET;
import static java.awt.event.KeyEvent.VK_COMMA;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_EQUALS;
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
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class Robotonous {
    private static final int ACTION_DELAY = -1;
    private static final int ACTION_PASTE = -2;

    private final Robot robot;
    private final Clipboard clipboard;
    private Long startTime;

    public Robotonous(Robot robot, Clipboard clipboard) {
        this.robot = robot;
        this.clipboard = clipboard;
        this.startTime = null;
    }

    public void type(String s) {
        long stepStart = System.nanoTime();
        startTime = Optional.ofNullable(startTime)
                .orElse(stepStart);
        if (startTime == null) {
            startTime = stepStart;
        }
        System.out.printf("Computing key sequences for '%s'%n", s.strip());
        int[][] sentence = toSentence(s);
        typeSentence(sentence);

        long end = System.nanoTime();
        System.out.printf("Total time=%5dms; step time=%5dms%n",
                TimeUnit.NANOSECONDS.toMillis(end - startTime),
                TimeUnit.NANOSECONDS.toMillis(end - stepStart));
    }

    private int[][] toSentence(String s) {
        List<List<Integer>> sentences = new ArrayList<>();

        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);

            if (c == '«') {
                ++i;
                int start = i;
                List<Integer> seq = new ArrayList<>();
                int end = s.indexOf('»', start);
                for (int j = start; j < end; ++j) {
                    for (int keycode : toKeyEvent(s.charAt(j))) {
                        seq.add(keycode);
                    }
                }
                sentences.add(seq);
                i = end;
            } else if (c == '‹') {
                // insert contents into the system clipboard
                ++i;
                int start = i;
                int end = s.indexOf('›', start);
                List<Integer> seq = new ArrayList<>();
                seq.add(ACTION_PASTE);
                String pasteContents = s.substring(start, end);
                pasteContents.chars()
                        .boxed()
                        .forEach(seq::add);
                sentences.add(seq);
                i = end;
            } else {
                sentences.add(Arrays.stream(toKeyEvent(c))
                        .boxed()
                        .collect(toList()));
            }
        }

        int[][] result = new int[sentences.size()][];
        for (int i = 0; i < sentences.size(); ++i) {
            result[i] = sentences.get(i).stream()
                    .mapToInt(Integer::valueOf)
                    .toArray();
        } // TODO use List.stream to convert this into an int[][] array

        return result;
    }

    private void typeSentence(int[][] sentence) {
        for (int[] chord : sentence) {
            typeChord(chord);
        }
    }

    private void typeChord(int[] chord) {
        if (chord[0] < 0) {
            nonKeyEvent(chord);
            return;
        }

        Arrays.stream(chord)
                .forEach(robot::keyPress);

        IntStream.iterate(chord.length - 1, i -> i >= 0, i -> i - 1)
                .map(i -> chord[i])
                .forEach(robot::keyRelease);
    }

    private int[] toKeyEvent(char c) {
        if (c >= 'a' && c <= 'z') {
            int diff = c - 'a';
            return new int[] { KeyEvent.VK_A + diff };
        }

        if (c >= 'A' && c <= 'Z') {
            var lc = Character.toLowerCase(c);
            return new int[] { VK_SHIFT, toKeyEvent(lc)[0] };
        }

        if (c >= '0' && c <= '9') {
            int diff = c - '0';
            return new int[] { VK_0 + diff };
        }

        if (c >= '①' && c <= '⑳') {
            int diff = c - '①';
            int val = diff + 1;
            return new int[] { ACTION_DELAY, val };
        }

        return switch (c) {
            case 'Λ' -> new int[] { VK_CONTROL };
            case '⌥' -> new int[] { VK_ALT };
            case '⌘' -> new int[] { VK_META };
            case '⇧' -> new int[] { VK_SHIFT };
            case '→' -> new int[] { VK_RIGHT };
            case '←' -> new int[] { VK_LEFT };
            case '⌫' -> new int[] { VK_BACK_SPACE };
            case '\n'-> new int[] { VK_ENTER };
            case '\t'-> new int[] { VK_TAB };
            case ' ' -> new int[] { VK_SPACE };
            case '!' -> new int[] { VK_SHIFT, toKeyEvent('1')[0] };
            case '@' -> new int[] { VK_SHIFT, toKeyEvent('2')[0] };
            case '#' -> new int[] { VK_SHIFT, toKeyEvent('3')[0] };
            case '$' -> new int[] { VK_SHIFT, toKeyEvent('4')[0] };
            case '%' -> new int[] { VK_SHIFT, toKeyEvent('5')[0] };
            case '^' -> new int[] { VK_SHIFT, toKeyEvent('6')[0] };
            case '&' -> new int[] { VK_SHIFT, toKeyEvent('7')[0] };
            case '*' -> new int[] { VK_SHIFT, toKeyEvent('8')[0] };
            case '(' -> new int[] { VK_SHIFT, toKeyEvent('9')[0] };
            case ')' -> new int[] { VK_SHIFT, toKeyEvent('0')[0] };
            case '\''-> new int[] { VK_QUOTE };
            case '"' -> new int[] { VK_SHIFT, VK_QUOTE };
            case ',' -> new int[] { VK_COMMA };
            case ';' -> new int[] { VK_SEMICOLON };
            case ':' -> new int[] { VK_SHIFT, VK_SEMICOLON };
            case '=' -> new int[] { VK_EQUALS };
            case '+' -> new int[] { VK_SHIFT, VK_EQUALS };
            case '.' -> new int[] { VK_PERIOD };
            case '>' -> new int[] { VK_SHIFT, VK_PERIOD };
            case '/' -> new int[] { VK_SLASH };
            case '?' -> new int[] { VK_SHIFT, VK_SLASH };
            case '[' -> new int[] { VK_OPEN_BRACKET };
            case '{' -> new int[] { VK_SHIFT, VK_OPEN_BRACKET };
            case ']' -> new int[] { VK_CLOSE_BRACKET };
            case '}' -> new int[] { VK_SHIFT, VK_CLOSE_BRACKET };
            case '-' -> new int[] { VK_MINUS };
            case '_' -> new int[] { VK_SHIFT, VK_MINUS };
            case '\\'-> new int[] { VK_BACK_SLASH };
            case '|' -> new int[] { VK_SHIFT, VK_BACK_SLASH };
            case '`' -> new int[] { VK_BACK_QUOTE };
            case '~' -> new int[] { VK_SHIFT, VK_BACK_QUOTE };
            default ->
                throw new IllegalArgumentException("Unknown character: " + c);
            };
    }

    private void nonKeyEvent(int[] chord) {
        int command = chord[0];

        switch (command) {
            case ACTION_DELAY -> Arrays.stream(chord, 1, chord.length)
                    .forEach(delay -> robot.delay(delay * 100));
            case ACTION_PASTE -> {
                var contents = new StringBuilder(chord.length - 1);
                Arrays.stream(chord, 1, chord.length)
                        .forEach(kc -> contents.append((char) kc));
                var pasteContents = new StringSelection(contents.toString());
                clipboard.setContents(pasteContents, null);
            }
            default -> throw new IllegalArgumentException("Unknown command " + command);
        }
    }

    public static void main(String[] args) throws Exception {
        var robot = new Robot();
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        for (int i = 0; i < 5; ++i) {
            System.out.println("Sleeping for 1 seconds...");
            robot.delay(1000);
        }

        var r = new Robotonous(robot, clipboard);
        for (var filename : args) {
            var contents = Files.readString(Paths.get(filename), UTF_8);
            r.type(contents);
        }
    }
}
