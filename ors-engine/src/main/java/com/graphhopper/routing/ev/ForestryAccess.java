package com.graphhopper.routing.ev;

public class ForestryAccess {
    public static final String KEY = "forestry_access";

    private ForestryAccess() {
        // do not instantiate
    }

    public static BooleanEncodedValue create() {
        return new SimpleBooleanEncodedValue(KEY, false);
    }
}
