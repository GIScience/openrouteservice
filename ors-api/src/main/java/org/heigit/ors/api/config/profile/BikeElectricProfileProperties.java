package org.heigit.ors.api.config.profile;

public class BikeElectricProfileProperties extends ProfileProperties {
    public BikeElectricProfileProperties() {
        super();
        this.setEncoderName("cycling-electric");
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
