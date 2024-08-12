package org.heigit.ors.config.profile;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.defaults.*;
import org.heigit.ors.config.profile.storages.ExtendedStorage;
import org.heigit.ors.config.utils.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

@Getter
@Setter
@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyMapFilter.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "encoder_name", defaultImpl = DefaultProfileProperties.class)
@JsonSubTypes({@JsonSubTypes.Type(name = "default", value = DefaultProfileProperties.class), @JsonSubTypes.Type(name = "driving-car", value = DefaultProfilePropertiesCar.class), @JsonSubTypes.Type(name = "driving-hgv", value = DefaultProfilePropertiesHgv.class), @JsonSubTypes.Type(name = "cycling-regular", value = DefaultProfilePropertiesBikeRegular.class), @JsonSubTypes.Type(name = "cycling-electric", value = DefaultProfilePropertiesBikeElectric.class), @JsonSubTypes.Type(name = "cycling-mountain", value = DefaultProfilePropertiesBikeMountain.class), @JsonSubTypes.Type(name = "cycling-road", value = DefaultProfilePropertiesBikeRoad.class), @JsonSubTypes.Type(name = "foot-walking", value = DefaultProfilePropertiesWalking.class), @JsonSubTypes.Type(name = "foot-hiking", value = DefaultProfilePropertiesHiking.class), @JsonSubTypes.Type(name = "wheelchair", value = DefaultProfilePropertiesWheelchair.class), @JsonSubTypes.Type(name = "public-transport", value = DefaultProfilePropertiesPublicTransport.class),})
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
    @Setter(AccessLevel.PUBLIC)
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
    private Map<String, ExtendedStorage> extStorages;

    protected ProfileProperties() {
        this(false, null);
    }

    protected ProfileProperties(Boolean setDefaults) {
        this(setDefaults, null);
    }

    protected ProfileProperties(Boolean setDefaults, EncoderNameEnum encoderName) {
        setEncoderName(encoderName);
        if (setDefaults) {
            encoderOptions = new DefaultEncoderOptionsProperties(true, this.encoderName);
            preparation = new DefaultPreparationProperties(this.encoderName);
            execution = new DefaultExecutionProperties(this.encoderName);
        } else {
            encoderOptions = new EncoderOptionsProperties();
            preparation = new PreparationProperties();
            execution = new ExecutionProperties();
        }
    }

    @JsonIgnore
    public String getEncoderOptionsString() {
        if (encoderOptions == null) return "";
        return encoderOptions.toString();
    }

    @JsonIgnore
    public Integer[] getProfilesTypes() {
        ArrayList<Integer> list = new ArrayList<>();
        // TODO check why this originally tries to split the encoderName. Can we add more than one?
        if (encoderName != null && encoderName != EncoderNameEnum.UNKNOWN) {
            list.add(encoderName.getValue());
        }
        return list.toArray(new Integer[0]);
    }
}
