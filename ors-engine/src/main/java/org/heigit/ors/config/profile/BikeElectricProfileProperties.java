package org.heigit.ors.config.profile;

import org.heigit.ors.config.profile.storages.ExtendedStorageHillIndex;
import org.heigit.ors.config.profile.storages.ExtendedStorageTrailDifficulty;
import org.heigit.ors.config.profile.storages.ExtendedStorageWayCategory;
import org.heigit.ors.config.profile.storages.ExtendedStorageWaySurfaceType;

public class BikeElectricProfileProperties extends ProfileProperties {
    public BikeElectricProfileProperties() {
        super();
        this.setEncoderName("cycling-electric");
        getEncoderOptions().setConsiderElevation(true);
        getEncoderOptions().setTurnCosts(true);
        getEncoderOptions().setBlockFords(false);
        getExtStorages().put("WayCategory", new ExtendedStorageWayCategory());
        getExtStorages().put("WaySurfaceType", new ExtendedStorageWaySurfaceType());
        getExtStorages().put("HillIndex", new ExtendedStorageHillIndex());
        getExtStorages().put("TrailDifficulty", new ExtendedStorageTrailDifficulty());
    }
}
