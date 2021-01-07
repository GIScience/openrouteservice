package org.heigit.ors.routing.graphhopper.extensions;

import java.util.HashMap;
import java.util.Map;

public class RoadPropertySpeedMap {
    private Map<PropertyType, Double> propToSpeed = new HashMap();

    public void addRoadPropertySpeed(String property, double speed) {
        SurfaceType surfaceType = SurfaceType.getFromString(property);
        if (surfaceType != SurfaceType.UNKNOWN) {
            propToSpeed.put(surfaceType, speed);
            return;
        }
        WayType wayType = WayType.getFromString(property);
        if (wayType != WayType.UNKNOWN) {
            propToSpeed.put(wayType, speed);
            return;
        }
        throw new IllegalArgumentException("Unknown property type: " + property);
    }

    public Double getByTypedOrdinal(Class<?> type, int ordinal) {
        for (Map.Entry<PropertyType, Double> entry : propToSpeed.entrySet()) {
            if (type.isInstance(entry.getKey()) && entry.getKey().getOrdinal() == ordinal)
                return entry.getValue();
        }
        return null;
    }

    public Double getByTypedOrdinal(Class<?> type, byte ordinal) {
        for (Map.Entry<PropertyType, Double> entry : propToSpeed.entrySet()) {
            if (type.isInstance(entry.getKey()) && entry.getKey().getOrdinal() == (int) ordinal)
                return entry.getValue();
        }
        return null;
    }
}
