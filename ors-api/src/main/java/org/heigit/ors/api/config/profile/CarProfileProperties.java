package org.heigit.ors.api.config.profile;

public class CarProfileProperties extends ProfileProperties {
    public CarProfileProperties() {
        super();
        this.setEncoderName("driving-car");
        this.setEncoderFlagsSize(999);
//#      car:
//#        profile: driving-car
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
//#              weightings: fastest
//#            lm:
//#              enabled: false
//#              threads: 1
//#              weightings: fastest,shortest
//#              landmarks: 16
//#            core:
//#              enabled: true
//#              threads: 1
//#              weightings: fastest,shortest
//#              landmarks: 64
//#              lmsets: highways;allow_all
//#        execution:
//#          methods:
//#            lm:
//#              active_landmarks: 6
//#            core:
//#              active_landmarks: 6
//#        ext_storages:
//#          WayCategory:
//#          HeavyVehicle:
//#          WaySurfaceType:
//#          RoadAccessRestrictions:
//#            use_for_warnings: true
    }
}
