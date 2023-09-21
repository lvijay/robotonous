package com.lvijay.robotonous.speak.festival;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public class FestivalClient {
    public static final String END = "ft_StUfF_key";

    private final int port;
    private final Path cacheDirectory;
    private final String algorithm = "SHA-1";
    private final String voice;
    private final String setFestivalAudioFormat;

    private FestivalClient(
            int port,
            Path cacheDirectory,
            String voice,
            String setFestivalAudioFormat) {
        this.port = port;
        this.cacheDirectory = cacheDirectory;
        this.voice = '(' + voice + ')';
        this.setFestivalAudioFormat = setFestivalAudioFormat;
    }

    public FestivalClient(int port, Path cacheDirectory, String voice) {
        this(port, cacheDirectory, voice, "(Parameter.set 'Wavefiletype 'riff)");
    }

    public FestivalClient(int port, Path cacheDirectory) {
        this(port, cacheDirectory, "voice_cmu_us_aew_cg");
    }

    public byte[] toAudioData(String content) throws IOException {
        content = content.replace("\"", "\\\""); // poor escaping but it'll do for now

        var hash = hash(content);
        var fileData = getFileData(hash);

        if (fileData != null) {
            return fileData;
        }

        var setFormat = setFestivalAudioFormat;
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

        var waveData = resp.stream()
                .filter(r -> r instanceof ResponseWave)
                .map(r -> (ResponseWave) r)
                .findFirst()
                .map(v -> v.wavData())
                .get();

        saveFileData(hash, waveData);

        return waveData;
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

    private String hash(String contents) {
        try {
            var md = MessageDigest.getInstance(algorithm);
            var hash = md.digest(contents.getBytes(UTF_8));
            var hashed = new BigInteger(+1, hash);
            return hashed.toString(16);
        } catch (NoSuchAlgorithmException e) {
            // unreachable code, see
            // https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/security/MessageDigest.html
            throw new IllegalStateException(e);
        }
    }

    private byte[] getFileData(String hash) throws IOException {
        try {
            var filename = hash + ".wav";
            Path filepath = cacheDirectory.resolve(filename);

            if (filepath.toFile().exists()) {
                return Files.readAllBytes(filepath);
            }

            return null;
        } catch (NoSuchFileException e) {
            return null;
        }
    }

    private void saveFileData(String hash, byte[] waveData) throws IOException {
        try {
            var filename = hash + ".wav";
            var filepath = cacheDirectory.resolve(filename);

            Files.write(filepath, waveData);
        } catch (NoSuchFileException e) {
            return;
        }
    }
}
