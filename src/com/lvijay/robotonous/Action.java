package com.lvijay.robotonous;

import static java.awt.event.KeyEvent.VK_ALT;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_META;
import static java.awt.event.KeyEvent.VK_SHIFT;
import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.stream.IntStream;

public record Action(Event event, int... arguments) {
    public Action(int... arguments) {
        this(Event.TYPE, arguments);
    }

    // debugging functions
    @Override
    public String toString() {
        String args = switch (event) {
            case DELAY -> String.valueOf(Arrays.stream(arguments).sum());
            case PASTE -> asString();
            case TYPE -> Arrays.stream(arguments)
                    .mapToObj(i -> "'" + charName(i) + "'")
                    .collect(joining(" ", "[", "]"));
            case ASIDE_INIT -> String.format("Aside execution of %s", asString());
            case ASIDE_WAIT -> "";
        };
        return String.format("<%s %s>", event, args);
    }

    private String asString() {
        char[] chars = new char[arguments.length];
        IntStream.range(0, arguments.length)
                .forEach(i -> chars[i] = (char) arguments[i]);

        return new String(chars);
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
