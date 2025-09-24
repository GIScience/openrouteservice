package com.graphhopper.routing.ev;

public class Highway {
    public static final String KEY = "highway";

    public static BooleanEncodedValue create() {
        return new SimpleBooleanEncodedValue(KEY, true);
    }
}
