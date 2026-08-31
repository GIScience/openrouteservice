package com.graphhopper.routing.ev;

public class Rest {
    public static final String KEY = "rest";

    private Rest() {
        // prevent instantiation
    }

    public static DecimalEncodedValue create() {
        return new OrsUnsignedDecimalEncodedValue(KEY, 7, 0.01, false);
    }
}
