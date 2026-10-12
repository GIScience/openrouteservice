package com.graphhopper.routing.ev;

public class WheelchairSurfaceQualityKnown {
    private WheelchairSurfaceQualityKnown() {
        // do not instantiate
    }

    public static final String KEY = "wheelchair_surface_quality_known";

    public static SimpleBooleanEncodedValue create() {
        return new SimpleBooleanEncodedValue(KEY, false);
    }
}
