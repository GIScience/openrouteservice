package com.graphhopper.routing.ev;

import org.heigit.ors.routing.graphhopper.extensions.WheelchairAttributes;

// Side of the sidewalk, if attached.
public class WheelchairSide {
    public static final String KEY = "wheelchair_side";

    public static EnumEncodedValue<WheelchairAttributes.Side> create() {
        return new EnumEncodedValue<>(KEY, WheelchairAttributes.Side.class);
    }
}
