package com.lvijay.robotonous;

import java.util.List;
import java.util.Set;

public record SpecialKeys(
        char commentLineKey,
        char keyAction,
        char keyCopy,
        char keyControl,
        char keyAlt,
        char keyShift,
        char keyMeta,
        char keyEscape,
        char keyBackspace,
        char keyDelete,
        String pasteChord) {
    public SpecialKeys {
        var keys = List.of(
                keyAction,
                keyCopy,
                keyControl,
                keyAlt,
                keyShift,
                keyMeta,
                keyEscape,
                keyBackspace,
                keyDelete);
        var skeys = Set.copyOf(keys);

        if (keys.size() != skeys.size()) {
            throw new IllegalArgumentException("keys must be unique " + keys);
        }

        if (keyAction == keyCopy) {
            throw new IllegalArgumentException("Action keys must be unique");
        }

        // TODO ensure pasteChord doesn't collide with the other keys
    }
}
