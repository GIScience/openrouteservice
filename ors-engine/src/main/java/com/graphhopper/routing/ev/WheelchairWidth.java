package com.graphhopper.routing.ev;

/**
 * EncodedValue for wheelchair width attribute.
 * Stores width in centimeters as an unsigned integer value (range: 0-500cm).
 */
public class WheelchairWidth {
    public static final String KEY = "wheelchair_width";

    private WheelchairWidth() {
        // Private constructor to prevent instantiation
    }

    public static IntEncodedValue create() {
        // 9 bits unsigned to store 0-500cm (0-5 meters)
        return new UnsignedIntEncodedValue(KEY, 9, false);
    }
}

