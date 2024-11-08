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
    @JsonProperty("maximum_speed_lower_bound")
    private Double maximumSpeedLowerBound;

    public BuildProperties() {
    }

    public BuildProperties(String ignored) {
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
        // Fix paths
        sourceFile = ofNullable(sourceFile).orElse(other.sourceFile);
        gtfsFile = ofNullable(gtfsFile).orElse(other.gtfsFile);
        if (gtfsFile != null) {
            gtfsFile = gtfsFile.toAbsolutePath();
        }
    }

    public void initExtStorages() {
        if (extStorages != null) {
            extStorages.forEach((key, storage) -> {
                if (storage != null) {
                    storage.initialize(ExtendedStorageName.getEnum(key));
                    this.extStorages.put(key, storage);
                }
            });
        }
    }

    @JsonIgnore
    public String getEncoderOptionsString() {
        if (encoderOptions == null) return "";
        return encoderOptions.toString();
    }

}
