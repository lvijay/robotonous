import pyrobotonous as pyr
from pyrobotonous import (
    Chord,
    CopyPaste,
    Presskey,
    Sleep,
    Typewrite,
)


def test_simplify():
    v = pyr.simplify([Typewrite("ab"), Typewrite("cde"), Sleep(3), Sleep(40)])
    assert v == [Typewrite("abcde"), Sleep(43)]


def test_parse():
    pl = pyr.PyroLex()
    parsed = pl.parse("hello, ⊂x**⊂88⊃©wo\nrld㉚⑧")
    assert parsed == [
        Typewrite(text="hello, "),
        CopyPaste(text="x**⊂88"),
        Typewrite(text="rld"),
        Sleep(38),
    ]

    chordparse = pl.parse("«ca:staway»")
    assert chordparse == [Chord(commands=["ctrl", "alt"], text="staway")]

    specialparse = pl.parse("⏎‹↓")
    assert specialparse == [
        Typewrite(text="\n"),
        Presskey(["backspace", "down"]),
    ]


def test_parse_negative():
    pl = pyr.PyroLex()
    try:
        pl.parse("hello,⊃ x")  # unexpected character
        assert False
    except ValueError as e:
        assert e.args[0] == "Error at index 6, unknown char ⊃"

    ## many other negative tests TBD


## test_pyrobotonous.py ends here
