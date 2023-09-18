package com.lvijay.robotonous;

import java.util.List;
import java.util.Set;

public record SpecialKeys(
        char keyCommentLine,
        char keyAction,
        char keyCopy,
        char keySpeak,
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
        String chordPaste) {
    public SpecialKeys {
        var keys = List.of(
                keyAction,
                keyCopy,
                keySpeak,
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

        // TODO ensure pasteChord doesn't collide with the other keys
    }
}
