package com.graphhopper.routing.ev;

public class PointData {
    public static final String KEY = "point_data";

    public static DecimalEncodedValue create() {
        return new UnsignedDecimalEncodedValue(KEY, 31, 0.01, false);
    }
}
