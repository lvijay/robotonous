package com.lvijay.robotonous;

import java.util.List;
import java.util.Set;

public record SpecialKeys(
        char keyCommentLine,
        KeyPair keyAction,
        KeyPair keyCopy,
        KeyPair keySpeak,
        KeyPair keyPrint,
        char keySpeakWait,
        char keyControl,
        char keyAlt,
        char keyShift,
        char keyMeta,
        char keyEscape,
        char keyNewline,
        char keyTab,
        char keyBackspace,
        char keyDelete,
        String chordPaste,
        KeyPair mouseLeft,
        KeyPair mouseRight,
        KeyPair mouseMove) {
    public SpecialKeys {
        var keys = List.of(
                keyAction.open(),
                keyAction.close(),
                keyCopy.open(),
                keyCopy.close(),
                keySpeak.open(),
                keySpeak.close(),
                keySpeakWait,
                keyControl,
                keyAlt,
                keyShift,
                keyMeta,
                keyEscape,
                keyNewline,
                keyTab,
                keyBackspace,
                keyDelete);
        var skeys = Set.copyOf(keys);

        if (keys.size() != skeys.size()) {
            throw new IllegalArgumentException("keys must be unique " + keys);
        }

        var mouseChars = Set.of(mouseLeft, mouseRight, mouseMove);
        if (mouseChars.size() != 3) {
            throw new IllegalArgumentException("mouse chars must be unique " + mouseChars);
        }

        // TODO ensure pasteChord doesn't collide with the other keys
    }
}
