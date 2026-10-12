package com.graphhopper.routing.ev;

/**
 * EncodedValue for wheelchair incline attribute.
 * Stores incline percentage as a 4-bit unsigned integer value (range: 0 to 15, see WheelchairInclineParser.INCLINE_MAX_VALUE).
 */
public class WheelchairIncline {
    public static final String KEY = "wheelchair_incline";

    private WheelchairIncline() {
        // Private constructor to prevent instantiation
    }

    public static IntEncodedValue create() {
        // 4 bits unsigned to store 0% to +15% incline
        return new UnsignedIntEncodedValue(KEY, 4, false) {
        };
    }
}