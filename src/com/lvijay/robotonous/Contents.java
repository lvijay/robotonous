package com.lvijay.robotonous;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Contents(String header, String body, SpecialKeys keys) {
    private static final Pattern KEY_COMMENT_LINE = Pattern.compile("(?m)^\\s*keyCommentLine\\s*=\\s*(.)\\s*$");
    private static final Pattern KEY_CHORD = Pattern.compile("(?m)^\\s*keyChord\\s*=\\s*(..)\\s*$");
    private static final Pattern KEY_COPY = Pattern.compile("(?m)^\\s*keyCopy\\s*=\\s*(..)\\s*$");
    private static final Pattern KEY_SPEAK = Pattern.compile("(?m)^\\s*keySpeak\\s*=\\s*(..)\\s*$");
    private static final Pattern KEY_PRINT = Pattern.compile("(?m)^\\s*keyPrint\\s*=\\s*(..)\\s*$");
    private static final Pattern KEY_SPEAK_WAIT = Pattern.compile("(?m)^\\s*keySpeakWait\\s*=\\s*(.)\\s*$");
    private static final Pattern KEY_CONTROL = Pattern.compile("(?m)^\\s*keyControl\\s*=\\s*(.)\\s*$");
    private static final Pattern KEY_ALT = Pattern.compile("(?m)^\\s*keyAlt\\s*=\\s*(.)\\s*$");
    private static final Pattern KEY_SHIFT = Pattern.compile("(?m)^\\s*keyShift\\s*=\\s*(.)\\s*$");
    private static final Pattern KEY_META = Pattern.compile("(?m)^\\s*keyMeta\\s*=\\s*(.)\\s*$");
    private static final Pattern KEY_ESCAPE = Pattern.compile("(?m)^\\s*keyEscape\\s*=\\s*(.)\\s*$");
    private static final Pattern KEY_NEWLINE = Pattern.compile("(?m)^\\s*keyNewline\\s*=\\s*(.)\\s*$");
    private static final Pattern KEY_TAB = Pattern.compile("(?m)^\\s*keyTab\\s*=\\s*(.)\\s*$");
    private static final Pattern KEY_BACKSPACE = Pattern.compile("(?m)^\\s*keyBackspace\\s*=\\s*(.)\\s*$");
    private static final Pattern KEY_DELETE = Pattern.compile("(?m)^\\s*keyDelete\\s*=\\s*(.)\\s*$");
    private static final Pattern CHORD_PASTE = Pattern.compile("(?m)^\\s*chordPaste\\s*=\\s*(.)\\s*$");
    private static final Pattern HEADER_END = Pattern.compile("(?m)^" + Pattern.quote("----") + "$");
    private static final Pattern MOUSE_LEFT = Pattern.compile("(?m)^\\s*mouseLeft\\s*=\\s*(..)\\s*$");
    private static final Pattern MOUSE_RIGHT = Pattern.compile("(?m)^\\s*mouseRight\\s*=\\s*(..)\\s*$");
    private static final Pattern MOUSE_MOVE = Pattern.compile("(?m)^\\s*mouseMove\\s*=\\s*(..)\\s*$");

    public static Contents toContents(String contents) {
        var headerContents = getHeader(contents);
        var keys = parseSpecialKeys(headerContents[0]);

        return new Contents(headerContents[0], headerContents[1], keys);
    }

    private static String[] getHeader(String contents) {
        var endM = HEADER_END.matcher(contents);
        int end = endM.find() ? endM.end() + 1 : 0;
        var header = contents.substring(0, end);

        return new String[] { header, contents.substring(end) };
    }

    private static SpecialKeys parseSpecialKeys(String header) {
        var keyCommentLine = get(KEY_COMMENT_LINE.matcher(header), "©");
        var keyAction = get(KEY_CHORD.matcher(header), "«»");
        var keyCopy = get(KEY_COPY.matcher(header), "⊂⊃");
        var keySpeak = get(KEY_SPEAK.matcher(header), "♩♭");
        var keyPrint = get(KEY_PRINT.matcher(header), "ɛɜ");
        var keySpeakWait = get(KEY_SPEAK_WAIT.matcher(header), "ω");
        var keyControl = get(KEY_CONTROL.matcher(header), "¢");
        var keyAlt = get(KEY_ALT.matcher(header), "æ");
        var keyShift = get(KEY_SHIFT.matcher(header), "§");
        var keyMeta = get(KEY_META.matcher(header), "±");
        var keyEscape = get(KEY_ESCAPE.matcher(header), "␛");
        var keyReturn = get(KEY_NEWLINE.matcher(header), "␍");
        var keyTab = get(KEY_TAB.matcher(header), "␉");
        var keyBackspace = get(KEY_BACKSPACE.matcher(header), "‹");
        var keyDelete = get(KEY_DELETE.matcher(header), "›");
        // set this in an OS dependent manner?
        var chordPaste = get(CHORD_PASTE.matcher(header), keyMeta + "v");
        var mouseLeft = get(MOUSE_LEFT.matcher(header), "┫┛");
        var mouseRight = get(MOUSE_RIGHT.matcher(header), "┣┗");
        var mouseMove = get(MOUSE_MOVE.matcher(header), "∏∐");

        return new SpecialKeys(
                keyCommentLine.charAt(0),
                KeyPair.fromString(keyAction),
                KeyPair.fromString(keyCopy),
                KeyPair.fromString(keySpeak),
                KeyPair.fromString(keyPrint),
                keySpeakWait.charAt(0),
                keyControl.charAt(0),
                keyAlt.charAt(0),
                keyShift.charAt(0),
                keyMeta.charAt(0),
                keyEscape.charAt(0),
                keyReturn.charAt(0),
                keyTab.charAt(0),
                keyBackspace.charAt(0),
                keyDelete.charAt(0),
                chordPaste,
                KeyPair.fromString(mouseLeft),
                KeyPair.fromString(mouseRight),
                KeyPair.fromString(mouseMove));
    }

    private static String get(Matcher matcher, String defaultValue) {
        return matcher.find() ? matcher.group(1) : defaultValue;
    }
}
