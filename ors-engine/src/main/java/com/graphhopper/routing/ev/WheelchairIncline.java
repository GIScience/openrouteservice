package com.graphhopper.routing.ev;

/**
 * EncodedValue for wheelchair incline attribute.
 * Stores incline percentage as a 5-bit unsigned integer value (range: 0 to 30).
 */
public class WheelchairIncline {
    public static final String KEY = "wheelchair_incline";

    private WheelchairIncline() {
        // Private constructor to prevent instantiation
    }

    public static IntEncodedValue create() {
        // 6 bits signed to store 0% to +30% incline
        return new UnsignedIntEncodedValue(KEY, 5, false) {
        };
    }
}