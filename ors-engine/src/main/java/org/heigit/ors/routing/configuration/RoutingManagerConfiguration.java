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

import com.graphhopper.util.Helper;
import com.typesafe.config.ConfigFactory;
import org.heigit.ors.config.AppConfig;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.util.FileUtility;
import org.heigit.ors.util.StringUtility;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoutingManagerConfiguration {
    public static final String PARAM_ELEVATION_CACHE_CLEAR = "elevation_cache_clear";
    public static final String PARAM_ELEVATION_DATA_ACCESS = "elevation_data_access";
    public static final String PARAM_ELEVATION_SMOOTHING = "elevation_smoothing";
    public static final String PARAM_INTERPOLATE_BRIDGES_AND_TUNNELS = "interpolate_bridges_and_tunnels";

    public RouteProfileConfiguration[] getProfiles() {
        return profiles;
    }

    public void setProfiles(RouteProfileConfiguration[] profiles) {
        this.profiles = profiles;
    }

    private RouteProfileConfiguration[] profiles;

    private static void addFastIsochronesToProfileConfiguration(Map<String, Object> defaultFastIsochroneParams, RouteProfileConfiguration profile) {
        String profileRef = "fastisochrones.profiles." + profile.getName();
        Map<String, Object> profileParams = AppConfig.getIsochronesParametersMap(profileRef, true);

        if (profileParams == null)
            profileParams = defaultFastIsochroneParams;
        else if (defaultFastIsochroneParams != null) {
            for (Map.Entry<String, Object> defParamItem : defaultFastIsochroneParams.entrySet()) {
                if (!profileParams.containsKey(defParamItem.getKey()))
                    profileParams.put(defParamItem.getKey(), defParamItem.getValue());
            }
        }
        profile.setIsochronePreparationOpts(ConfigFactory.parseString(profileParams.toString()));
    }

    public static RoutingManagerConfiguration loadFromFile(String rootGraphsPath) {
        RoutingManagerConfiguration configuration = new RoutingManagerConfiguration();

        // Read profile settings
        List<RouteProfileConfiguration> newProfiles = new ArrayList<>();
        Map<String, Object> defaultFastIsochroneParams = AppConfig.getIsochronesParametersMap("fastisochrones.profiles.default_params", true);
        if (defaultFastIsochroneParams == null) { // default to disabled if ors.services.isochrones.fastisochrones not available in ors-config.json
            defaultFastIsochroneParams = new HashMap<>();
            defaultFastIsochroneParams.put("enabled", false);
        }
        List<String> profileList = AppConfig.getRoutingParametersList("profiles.active");
        Map<String, Object> defaultParams = AppConfig.getRoutingParametersMap("profiles.default_params", true);

        for (String item : profileList) {
            String profileRef = "profiles.profile-" + item;

            RouteProfileConfiguration profile = new RouteProfileConfiguration();
            profile.setName(item);
            profile.setEnabled(true);
            profile.setProfiles(AppConfig.getRoutingParameter(profileRef + ".profiles"));

            String graphPath = AppConfig.getRoutingParameter(profileRef + ".graph_path", false);
            if (!Helper.isEmpty(rootGraphsPath)) {
                if (Helper.isEmpty(graphPath))
                    graphPath = Paths.get(rootGraphsPath, item).toString();
                else if (!FileUtility.isAbsolutePath(graphPath))
                    graphPath = Paths.get(rootGraphsPath, graphPath).toString();
            }

            profile.setGraphPath(graphPath);

            addFastIsochronesToProfileConfiguration(defaultFastIsochroneParams, profile);

            Map<String, Object> profileParams = AppConfig.getRoutingParametersMap(profileRef + ".parameters", true);

            if (profileParams == null)
                profileParams = defaultParams;
            else if (defaultParams != null) {
                for (Map.Entry<String, Object> defParamItem : defaultParams.entrySet()) {
                    if (!profileParams.containsKey(defParamItem.getKey()))
                        profileParams.put(defParamItem.getKey(), defParamItem.getValue());
                }
            }

            if (profileParams != null) {
                for (Map.Entry<String, Object> paramItem : profileParams.entrySet()) {
                    switch (paramItem.getKey()) {
                        case "preparation" ->
                                profile.setPreparationOpts(ConfigFactory.parseString(paramItem.getValue().toString()));
                        case "execution" ->
                                profile.setExecutionOpts(ConfigFactory.parseString(paramItem.getValue().toString()));
                        case "encoder_options" ->
                                profile.setEncoderOptions(StringUtility.trimQuotes(paramItem.getValue().toString()));
                        case "optimize" -> profile.setOptimize(Boolean.parseBoolean(paramItem.getValue().toString()));
                        case "encoder_flags_size" ->
                                profile.setEncoderFlagsSize(Integer.parseInt(paramItem.getValue().toString()));
                        case "instructions" ->
                                profile.setInstructions(Boolean.parseBoolean(paramItem.getValue().toString()));
                        case "elevation" -> {
                            if (Boolean.parseBoolean(paramItem.getValue().toString())) {
                                profile.setElevationProvider(StringUtility.trimQuotes(profileParams.get("elevation_provider").toString()));
                                if (profileParams.get(PARAM_ELEVATION_DATA_ACCESS) != null)
                                    profile.setElevationDataAccess(StringUtility.trimQuotes(profileParams.get(PARAM_ELEVATION_DATA_ACCESS).toString()));
                                profile.setElevationCachePath(StringUtility.trimQuotes(profileParams.get("elevation_cache_path").toString()));
                                if (profileParams.get(PARAM_ELEVATION_CACHE_CLEAR) != null) {
                                    String clearCache = StringUtility.trimQuotes(profileParams.get(PARAM_ELEVATION_CACHE_CLEAR).toString());
                                    profile.setElevationCacheClear(Boolean.parseBoolean(clearCache));
                                }
                                if (profileParams.get(PARAM_INTERPOLATE_BRIDGES_AND_TUNNELS) != null) {
                                    String interpolateBridgesAndTunnels = StringUtility.trimQuotes(profileParams.get(PARAM_INTERPOLATE_BRIDGES_AND_TUNNELS).toString());
                                    profile.setInterpolateBridgesAndTunnels(Boolean.parseBoolean(interpolateBridgesAndTunnels));
                                }
                                if (profileParams.get(PARAM_ELEVATION_SMOOTHING) != null) {
                                    String elevationSmoothing = StringUtility.trimQuotes(profileParams.get(PARAM_ELEVATION_SMOOTHING).toString());
                                    profile.setElevationSmoothing(Boolean.parseBoolean(elevationSmoothing));
                                }
                            }
                        }
                        case "ext_storages" -> {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> storageList = (Map<String, Object>) paramItem.getValue();
                            for (Map.Entry<String, Object> storageEntry : storageList.entrySet()) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> entryValue = (Map<String, Object>) storageEntry.getValue();
                                Map<String, String> storageParams = new HashMap<>();

                                for (Map.Entry<String, Object> entry : entryValue.entrySet()) {
                                    storageParams.put(entry.getKey(), StringUtility.trimQuotes(entry.getValue().toString()));
                                }

                                storageParams.put("gh_profile", RoutingProfile.makeProfileName(RoutingProfileType.getEncoderName(RoutingProfileType.getFromString(profile.getProfiles())), "fastest", RouteProfileConfiguration.hasTurnCosts(profile.getEncoderOptions())));

                                profile.getExtStorages().put(storageEntry.getKey(), storageParams);
                            }
                        }
                        case "maximum_distance" ->
                                profile.setMaximumDistance(Double.parseDouble(paramItem.getValue().toString()));
                        case "maximum_distance_dynamic_weights" ->
                                profile.setMaximumDistanceDynamicWeights(Double.parseDouble(paramItem.getValue().toString()));
                        case "maximum_distance_avoid_areas" ->
                                profile.setMaximumDistanceAvoidAreas(Double.parseDouble(paramItem.getValue().toString()));
                        case "maximum_distance_alternative_routes" ->
                                profile.setMaximumDistanceAlternativeRoutes(Double.parseDouble(paramItem.getValue().toString()));
                        case "maximum_distance_round_trip_routes" ->
                                profile.setMaximumDistanceRoundTripRoutes(Double.parseDouble(paramItem.getValue().toString()));
                        case "maximum_waypoints" ->
                                profile.setMaximumWayPoints(Integer.parseInt(paramItem.getValue().toString()));
                        case "maximum_snapping_radius" ->
                                profile.setMaximumSnappingRadius(Integer.parseInt(paramItem.getValue().toString()));
                        case "location_index_resolution" ->
                                profile.setLocationIndexResolution(Integer.parseInt(paramItem.getValue().toString()));
                        case "location_index_search_iterations" ->
                                profile.setLocationIndexSearchIterations(Integer.parseInt(paramItem.getValue().toString()));
                        case "maximum_speed_lower_bound" ->
                                profile.setMaximumSpeedLowerBound(Double.parseDouble(paramItem.getValue().toString()));
                        case "force_turn_costs" ->
                                profile.setEnforceTurnCosts(Boolean.parseBoolean(paramItem.getValue().toString()));
                        case "gtfs_file" ->
                                profile.setGtfsFile(StringUtility.trimQuotes(paramItem.getValue().toString()));
                        case "maximum_visited_nodes" ->
                                profile.setMaximumVisitedNodesPT(Integer.parseInt(paramItem.getValue().toString()));
                        default -> {
                        }
                    }
                }
            }
            newProfiles.add(profile);
        }
        configuration.setProfiles(newProfiles.toArray(new RouteProfileConfiguration[0]));
        return configuration;
    }
}
