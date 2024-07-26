package org.heigit.ors.config.profile;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.defaults.*;
import org.heigit.ors.config.profile.storages.ExtendedStorage;
import org.heigit.ors.config.utils.*;
import org.heigit.ors.routing.RoutingProfileType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter(AccessLevel.PROTECTED)
@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyMapFilter.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "encoder_name", defaultImpl = DefaultProfileProperties.class)
@JsonSubTypes({
        @JsonSubTypes.Type(name = "default", value = DefaultProfileProperties.class),
        @JsonSubTypes.Type(name = "driving-car", value = CarProfileProperties.class),
        @JsonSubTypes.Type(name = "driving-hgv", value = HgvProfileProperties.class),
        @JsonSubTypes.Type(name = "cycling-regular", value = BikeRegularProfileProperties.class),
        @JsonSubTypes.Type(name = "cycling-electric", value = BikeElectricProfileProperties.class),
        @JsonSubTypes.Type(name = "cycling-mountain", value = BikeMountainProfileProperties.class),
        @JsonSubTypes.Type(name = "cycling-road", value = BikeRoadProfileProperties.class),
        @JsonSubTypes.Type(name = "foot-walking", value = WalkingProfileProperties.class),
        @JsonSubTypes.Type(name = "foot-hiking", value = HikingProfileProperties.class),
        @JsonSubTypes.Type(name = "wheelchair", value = WheelchairProfileProperties.class),
        @JsonSubTypes.Type(name = "public-transport", value = PublicTransportProfileProperties.class),
})
public abstract class ProfileProperties {
    @JsonProperty("enabled")
    private Boolean enabled;
    @JsonProperty("encoder_name")
    private EncoderNameEnum encoderName;
    @JsonProperty("elevation")
    private Boolean elevation;
    @JsonProperty("elevation_smoothing")
    private Boolean elevationSmoothing;
    @JsonProperty("encoder_flags_size")
    private Integer encoderFlagsSize;
    @JsonProperty("instructions")
    private Boolean instructions;
    @JsonProperty("optimize")
    private Boolean optimize;
    @JsonProperty("traffic")
    private Boolean traffic;
    @JsonProperty("interpolate_bridges_and_tunnels")
    private Boolean interpolateBridgesAndTunnels;
    @JsonProperty("force_turn_costs")
    private Boolean forceTurnCosts;
    @JsonProperty("graph_path")
    @JsonDeserialize(using = PathDeserializer.class)
    @JsonSerialize(using = PathSerializer.class)
    private Path graphPath;
    @JsonProperty("location_index_resolution")
    private Integer locationIndexResolution;
    @JsonProperty("location_index_search_iterations")
    private Integer locationIndexSearchIterations;
    @JsonProperty("gtfs_file")
    @JsonDeserialize(using = PathDeserializer.class)
    @JsonSerialize(using = PathSerializer.class)
    private Path gtfsFile;

    @JsonProperty("maximum_distance")
    private Double maximumDistance;
    @JsonProperty("maximum_distance_dynamic_weights")
    private Double maximumDistanceDynamicWeights;
    @JsonProperty("maximum_distance_avoid_areas")
    private Double maximumDistanceAvoidAreas;
    @JsonProperty("maximum_distance_alternative_routes")
    private Double maximumDistanceAlternativeRoutes;
    @JsonProperty("maximum_distance_round_trip_routes")
    private Double maximumDistanceRoundTripRoutes;
    @JsonProperty("maximum_speed_lower_bound")
    private Double maximumSpeedLowerBound;
    @JsonProperty("maximum_way_points")
    private Integer maximumWayPoints;
    @JsonProperty("maximum_snapping_radius")
    private Integer maximumSnappingRadius;
    @JsonProperty("maximum_visited_nodes")
    private Integer maximumVisitedNodes;

    @JsonProperty("encoder_options")
    private EncoderOptionsProperties encoderOptions;
    @JsonProperty("preparation")
    private PreparationProperties preparation;
    @JsonProperty("execution")
    private ExecutionProperties execution;
    @JsonProperty("ext_storages")
    @JsonSerialize(using = ExtendedStorageMapSerializer.class)
    @JsonDeserialize(using = ExtendedStorageMapDeserializer.class)
    private Map<String, ExtendedStorage> extStorages = new HashMap<>();

    protected ProfileProperties() {
        encoderOptions = new DefaultEncoderOptionsProperties();
        preparation = new PreparationProperties();
        execution = new ExecutionProperties();
    }

