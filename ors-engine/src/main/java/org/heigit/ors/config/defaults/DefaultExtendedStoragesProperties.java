package org.heigit.ors.config.defaults;

import lombok.Getter;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.storages.ExtendedStorage;
import org.heigit.ors.config.profile.storages.ExtendedStorageName;

import java.util.HashMap;
import java.util.Map;

@Getter
public class DefaultExtendedStoragesProperties {

    Map<String, ExtendedStorage> extStorages;

    public DefaultExtendedStoragesProperties() {
        extStorages = new HashMap<>();
    }

    public DefaultExtendedStoragesProperties(EncoderNameEnum encoderName) {
        this();
        if (encoderName == null) {
            encoderName = EncoderNameEnum.DEFAULT;
        }

        switch (encoderName) {
            case DRIVING_CAR -> {
                extStorages.put("WayCategory", new ExtendedStorage(ExtendedStorageName.WAY_CATEGORY));
                extStorages.put("WaySurfaceType", new ExtendedStorage(ExtendedStorageName.WAY_SURFACE_TYPE));
                extStorages.put("HeavyVehicle", new ExtendedStorage(ExtendedStorageName.HEAVY_VEHICLE));
                extStorages.put("RoadAccessRestrictions", new ExtendedStorage(ExtendedStorageName.ROAD_ACCESS_RESTRICTIONS));
                extStorages.put("Tollways", new ExtendedStorage(ExtendedStorageName.TOLLWAYS));
            }
            case DRIVING_HGV -> {
                extStorages.put("WayCategory", new ExtendedStorage(ExtendedStorageName.WAY_CATEGORY));
                extStorages.put("WaySurfaceType", new ExtendedStorage(ExtendedStorageName.WAY_SURFACE_TYPE));
                extStorages.put("HeavyVehicle", new ExtendedStorage(ExtendedStorageName.HEAVY_VEHICLE));
                extStorages.put("Tollways", new ExtendedStorage(ExtendedStorageName.TOLLWAYS));
            }
            case CYCLING_REGULAR, CYCLING_MOUNTAIN, CYCLING_ROAD, CYCLING_ELECTRIC, FOOT_WALKING, FOOT_HIKING -> {
                extStorages.put("WayCategory", new ExtendedStorage(ExtendedStorageName.WAY_CATEGORY));
                extStorages.put("WaySurfaceType", new ExtendedStorage(ExtendedStorageName.WAY_SURFACE_TYPE));
                extStorages.put("HillIndex", new ExtendedStorage(ExtendedStorageName.HILL_INDEX));
                extStorages.put("TrailDifficulty", new ExtendedStorage(ExtendedStorageName.TRAIL_DIFFICULTY));
            }
            case WHEELCHAIR -> {
                extStorages.put("WayCategory", new ExtendedStorage(ExtendedStorageName.WAY_CATEGORY));
                extStorages.put("WaySurfaceType", new ExtendedStorage(ExtendedStorageName.WAY_SURFACE_TYPE));
                extStorages.put("Wheelchair", new ExtendedStorage(ExtendedStorageName.WHEELCHAIR));
                extStorages.put("OsmId", new ExtendedStorage(ExtendedStorageName.OSM_ID));
            }
            case PUBLIC_TRANSPORT -> {
            }
            default -> {
                extStorages.put("WayCategory", new ExtendedStorage(ExtendedStorageName.WAY_CATEGORY));
                extStorages.put("WaySurfaceType", new ExtendedStorage(ExtendedStorageName.WAY_SURFACE_TYPE));
            }
        }
    }

}
