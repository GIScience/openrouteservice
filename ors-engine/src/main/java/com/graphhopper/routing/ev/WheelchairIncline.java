package com.graphhopper.routing.ev;

/**
 * EncodedValue for wheelchair incline attribute.
 * Stores incline percentage as a signed integer value (range: -50 to +50).
 */
public class WheelchairIncline {
    public static final String KEY = "wheelchair_incline";

    private WheelchairIncline() {
        // Private constructor to prevent instantiation
    }

    public static IntEncodedValue create() {
        // 6 bits signed to store 0% to +50% incline
        return new UnsignedIntEncodedValue(KEY, 6, false) {
        };
    }
}