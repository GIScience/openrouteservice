/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.routing.configuration;

import com.typesafe.config.Config;
import org.heigit.ors.routing.RoutingProfileType;
import org.locationtech.jts.geom.Envelope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RouteProfileConfiguration {
    private String name = "";
    private boolean enabled = true;
    private String profiles = ""; // comma separated
    private String graphPath;
    private Map<String, Map<String, String>> extStorages;
    private Map<String, Map<String, String>> graphBuilders;
    private Double maximumDistance = 0.0;
    private Double maximumDistanceDynamicWeights = 0.0;
    private Double maximumDistanceAvoidAreas = 0.0;
    private Double maximumDistanceAlternativeRoutes = 0.0;
    private Double maximumDistanceRoundTripRoutes = 0.0;
    private Integer maximumWayPoints = 0;
    private boolean instructions = true;
    private boolean optimize = false;

    private int encoderFlagsSize = 4;
    private String encoderOptions = "";
    private String gtfsFile = "";
    private Config isochronePreparationOpts;
    private Config preparationOpts;
    private Config executionOpts;

    private String elevationProvider = null;
    private String elevationCachePath = null;
    private String elevationDataAccess = "MMAP";
    private boolean elevationCacheClear = true;
    private boolean elevationSmoothing = true;
    private boolean interpolateBridgesAndTunnels = true;
    private int maximumSnappingRadius = 350;

    private Envelope extent;
    private boolean hasMaximumSnappingRadius = false;

    private int locationIndexResolution = 500;
    private int locationIndexSearchIterations = 4;

    private double maximumSpeedLowerBound = 80;

    private final int trafficExpirationMin = 15;

    private int maximumVisitedNodesPT = 1000000;

    private boolean turnCostEnabled = false;//FIXME: even though the field is read by external methods, its setter is never called.
    private boolean enforceTurnCosts = false;
    private String graphDataAccess = "";

    public RouteProfileConfiguration() {
        extStorages = new HashMap<>();
        graphBuilders = new HashMap<>();
    }

    public static boolean hasTurnCosts(String encoderOptions) {
        for (String option : encoderOptions.split("\\|")) {
            String[] keyValuePair = option.split("=");
            if (keyValuePair.length > 0 && keyValuePair[0].equals("turn_costs")) {
                return keyValuePair[1].equals("true");
            }
        }
        return false;
    }

    public Integer[] getProfilesTypes() {
        ArrayList<Integer> list = new ArrayList<>();

        String[] elements = profiles.split("\\s*,\\s*");

        for (String element : elements) {
            int profileType = RoutingProfileType.getFromString(element);

            if (profileType != RoutingProfileType.UNKNOWN) {
                list.add(profileType);
            }
        }

        return list.toArray(new Integer[0]);
    }

    public void setName(String value) {
        name = value;
    }

    public String getName() {
        return name;
    }

    public void setEnabled(Boolean value) {
        enabled = value;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setProfiles(String value) {
        profiles = value;
    }

    public String getProfiles() {
        return profiles;
    }

    public void setGraphPath(String value) {
        graphPath = value;
    }

    public String getGraphPath() {
        return graphPath;
    }

    public void setExtStorages(Map<String, Map<String, String>> value) {
        extStorages = value;
    }

    public Map<String, Map<String, String>> getExtStorages() {
        return extStorages;
    }

    public void setGraphBuilders(Map<String, Map<String, String>> value) {
        graphBuilders = value;
    }

    public Map<String, Map<String, String>> getGraphBuilders() {
        return graphBuilders;
    }

    public void setInstructions(Boolean value) {
        instructions = value;
    }

    public boolean getInstructions() {
        return instructions;
    }

    public void setMaximumDistance(Double value) {
        maximumDistance = value;
    }

    public Double getMaximumDistance() {
        return maximumDistance;
    }

    public void setMaximumDistanceDynamicWeights(Double value) {
        maximumDistanceDynamicWeights = value;
    }

    public Double getMaximumDistanceDynamicWeights() {
        return maximumDistanceDynamicWeights;
    }

    public void setMaximumDistanceAvoidAreas(Double value) {
        maximumDistanceAvoidAreas = value;
    }

    public Double getMaximumDistanceAvoidAreas() {
        return maximumDistanceAvoidAreas;
    }

    public Double getMaximumDistanceAlternativeRoutes() {
        return maximumDistanceAlternativeRoutes;
    }

    public void setMaximumDistanceAlternativeRoutes(Double maximumDistanceAlternativeRoutes) {
        this.maximumDistanceAlternativeRoutes = maximumDistanceAlternativeRoutes;
    }

    public Double getMaximumDistanceRoundTripRoutes() {
        return maximumDistanceRoundTripRoutes;
    }

    public void setMaximumDistanceRoundTripRoutes(Double maximumDistanceRoundTripRoutes) {
        this.maximumDistanceRoundTripRoutes = maximumDistanceRoundTripRoutes;
    }

    public void setMaximumWayPoints(Integer value) {
        maximumWayPoints = value;
    }

    public Integer getMaximumWayPoints() {
        return maximumWayPoints;
    }

    public void setEncoderFlagsSize(Integer value) {
        encoderFlagsSize = value;
    }

    public Integer getEncoderFlagsSize() {
        return encoderFlagsSize;
    }

    public void setEncoderOptions(String value) {
        encoderOptions = value;
        turnCostEnabled = hasTurnCosts(encoderOptions);
    }

    public String getEncoderOptions() {
        return encoderOptions;
    }

    public void setElevationProvider(String value) {
        elevationProvider = value;
    }

    public String getElevationProvider() {
        return elevationProvider;
    }

    public void setElevationCachePath(String value) {
        elevationCachePath = value;
    }

    public String getElevationCachePath() {
        return elevationCachePath;
    }

    public void setElevationDataAccess(String value) {
        elevationDataAccess = value;
    }

    public String getElevationDataAccess() {
        return elevationDataAccess;
    }

    public void setElevationCacheClear(Boolean value) {
        elevationCacheClear = value;
    }

    public boolean getElevationCacheClear() {
        return elevationCacheClear;
    }

    public boolean getElevationSmoothing() {
        return elevationSmoothing;
    }

    public void setElevationSmoothing(boolean elevationSmoothing) {
        this.elevationSmoothing = elevationSmoothing;
    }

    public boolean getInterpolateBridgesAndTunnels() {
        return interpolateBridgesAndTunnels;
    }

    public void setInterpolateBridgesAndTunnels(boolean interpolateBridgesAndTunnels) {
        this.interpolateBridgesAndTunnels = interpolateBridgesAndTunnels;
    }

    public Config getIsochronePreparationOpts() {
        return isochronePreparationOpts;
    }

    public void setIsochronePreparationOpts(Config isochronePreparationOpts) {
        this.isochronePreparationOpts = isochronePreparationOpts;
    }

    public Config getPreparationOpts() {
        return preparationOpts;
    }

    public void setPreparationOpts(Config preparationOpts) {
        this.preparationOpts = preparationOpts;
    }

    public Config getExecutionOpts() {
        return executionOpts;
    }

    public void setExecutionOpts(Config executionOpts) {
        this.executionOpts = executionOpts;
    }

    public boolean getOptimize() {
        return optimize;
    }

    public void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }

    public boolean hasMaximumSnappingRadius() {
        return hasMaximumSnappingRadius;
    }

    public int getMaximumSnappingRadius() {
        return maximumSnappingRadius;
    }

    public void setMaximumSnappingRadius(int maximumSnappingRadius) {
        this.maximumSnappingRadius = maximumSnappingRadius;
        this.hasMaximumSnappingRadius = true;
    }

    public int getLocationIndexResolution() {
        return locationIndexResolution;
    }

    public void setLocationIndexResolution(int locationIndexResolution) {
        this.locationIndexResolution = locationIndexResolution;
    }

    public int getLocationIndexSearchIterations() {
        return locationIndexSearchIterations;
    }

    public void setLocationIndexSearchIterations(int locationIndexSearchIterations) {
        this.locationIndexSearchIterations = locationIndexSearchIterations;
    }

    public void setMaximumSpeedLowerBound(double maximumSpeedLowerBound) {
        this.maximumSpeedLowerBound = maximumSpeedLowerBound;
    }

    public double getMaximumSpeedLowerBound() {
        return maximumSpeedLowerBound;
    }

    public boolean isTurnCostEnabled() {
        return turnCostEnabled;
    }

    public void setTurnCostEnabled(boolean turnCostEnabled) {
        this.turnCostEnabled = turnCostEnabled;
    }

    public void setEnforceTurnCosts(boolean enforceTurnCosts) {
        this.enforceTurnCosts = enforceTurnCosts;
    }

    public boolean isEnforceTurnCosts() {
        return enforceTurnCosts;
    }

    public void setGtfsFile(String gtfsFile) {
        this.gtfsFile = gtfsFile;
    }

    public String getGtfsFile() {
        return this.gtfsFile;
    }

    public int getMaximumVisitedNodesPT() {
        return maximumVisitedNodesPT;
    }

    public void setMaximumVisitedNodesPT(int maximumVisitedNodesPT) {
        this.maximumVisitedNodesPT = maximumVisitedNodesPT;
    }

    public String getGraphDataAccess() {
        return graphDataAccess;
    }

    public void setGraphDataAccess(String graphDataAccess) {
        this.graphDataAccess = graphDataAccess;
    }

    @Override
    public String toString() {
        return "RouteProfileConfiguration{" +
                "name='" + name + '\'' +
                ", enabled=" + enabled +
                ", profiles='" + profiles + '\'' +
                ", graphPath='" + graphPath + '\'' +
                ", extStorages=" + extStorages +
                ", graphBuilders=" + graphBuilders +
                ", maximumDistance=" + maximumDistance +
                ", maximumDistanceDynamicWeights=" + maximumDistanceDynamicWeights +
                ", maximumDistanceAvoidAreas=" + maximumDistanceAvoidAreas +
                ", maximumDistanceAlternativeRoutes=" + maximumDistanceAlternativeRoutes +
                ", maximumDistanceRoundTripRoutes=" + maximumDistanceRoundTripRoutes +
                ", maximumWayPoints=" + maximumWayPoints +
                ", instructions=" + instructions +
                ", optimize=" + optimize +
                ", encoderFlagsSize=" + encoderFlagsSize +
                ", encoderOptions='" + encoderOptions + '\'' +
                ", gtfsFile='" + gtfsFile + '\'' +
                ", isochronePreparationOpts=" + isochronePreparationOpts +
                ", preparationOpts=" + preparationOpts +
                ", executionOpts=" + executionOpts +
                ", elevationProvider='" + elevationProvider + '\'' +
                ", elevationCachePath='" + elevationCachePath + '\'' +
                ", elevationDataAccess='" + elevationDataAccess + '\'' +
                ", elevationCacheClear=" + elevationCacheClear +
                ", elevationSmoothing=" + elevationSmoothing +
                ", interpolateBridgesAndTunnels=" + interpolateBridgesAndTunnels +
                ", maximumSnappingRadius=" + maximumSnappingRadius +
                ", extent=" + extent +
                ", hasMaximumSnappingRadius=" + hasMaximumSnappingRadius +
                ", locationIndexResolution=" + locationIndexResolution +
                ", locationIndexSearchIterations=" + locationIndexSearchIterations +
                ", maximumSpeedLowerBound=" + maximumSpeedLowerBound +
                ", trafficExpirationMin=" + trafficExpirationMin +
                ", maximumVisitedNodesPT=" + maximumVisitedNodesPT +
                ", turnCostEnabled=" + turnCostEnabled +
                ", enforceTurnCosts=" + enforceTurnCosts +
                ", graphDataAccess='" + graphDataAccess + '\'' +
                '}';
    }
}
