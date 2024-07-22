package org.heigit.ors.api.config.profile;

public class CarProfileProperties extends ProfileProperties {
    public CarProfileProperties() {
        super();
        this.setEncoderName("driving-car");
        setElevation(true);
        setElevationSmoothing(true);
        setEncoderFlagsSize(8);
        setInstructions(true);
        setOptimize(false);
        setTraffic(false);
        setInterpolateBridgesAndTunnels(true);
        setForceTurnCosts(false);
        setLocationIndexResolution(500);
        setLocationIndexSearchIterations(4);
        setMaximumDistance(100000d);
        setMaximumDistanceDynamicWeights(100000d);
        setMaximumDistanceAvoidAreas(100000d);
        setMaximumDistanceAlternativeRoutes(100000d);
        setMaximumDistanceRoundTripRoutes(100000d);
        setMaximumSpeedLowerBound(80d);
        setMaximumWayPoints(50);
        setMaximumSnappingRadius(400);
        setMaximumVisitedNodes(1000000);
        getPreparation().setMinNetworkSize(200);
        getPreparation().getMethods().getLm().setEnabled(true);
        getPreparation().getMethods().getLm().setThreads(1);
        getPreparation().getMethods().getLm().setWeightings("recommended,shortest");
        getPreparation().getMethods().getLm().setLandmarks(16);
        getExecution().getMethods().getLm().setActiveLandmarks(8);

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
