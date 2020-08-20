Robotonous
----------

Autonomous actions on the computer using `java.awt.Robot`.

Recently, I pulled off a git "magic trick".  It was recorded and
shared as a YouTube video.  The entire narration was done by typing on
a screen.  Now, I type fast, but it's a different story when you're
recording your typing.  I found I was either making too many mistakes
while recording or someone would find a way to pull me off the
keyboard.  The software engineer's credo is, of course, don't do
manually what you can make a computer do.  Enter Robotonous.  Java,
since 1.3, has had the class
[java.awt.Robot](https://docs.oracle.com/en/java/javase/14/docs/api/java.desktop/java/awt/Robot.html)
that allows mouse control and text entry.

This implementation is quite limited and enhancements will only be
made on an as-is-needed basis (though pull requests always welcome).

## Running

Running the program should be as straightforward as shown below.
However the program takes no inputs.  Its execution instructions are
now part of the code.

```java
java src/Robotonous.java
```

## Usage
The idea is to provide text you want the computer to type out and
these instructions should match as much as they do actual typing.
Thus, `r.type("abcd");` should type the keyboard keys "a", "b", "c",
and "d" respectively.  Similarly, `r.type("#");` should type the
keyboard keys, "Shift" and "3", and so on.  You can also type control
sequences such as "Control c", and "Alt b", and "Command v".

The current version is extremely limited -- specifically it supports
only those keys which I needed for the video.

### Instructions

1. all lower case alphabets are supported.  `r.type("x")` types x.
2. all numeric characters are supported.  `r.type("3")` types 3.
3. most special characters are supported (see the code for details).
4. the brace character, `{` is special and defines a control sequence
   or a wait.
5. `{Λy}` -- the computer types "Control y".  The first character
   after the brace, Λ (Unicode GREEK CAPITAL LETTER LAMDA), represents
   Control.
6. `{⌘c}` -- the computer types "Command c".  The character after the
   brace, ⌘ (Unicode PLACE OF INTEREST SIGN), represents the Mac
   keyboard's Command key.
7. `{⌥b}` -- the computer types "Alt b".  The first character after
   the brace, ⌥ (Unicode OPTION KEY), represents the Mac keyboard's
   Alt (or Option) key.
8. `{‹}` -- the computer types backspace.  The character after the
   brace, ‹ (Unicode SINGLE LEFT-POINTING ANGLE QUOTATION MARK),
   represents the Mac keyboard's Backspace/Delete key.
9. `{ςd}` -- the computer types "Shift d".  The first character after
   the brace, ς (Unicode GREEK SMALL LETTER FINAL SIGMA), represents
   the keyboard's Shift key.
10. `{→}` -- type the right arrow.  The character after the brace, →
    (Unicode RIGHTWARDS ARROW), represents the right arrow.
11. `{←}` -- type the left arrow.  The character after the brace, →
    (Unicode LEFTWARDS ARROW), represents the left arrow.
12. The following numeric characters add a pause to typing.  They add
    approximately 10*their numeric value in milliseconds plus
    approximately 100ms.  The characters are: ①, ②, ③, ④, ⑤, ⑥, ⑦, ⑧,
    or ⑨.

I went quite overboard with my usage of unicode.  Future versions will
be saner.  I'd prefer if they were just ascii characters (for example:
`{C}` representing Control, `{M}` for Alt, `{H}` for Command, `{S}`
for Shift, and so on.

# The git video
[![git
magic](http://img.youtube.com/vi/esILqJRuvN4/0.jpg)](https://www.youtube.com/watch?v=esILqJRuvN4).

# Licence

The code is available under the Affero GNU GPL v3.0.  Share and share
alike.
