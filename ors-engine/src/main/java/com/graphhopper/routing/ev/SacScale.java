package com.graphhopper.routing.ev;

public class SacScale {
    public static final String KEY = "sac_scale";

    private SacScale() {
        // prevent instantiation
    }

    public static IntEncodedValue create() {
        return new UnsignedIntEncodedValue(KEY, 3, false);
    }
}
