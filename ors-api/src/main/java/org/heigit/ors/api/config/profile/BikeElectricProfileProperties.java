package org.heigit.ors.api.config.profile;

public class BikeElectricProfileProperties extends ProfileProperties {
    public BikeElectricProfileProperties() {
        super();
        this.setEncoderName("cycling-electric");
        this.setEncoderFlagsSize(999);
//#      bike-electric:
//#        profile: cycling-electric
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
