package com.graphhopper.routing.ev;

public class WheelchairSmoothness {
    public static final String KEY = "wheelchair_smoothness";

    public static IntEncodedValue create() {
        return new UnsignedIntEncodedValue(KEY, 4, false);
    }
}
