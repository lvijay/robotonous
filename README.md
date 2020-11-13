Robotonous
----------

Autonomous actions on the computer using `java.awt.Robot`.

Have there been times where you'd like to automate many of your typing
tasks?  Have your actions required coordination across windows — type
a few letters here, click on the other window, wait a few seconds,
type some more?  Robotonous ~~is~~ might be for you.

## Robotonous

Since Java 1.3, the Java SDK has had the class
[java.awt.Robot](https://docs.oracle.com/en/java/javase/14/docs/api/java.desktop/java/awt/Robot.html)
that allows mouse control and text entry.  Robot is low level and
clunky to use.  This project attempts to make its use easier.

## Building

```shell
javac -g -cp lib/jsexp-0.2.2.jar:src -d bin src/robotonous/Robotonous.java
```

## Running

Running the program is as shown below.  It expects input as lisp forms
(technically, S-Expressions).  If no file is provided, users are
dropped into a REPL with... odd results.  See
[§Instructions](#instructions) for details.

```shell
java -cp lib/jsexp-0.2.2.jar:bin robotonous.Robotonous instructions.lisp
```

## Instructions

Robotonous expects arguments as lisp forms.  All instructions are
specified as Lisp-style lists.

### Sidebar on Lisp lists
A list in Lisp family of programming languages is anything encased in
parentheses.  For instance, `(a b c d)` is a list with 4 elements:
`a`, `b`, `c`, and `d`.  `(e (f (g h) i) j)` is a list with 3
elements: `e`, `(f (g h) i)`, and `j`.  The second element in this
list is itself a list with 3 elements.  Whitespace between elements is
ignored.

### Robotonous syntax
In Robotonous, instructions to the robot are specified in lists.
Every list starts with a command followed by arguments to that
command. A `command`, which can be typing, moving a mouse, clicking a
mouse, or waiting defines what to do with its arguments.  For example,
if you want to type the programmers favorite string, `hello, world!`,
you specify it with the lisp form, `(:type "hello, world!")`.  Lists
are useful especially for cases where you want to do multiple things
together.  For instance, if you want to press down the keys _Ctrl_,
_Alt_, and _d_, use `(:type ($ctrl ($alt d)))`.  If you want to click
on some location while holding down the _Shift_ key, use `(:type
($shift (:mousemove 100 200) (:mouseclick $left)))`.

Robotonous commands start with `:`.  The following commands are
supported:

- `:type` — types the letters as is.
  + Individual words are typed as they are with whitespace between
    them ignored.  So, `(:type one two Three)` types `onetwoThree`.
  + doublequotes (`"`) and pipes (`|`) are two ways to type including
    whitespace.  So, `(:type "abcd efgh")` types `abcd efgh` and
    `(:type |"AGPLv3" license used here|)` types `"AGPLv3" license
    used here`.  It is also the simplest way to type pipes and
    doublequotes.  See
    [§Whitespace](#whitespace-and-other-special-cases), below for
    details.
  + Since Robot simulates actual key presses, upper case letters are
    typed as if pressing shift followed by the letter.  The following
    examples are all equivalent, feel free to use them
    interchangeably.
    - `(:type ($shift a))` is the same as `(:type A)`
    - `(:type "#(abcd)")` is the same as `(:type ($shift 39) abcd
      (:shift 0)) ` though the former is more convenient.
  + **Subcommands** are ways to type control characters — keys you
    must keep pressed when typing other keys.  These include special
    characters like `!@#$%^` and most keyboard shortcuts.  For
    instance, to press control, alt, and 4, use `($ctrl ($alt 4))`.
    At this time, the following subcommands are supported:
    - $ctrl
    - $alt
    - $meta — The Command key on Mac keyboards and Windows key on
      windows keyboards.
    - $shift
  + **Specials** are ways to type characters that you can't represent
    easily.  These include the arrow keys, the Escape key among
    others.  The following special keys are supported:
    - $left — type the left arrow
    - $right — type the right arrow
    - $up — type the up arrow
    - $down — type the down arrow
    - $backspace
    - $space
    - $enter
    - $home
    - $end
    - $pageup
    - $pagedown
    - $delete
    - $doublequote — type `"` (actually types `SHIFT` followed by `'`)
    - $pipe — type `|`.
- `:mousemove` — moves the mouse to the given x y position.
  `(:mousemove 100 200)` moves the mouse cursor to x=100, y=200.
- `:mouseclick` — clicks the specified mouse key.  The mousekeys are
  defined with
  + $left — left click button
  + $right — right click button
  + $middle — middle click button

  To double click, use `(:mouseclick $left $left)`.  To press both
  left and right buttons together, use `(:mouseclick (:left
  (:right)))`.
- `:delay` — sleeps for milliseconds.  `(:delay 1000)` sleeps for
   1000 milliseconds.
- `:typeline` — same as `:type` and types a newline at the end of all
  its instructions.

Keeping with traditional Lisp convention, all content from a semicolon
until the end of line is ignored as a comment.

### Whitespace and other special cases

As in lisp, Robotonous ignores whitespaces between tokens.  So,
`(:type a b c d)` will type out `abcd`.  There are two ways to respect
whitespaces:

1. Double quotes — The `"` character defines a string.  Content within
   it is treated literally.  For instance, `(:type "a b c d")` will
   type `a b c d`. Commands and subcommands are ignored and treated as
   ordinary characters to be typed out too.  `(:type "$end $(shift
   4)")` literally types `"$end $(shift 4)"`.  Backslashes are treated
   literally too.  `"\n"` is treated as a String of two characters:
   `\` followed by `n`.

   Is there no way to type a double quote, then?  How do you escape
   characters?

   Excellent questions.  I'll answer the latter first.  Robotonous
   provides no means to escape the content of strings.  However,
   alternatives do exist.  That will answer the former question.
   And the answer is
2. pipes — The `|` character also defines a string.  Everything that
   applied to double quotes above also applies to the pipe.  So you
   can type a double quote by encasing it in a pipe like so: `(:type
   |"|)` and you can type a pipe by encasing it in double quotes:
   `(:type "|")`.

   But how do I type `|"`, you ask?  There are several ways to do
   this, some of which are shown below.

```lisp
(:type "|" |"|)               ;; solution 1, ugly, not recommended
(:type $doublequote $pipe)    ;; solution 2
(:type ($shift ') ($shift \)) ;; solution 3
```

## Demo

TBD

# History

Recently, I pulled off a git "magic trick".  It was screen recorded
and shared as a YouTube video.  The entire narration was done typing
on a screen.  Now, I type fast, but it's a different story when
recording for an audience.  I made too many mistakes while recording.
(Performance art is _hard_.  Who knew?)  The software engineer's credo
is, of course, don't do manually what you can make a computer do and
thus was born Robotonous.

## The git video
Click the image below.

[![git
magic](http://img.youtube.com/vi/esILqJRuvN4/0.jpg)](https://www.youtube.com/watch?v=esILqJRuvN4).

# Licence

The code is available under the Affero GNU GPL v3.0.  Share and share
alike.
