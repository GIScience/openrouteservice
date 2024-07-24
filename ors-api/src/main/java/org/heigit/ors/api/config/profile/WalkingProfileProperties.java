package org.heigit.ors.api.config.profile;

import org.heigit.ors.api.config.profile.storages.ExtendedStorageHillIndex;
import org.heigit.ors.api.config.profile.storages.ExtendedStorageTrailDifficulty;
import org.heigit.ors.api.config.profile.storages.ExtendedStorageWayCategory;
import org.heigit.ors.api.config.profile.storages.ExtendedStorageWaySurfaceType;

public class WalkingProfileProperties extends ProfileProperties {
    public WalkingProfileProperties() {
        super();
        this.setEncoderName("foot-walking");
        getEncoderOptions().setBlockFords(false);
        getExtStorages().put("WayCategory", new ExtendedStorageWayCategory());
        getExtStorages().put("WaySurfaceType", new ExtendedStorageWaySurfaceType());
        getExtStorages().put("HillIndex", new ExtendedStorageHillIndex());
        getExtStorages().put("TrailDifficulty", new ExtendedStorageTrailDifficulty());
    }
}