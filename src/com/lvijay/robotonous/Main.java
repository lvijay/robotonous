package com.lvijay.robotonous;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.awt.Robot;
import java.awt.Toolkit;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

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
        var audioPlayer = getPlayer();

        audioPlayer.open();
        audioPlayer.start();

        /* initialize and test sound */

        byte[] say = festivalClient.say("if you heard this, sound works");
        Robotonous.play(say, audioPlayer);

        try {
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
                        festivalClient,
                        audioPlayer);
                var actions = executor.toActions(contents.body());

                executor.execute(actions);
            }
        } finally {
            audioPlayer.close();
        }

    }

    private static SourceDataLine getPlayer() throws LineUnavailableException {
        var encoding = Encoding.PCM_SIGNED;
        int channels = 1;
        var bigEndian = false;
        var sampleRate = 16000.0f;
        int sampleSizeInBits = 16;
        int frameSize = 2;
        var frameRate = 16000.0f;
        var pcmFormat = new AudioFormat(
                encoding,
                sampleRate,
                sampleSizeInBits,
                channels,
                frameSize,
                frameRate,
                bigEndian);
        var info = new DataLine.Info(SourceDataLine.class, pcmFormat);
        var waveLine = (SourceDataLine) AudioSystem.getLine(info);

        return waveLine;
    }
}
