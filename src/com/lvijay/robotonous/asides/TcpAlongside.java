package com.lvijay.robotonous.asides;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

/**
 * Writes argument to TCP socket.
 */
public class TcpAlongside implements Alongside {
    private final int port;

    public TcpAlongside(int port) {
        this.port = port;
    }

    @Override
    public void execute(String arg) throws IOException {
        try (var channel = SocketChannel.open(new InetSocketAddress(port))) {
            if (!channel.isConnected()) {
                throw new IllegalStateException("Not connected to destination port");
            }

            channel.configureBlocking(true);

            byte[] data = arg.getBytes(UTF_8);
            int written = channel.write(ByteBuffer.wrap(data));

            if (data.length != written) {
                throw new IllegalStateException("Did not write everything.");
            }

            int bufferSize = 4096;
            ByteBuffer readBuf = ByteBuffer.allocate(bufferSize);
            ByteArrayOutputStream out = new ByteArrayOutputStream(bufferSize);
            while (true) {
                readBuf.clear();
                int read = channel.read(readBuf);
                byte[] responseData = new byte[read];

                readBuf.flip().get(responseData);
                out.write(responseData);;
                if (read < bufferSize) {
                    break;
                }
            }

            System.out.printf("Response: `%s'", out.toString(UTF_8));
        }
    }

    public static void main(String[] args) throws IOException {
        var tcp = new TcpAlongside(8989);

        long start = System.nanoTime();
        tcp.execute("(SayText \"can we type and talk concurrently?\")");
        long end = System.nanoTime();
        long diff = TimeUnit.NANOSECONDS.toMillis(end - start);

        System.out.println("time taken = " + diff);
    }
}
