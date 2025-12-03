package com.graphhopper.routing.ev;

public class GoodsAccess {
    public static final String KEY = "goods_access";

    private GoodsAccess() {
        // do not instantiate
    }

    public static BooleanEncodedValue create() {
        return new SimpleBooleanEncodedValue(KEY, false);
    }
}
