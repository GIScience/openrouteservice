package org.heigit.ors.config.profile;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.routing.graphhopper.extensions.util.parsers.wheelchair.WheelchairKerbHeightParser;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.Optional.ofNullable;

@Getter
@Setter
public class BuildProperties {
    @JsonIgnore
    private Path sourceFile;
    @JsonIgnore
    private String profileGroup;
    @JsonIgnore
    private String graphExtent;

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
    @JsonProperty("location_index_resolution")
    private Integer locationIndexResolution;
    @JsonProperty("location_index_search_iterations")
    private Integer locationIndexSearchIterations;
    @JsonIgnore
    private Path gtfsFile;
    @JsonProperty("encoder_options")
    private EncoderOptionsProperties encoderOptions = new EncoderOptionsProperties();
    @JsonProperty("preparation")
    private PreparationProperties preparation = new PreparationProperties();
    @JsonProperty("ext_storages")
    private Map<String, ExtendedStorageProperties> extStorages = new LinkedHashMap<>();
    @JsonProperty("encoded_values")
    private EncodedValuesProperties encodedValues = new EncodedValuesProperties();
    @JsonProperty("maximum_speed_lower_bound")
    private Double maximumSpeedLowerBound;

    public BuildProperties() {
    }

    public BuildProperties(String ignored) {
        // This constructor is used to create an empty object for the purpose of ignoring it in the JSON serialization.
    }

    public void merge(BuildProperties other) {
        profileGroup = ofNullable(profileGroup).orElse(other.profileGroup);
        graphExtent = ofNullable(graphExtent).orElse(other.graphExtent);
        elevation = ofNullable(elevation).orElse(other.elevation);
        elevationSmoothing = ofNullable(elevationSmoothing).orElse(other.elevationSmoothing);
        encoderFlagsSize = ofNullable(encoderFlagsSize).orElse(other.encoderFlagsSize);
        instructions = ofNullable(instructions).orElse(other.instructions);
        optimize = ofNullable(optimize).orElse(other.optimize);
        traffic = ofNullable(traffic).orElse(other.traffic);
        interpolateBridgesAndTunnels = ofNullable(interpolateBridgesAndTunnels).orElse(other.interpolateBridgesAndTunnels);
        maximumSpeedLowerBound = ofNullable(this.maximumSpeedLowerBound).orElse(other.maximumSpeedLowerBound);
        locationIndexResolution = ofNullable(locationIndexResolution).orElse(other.locationIndexResolution);
        locationIndexSearchIterations = ofNullable(locationIndexSearchIterations).orElse(other.locationIndexSearchIterations);
        encoderOptions.merge(other.encoderOptions);
        preparation.merge(other.preparation);
        for (Map.Entry<String, ExtendedStorageProperties> entry : other.extStorages.entrySet()) {
            if (extStorages.containsKey(entry.getKey())) {
                extStorages.get(entry.getKey()).merge(entry.getValue());
            } else {
                extStorages.put(entry.getKey(), entry.getValue());
            }
        }
        encodedValues.merge(other.encodedValues);
        // Fix paths
        sourceFile = ofNullable(sourceFile).orElse(other.sourceFile);
        gtfsFile = ofNullable(gtfsFile).orElse(other.gtfsFile);
        if (gtfsFile != null) {
            gtfsFile = gtfsFile.toAbsolutePath();
        }
    }

    public void initExtStorages() {
        if (extStorages == null) {
            return;
        }

        for (Map.Entry<String, ExtendedStorageProperties> entry : extStorages.entrySet()) {
            String key = entry.getKey();
            ExtendedStorageProperties storage = entry.getValue();
            if (storage == null) {
                continue;
            }
            ExtendedStorageName extendedStorageName = ExtendedStorageName.getEnum(key);

            switch (extendedStorageName) {
                case HEAVY_VEHICLE -> handleHeavyVehicle(storage);
                case OSM_ID -> handleOsmId();
                case WAY_CATEGORY -> handleWayCategory();
                case WAY_SURFACE_TYPE -> handleWaySurfaceType();
                case ROAD_ACCESS_RESTRICTIONS -> handleAccessRestrictions();
                case TOLLWAYS -> handleTollways();
                case HILL_INDEX -> handleHillIndex();
                case TRAIL_DIFFICULTY -> handleTrailDifficulty();
                case WHEELCHAIR -> handleWheelchair(storage);
                default -> {
                    storage.initialize(extendedStorageName);
                    this.extStorages.put(key, storage);
                }
            }
        }
    }

    private void handleHeavyVehicle(ExtendedStorageProperties storage) {
        handleVehicleAccess();
        if (Boolean.TRUE.equals(storage.getRestrictions())) {
            handleVehicleRestrictions();
        }
    }

