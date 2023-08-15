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
    private final Robot robot;
    private Long startTime;

    public Robotonous(Robot robot) {
        this.robot = robot;
        this.startTime = null;
    }

    public static void main(String[] args) throws Exception {
        var robot = new Robot();

        for (int i = 0; i < 5; ++i) {
                System.out.println("Sleeping for 1 seconds...");
                robot.delay(1000);
        }

        var r = new Robotonous(robot);

        for (String arg : args) {
            Files.lines(Paths.get(arg), UTF_8)
                    .map(line -> line + "\n")
                    .forEach(r::type);
        }
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

            if (c != '«') {
                sentences.add(Arrays.stream(toKeyEvent(c))
                        .boxed()
                        .collect(toList()));
            } else {
                ++i;
                int start = i;
                List<Integer> seq = new ArrayList<>();
                int end = s.indexOf('»', start);
                for (int j = start; j < end; ++j) {
                    seq.addAll(Arrays.stream(toKeyEvent(s.charAt(j)))
                            .boxed()
                            .collect(toList()));
                }
                sentences.add(seq);
                i = end;
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
        Arrays.stream(chord)
                .forEach(key -> {
                    if (key < 0) {
                        robot.delay(Math.abs(key));
                    } else {
                        robot.keyPress(key);
                    }
                });

        IntStream.iterate(chord.length - 1, i -> i >= 0, i -> i - 1)
                .map(i -> chord[i])
                .filter(key -> key > 0)
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

        switch (c) {
        case 'Λ':  return new int[] { VK_CONTROL };
        case '⌥':  return new int[] { VK_ALT };
        case '⌘':  return new int[] { VK_META };
        case '⇧':  return new int[] { VK_SHIFT };
        case '→':  return new int[] { VK_RIGHT };
        case '←':  return new int[] { VK_LEFT };
        case '⌫':  return new int[] { VK_BACK_SPACE };
        case '\n': return new int[] { VK_ENTER };
        case '\t': return new int[] { VK_TAB };
        case ' ':  return new int[] { VK_SPACE };
        case '!':  return new int[] { VK_SHIFT, toKeyEvent('1')[0] };
        case '@':  return new int[] { VK_SHIFT, toKeyEvent('2')[0] };
        case '#':  return new int[] { VK_SHIFT, toKeyEvent('3')[0] };
        case '$':  return new int[] { VK_SHIFT, toKeyEvent('4')[0] };
        case '%':  return new int[] { VK_SHIFT, toKeyEvent('5')[0] };
        case '^':  return new int[] { VK_SHIFT, toKeyEvent('6')[0] };
        case '&':  return new int[] { VK_SHIFT, toKeyEvent('7')[0] };
        case '*':  return new int[] { VK_SHIFT, toKeyEvent('8')[0] };
        case '(':  return new int[] { VK_SHIFT, toKeyEvent('9')[0] };
        case ')':  return new int[] { VK_SHIFT, toKeyEvent('0')[0] };
        case '\'': return new int[] { VK_QUOTE };
        case '"':  return new int[] { VK_SHIFT, VK_QUOTE };
        case ',':  return new int[] { VK_COMMA };
        case ';':  return new int[] { VK_SEMICOLON };
        case ':':  return new int[] { VK_SHIFT, VK_SEMICOLON };
        case '=':  return new int[] { VK_EQUALS };
        case '+':  return new int[] { VK_SHIFT, VK_EQUALS };
        case '.':  return new int[] { VK_PERIOD };
        case '>':  return new int[] { VK_SHIFT, VK_PERIOD };
        case '/':  return new int[] { VK_SLASH };
        case '?':  return new int[] { VK_SHIFT, VK_SLASH };
        case '[':  return new int[] { VK_OPEN_BRACKET };
        case '{':  return new int[] { VK_SHIFT, VK_OPEN_BRACKET };
        case ']':  return new int[] { VK_CLOSE_BRACKET };
        case '}':  return new int[] { VK_SHIFT, VK_CLOSE_BRACKET };
        case '-':  return new int[] { VK_MINUS };
        case '_':  return new int[] { VK_SHIFT, VK_MINUS };
        case '\\': return new int[] { VK_BACK_SLASH };
        case '|':  return new int[] { VK_SHIFT, VK_BACK_SLASH };
        case '`':  return new int[] { VK_BACK_QUOTE };
        case '~':  return new int[] { VK_SHIFT, VK_BACK_QUOTE };
        case '①': return new int[] { -100 };
        case '②': return new int[] { -200 };
        case '③': return new int[] { -300 };
        case '④': return new int[] { -400 };
        case '⑤': return new int[] { -500 };
        case '⑥': return new int[] { -600 };
        case '⑦': return new int[] { -700 };
        case '⑧': return new int[] { -800 };
        case '⑨': return new int[] { -900 };
        default:
            throw new IllegalArgumentException("Unknown character: " + c);
        }
    }
}
