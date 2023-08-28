package com.lvijay.robotonous;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static final SpecialKeys SCHEME_1 = new SpecialKeys(
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

    public static void main(String[] args) throws AWTException, IOException {
        var sequencer = new KeyEventSequencerQwerty(SCHEME_1);
        var robot = new Robot();
        var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        var executor = new Robotonous(SCHEME_1, sequencer, robot, clipboard);

        for (int i = 3; i > 0; i--) {
            System.out.println("starting in " + i + " seconds");
            robot.delay(1000);
        }

        for (String file : args) {
            String contents = Files.readString(Paths.get(file), UTF_8);
            List<Action> actions = executor.toActions(contents);

            executor.execute(actions);
        }
    }
}
