import static java.awt.event.KeyEvent.VK_0;
import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_ALT;
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
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Robotonous2 {
    private final Robot robot;
    private final Random random;
    private long startTime;

    public Robotonous2(Robot robot, Random random) {
        this.robot = robot;
        this.random = random;
        this.startTime = -1;
    }

    public static void main(String[] args) throws Exception {
        Robot robot = new Robot();
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        Point location = pointerInfo.getLocation();

        System.out.println(location);

        Random rand = new Random(100);

        for (int i = 0; i < 5; ++i) {
                System.out.println("Sleeping for 1 seconds...");
                robot.delay(1000);
        }

        var r = new Robotonous2(robot, rand);
        Files.lines(Paths.get("actions.txt"), UTF_8)
                .peek(System.out::println)
                .map(line -> line + "\n")
                .forEach(r::type);
    }

    public void type(String s) {
        long stepStart = System.nanoTime();
        if (startTime < 0) {
            startTime = stepStart;
        }
        System.out.printf("Computing key sequences for '%s'%n", s.strip());
        int[][] sentence = toSentence(s);
        typeSentence(sentence);
        robot.delay(90 + random.nextInt(30));

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
        }

        return result;
    }

    private void typeSentence(int[][] sentence) {
        for (int[] chord : sentence) {
            typeChord(chord);
        }
    }

    private void typeChord(int[] chord) {


        for (int i = 0; i < chord.length; ++i) {
            int key = chord[i];

            if (key < 0) { robot.delay(Math.abs(key)); }
            else         { robot.keyPress(chord[i]);   }

            robot.delay(10 + random.nextInt(30));
        }
        for (int i = chord.length - 1; i >= 0; --i) {
            if (chord[i] > 0) { robot.keyRelease(chord[i]); }
            robot.delay(10 + random.nextInt(30));
        }
    }

    private int[] toKeyEvent(char c) {
            if (c >= 'A' && c <= 'Z') {
                var lc = Character.toLowerCase(c);
                return new int[] { toKeyEvent('ς')[0], toKeyEvent(lc)[0] };
            }

            if (c >= '0' && c <= '9') {
                int diff = c - '0';
                return new int[] { VK_0 + diff };
            }

            if (c >= 'a' && c <= 'z') {
                int diff = c - 'a';
                return new int[] { VK_A + diff };
            }

            switch (c) {
            case 'Λ': return new int[] { VK_CONTROL };
            case '⌥': return new int[] { VK_ALT };
            case '⌘': return new int[] { VK_META };
            case 'ς': return new int[] { VK_SHIFT };
            case '→': return new int[] { VK_RIGHT };
            case '←': return new int[] { VK_LEFT };
            case '‹': return new int[] { VK_BACK_SPACE };
            case '\n': return new int[] { VK_ENTER };
            case ' ': return new int[] { VK_SPACE };
            case '!': return new int[] { toKeyEvent('ς')[0], toKeyEvent('1')[0] };
            case '@': return new int[] { toKeyEvent('ς')[0], toKeyEvent('2')[0] };
            case '#': return new int[] { toKeyEvent('ς')[0], toKeyEvent('3')[0] };
            case '$': return new int[] { toKeyEvent('ς')[0], toKeyEvent('4')[0] };
            case '%': return new int[] { toKeyEvent('ς')[0], toKeyEvent('5')[0] };
            case '^': return new int[] { toKeyEvent('ς')[0], toKeyEvent('6')[0] };
            case '&': return new int[] { toKeyEvent('ς')[0], toKeyEvent('7')[0] };
            case '*': return new int[] { toKeyEvent('ς')[0], toKeyEvent('8')[0] };
            case '(': return new int[] { toKeyEvent('ς')[0], toKeyEvent('9')[0] };
            case ')': return new int[] { toKeyEvent('ς')[0], toKeyEvent('0')[0] };
            case '+': return new int[] { toKeyEvent('ς')[0], toKeyEvent('=')[0] };
            case '\'': return new int[] { VK_QUOTE };
            case ',': return new int[] { VK_COMMA };
            case '-': return new int[] { VK_MINUS };
            case '.': return new int[] { VK_PERIOD };
            case '/': return new int[] { VK_SLASH };
            case ';': return new int[] { VK_SEMICOLON };
            case ':': return new int[] { toKeyEvent('ς')[0], toKeyEvent(';')[0] };
            case '=': return new int[] { VK_EQUALS };
            case '>': return new int[] { toKeyEvent('ς')[0], toKeyEvent('.')[0] };
            case '?': return new int[] { toKeyEvent('ς')[0], toKeyEvent('/')[0] };
            case '[': return new int[] { VK_OPEN_BRACKET };
            case ']': return new int[] { VK_CLOSE_BRACKET };
            case '_': return new int[] { toKeyEvent('ς')[0], toKeyEvent('-')[0] };
            case '|': return new int[] { toKeyEvent('ς')[0], toKeyEvent('\\')[0] };
            case '①': return new int[] { -10 };
            case '②': return new int[] { -20 };
            case '③': return new int[] { -30 };
            case '④': return new int[] { -40 };
            case '⑤': return new int[] { -50 };
            case '⑥': return new int[] { -60 };
            case '⑦': return new int[] { -70 };
            case '⑧': return new int[] { -80 };
            case '⑨': return new int[] { -90 };
            default:
                throw new IllegalArgumentException("Unknown character: " + c);
        }
    }
}
