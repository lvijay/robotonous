package com.lvijay.robotonous.speak.festival;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class FestivalClient {
    public static final String END = "ft_StUfF_key";

    private final int port;
    private final String voice;
    private final String audioFormat;

    private FestivalClient(int port, String voice, String audioFormat) {
        this.port = port;
        this.voice = '(' + voice + ')';
        this.audioFormat = audioFormat;
    }

    public FestivalClient(int port, String voice) {
        this(port, voice, "(Parameter.set 'Wavefiletype 'riff)");
    }

    public FestivalClient(int port) {
        this(port, "voice_cmu_us_aew_cg");
    }

    public byte[] say(String content) throws IOException {
        content = content.replace("\"", "\\\""); // poor escaping but it'll do for now

        var setFormat = audioFormat;
        var setVoice = voice;
        var getContent = String.format("(tts_textall %c%s%c %cfundamental%c)",
                '"', content, '"', '"', '"');
        var emptyComputation = "(= nil nil)";

        List<FestivalResponse> resp = send(List.of(
                setFormat,
                setVoice,
                getContent,
                emptyComputation
                ));

        return resp.stream()
                .filter(r -> r instanceof ResponseWave)
                .map(r -> (ResponseWave) r)
                .findFirst()
                .map(v -> v.wavData())
                .get();
    }

    private List<FestivalResponse> send(List<String> sexps) throws IOException {
        try (var socket = new Socket("localhost", port);
                var in = socket.getInputStream();
                var out = socket.getOutputStream()) {
            return sexps.stream()
                    .map(lsp -> {
                        try {
                            var response = sendInternal(lsp, in, out);
                            if (response instanceof ResponseOk) {
                                // ignore OK, go again
                                return sendInternal(lsp, in, out);
                            }
                            return response;
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .toList();
        }
    }

    private static FestivalResponse sendInternal(String lisp, InputStream in, OutputStream out)
            throws IOException {
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

    private static String readStatus(InputStream in) throws IOException {
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

    private static byte[] readUntil(InputStream in, byte[] endMatch)
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

    /* test */
    public static void main(String[] args)
            throws IOException,
                    InterruptedException,
                    LineUnavailableException,
                    UnsupportedAudioFileException
    {
        var client = new FestivalClient(8989);

        LocalTime now = LocalTime.now();
        var msg = now.getHour() + ":" + now.getMinute();
        byte[] sayData = client.say(String.format("The time is %s.", msg));
        playClip(new ByteArrayInputStream(sayData));
    }

    static void playClip(ByteArrayInputStream sayData)
            throws IOException,
                    InterruptedException,
                    LineUnavailableException,
                    UnsupportedAudioFileException
    {
        var audioStream = AudioSystem.getAudioInputStream(sayData);
        Clip audioClip = AudioSystem.getClip();
        var wait = new SynchronousQueue<String>();

        audioClip.addLineListener(evt -> {
            System.out.println(evt);
            if (evt.getType().equals(Type.STOP)) {
                try {
                    System.out.println("Stopping");
                    wait.put("done");
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }
        });
        audioClip.open(audioStream);
        audioClip.start();
        wait.take(); // TODO should we check return value?
        audioClip.drain();
        audioClip.close();
    }
}
