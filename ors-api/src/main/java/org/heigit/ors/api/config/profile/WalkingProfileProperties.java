package org.heigit.ors.api.config.profile;

public class WalkingProfileProperties extends ProfileProperties {
    public WalkingProfileProperties() {
        super();
        this.setEncoderName("foot-walking");
        this.setEncoderFlagsSize(999);
//#      walking:
//#        profile: foot-walking
//#        encoder_options:
//#          block_fords: false
//#        ext_storages:
//#          WayCategory:
//#          WaySurfaceType:
//#          HillIndex:
//#          TrailDifficulty:
    }
}
