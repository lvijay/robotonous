package com.lvijay.robotonous;

public interface KeyEventSequencer {
    Action toKeyEvent(char c) throws IllegalArgumentException;
}
