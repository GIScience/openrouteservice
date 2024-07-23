package org.heigit.ors.api.config.profile;

public class WheelchairProfileProperties extends ProfileProperties {
    public WheelchairProfileProperties() {
        super();
        this.setEncoderName("wheelchair");
        getEncoderOptions().setBlockFords(false);
        setMaximumSnappingRadius(50);
//#        ext_storages:
//#          WayCategory:
//#          WaySurfaceType:
//#          Wheelchair:
//#            KerbsOnCrossings: true
//#          OsmId:
    }
}
