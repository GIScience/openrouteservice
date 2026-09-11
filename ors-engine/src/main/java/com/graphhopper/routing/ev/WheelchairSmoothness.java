package com.graphhopper.routing.ev;

public class WheelchairSmoothness {
    private WheelchairSmoothness() {
        // do not instantiate
    }

    public static final String KEY = "wheelchair_smoothness";

    public static IntEncodedValue create() {
        return new UnsignedIntEncodedValue(KEY, 4, false);
    }
}
