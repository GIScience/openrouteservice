package org.heigit.ors.routing.graphhopper.extensions.userspeed;

import org.heigit.ors.routing.graphhopper.extensions.PropertyType;
import org.heigit.ors.routing.graphhopper.extensions.SurfaceType;
import org.heigit.ors.routing.graphhopper.extensions.WayType;

import java.util.HashMap;
import java.util.Map;

public class RoadPropertySpeedMap {
    private Map<PropertyType, Double> propToSpeed = new HashMap();

    /**
     * Add a maximum speed for a road property. Properties can be SurfaceType or WayType, defined in resp. files.
     * @param property The property affected by the speed
     * @param speed The maximum speed for this property
     */
    public void addRoadPropertySpeed(String property, double speed) {
        if(speed < 0) {
            throw new IllegalArgumentException("Speed must be >= 0 but is " + speed);
        }
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

    /**
     * Get speed for property by ordinal of specified type
     * @param type Class of property. Currently surfacetype and waytye
     * @param ordinal ordinal of enum
     * @return resp. speed
     */
    public Double getByTypedOrdinal(Class<?> type, int ordinal) {
        for (Map.Entry<PropertyType, Double> entry : propToSpeed.entrySet()) {
            if (type.isInstance(entry.getKey()) && entry.getKey().getOrdinal() == ordinal)
                return entry.getValue();
        }
        return null;
    }

    /**
     * Get speed for property by ordinal of specified type
     * @param type Class of property. Currently surfacetype and waytye
     * @param ordinal ordinal of enum
     * @return resp. speed
     */
    public Double getByTypedOrdinal(Class<?> type, byte ordinal) {
        for (Map.Entry<PropertyType, Double> entry : propToSpeed.entrySet()) {
            if (type.isInstance(entry.getKey()) && entry.getKey().getOrdinal() == (int) ordinal)
                return entry.getValue();
        }
        return null;
    }
}
