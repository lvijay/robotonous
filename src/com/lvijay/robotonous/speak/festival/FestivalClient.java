package com.lvijay.robotonous.speak.festival;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.lvijay.robotonous.speak.Audio;

public class FestivalClient {
    public static final String END = "ft_StUfF_key";

    private final int port;

    public FestivalClient(int port) {
        this.port = port;
    }

    public List<FestivalResponse> send(List<String> sexps) throws IOException {
        try (var socket = new Socket("localhost", port);
                var in = socket.getInputStream();
                var out = socket.getOutputStream()) {
            return sexps.stream()
                    .map(lsp -> {
                        try {
                            return sendInternal(lsp, in, out);
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .toList();
        }
    }

    private FestivalResponse sendInternal(String lisp, InputStream in, OutputStream out)
            throws IOException {
        {
            out.write(lisp.getBytes(UTF_8));

            byte[] endbytes = END.getBytes(US_ASCII);
            var resp = switch (readStatus(in)) {
                case "ER" -> new ResponseError();
                case "OK" -> new ResponseOk();
                case "WV" -> new ResponseWave(readUntil(in, endbytes));
                case "LP" -> new ResponseLisp(new String(readUntil(in, endbytes)));
                default -> throw new IllegalArgumentException();
            };

            return resp;
        }
    }

    private String readStatus(InputStream in) throws IOException {
        int c1 = in.read();
        int c2 = in.read();
        int c3 = in.read();

        if (List.of(c1, c2, c3).contains(Integer.valueOf(-1))) {
            throw new IOException("Stream ended");
        }

        if (c3 != '\n') {
            throw new IllegalStateException("Expected \\n, found " + ((char) c3));
        }

        return String.format("%c%c", c1, c2);
    }

    private byte[] readUntil(InputStream in, byte[] endMatch)
            throws IOException {
        var bin = new BufferedInputStream(in);
        var out = new ByteArrayOutputStream();
        int data;
        while ((data = bin.read()) != -1) {
            out.write(data);

            if (data == endMatch[0]) {
                boolean found = true;
                for (int i = 1; i < endMatch.length; ++i) {
                    data = bin.read();
                    out.write(data);

                    if (endMatch[i] != data) {
                        found = false;
                        break;
                    }
                }

                if (found) {
                    return Arrays.copyOf(out.toByteArray(), out.size() - endMatch.length);
                }
            }
        }

        throw new IllegalStateException();
    }

    public static void main(String[] args)
            throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        var client = new FestivalClient(8989);

        var msg = LocalTime.now().getHour() + ":" + LocalTime.now().getMinute();

        List<FestivalResponse> resp = client.send(List.of(
                "(Parameter.set 'Wavefiletype 'riff)",
                "(voice_cmu_us_slp_cg)",
                "(tts_textall \"hello " + msg + "\" \"fundamental\")",
                "(+ 1 29)",
                "(* 38 11)",
//                "(SayText \"three 3\")",
                "(tts_textall \"hello\" \"fundamental\")",
                "(/ 60 23)"));

        byte[] wavData = resp.stream()
                .filter(r -> r instanceof ResponseWave)
                .map(r -> (ResponseWave) r)
                .findFirst()
                .map(r -> r.wavData())
                .get();

        System.out.println(wavData.length);

        Path saveTo = Paths.get("out.wav");
        Files.write(saveTo, wavData,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);

        Audio audio = Audio.createAudio(saveTo);

        var audioStream = AudioSystem.getAudioInputStream(saveTo.toFile());
        var audioFormat = audioStream.getFormat();
        var info = new DataLine.Info(SourceDataLine.class, audioFormat);
        var wavLine = (SourceDataLine) AudioSystem.getLine(info);

        System.out.println("audioFormat = " + audioFormat);
        System.out.println("datalineinfo = " + info);
        System.out.println("wavline = " + wavLine);

        wavLine.open(audioFormat);
        wavLine.start();
        audio.play(wavLine);
        wavLine.close();
    }
}
