package org.heigit.ors.api.config.profile;

public class WheelchairProfileProperties extends ProfileProperties {
    public WheelchairProfileProperties() {
        super();
        this.setEncoderName("wheelchair");
        this.setEncoderFlagsSize(999);
//#      wheelchair:
//#        profile: wheelchair
//#        encoder_options:
//#          block_fords: true
//#        maximum_snapping_radius: 50
//#        ext_storages:
//#          WayCategory:
//#          WaySurfaceType:
//#          Wheelchair:
//#            KerbsOnCrossings: true
//#          OsmId:
    }
}
