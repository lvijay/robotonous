package com.lvijay.robotonous;

import static java.awt.event.KeyEvent.VK_0;
import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_ALT;
import static java.awt.event.KeyEvent.VK_BACK_QUOTE;
import static java.awt.event.KeyEvent.VK_BACK_SLASH;
import static java.awt.event.KeyEvent.VK_BACK_SPACE;
import static java.awt.event.KeyEvent.VK_CLOSE_BRACKET;
import static java.awt.event.KeyEvent.VK_COMMA;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_DELETE;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_EQUALS;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_META;
import static java.awt.event.KeyEvent.VK_MINUS;
import static java.awt.event.KeyEvent.VK_OPEN_BRACKET;
import static java.awt.event.KeyEvent.VK_PERIOD;
import static java.awt.event.KeyEvent.VK_QUOTE;
import static java.awt.event.KeyEvent.VK_SEMICOLON;
import static java.awt.event.KeyEvent.VK_SHIFT;
import static java.awt.event.KeyEvent.VK_SLASH;
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.awt.event.KeyEvent.VK_TAB;

public record KeyEventSequencerQwerty(SpecialKeys keys)
        implements KeyEventSequencer {
    @Override
    public Action toKeyEvent(char c) {
        if (c >= 'a' && c <= 'z') {
            int diff = c - 'a';
            return new Action(VK_A + diff);
        }

        if (c >= '0' && c <= '9') {
            int diff = c - '0';
            return new Action(VK_0 + diff);
        }

        if (c >= 'A' && c <= 'Z') {
            var lc = Character.toLowerCase(c);
            int[] arguments = toKeyEvent(lc).arguments();
            return new Action(VK_SHIFT, arguments[0]);
        }

        if (c >= '①' && c <= '⑳') { // 9312...9331
            int diff = c - '①';
            int val = diff + 1;
            return new Action(Event.DELAY, val);
        }

        if (c >= '㉑' && c <= '㉟') { // 12881...12895
            int diff = c - '㉑';
            int val = diff + 21;
            return new Action(Event.DELAY, val);
        }

        if (c >= '㊱' && c <= '㊿') { // 12977...12991
            int diff = c - '㊱';
            int val = diff + 36;
            return new Action(Event.DELAY, val);
        }

        if (c == keys.keyControl())   { return new Action(VK_CONTROL);    }
        if (c == keys.keyAlt())       { return new Action(VK_ALT);        }
        if (c == keys.keyMeta())      { return new Action(VK_META);       }
        if (c == keys.keyShift())     { return new Action(VK_SHIFT);      }
        if (c == keys.keyBackspace()) { return new Action(VK_BACK_SPACE); }
        if (c == keys.keyDelete())    { return new Action(VK_DELETE);     }
        if (c == keys.keyEscape())    { return new Action(VK_ESCAPE);     }
        if (c == keys.keyReturn())    { return new Action(VK_ENTER);      }
        if (c == keys.keyTab())       { return new Action(VK_TAB);        }

        return switch (c) {
            case '\n'-> new Action(VK_ENTER);
            case '\t'-> new Action(VK_TAB);
            case ' ' -> new Action(VK_SPACE);
            case '!' -> new Action(VK_SHIFT, toKeyEvent('1').arguments()[0]);
            case '@' -> new Action(VK_SHIFT, toKeyEvent('2').arguments()[0]);
            case '#' -> new Action(VK_SHIFT, toKeyEvent('3').arguments()[0]);
            case '$' -> new Action(VK_SHIFT, toKeyEvent('4').arguments()[0]);
            case '%' -> new Action(VK_SHIFT, toKeyEvent('5').arguments()[0]);
            case '^' -> new Action(VK_SHIFT, toKeyEvent('6').arguments()[0]);
            case '&' -> new Action(VK_SHIFT, toKeyEvent('7').arguments()[0]);
            case '*' -> new Action(VK_SHIFT, toKeyEvent('8').arguments()[0]);
            case '(' -> new Action(VK_SHIFT, toKeyEvent('9').arguments()[0]);
            case ')' -> new Action(VK_SHIFT, toKeyEvent('0').arguments()[0]);
            case '\''-> new Action(VK_QUOTE);
            case '"' -> new Action(VK_SHIFT, VK_QUOTE);
            case ',' -> new Action(VK_COMMA);
            case ';' -> new Action(VK_SEMICOLON);
            case ':' -> new Action(VK_SHIFT, VK_SEMICOLON);
            case '=' -> new Action(VK_EQUALS);
            case '+' -> new Action(VK_SHIFT, VK_EQUALS);
            case '.' -> new Action(VK_PERIOD);
            case '>' -> new Action(VK_SHIFT, VK_PERIOD);
            case '/' -> new Action(VK_SLASH);
            case '?' -> new Action(VK_SHIFT, VK_SLASH);
            case '[' -> new Action(VK_OPEN_BRACKET);
            case '{' -> new Action(VK_SHIFT, VK_OPEN_BRACKET);
            case ']' -> new Action(VK_CLOSE_BRACKET);
            case '}' -> new Action(VK_SHIFT, VK_CLOSE_BRACKET);
            case '-' -> new Action(VK_MINUS);
            case '_' -> new Action(VK_SHIFT, VK_MINUS);
            case '\\'-> new Action(VK_BACK_SLASH);
            case '|' -> new Action(VK_SHIFT, VK_BACK_SLASH);
            case '`' -> new Action(VK_BACK_QUOTE);
            case '~' -> new Action(VK_SHIFT, VK_BACK_QUOTE);
            default ->
                throw new IllegalArgumentException("Unknown character: " + c);
        };
    }
}
