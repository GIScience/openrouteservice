package com.graphhopper.routing.ev;

public class DeliveryAccess {
    public static final String KEY = "delivery_access";

    private DeliveryAccess() {
        // do not instantiate
    }

    public static BooleanEncodedValue create() {
        return new SimpleBooleanEncodedValue(KEY, false);
    }
}
