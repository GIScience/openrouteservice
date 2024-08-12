package org.heigit.ors.config.profile;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
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
@Setter(AccessLevel.PROTECTED)
@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyMapFilter.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "encoder_name", defaultImpl = DefaultProfileProperties.class)
@JsonSubTypes({@JsonSubTypes.Type(name = "default", value = DefaultProfileProperties.class), @JsonSubTypes.Type(name = "driving-car", value = DefaultProfilePropertiesCar.class), @JsonSubTypes.Type(name = "driving-hgv", value = DefaultProfilePropertiesHgv.class), @JsonSubTypes.Type(name = "cycling-regular", value = DefaultProfilePropertiesBikeRegular.class), @JsonSubTypes.Type(name = "cycling-electric", value = DefaultProfilePropertiesBikeElectric.class), @JsonSubTypes.Type(name = "cycling-mountain", value = DefaultProfilePropertiesBikeMountain.class), @JsonSubTypes.Type(name = "cycling-road", value = DefaultProfilePropertiesBikeRoad.class), @JsonSubTypes.Type(name = "foot-walking", value = DefaultProfilePropertiesWalking.class), @JsonSubTypes.Type(name = "foot-hiking", value = DefaultProfilePropertiesHiking.class), @JsonSubTypes.Type(name = "wheelchair", value = DefaultProfilePropertiesWheelchair.class), @JsonSubTypes.Type(name = "public-transport", value = DefaultProfilePropertiesPublicTransport.class),})
@EqualsAndHashCode
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

    @JsonIgnore
    public void copyProperties(ProfileProperties source, boolean overwrite) {
        if (source == null) {
            return;
        }

        // use lombok getter and setter

        if (this.getEnabled() == null || (source.getEnabled() != null && overwrite)) {
            this.setEnabled(source.getEnabled());
        }

        if (this.getEncoderName() == null || (source.getEncoderName() != null && overwrite)) {
            setEncoderName(source.getEncoderName());
        }

        if (this.getElevation() == null || (source.getElevation() != null && overwrite)) {
            setElevation(source.getElevation());
        }

        if (this.getElevationSmoothing() == null || (source.getElevationSmoothing() != null && overwrite)) {
            setElevationSmoothing(source.getElevationSmoothing());
        }

        if (this.getEncoderFlagsSize() == null || (source.getEncoderFlagsSize() != null && overwrite)) {
            setEncoderFlagsSize(source.getEncoderFlagsSize());
        }

        if (this.getInstructions() == null || (source.getInstructions() != null && overwrite)) {
            setInstructions(source.getInstructions());
        }

        if (this.getOptimize() == null || (source.getOptimize() != null && overwrite)) {
            setOptimize(source.getOptimize());
        }

        if (this.getTraffic() == null || (source.getTraffic() != null && overwrite)) {
            setTraffic(source.getTraffic());
        }

        if (this.getInterpolateBridgesAndTunnels() == null || (source.getInterpolateBridgesAndTunnels() != null && overwrite)) {
            setInterpolateBridgesAndTunnels(source.getInterpolateBridgesAndTunnels());
        }

        if (this.getForceTurnCosts() == null || (source.getForceTurnCosts() != null && overwrite)) {
            setForceTurnCosts(source.getForceTurnCosts());
        }

        if (this.getGraphPath() == null || (source.getGraphPath() != null && overwrite)) {
            setGraphPath(source.getGraphPath());
        }

        if (this.getLocationIndexResolution() == null || (source.getLocationIndexResolution() != null && overwrite)) {
            setLocationIndexResolution(source.getLocationIndexResolution());
        }

        if (this.getLocationIndexSearchIterations() == null || (source.getLocationIndexSearchIterations() != null && overwrite)) {
            setLocationIndexSearchIterations(source.getLocationIndexSearchIterations());
        }

        if (this.getGtfsFile() == null || (source.getGtfsFile() != null && overwrite)) {
            setGtfsFile(source.getGtfsFile());
        }

        if (this.getMaximumDistance() == null || (source.getMaximumDistance() != null && overwrite)) {
            setMaximumDistance(source.getMaximumDistance());
        }

        if (this.getMaximumDistanceDynamicWeights() == null || (source.getMaximumDistanceDynamicWeights() != null && overwrite)) {
            setMaximumDistanceDynamicWeights(source.getMaximumDistanceDynamicWeights());
        }

        if (this.getMaximumDistanceAvoidAreas() == null || (source.getMaximumDistanceAvoidAreas() != null && overwrite)) {
            setMaximumDistanceAvoidAreas(source.getMaximumDistanceAvoidAreas());
        }

        if (this.getMaximumDistanceAlternativeRoutes() == null || (source.getMaximumDistanceAlternativeRoutes() != null && overwrite)) {
            setMaximumDistanceAlternativeRoutes(source.getMaximumDistanceAlternativeRoutes());
        }

        if (this.getMaximumDistanceRoundTripRoutes() == null || (source.getMaximumDistanceRoundTripRoutes() != null && overwrite)) {
            setMaximumDistanceRoundTripRoutes(source.getMaximumDistanceRoundTripRoutes());
        }

        if (this.getMaximumSpeedLowerBound() == null || (source.getMaximumSpeedLowerBound() != null && overwrite)) {
            setMaximumSpeedLowerBound(source.getMaximumSpeedLowerBound());
        }

        if (this.getMaximumWayPoints() == null || (source.getMaximumWayPoints() != null && overwrite)) {
            setMaximumWayPoints(source.getMaximumWayPoints());
        }

        if (this.getMaximumSnappingRadius() == null || (source.getMaximumSnappingRadius() != null && overwrite)) {
            setMaximumSnappingRadius(source.getMaximumSnappingRadius());
        }

        if (this.getMaximumVisitedNodes() == null || (source.getMaximumVisitedNodes() != null && overwrite)) {
            setMaximumVisitedNodes(source.getMaximumVisitedNodes());
        }

        if (this.getEncoderOptions() == null) {
            setEncoderOptions(source.getEncoderOptions());
        } else {
            if (source.getEncoderOptions() != null) {
                this.getEncoderOptions().copyProperties(source.getEncoderOptions(), overwrite);
            }
        }

        if (this.getPreparation() == null) {
            setPreparation(source.getPreparation());
        } else {
            if (source.getPreparation() != null) {
                this.getPreparation().copyProperties(source.getPreparation(), overwrite);
            }
        }

        if (this.getExecution() == null) {
            setExecution(source.getExecution());
        } else {
            if (source.getExecution() != null) {
                this.getExecution().copyProperties(source.getExecution(), overwrite);
            }
        }

        if (this.getExtStorages() == null) {
            setExtStorages(source.getExtStorages());
        } else {
            if (source.getExtStorages() != null) {
                for (Map.Entry<String, ExtendedStorage> entry : source.getExtStorages().entrySet()) {
                    if (this.getExtStorages().containsKey(entry.getKey()) && this.getExtStorages().get(entry.getKey()) != null) {
                        this.getExtStorages().get(entry.getKey()).copyProperties(entry.getValue(), overwrite);
                    } else {
                        this.getExtStorages().put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
    }
}
