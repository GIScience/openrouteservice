package org.heigit.ors.api.config.profile;

public class BikeMountainProfileProperties extends ProfileProperties {
    public BikeMountainProfileProperties() {
        super();
        this.setEncoderName("cycling-mountain");
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
