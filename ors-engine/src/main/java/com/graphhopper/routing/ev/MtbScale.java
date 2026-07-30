package com.graphhopper.routing.ev;

public class MtbScale {
    public static final String KEY = "mtb_scale";

    private MtbScale() {
        // prevent instantiation
    }

    public static IntEncodedValue create() {
        return new UnsignedIntEncodedValue(KEY, 3, false);
    }
}
