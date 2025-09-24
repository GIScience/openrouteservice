package org.heigit.ors.config.profile;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;

@Getter
@Setter
public class BuildProperties {
    @JsonIgnore
    private Path sourceFile;
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
            if (storage != null) {
                ExtendedStorageName extendedStorageName = ExtendedStorageName.getEnum(key);
                switch (extendedStorageName) {
                    case OSM_ID:
                        if (encodedValues.getOsmWayId() == null) {
                            encodedValues.setOsmWayId(true);
                        }
                        break;
                    case WAY_CATEGORY:
                        handleWayCategory();
                        // TODO: remove storage after transfer to EV
                        storage.initialize(extendedStorageName);
                        this.extStorages.put(key, storage);
                        break;
                    case WAY_SURFACE_TYPE:
                        handleWaySurfaceType();
                        break;
                    default:
                        storage.initialize(extendedStorageName);
                        this.extStorages.put(key, storage);
                        break;
                }
            }
        }
    }

    private void handleWayCategory() {
        if (encodedValues.getHighway() == null) {
            encodedValues.setHighway(true);
        }
        if (encodedValues.getFord() == null) {
            encodedValues.setFord(true);
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
