package org.heigit.ors.api.config.profile;

public class BikeRegularProfileProperties extends ProfileProperties {
    public BikeRegularProfileProperties() {
        super();
        this.setEncoderName("cycling-regular");
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
