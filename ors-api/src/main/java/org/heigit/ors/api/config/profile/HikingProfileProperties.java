package org.heigit.ors.api.config.profile;

public class HikingProfileProperties extends ProfileProperties {
    public HikingProfileProperties() {
        super();
        this.setEncoderName("foot-hiking");
        getEncoderOptions().setBlockFords(false);
//#        ext_storages:
//#          WayCategory:
//#          WaySurfaceType:
//#          HillIndex:
//#          TrailDifficulty:
    }
}