    public void mergeDefaultsAndSetGraphPath(ProfileProperties defaultProfile, Path graphsRootPath, String profileName) {
        if (enabled == null) enabled = defaultProfile.enabled;
        if (encoderName == null) encoderName = defaultProfile.encoderName;
        if (elevation == null) elevation = defaultProfile.elevation;
        if (elevationSmoothing == null) elevationSmoothing = defaultProfile.elevationSmoothing;
        if (encoderFlagsSize == null) encoderFlagsSize = defaultProfile.encoderFlagsSize;
        if (instructions == null) instructions = defaultProfile.instructions;
        if (optimize == null) optimize = defaultProfile.optimize;
        if (traffic == null) traffic = defaultProfile.traffic;
        if (interpolateBridgesAndTunnels == null)
            interpolateBridgesAndTunnels = defaultProfile.interpolateBridgesAndTunnels;
        if (forceTurnCosts == null) forceTurnCosts = defaultProfile.forceTurnCosts;
        if (graphPath == null)
            graphPath = defaultProfile.graphPath == null ? Paths.get(graphsRootPath.toString(), profileName) : Paths.get(defaultProfile.graphPath.toString(), profileName);
        if (locationIndexResolution == null) locationIndexResolution = defaultProfile.locationIndexResolution;
        if (locationIndexSearchIterations == null)
            locationIndexSearchIterations = defaultProfile.locationIndexSearchIterations;
        if (gtfsFile == null) gtfsFile = defaultProfile.gtfsFile;

        if (maximumDistance == null) maximumDistance = defaultProfile.maximumDistance;
        if (maximumDistanceDynamicWeights == null)
            maximumDistanceDynamicWeights = defaultProfile.maximumDistanceDynamicWeights;
        if (maximumDistanceAvoidAreas == null) maximumDistanceAvoidAreas = defaultProfile.maximumDistanceAvoidAreas;
        if (maximumDistanceAlternativeRoutes == null)
            maximumDistanceAlternativeRoutes = defaultProfile.maximumDistanceAlternativeRoutes;
        if (maximumDistanceRoundTripRoutes == null)
            maximumDistanceRoundTripRoutes = defaultProfile.maximumDistanceRoundTripRoutes;
        if (maximumSpeedLowerBound == null) maximumSpeedLowerBound = defaultProfile.maximumSpeedLowerBound;
        if (maximumWayPoints == null) maximumWayPoints = defaultProfile.maximumWayPoints;
        if (maximumSnappingRadius == null) maximumSnappingRadius = defaultProfile.maximumSnappingRadius;
        if (maximumVisitedNodes == null) maximumVisitedNodes = defaultProfile.maximumVisitedNodes;

        if (encoderOptions.isEmpty()) encoderOptions = defaultProfile.encoderOptions;
        if (preparation.isEmpty()) preparation = defaultProfile.preparation;
        if (execution.isEmpty()) execution = defaultProfile.execution;
        if (extStorages.isEmpty()) extStorages = defaultProfile.extStorages;
    }

    @JsonSetter("encoder_options")
    public void setEncoderOptions(EncoderOptionsProperties encoderOptions) {
        DefaultEncoderOptionsProperties defaultEncoderOptionsProperties = new DefaultEncoderOptionsProperties(this.getEncoderName());
        encoderOptions = (EncoderOptionsProperties) PropertyUtils.copyObjectPropertiesDeep(defaultEncoderOptionsProperties, encoderOptions, false);
        this.encoderOptions = encoderOptions;
    }

    @JsonSetter("preparation")
    public void setPreparation(PreparationProperties preparation) {
        DefaultPreparationProperties defaultPreparationProperties = new DefaultPreparationProperties(this.getEncoderName());
        preparation = (PreparationProperties) PropertyUtils.copyObjectPropertiesDeep(defaultPreparationProperties, preparation, false);
        this.preparation = preparation;
    }

    @JsonIgnore
    public String getEncoderOptionsString() {
        if (encoderOptions == null)
            return "";
        return encoderOptions.toString();
    }

    @JsonIgnore
    public Integer[] getProfilesTypes() {
        ArrayList<Integer> list = new ArrayList<>();
        String[] elements = encoderName.split("\\s*,\\s*");
        for (String element : elements) {
            int profileType = RoutingProfileType.getFromString(element);
            if (profileType != RoutingProfileType.UNKNOWN) {
                list.add(profileType);
            }
        }
        return list.toArray(new Integer[0]);
    }
}
