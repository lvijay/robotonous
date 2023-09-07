package com.lvijay.robotonous;

import java.util.List;
import java.util.Set;

public record SpecialKeys(
        char keyCommentLine,
        char keyAction,
        char keyCopy,
        char keyAsideInit,
        char keyAsideWait,
        char keyControl,
        char keyAlt,
        char keyShift,
        char keyMeta,
        char keyEscape,
        char keyNewline,
        char keyTab,
        char keyBackspace,
        char keyDelete,
        String pasteChord) {
    public SpecialKeys {
        var keys = List.of(
                keyAction,
                keyCopy,
                keyAsideInit,
                keyAsideWait,
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
