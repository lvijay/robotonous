package com.lvijay.robotonous.print;

import java.util.Optional;
import java.util.regex.Pattern;

public record Formatter(String format, int delay, String contents) {
    private static final Pattern IDENTIFY = Pattern.compile(
            "(?<format>[;0-9]+)?"
            + "(?:\\Q&\\E(?<delay>[0-9]+))?");

    // https://misc.flogisoft.com/bash/tip_colors_and_formatting
    public static Formatter fromString(String formatContents) {
        // 31;4;1:hello, world
        String[] split = formatContents.split(":", 2);
        var contents = split[1];
        var definition = split[0];
        var m = IDENTIFY.matcher(definition);

        if (!m.find()) {
            return new Formatter("", -1, contents);
        }
        var frmt = m.group("format");
        var dly = m.group("delay");
        var format = Optional.ofNullable(frmt).orElse("");
        int delay = Optional.ofNullable(dly)
                .map(Integer::parseInt)
                .orElse(Integer.valueOf(-1))
                .intValue();

        return new Formatter(format, delay, contents);
    }
}
