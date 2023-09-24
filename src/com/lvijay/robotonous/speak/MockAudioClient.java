package com.lvijay.robotonous.speak;

import java.io.IOException;

public class MockAudioClient implements AudioClient {
    @Override
    public AudioPlayer toAudioPlayer(@SuppressWarnings("unused") String content)
            throws IOException {
        return () -> {}; // does nothing, returns immediately.
    }
}
