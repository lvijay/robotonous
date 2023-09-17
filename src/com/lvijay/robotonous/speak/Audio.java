package com.lvijay.robotonous.speak;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Audio {
    public static Audio createAudio(Path audio)
            throws UnsupportedAudioFileException, IOException {
        var file = audio.toFile();
        var audioStream = AudioSystem.getAudioInputStream(file);
        var audioFormat = audioStream.getFormat();
        long fileLen = file.length();
        double frameRate = audioFormat.getFrameRate();
        int frameSize = audioFormat.getFrameSize();
        var duration = 1e9 * fileLen / (frameRate * frameSize);

        var out = new ByteArrayOutputStream((int) file.length());
        int bufSize = 8192; // why 8192?  who knows
        byte[] buf = new byte[bufSize];
        int len;
        while ((len = audioStream.read(buf)) != -1) {
            out.write(buf, 0, len);
        }

        return new Audio(
                out.toByteArray(),
                audioFormat,
                Duration.ofNanos((long) duration));
    }

    private final byte[] data;
    private final AudioFormat format;
    private final Duration duration;

    private Audio(byte[] audioData, AudioFormat format, Duration duration) {
        this.data = audioData;
        this.format = format;
        this.duration = duration;
    }

    public Duration duration() {
        return duration;
    }

    public AudioFormat format() {
        return format;
    }

    public void play(SourceDataLine line) {
        int lineBufferSize = line.getBufferSize();
        for (int i = 0; i < data.length; ) {
            int remaining = data.length - i;
            int written = line.write(data, i, Math.min(remaining, lineBufferSize));

            i += written;
        }

        line.drain();
    }
}
