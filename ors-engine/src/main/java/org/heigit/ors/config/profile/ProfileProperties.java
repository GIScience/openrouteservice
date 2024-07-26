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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter(AccessLevel.PROTECTED)
@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyObjectFilter.class)
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

    @JsonSetter("encoder_options")
    public void setEncoderOptions(EncoderOptionsProperties encoderOptions) {
        DefaultEncoderOptionsProperties defaultEncoderOptionsProperties = new DefaultEncoderOptionsProperties(this.getEncoderName());
        encoderOptions.updateObject(defaultEncoderOptionsProperties, false);
        this.encoderOptions = encoderOptions;
    }

    @JsonIgnore
    public String getEncoderOptionsString() {
        if (encoderOptions == null)
            return "";
        List<String> out = new ArrayList<>();
        if (encoderOptions.getBlockFords() != null) {
            out.add("block_fords=" + encoderOptions.getBlockFords());
        }
        if (encoderOptions.getConsiderElevation() != null) {
            out.add("consider_elevation=" + encoderOptions.getConsiderElevation());
        }
        if (encoderOptions.getTurnCosts() != null) {
            out.add("turn_costs=" + encoderOptions.getTurnCosts());
        }
        if (encoderOptions.getUseAcceleration() != null) {
            out.add("use_acceleration=" + encoderOptions.getUseAcceleration());
        }
        if (encoderOptions.getMaximumGradeLevel() != null) {
            out.add("maximum_grade_level=" + encoderOptions.getMaximumGradeLevel());
        }
        if (encoderOptions.getPreferredSpeedFactor() != null) {
            out.add("preferred_speed_factor=" + encoderOptions.getPreferredSpeedFactor());
        }
        if (encoderOptions.getProblematicSpeedFactor() != null) {
            out.add("problematic_speed_factor=" + encoderOptions.getProblematicSpeedFactor());
        }
        return String.join("|", out);
    }
}
