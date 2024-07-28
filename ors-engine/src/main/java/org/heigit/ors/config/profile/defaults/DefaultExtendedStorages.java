package org.heigit.ors.config.profile.defaults;

import lombok.Getter;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.storages.*;

import java.util.HashMap;
import java.util.Map;

@Getter
public class DefaultExtendedStorages {

    Map<String, ExtendedStorage> extStorages;

    public DefaultExtendedStorages() {
        extStorages = new HashMap<>();
        extStorages.put("WayCategory", new ExtendedStorageWayCategory());
        extStorages.put("WaySurfaceType", new ExtendedStorageWaySurfaceType());
    }

    public DefaultExtendedStorages(EncoderNameEnum encoderName) {
        this();
        if (encoderName == null) {
            encoderName = EncoderNameEnum.UNKNOWN;
        }

        switch (encoderName) {
            case DRIVING_CAR -> {
                extStorages.put("WayCategory", new ExtendedStorageWayCategory());
                extStorages.put("WaySurfaceType", new ExtendedStorageWaySurfaceType());
                extStorages.put("HeavyVehicle", new ExtendedStorageHeavyVehicle());
                extStorages.put("RoadAccessRestrictions", new ExtendedStorageRoadAccessRestrictions(true));
            }
            case DRIVING_HGV -> {
                extStorages.put("WayCategory", new ExtendedStorageWayCategory());
                extStorages.put("WaySurfaceType", new ExtendedStorageWaySurfaceType());
                extStorages.put("HeavyVehicle", new ExtendedStorageHeavyVehicle(true));
            }
            case CYCLING_REGULAR, CYCLING_MOUNTAIN, CYCLING_ROAD, CYCLING_ELECTRIC, FOOT_WALKING, FOOT_HIKING -> {
                extStorages.put("WayCategory", new ExtendedStorageWayCategory());
                extStorages.put("WaySurfaceType", new ExtendedStorageWaySurfaceType());
                extStorages.put("HillIndex", new ExtendedStorageHillIndex());
                extStorages.put("TrailDifficulty", new ExtendedStorageTrailDifficulty());
            }
            case WHEELCHAIR -> {
                extStorages.put("WayCategory", new ExtendedStorageWayCategory());
                extStorages.put("WaySurfaceType", new ExtendedStorageWaySurfaceType());
                extStorages.put("Wheelchair", new ExtendedStorageWheelchair(true));
                extStorages.put("OsmId", new ExtendedStorageOsmId());
            }
            default -> {
            }
        }
    }

}
