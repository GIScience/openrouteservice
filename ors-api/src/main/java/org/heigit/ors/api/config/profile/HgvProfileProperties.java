package org.heigit.ors.api.config.profile;

public class HgvProfileProperties extends ProfileProperties {
    public HgvProfileProperties() {
        super();
        this.setEncoderName("driving-hgv");
        this.setEncoderFlagsSize(999);
//#      hgv:
//#        profile: driving-hgv
//#        encoder_options:
//#          turn_costs: true
//#          block_fords: false
//#          use_acceleration: true
//#        preparation:
//#          min_network_size: 200
//#          methods:
//#            ch:
//#              enabled: true
//#              threads: 1
//#              weightings: recommended
//#            core:
//#              enabled: true
//#              threads: 1
//#              weightings: recommended,shortest
//#              landmarks: 64
//#              lmsets: highways;allow_all
//#        execution:
//#          methods:
//#            core:
//#              active_landmarks: 6
//#        ext_storages:
//#          WayCategory:
//#          HeavyVehicle:
//#            restrictions: true
//#          WaySurfaceType:
    }
}
