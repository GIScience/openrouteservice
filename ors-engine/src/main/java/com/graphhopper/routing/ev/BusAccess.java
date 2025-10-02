package com.graphhopper.routing.ev;

public class BusAccess {
    public static final String KEY = "bus_access";

    private BusAccess() {
        // do not instantiate
    }

    public static BooleanEncodedValue create() {
        return new SimpleBooleanEncodedValue(KEY, false);
    }
}
