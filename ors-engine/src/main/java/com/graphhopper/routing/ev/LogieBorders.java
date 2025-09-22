package com.graphhopper.routing.ev;

public enum LogieBorders {
    UNKNOWN(0),
    OPEN(1),
    RESTRICTED(2),
    CLOSED(3),
    UNSPECIFIED(4);


    public static final String KEY = "logie_borders";

    private final byte value;

    LogieBorders(int value) {
        this.value = (byte) value;
    }

    public byte value() {
        return value;
    }

}