    private void handleVehicleAccess() {
        if (encodedValues.getAgriculturalAccess() == null) {
            encodedValues.setAgriculturalAccess(true);
        }
        if (encodedValues.getBusAccess() == null) {
            encodedValues.setBusAccess(true);
        }
        if (encodedValues.getDeliveryAccess() == null) {
            encodedValues.setDeliveryAccess(true);
        }
        if (encodedValues.getForestryAccess() == null) {
            encodedValues.setForestryAccess(true);
        }
        if (encodedValues.getGoodsAccess() == null) {
            encodedValues.setGoodsAccess(true);
        }
        if (encodedValues.getHgvAccess() == null) {
            encodedValues.setHgvAccess(true);
        }
        if (encodedValues.getHazmatAccess() == null) {
            encodedValues.setHazmatAccess(true);
        }
    }

    private void handleVehicleRestrictions() {
        if (encodedValues.getMaxAxleLoad() == null) {
            encodedValues.setMaxAxleLoad(true);
        }
        if (encodedValues.getMaxHeight() == null) {
            encodedValues.setMaxHeight(true);
        }
        if (encodedValues.getMaxLength() == null) {
            encodedValues.setMaxLength(true);
        }
        if (encodedValues.getMaxWeight() == null) {
            encodedValues.setMaxWeight(true);
        }
        if (encodedValues.getMaxWidth() == null) {
            encodedValues.setMaxWidth(true);
        }
    }

    private void handleOsmId() {
        if (encodedValues.getOsmWayId() == null) {
            encodedValues.setOsmWayId(true);
        }
    }

    private void handleWayCategory() {
        if (encodedValues.getHighway() == null) {
            encodedValues.setHighway(true);
        }
        if (encodedValues.getFord() == null) {
            encodedValues.setFord(true);
        }
        // Used for ferries and steps
        if (encodedValues.getWayType() == null) {
            encodedValues.setWayType(true);
        }
    }

    private void handleWaySurfaceType() {
        if (encodedValues.getWaySurface() == null) {
            encodedValues.setWaySurface(true);
        }
        if (encodedValues.getWayType() == null) {
            encodedValues.setWayType(true);
        }
    }

    private void handleAccessRestrictions() {
        if (encodedValues.getAccessRestriction() == null) {
            encodedValues.setAccessRestriction(true);
        }
    }

    private void handleTollways() {
        if (encodedValues.getToll() == null) {
            encodedValues.setToll(true);
        }
    }

    private void handleHillIndex() {
        if (encodedValues.getHillIndex() == null) {
            encodedValues.setHillIndex(true);
        }
    }

    private void handleTrailDifficulty() {
        if (encodedValues.getSacScale() == null) {
            encodedValues.setSacScale(true);
        }
        if (encodedValues.getMtbScale() == null) {
            encodedValues.setMtbScale(true);
        }
        if (encodedValues.getMtbScaleUphill() == null) {
            encodedValues.setMtbScaleUphill(true);
        }
    }

    private void handleWheelchair(ExtendedStorageProperties storage) {
        WheelchairKerbHeightParser.setKerbHeightOnlyOnCrossing(Objects.requireNonNullElse(storage.getKerbsOnCrossings(), true));

        if (encodedValues.getWheelchairSurface() == null) {
            encodedValues.setWheelchairSurface(true);
        }
        if (encodedValues.getWheelchairSmoothness() == null) {
            encodedValues.setWheelchairSmoothness(true);
        }
        if (encodedValues.getWheelchairTrackType() == null) {
            encodedValues.setWheelchairTrackType(true);
        }
        if (encodedValues.getWheelchairIncline() == null) {
            encodedValues.setWheelchairIncline(true);
        }
        if (encodedValues.getWheelchairWidth() == null) {
            encodedValues.setWheelchairWidth(true);
        }
        if (encodedValues.getWheelchairKerb() == null) {
            encodedValues.setWheelchairKerb(true);
        }
        if (encodedValues.getWheelchairSuitable() == null) {
            encodedValues.setWheelchairSuitable(true);
        }
        if (encodedValues.getWheelchairSide() == null) {
            encodedValues.setWheelchairSide(true);
        }
        if (encodedValues.getWheelchairSurfaceQualityKnown() == null) {
            encodedValues.setWheelchairSurfaceQualityKnown(true);
        }
    }

    @JsonIgnore
    public String getEncoderOptionsString() {
        if (encoderOptions == null) return "";
        return encoderOptions.toString();
    }

    @JsonIgnore
    public String getEncodedValuesString() {
        return encodedValues == null ? "" : encodedValues.toString();
    }
}
