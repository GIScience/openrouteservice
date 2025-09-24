package com.graphhopper.routing.ev;

public class Ford {
    public static final String KEY = "ford";

    public static BooleanEncodedValue create() {
        return new SimpleBooleanEncodedValue(KEY, false);
    }
}
