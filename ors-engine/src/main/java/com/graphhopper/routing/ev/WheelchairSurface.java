package com.graphhopper.routing.ev;

public class WheelchairSurface {
    private WheelchairSurface() {
        // do not instantiate
    }

    public static final String KEY = "wheelchair_surface";

    public static IntEncodedValue create() {
        return new UnsignedIntEncodedValue(KEY, 5, false);
    }
}
