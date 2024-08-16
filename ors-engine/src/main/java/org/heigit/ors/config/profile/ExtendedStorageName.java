package org.heigit.ors.config.profile;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum ExtendedStorageName {
    BORDERS("Borders"),
    CSV("csv"),
    GREEN_INDEX("GreenIndex"),
    HEAVY_VEHICLE("HeavyVehicle"),
    HERE_TRAFFIC("HereTraffic"),
    HILL_INDEX("HillIndex"),
    NOISE_INDEX("NoiseIndex"),
    OSM_ID("OsmId"),
    ROAD_ACCESS_RESTRICTIONS("RoadAccessRestrictions"),
    SHADOW_INDEX("ShadowIndex"),
    TOLLWAYS("TollWays"),
    TRAIL_DIFFICULTY("TrailDifficulty"),
    WAY_CATEGORY("WayCategory"),
    WAY_SURFACE_TYPE("WaySurfaceType"),
    WHEELCHAIR("Wheelchair");

    final String name;

    @JsonCreator
    ExtendedStorageName(String wayCategory) {
        this.name = wayCategory;
    }

    public static ExtendedStorageName getEnum(String value) {
        for (ExtendedStorageName v : values())
            if (v.getValue().equalsIgnoreCase(value)) return v;
        throw new IllegalArgumentException();
    }

    public String getValue() {
        return name;
    }

    @Override
    public String toString() {
        return this.getValue();
    }
}
