#!/usr/bin/env python
# -*- coding: utf-8 -*-

from dataclasses import dataclass, field
from functools import reduce
import re

import pyautogui
import pyperclip


class Action:
    def __call__(self) -> None:
        pass


@dataclass
class Typewrite(Action):
    text: str

    def __call__(self) -> None:
        pyautogui.write(self.text)


@dataclass
class Chord:
    commands: str
    text: str

    def __call__(self) -> None:
        with pyautogui.hold(self.commands):
            pyautogui.typewrite(self.text)

    @staticmethod
    def make_chord(commands, text):
        cdict = {"c": "ctrl", "a": "alt", "s": "shift", "m": "command"}
        cmds = [cdict[c] for c in commands]
        return Chord(cmds, text)


@dataclass
class Presskey(Action):
    key: list[str]

    def __post_init__(self):
        if type(self.key) == str:
            self.key = [self.key]

    def __call__(self) -> None:
        pyautogui.press(self.key)


@dataclass
class CopyPaste(Action):
    text: str
    paste_chord: Chord = field(default_factory=lambda: Chord.make_chord("m", "v"))

    def __call__(self) -> None:
        pyperclip.copy(self.text)
        self.paste_chord()


@dataclass
class Sleep(Action):
    time: int

    def __call__(self) -> None:
        pyautogui.sleep(self.time / 10)

    @staticmethod
    def is_sleep_char(c: str) -> bool:
        return (
            (c >= "①" and c <= "⑳")
            or (c >= "㉑" and c <= "㉟")
            or (c >= "㊱" and c <= "㊿")
        )

    @staticmethod
    def make_sleep(c: str):
        if c >= "①" and c <= "⑳":
            diff = ord(c) - ord("①")
            v = diff + 1
        elif c >= "㉑" and c <= "㉟":
            diff = ord(c) - ord("㉑")
            v = diff + 21
        elif c >= "㊱" and c <= "㊿":
            diff = ord(c) - ord("㊱")
            v = diff + 36
        else:
            raise ValueError(f"Cannot understand value `{c}'")
        return Sleep(v)


def simplify(actions: list[Action]) -> list[Action]:
    # merge adjacent instances that are together
    acc = [actions[0]]
    typeflds = {
        (Typewrite, "text"),
        (Presskey, "key"),
        (CopyPaste, "text"),
        (Sleep, "time"),
    }
    types = {t for t, _ in typeflds}
    for axn in actions[1:]:
        prev = acc[-1]
        if type(prev) == type(axn) and type(prev) in types:
            typ, fld = next(((t, f) for t, f in typeflds if t == type(prev)))
            oval = acc.pop().__dict__[fld]
            nval = axn.__dict__[fld]
            acc.append(typ(oval + nval))
        else:
            acc.append(axn)
    return acc


@dataclass
class PyroLex:
    comment: str = "©"
    chord: str = "«»"
    copypaste: str = "⊂⊃"
    newline: str = "⏎"
    tab: str = "␉"
    escape: str = "␛"
    backspace: str = "‹"
    delete: str = "›"
    arrow_left: str = "←"
    arrow_right: str = "→"
    arrow_down: str = "↓"
    arrow_up: str = "↑"
    mouse_move: str = "∏∐"
    mouse_left: str = "┫┛"
    mouse_right: str = "┣┗"

    def __post_init__(self):
        self.__extrakeymap: dict[str, Presskey | Typewrite] = {
            self.newline:     Typewrite("\n"),
            self.tab:         Typewrite("\t"),
            self.escape:      Presskey(key="esc"),
            self.backspace:   Presskey(key="backspace"),
            self.delete:      Presskey(key="delete"),
            self.arrow_left:  Presskey(key="left"),
            self.arrow_right: Presskey(key="right"),
            self.arrow_up:    Presskey(key="up"),
            self.arrow_down:  Presskey(key="down"),
        }

    def parse(self, contents: str) -> list[Action]:
        actions: list[Action] = []
        mouseLeftActive = False
        mouseRightActive = False
        i = 0
        while i < len(contents):
            c = contents[i]
            if c == self.comment:
                eolidx = contents.find("\n", i)
                i = eolidx
            elif c == self.chord[0]:
                i += 1
                chordendidx = contents.find(self.chord[1], i)
                chordstr = contents[i:chordendidx]
                chordsepidx = chordstr.find(":")
                chordcmds = chordstr[:chordsepidx]
                chordtext = chordstr[chordsepidx+1:]
                actions.append(Chord.make_chord(chordcmds, chordtext))
                i = chordendidx
            elif c == self.copypaste[0]:
                i += 1
                pastendidx = contents.find(self.copypaste[1], i)
                pastestr = contents[i:pastendidx]
                actions.append(CopyPaste(pastestr))
                i = pastendidx
            elif c in self.__extrakeymap.keys():
                actions.append(self.__extrakeymap[c])
            elif c == self.mouse_move[0] or c == self.mouse_move[1]:
                pass
            elif c == self.mouse_left[0] or c == self.mouse_left[1]:
                pass
            elif c == self.mouse_right[0] or c == self.mouse_right[1]:
                pass
            elif Sleep.is_sleep_char(c):
                actions.append(Sleep.make_sleep(c))
            else:  # just type the damn thing
                actions.append(Typewrite(c))
            i += 1
        return simplify(actions)


def parse(contents: str) -> list[Action]:
    pl = PyroLex()  # TODO: header parsing
    return pl.parse(contents)


def execute(steps: list[Action]) -> None:
    for step in steps:
        step()


if __name__ == "__main__":
    import sys

    pyrofile = sys.argv[1]
    with open(pyrofile, "r") as fin:
        steps = parse(fin.read())
    execute(steps)


## pyrobotonous.py ends here
