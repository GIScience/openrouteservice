package org.heigit.ors.api.config.profile;

public class HikingProfileProperties extends ProfileProperties {
    public HikingProfileProperties() {
        super();
        this.setEncoderName("foot-hiking");
        this.setEncoderFlagsSize(999);
//#      hiking:
//#        profile: foot-hiking
//#        encoder_options:
//#          block_fords: false
//#        ext_storages:
//#          WayCategory:
//#          WaySurfaceType:
//#          HillIndex:
//#          TrailDifficulty:
    }
}
