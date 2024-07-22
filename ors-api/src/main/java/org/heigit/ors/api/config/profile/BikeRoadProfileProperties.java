package org.heigit.ors.api.config.profile;

public class BikeRoadProfileProperties extends ProfileProperties {
    public BikeRoadProfileProperties() {
        super();
        this.setEncoderName("cycling-road");
        this.setEncoderFlagsSize(999);
//#      bike-road:
//#        profile: cycling-road
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
