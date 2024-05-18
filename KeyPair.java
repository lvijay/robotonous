package com.lvijay.robotonous;

public record KeyPair(char open, char close) {
    public static KeyPair fromString(String v) {
        if (v.length() != 2) { throw new IllegalArgumentException(); }

        return new KeyPair(v.charAt(0), v.charAt(1));
    }
}
