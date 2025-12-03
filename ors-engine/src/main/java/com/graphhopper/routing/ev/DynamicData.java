package com.graphhopper.routing.ev;

/**
 * This class is just an example for showing how to use
 * encoded values dynamically.
 */
public class DynamicData {
    public static final String KEY = "dynamic_data";

    public static BooleanEncodedValue create() {
        return new SimpleBooleanEncodedValue(KEY, false);
    }

    private DynamicData() {
        // hide implicit constructor
    }
}
