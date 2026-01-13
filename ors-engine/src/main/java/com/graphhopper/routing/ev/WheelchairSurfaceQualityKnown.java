package com.graphhopper.routing.ev;

public class WheelchairSurfaceQualityKnown {
    public static final String KEY = "wheelchair_surface_quality_known";

    private WheelchairSurfaceQualityKnown() {
        // Private constructor to prevent instantiation
    }

    public static SimpleBooleanEncodedValue create() {
        return new SimpleBooleanEncodedValue(KEY, false);
    }
}
