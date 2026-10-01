package com.graphhopper.routing.ev;

/**
 * EncodedValue for wheelchair width attribute.
 * Stores width in centimeters as an unsigned decimal value with 10 cm precision (range: 0-300cm).
 */
public class WheelchairWidth {
    private WheelchairWidth() {
        // do not instantiate
    }

    public static final String KEY = "wheelchair_width";

    public static UnsignedDecimalEncodedValue create() {
        // 5 bits unsigned to store 0-300cm (0-3 meters) using 10 cm precision.
        return new UnsignedDecimalEncodedValue(KEY, 5, 10, 0.0, false);
    }
}

