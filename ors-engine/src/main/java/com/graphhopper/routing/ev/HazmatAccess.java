package com.graphhopper.routing.ev;

public class HazmatAccess {
    public static final String KEY = "hazmat_access";

    private HazmatAccess() {
        // do not instantiate
    }

    public static BooleanEncodedValue create() {
        return new SimpleBooleanEncodedValue(KEY, false);
    }
}
