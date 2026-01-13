package com.graphhopper.routing.ev;

/**
 * EncodedValue for wheelchair kerb/curb height attribute.
 * Stores kerb height in centimeters as an unsigned integer value (range: 0-30cm).
 */
public class WheelchairKerb {
    public static final String KEY = "wheelchair_kerb";

    private WheelchairKerb() {
        // Private constructor to prevent instantiation
    }

    public static IntEncodedValue create() {
        // 6 bits unsigned to store 0-30cm kerb height
        return new UnsignedIntEncodedValue(KEY, 6, false);
    }
}

