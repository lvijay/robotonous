package com.lvijay.robotonous;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.util.List;

public class Main {
    public static void main(String[] args) throws AWTException {
        var keys = new SpecialKeys(
                '©', // commentLineKey
                '«', // actionChord
                '¶', // actionCopy
                '¢', // keyControl
                'æ', // keyAlt
                '§', // keyShift
                '±', // keyMeta
                'ƒ', // keyEscape
                '‹', // keyBackspace
                '›', // keyDelete
                "¢y" // pasteChord
        );
        var sequencer = new KeyEventSequencerQwerty(keys);
        var robot = new Robot();
        var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        var executor = new Robotonous(keys, sequencer, robot, clipboard);

        String text = """
                ①㉟㊱The quick brown jumps «æb«fox «¢e««§over« the lazy dog«æbbb««æk««¢y«!@#
                ¶the quick brown fox¶ jumps over ¶the lazy dog¶
                «§1234«

                """;

        for (int i = 3; i > 0; i--) {
            System.out.println("starting in " + i + " seconds");
            robot.delay(1000);
        }

        List<Action> actions = executor.toActions(text);
        executor.execute(actions);
    }
}
