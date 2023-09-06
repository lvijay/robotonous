package com.lvijay.robotonous;

import static java.awt.event.KeyEvent.VK_ALT;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_META;
import static java.awt.event.KeyEvent.VK_SHIFT;
import static java.util.stream.Collectors.joining;

import java.util.Arrays;

public record Action(Event event, int... arguments) {
    public Action(int... arguments) {
        this(Event.TYPE, arguments);
    }

    // debugging functions
    @Override
    public String toString() {
        String args = switch (event) {
            case DELAY -> String.valueOf(Arrays.stream(arguments).sum());
            case PASTE -> Arrays.stream(arguments)
                    .boxed()
                    .collect(
                            () -> new StringBuilder(),
                            (b, c) -> b.append((char) c.intValue()),
                            (s1, s2) -> s1.append(s2.toString()))
                    .toString();
            case TYPE -> Arrays.stream(arguments)
                    .mapToObj(i -> "'" + charName(i) + "'")
                    .collect(joining(" ", "[", "]"));
        };
        return String.format("<%s %s>", event, args);
    }

    private String charName(int i) {
        return switch (i) {
            case VK_SHIFT -> "SHIFT";
            case VK_CONTROL -> "CTRL";
            case VK_ALT -> "ALT";
            case VK_META -> "META";
            default -> {
                var name = Character.getName((char) i);
                if (name.startsWith("LATIN CAPITAL LETTER ")) {
                    yield ("" + name.charAt(name.length() - 1)).toLowerCase();
                }

                yield name;
            }
        };
    }
}
