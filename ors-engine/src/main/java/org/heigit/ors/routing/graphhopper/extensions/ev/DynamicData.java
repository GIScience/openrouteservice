package org.heigit.ors.routing.graphhopper.extensions.ev;

import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.SimpleBooleanEncodedValue;

public class DynamicData {
    public static final String KEY = "dynamic_data";

    public static BooleanEncodedValue create() {
        return new SimpleBooleanEncodedValue(KEY, false);
    }
}
