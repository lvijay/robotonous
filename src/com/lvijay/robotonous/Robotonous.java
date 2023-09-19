package com.lvijay.robotonous;

import java.awt.Robot;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.stream.IntStream;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.lvijay.robotonous.speak.festival.FestivalClient;

public class Robotonous {
    private final SpecialKeys keys;
    private final KeyEventSequencer sequencer;
    private final Robot robot;
    private final Clipboard clipboard;
    private final List<Action> pasteAction;
    private final ExecutorService threadpool;
    private final FestivalClient festivalClient;
    private final Queue<Future<Void>> futures;

    public Robotonous(
            SpecialKeys keys,
            KeyEventSequencerQwerty sequencer,
            Robot robot,
            Clipboard clipboard,
            ExecutorService threadpool,
            FestivalClient festivalClient) {
        this.keys = keys;
        this.sequencer = sequencer;
        this.robot = robot;
        this.clipboard = clipboard;
        this.pasteAction = toActions(
                keys.keyAction()
                + keys.chordPaste()
                + keys.keyAction());
        this.threadpool = threadpool;
        this.festivalClient = festivalClient;
        this.futures = new LinkedList<>();
    }

    public List<Action> toActions(String s) {
        List<Action> actions = new ArrayList<>(s.length());

        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);

            try {
                if (c == keys.keyAction()) {
                    ++i;
                    int start = i;
                    int end = s.indexOf(keys.keyAction(), start);

                    int[] chordKeys = s.substring(start, end)
                            .chars()
                            .mapToObj(ch -> sequencer.toKeyEvent((char) ch))
                            .flatMapToInt(axn -> IntStream.of(axn.arguments()))
                            .toArray();
                    actions.add(new Action(chordKeys));

                    i = end;
                } else if (c == keys.keyCopy()) { // add contents to system clipboard
                    int start = ++i;
                    int end = s.indexOf(keys.keyCopy(), start);
                    String pasteContents = s.substring(start, end);
                    int[] pastechars = pasteContents.chars()
                            .toArray();
                    actions.add(new Action(Event.PASTE, pastechars));
                    i = end;
                } else if (c == keys.keyCommentLine()) { // ignore until end of line
                    int end = s.indexOf('\n', i);
                    i = end;
                } else if (c == keys.keySpeak()) {
                    int start = ++i;
                    int end = s.indexOf(keys.keySpeak(), start);
                    String speakContent = s.substring(start, end);
                    byte[] audioData = festivalClient.say(speakContent);

                    actions.add(new Action(
                            Event.SPEAK,
                            IntStream.range(0, audioData.length)
                                .map(idx -> audioData[idx])
                                .toArray()));
                    i = end;
                } else if (c == keys.keySpeakWait()) {
                    long initCounts = actions.stream()
                            .filter(a -> a.event() == Event.SPEAK)
                            .count();
                    long waitCounts = actions.stream()
                            .filter(a -> a.event() == Event.SPEAK_WAIT)
                            .count();

                    if (initCounts <= waitCounts) {
                        throw new IllegalArgumentException("More waits than inits");
                    }

                    actions.add(new Action(Event.SPEAK_WAIT));
                } else {
                    actions.add(sequencer.toKeyEvent(c));
                }
            } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                System.err.printf("Exception at index %d c=%c%n", i, c);
                throw e;
            } catch (IOException e) {
                System.err.printf("Exception %s at index %d c=%c%n", e, i, c);
                throw new IllegalStateException(e);
            }
        }

        return actions;
    }

    public void execute(List<Action> actions) throws Exception {
        for (var action : actions) {
            System.out.println("Executing " + action);
            switch (action.event()) {
                case DELAY -> delay(action.arguments());
                case PASTE -> copyPaste(action.arguments());
                case TYPE -> chord(action.arguments());
                case SPEAK -> speak(action.arguments());
                case SPEAK_WAIT -> speakWait();
            };
        }
    }

    void chord(int[] keyEvents) {
        IntStream.of(keyEvents)
                .forEach(ke -> robot.keyPress(ke));

        IntStream.iterate(keyEvents.length - 1, i -> i >= 0, i -> i - 1)
                .map(i -> keyEvents[i])
                .forEach(ke -> robot.keyRelease(ke));
    }

    void delay(int[] delays) {
        IntStream.of(delays)
                .map(d -> d * 100) // to milliseconds
                .forEach(robot::delay);
    }

    void copyPaste(int[] chord) throws Exception {
        var contents = new StringBuilder(chord.length - 1);
        Arrays.stream(chord, 0, chord.length)
                .forEach(kc -> contents.append((char) kc));
        var pasteContents = new StringSelection(contents.toString());
        clipboard.setContents(pasteContents, null);
        execute(pasteAction);
    }

    void speak(int[] wavdata) {
        byte[] result = new byte[wavdata.length];
        IntStream.range(0, wavdata.length)
                .forEach(i -> result[i] = (byte) wavdata[i]);
        submit(result);
    }

    void speakWait() throws InterruptedException, ExecutionException {
        Future<Void> head = futures.remove();

        head.get(); // indefinite wait
    }

    private void submit(byte[] audio) {
        var future = threadpool.<Void>submit(() -> play(
                new ByteArrayInputStream(audio)), null);

        futures.add(future);
    }

    static void play(ByteArrayInputStream audioData) {
        /*
         * In my experience Clip::drain doesn't block. We roll out our own
         * blocker.
         */
        var playWaiter = new SynchronousQueue<Boolean>();
        try {
            var audioStream = AudioSystem.getAudioInputStream(audioData);
            var player = AudioSystem.getClip();

            // start waiting before playing
            player.addLineListener(evt -> {
                if (evt.getType().equals(Type.STOP)) {
                    playWaiter.add(Boolean.TRUE);
                }
            });

            player.open(audioStream);
            player.start();

            playWaiter.take(); // explicit wait
            player.drain();
        } catch (
                LineUnavailableException
                | IOException
                | InterruptedException
                | UnsupportedAudioFileException e) {
            throw new RuntimeException(e);
        }
    }
}
