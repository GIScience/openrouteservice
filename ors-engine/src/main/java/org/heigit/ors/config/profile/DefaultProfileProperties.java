package org.heigit.ors.config.profile;

public class DefaultProfileProperties extends ProfileProperties {
    public DefaultProfileProperties() {
        super();
        setEnabled(false);
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
        getExtStorages().entrySet().clear();
    }
}
