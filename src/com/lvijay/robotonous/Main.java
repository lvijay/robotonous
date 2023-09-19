package com.lvijay.robotonous;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.awt.Robot;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

import com.lvijay.robotonous.speak.festival.FestivalClient;

public class Main {
    public static void main(String[] args) throws Exception {
        var robot = new Robot();
        var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        var threadpool = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });
        int festivalPort = 8989;
        var festivalClient = new FestivalClient(festivalPort);

        /* initialize and test sound */

        byte[] say = festivalClient.say("if you heard this, sound works");
        Robotonous.play(new ByteArrayInputStream(say));

        for (int i = 3; i > 0; i--) {
            System.out.println("starting in " + i + " seconds");
            robot.delay(1000);
        }

        for (String file : args) {
            var contents = Contents.toContents(Files.readString(Paths.get(file), UTF_8));
            var sequencer = new KeyEventSequencerQwerty(contents.keys());
            var executor = new Robotonous(
                    contents.keys(),
                    sequencer,
                    robot,
                    clipboard,
                    threadpool,
                    festivalClient);
            var actions = executor.toActions(contents.body());

            executor.execute(actions);
        }
    }
}
