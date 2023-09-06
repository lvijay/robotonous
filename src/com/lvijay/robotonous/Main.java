package com.lvijay.robotonous;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws AWTException, IOException {
        var robot = new Robot();
        var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        for (int i = 3; i > 0; i--) {
            System.out.println("starting in " + i + " seconds");
            robot.delay(1000);
        }

        for (String file : args) {
            var contents = Contents.toContents(Files.readString(Paths.get(file), UTF_8));
            var sequencer = new KeyEventSequencerQwerty(contents.keys());
            var executor = new Robotonous(contents.keys(), sequencer, robot, clipboard);
            var actions = executor.toActions(contents.body());

            executor.execute(actions);
        }
    }
}
