package com.graphhopper.routing.ev;

public class HgvAccess {
    public static final String KEY = "hgv_access";

    private HgvAccess() {
        // do not instantiate
    }

    public static BooleanEncodedValue create() {
        return new SimpleBooleanEncodedValue(KEY, false);
    }
}
