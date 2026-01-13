package com.graphhopper.routing.ev;

/**
 * EncodedValue for wheelchair suitable attribute.
 */
public class WheelchairSuitable {
    public static final String KEY = "wheelchair_suitable";

    private WheelchairSuitable() {
        // Private constructor to prevent instantiation
    }

    public static SimpleBooleanEncodedValue create() {
        return new SimpleBooleanEncodedValue(KEY);
    }
}

