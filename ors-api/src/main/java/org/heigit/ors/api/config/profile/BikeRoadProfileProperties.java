package org.heigit.ors.api.config.profile;

public class BikeRoadProfileProperties extends ProfileProperties {
    public BikeRoadProfileProperties() {
        super();
        this.setEncoderName("cycling-road");
        getEncoderOptions().setConsiderElevation(true);
        getEncoderOptions().setTurnCosts(true);
        getEncoderOptions().setBlockFords(false);
//#        ext_storages:
//#          WayCategory:
//#          WaySurfaceType:
//#          HillIndex:
//#          TrailDifficulty:
    }
}
