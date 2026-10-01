package com.graphhopper.routing.ev;

/**
 * EncodedValue for wheelchair suitable attribute.
 */
public class WheelchairSuitable {
    private WheelchairSuitable() {
        // do not instantiate
    }

    public static final String KEY = "wheelchair_suitable";

    public static SimpleBooleanEncodedValue create() {
        return new SimpleBooleanEncodedValue(KEY);
    }
}

