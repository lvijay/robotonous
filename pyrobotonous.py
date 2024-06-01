#!/usr/bin/env python
# -*- coding: utf-8 -*-

from dataclasses import dataclass
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
class Withchord:
    key: str
    action: Action

    def __call__(self) -> None:
        with pyautogui.hold(self.key):
            self.action()


@dataclass
class Presskey(Action):
    key: str

    def __call__(self) -> None:
        for k in self.key:
            pyautogui.press(k)


@dataclass
class CopyPaste(Action):
    text: str

    def __call__(self) -> None:
        pyperclip.copy(self.text)
        pyperclip.paste()


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
            elif c == self.chord[0] or c == self.chord[1]:
                pass
            elif c == self.copypaste[0]:
                i += 1
                pastendidx = contents.find(self.copypaste[1], i)
                pastestr = contents[i:pastendidx]
                actions.append(CopyPaste(pastestr))
                i = pastendidx
            elif c == self.newline:
                actions.append(Typewrite("\n"))
            elif c == self.tab:
                actions.append(Typewrite("\t"))
            elif c == self.escape:
                actions.append(Presskey(key="esc"))
            elif c == self.backspace:
                actions.append(Presskey(key="backspace"))
            elif c == self.delete:
                actions.append(Presskey(key="delete"))
            elif c == self.arrow_left:
                actions.append(Presskey(key="left"))
            elif c == self.arrow_right:
                actions.append(Presskey(key="right"))
            elif c == self.arrow_down:
                actions.append(Presskey(key="down"))
            elif c == self.arrow_up:
                actions.append(Presskey(key="up"))
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
