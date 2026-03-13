package com.graphhopper.routing.ev;

public class WheelchairSurface {
    public static final String KEY = "wheelchair_surface";

    public static IntEncodedValue create() {
        return new UnsignedIntEncodedValue(KEY, 5, false);
    }
}
