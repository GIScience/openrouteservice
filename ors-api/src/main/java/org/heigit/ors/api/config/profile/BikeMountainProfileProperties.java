package org.heigit.ors.api.config.profile;

public class BikeMountainProfileProperties extends ProfileProperties {
    public BikeMountainProfileProperties() {
        super();
        this.setEncoderName("cycling-mountain");
        this.setEncoderFlagsSize(999);
//#      bike-mountain:
//#        profile: cycling-mountain
//#        encoder_options:
//#          consider_elevation: true
//#          turn_costs: true
//#          block_fords: false
//#        ext_storages:
//#          WayCategory:
//#          WaySurfaceType:
//#          HillIndex:
//#          TrailDifficulty:
    }
}
