package com.graphhopper.routing.ev;

public class AgriculturalAccess {
    public static final String KEY = "agricultural_access";

    private AgriculturalAccess() {
        // do not instantiate
    }

    public static BooleanEncodedValue create() {
        return new SimpleBooleanEncodedValue(KEY, false);
    }
}
