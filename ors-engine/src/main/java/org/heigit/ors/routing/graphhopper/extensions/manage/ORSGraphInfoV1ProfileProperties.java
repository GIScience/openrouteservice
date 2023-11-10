package org.heigit.ors.routing.graphhopper.extensions.manage;

import java.util.Map;

public record ORSGraphInfoV1ProfileProperties (
    String profile,
    Boolean enabled,
    Boolean elevation,
    Boolean elevationSmoothing,
    Boolean traffic,
    Boolean interpolateBridgesAndTunnels,
    Boolean instructions,
    Boolean optimize,
    String graphPath,
    Map<String, String> encoderOptions,
    Map<String, Object> preparation,
    Map<String, Object> execution,
    Map<String, Map<String, String>> extStorages,
    Double maximumDistance,
    Double maximumDistanceDynamicWeights,
    Double maximumDistanceAvoidAreas,
    Double maximumDistanceAlternativeRoutes,
    Double maximumDistanceRoundTripRoutes,
    Double maximumSpeedLowerBound,
    Integer maximumWayPoints,
    Integer maximumSnappingRadius,
    Integer maximumVisitedNodes,
    Integer encoderFlagsSize,
    Integer locationIndexResolution,
    Integer locationIndexSearchIterations,
    Boolean forceTurnCosts,
    String gtfsFile
){}
