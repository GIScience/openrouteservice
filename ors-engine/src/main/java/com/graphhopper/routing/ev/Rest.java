package com.graphhopper.routing.ev;

public class Rest {
    public static final String KEY = "rest";

    public static DecimalEncodedValue create() {
        return new OrsUnsignedDecimalEncodedValue(KEY, 7, 0.01, false);
    }
}
