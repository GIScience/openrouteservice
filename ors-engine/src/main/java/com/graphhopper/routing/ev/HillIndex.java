package com.graphhopper.routing.ev;

public class HillIndex {
    public static final String KEY = "hill_index";

    private HillIndex() {
        // prevent instantiation
    }

    public static IntEncodedValue create() {
        return new UnsignedIntEncodedValue(KEY, 6, true);
    }
}
