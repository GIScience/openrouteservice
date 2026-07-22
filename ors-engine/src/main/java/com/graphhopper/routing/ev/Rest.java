package com.graphhopper.routing.ev;

public class Rest {
    public static final String KEY = "rest";

    public static BooleanEncodedValue create() {
        return new SimpleBooleanEncodedValue(KEY, false);
    }
}
