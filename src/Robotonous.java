import static java.awt.event.InputEvent.BUTTON1_DOWN_MASK;
import static java.awt.event.KeyEvent.VK_0;
import static java.awt.event.KeyEvent.VK_1;
import static java.awt.event.KeyEvent.VK_2;
import static java.awt.event.KeyEvent.VK_3;
import static java.awt.event.KeyEvent.VK_4;
import static java.awt.event.KeyEvent.VK_5;
import static java.awt.event.KeyEvent.VK_6;
import static java.awt.event.KeyEvent.VK_7;
import static java.awt.event.KeyEvent.VK_8;
import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_ALT;
import static java.awt.event.KeyEvent.VK_B;
import static java.awt.event.KeyEvent.VK_BACK_SPACE;
import static java.awt.event.KeyEvent.VK_C;
import static java.awt.event.KeyEvent.VK_CLOSE_BRACKET;
import static java.awt.event.KeyEvent.VK_COMMA;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_D;
import static java.awt.event.KeyEvent.VK_E;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_EQUALS;
import static java.awt.event.KeyEvent.VK_F;
import static java.awt.event.KeyEvent.VK_G;
import static java.awt.event.KeyEvent.VK_H;
import static java.awt.event.KeyEvent.VK_I;
import static java.awt.event.KeyEvent.VK_K;
import static java.awt.event.KeyEvent.VK_L;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_M;
import static java.awt.event.KeyEvent.VK_META;
import static java.awt.event.KeyEvent.VK_MINUS;
import static java.awt.event.KeyEvent.VK_N;
import static java.awt.event.KeyEvent.VK_O;
import static java.awt.event.KeyEvent.VK_OPEN_BRACKET;
import static java.awt.event.KeyEvent.VK_P;
import static java.awt.event.KeyEvent.VK_PERIOD;
import static java.awt.event.KeyEvent.VK_QUOTE;
import static java.awt.event.KeyEvent.VK_R;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_S;
import static java.awt.event.KeyEvent.VK_SEMICOLON;
import static java.awt.event.KeyEvent.VK_SHIFT;
import static java.awt.event.KeyEvent.VK_SLASH;
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.awt.event.KeyEvent.VK_T;
import static java.awt.event.KeyEvent.VK_U;
import static java.awt.event.KeyEvent.VK_V;
import static java.awt.event.KeyEvent.VK_W;
import static java.awt.event.KeyEvent.VK_X;
import static java.awt.event.KeyEvent.VK_Y;
import static java.util.stream.Collectors.toList;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Robotonous {
    private static final Point initCommitMousePointStart = new Point(770, 608);
    private static final Point initCommitMousePointEndng = new Point(856, 607);
    private static final Point predictionStart = new Point(621-92, 715-13);
    private static final Point predictionEndng = new Point(767-92, 718-13);
    private static final Point commitStart = new Point(779-92, 667-13);
    private static final Point commitEndng = new Point(694-92, 668-13);

    private final Robot robot;
    private final Random random;
    private long startTime;

    public Robotonous(Robot robot, Random random) {
        this.robot = robot;
        this.random = random;
        this.startTime = -1;
    }

    public static void main(String[] args) throws Exception {
        Robot robot = new Robot();
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        Point location = pointerInfo.getLocation();

        System.out.println(location);

//        robot.mousePress(BUTTON1_DOWN_MASK);
//        robot.mouseMove(initCommitMousePointEndng.x, initCommitMousePointEndng.y);
//        robot.mouseRelease(BUTTON1_DOWN_MASK);

        Random rand = new Random(100);
        Robotonous r = new Robotonous(robot, rand);
//
//        r.type("{ςh}e{ςll}o{⌘←}, world\n");

//        robot.delay(10000);

//        r.type("# !#%'()*+,-./012345678\n");
//        r.type("#                  end.\n");
//        r.type("#;=>?[]_abcdefghiklmnoprs\n");
//        r.type("#                    end.\n");
//        r.type("#tuvwxy|\n");
//        r.type("#   end.\n");

        r.type("rm -rf .git* *\n");
        r.type("{Λl}"); robot.delay(5000);
        r.type("## hello!\n");
        r.type("## welcome to a git magic trick\n");
        r.type("## it's like a card trick⑤ but in git\n");
        r.type("## i'll predict teh①{Λt} commit id\n");
        r.type("## before a git commti②{Λb}{Λt}{Λe}⑤\n");
        r.type("## i don't know if it'll② work!\n");
        r.type("## let's begin①...\n");
        r.type("## here is an empty folder③\n");
        r.type("ls -a\n");
        r.type("## let's create a new git rpe②{Λt}o\n");
        r.type("git init .\n");
        r.type("## and a first commit\n");
        r.type("echo '## ignored files' > .git⑧ignore\n");
        r.type("git add -v {⌥.}\n");
        r.type("git commit -m 'initial commit'\n");
        robot.delay(5050);

        robot.mouseMove(initCommitMousePointStart.x, initCommitMousePointStart.y);
        robot.delay(500);

        robot.mousePress(BUTTON1_DOWN_MASK);
        robot.mouseMove(initCommitMousePointEndng.x, initCommitMousePointEndng.y);
        robot.mouseRelease(BUTTON1_DOWN_MASK);
        r.type("{⌘c}①{⌘c}⑤\n");

        r.type("## let's note this commit {⌘v}\n");
        r.type("## because according to the ⑧article i'm④ following\n");
        r.type("## it is crucial for what follows\n");
        r.type("echo 'magic {⌘v}' > file ④⑤{‹}_{⌘v}\n");
        r.type("git add -v {⌥.}\n");
        r.type("## now we run the formula and get the predict⑧{⌥b}④{Λk}⑧\n## {Λy}⑧ed commit id\n");
        r.type("python3 ④-c ⑤'from math import pi,e;⑤num={⌘v}④{⌥b}0x{⌥f};const=15");
        r.type(".540①2①8①7①22①8①00①611①8\n");
        r.type("fax=sum①()①{Λb}①①[]①{Λb}①①i ①for i in range①(1,num①//2①+1①) if num①%①i①==0①\n");
        r.type("residue=num/fax\n");
        r.type("print()①{Λb}①hex()①{Λb}①int(){Λb}①pi**residue{⌥b}②{⌥b}①({⌥f}{⌥f})②**e②)②{⌥b}{⌥b}①{⌥b}({⌥f}①{⌥f}①{⌥f}①{Λf}**const④{Λe}④'\n");

        robot.delay(5050);

        robot.mouseMove(predictionStart.x, predictionStart.y);
        robot.delay(1500);

        robot.mousePress(BUTTON1_DOWN_MASK);
        robot.mouseMove(predictionEndng.x, predictionEndng.y);
        robot.mouseRelease(BUTTON1_DOWN_MASK);
        r.type("{⌘c}①{⌘c}⑤\n");
        r.type("## the next commit⑤ should start wtih⑤{‹‹‹}ith⑤ {⌘v}①①{Λw}{Λy}\n");
        r.type("## let's run the experiment①!\n");
        r.type("git commit -m 'comm⑤it message irrelevant①'\n");

        r.type("## expected {Λy}, actual ");
        robot.delay(3000);

        robot.mouseMove(commitStart.x, commitStart.y);
        robot.delay(1500);
        robot.mousePress(BUTTON1_DOWN_MASK);
        robot.mouseMove(commitEndng.x, commitEndng.y);
        robot.mouseRelease(BUTTON1_DOWN_MASK);
        r.type("{⌘c}①{⌘c}⑤");

        r.type("{⌘v}①\n");
        r.type("## the ends match... f①①0①①7①①b③{‹}d③③③\n");
        r.type("## let's③ check the whole commit\n");
        r.type("git log -1③\n");
        r.type("{Λp} | grep ③{Λy} --color①=①au①to\n");
        r.type("③③③③③\n");
        r.type("\n③③");
        r.type("\n");
        r.type("\n");
        r.type("\n");
        r.type("## i①t works!③③\n");
        r.type("\n");
        r.type("\n");
        r.type("\n");
    }

    public void type(String s) {
        long stepStart = System.nanoTime();
        if (startTime < 0) {
            startTime = stepStart;
        }
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

            if (c != '{') {
                sentences.add(Arrays.stream(toKeyEvent(c))
                        .boxed()
                        .collect(toList()));
            } else {
                ++i;
                int start = i;
                List<Integer> seq = new ArrayList<>();
                int end = s.indexOf('}', start);
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

            if (key < 0) { robot.delay(Math.abs(key) + random.nextInt(20)); }
            else         { robot.keyPress(chord[i]);                        }

            robot.delay(30 + random.nextInt(50));
        }
        for (int i = chord.length - 1; i >= 0; --i) {
            if (chord[i] > 0) {
                robot.keyRelease(chord[i]);
            }
            robot.delay(30 + random.nextInt(50));
        }
    }

    private int[] toKeyEvent(char c) {
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
        case '#': return new int[] { toKeyEvent('ς')[0], toKeyEvent('3')[0] };
        case '%': return new int[] { toKeyEvent('ς')[0], toKeyEvent('5')[0] };
        case '\'': return new int[] { VK_QUOTE };
        case '(': return new int[] { toKeyEvent('ς')[0], toKeyEvent('9')[0] };
        case ')': return new int[] { toKeyEvent('ς')[0], toKeyEvent('0')[0] };
        case '*': return new int[] { toKeyEvent('ς')[0], toKeyEvent('8')[0] };
        case '+': return new int[] { toKeyEvent('ς')[0], toKeyEvent('=')[0] };
        case ',': return new int[] { VK_COMMA };
        case '-': return new int[] { VK_MINUS };
        case '.': return new int[] { VK_PERIOD };
        case '/': return new int[] { VK_SLASH };
        case ';': return new int[] { VK_SEMICOLON };
        case '=': return new int[] { VK_EQUALS };
        case '>': return new int[] { toKeyEvent('ς')[0], toKeyEvent('.')[0] };
        case '?': return new int[] { toKeyEvent('ς')[0], toKeyEvent('/')[0] };
        case '[': return new int[] { VK_OPEN_BRACKET };
        case ']': return new int[] { VK_CLOSE_BRACKET };
        case '_': return new int[] { toKeyEvent('ς')[0], toKeyEvent('-')[0] };
        case '|': return new int[] { toKeyEvent('ς')[0], toKeyEvent('\\')[0] };
        case '0': return new int[] { VK_0 };
        case '1': return new int[] { VK_1 };
        case '2': return new int[] { VK_2 };
        case '3': return new int[] { VK_3 };
        case '4': return new int[] { VK_4 };
        case '5': return new int[] { VK_5 };
        case '6': return new int[] { VK_6 };
        case '7': return new int[] { VK_7 };
        case '8': return new int[] { VK_8 };
        case 'a': return new int[] { VK_A };
        case 'b': return new int[] { VK_B };
        case 'c': return new int[] { VK_C };
        case 'd': return new int[] { VK_D };
        case 'e': return new int[] { VK_E };
        case 'f': return new int[] { VK_F };
        case 'g': return new int[] { VK_G };
        case 'h': return new int[] { VK_H };
        case 'i': return new int[] { VK_I };
        case 'k': return new int[] { VK_K };
        case 'l': return new int[] { VK_L };
        case 'm': return new int[] { VK_M };
        case 'n': return new int[] { VK_N };
        case 'o': return new int[] { VK_O };
        case 'p': return new int[] { VK_P };
        case 'r': return new int[] { VK_R };
        case 's': return new int[] { VK_S };
        case 't': return new int[] { VK_T };
        case 'u': return new int[] { VK_U };
        case 'v': return new int[] { VK_V };
        case 'w': return new int[] { VK_W };
        case 'x': return new int[] { VK_X };
        case 'y': return new int[] { VK_Y };
        case '①': return new int[] { -10 };
        case '②': return new int[] { -20 };
        case '③': return new int[] { -30 };
        case '④': return new int[] { -40 };
        case '⑤': return new int[] { -50 };
        case '⑥': return new int[] { -60 };
        case '⑦': return new int[] { -70 };
        case '⑧': return new int[] { -80 };
        case '⑨': return new int[] { -90 };
        default:  return new int[] { Character.codePointAt(new char[] { c }, 0) };
        }
    }
}
