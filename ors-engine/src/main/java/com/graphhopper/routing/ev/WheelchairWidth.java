package com.graphhopper.routing.ev;

/**
 * EncodedValue for wheelchair width attribute.
 * Stores width in centimeters as an unsigned integer value (range: 0-500cm).
 */
public class WheelchairWidth {
    private WheelchairWidth() {
        // do not instantiate
    }

    public static final String KEY = "wheelchair_width";

    public static IntEncodedValue create() {
        // 9 bits unsigned to store 0-500cm (0-5 meters)
        return new UnsignedIntEncodedValue(KEY, 9, false);
    }
}

