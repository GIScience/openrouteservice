package com.graphhopper.routing.ev;

public class MtbScaleUphill {
    public static final String KEY = "mtb_scale_uphill";

    private MtbScaleUphill() {
        // prevent instantiation
    }

    public static IntEncodedValue create() {
        return new UnsignedIntEncodedValue(KEY, 3, false);
    }
}
