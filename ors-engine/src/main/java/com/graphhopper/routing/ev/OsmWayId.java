package com.graphhopper.routing.ev;

public class OsmWayId {
    public static final String KEY = "osm_way_id";

    public static IntEncodedValue create() {
        return new UnsignedIntEncodedValue(KEY, 31, false);
    }
}
