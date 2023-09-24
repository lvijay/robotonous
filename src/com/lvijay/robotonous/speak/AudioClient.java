package com.lvijay.robotonous.speak;

import java.io.IOException;

public interface AudioClient {
    AudioPlayer toAudioPlayer(String content) throws IOException;
}
