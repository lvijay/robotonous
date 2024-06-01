import pyrobotonous as pyr
from pyrobotonous import Typewrite, Sleep, Presskey, CopyPaste


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


## test_pyrobotonous.py ends here
