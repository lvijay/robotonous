package com.lvijay.robotonous;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.awt.Robot;
import java.awt.Toolkit;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
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
        var cacheDirectory = Paths.get(".cache");
        var festivalClient = new FestivalClient(festivalPort, cacheDirectory);

        var file = args[0];

        var fileContents = Files.readString(Paths.get(file), UTF_8);
        var contents = Contents.toContents(fileContents);
        var executor = new Robotonous(
                contents.body(),
                contents.keys(),
                robot,
                clipboard,
                threadpool,
                festivalClient);
        long startNanos = System.nanoTime();
        executor.playAudio("voice check");

        System.out.println("Computing robot actions...");
        executor.init();
        long endNanos = System.nanoTime();
        long computeTimeS = Duration.ofNanos(endNanos - startNanos).toSeconds();
        System.out.printf("...took %ds%n", computeTimeS);

        for (int i = 3; i > 0; i--) {
            System.out.println("starting in " + i + " seconds");
            robot.delay(1000);
        }

        executor.execute();
    }
}
