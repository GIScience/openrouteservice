package org.heigit.ors.api.config.profile;

public class WalkingProfileProperties extends ProfileProperties {
    public WalkingProfileProperties() {
        super();
        this.setEncoderName("foot-walking");
        getEncoderOptions().setBlockFords(false);
//#        ext_storages:
//#          WayCategory:
//#          WaySurfaceType:
//#          HillIndex:
//#          TrailDifficulty:
    }
}
