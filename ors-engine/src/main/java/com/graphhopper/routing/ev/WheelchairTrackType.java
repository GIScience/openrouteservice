package com.graphhopper.routing.ev;

public class WheelchairTrackType {
    private WheelchairTrackType() {
        // do not instantiate
    }

    public static final String KEY = "wheelchair_track_type";

    public static IntEncodedValue create() {
        return new UnsignedIntEncodedValue(KEY, 3, false);
    }
}
