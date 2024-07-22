package org.heigit.ors.api.config.profile;

public class BikeRegularProfileProperties extends ProfileProperties {
    public BikeRegularProfileProperties() {
        super();
        this.setEncoderName("cycling-regular");
        this.setEncoderFlagsSize(999);
//#      bike-regular:
//#        profile: cycling-regular
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
