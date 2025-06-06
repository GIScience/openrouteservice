package org.heigit.ors.config.profile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;

import static java.util.Optional.ofNullable;

/**
 * This the base class for the extended storage configuration. It contains a boolean field to enable or disable the storage.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtendedStorageProperties {
    @JsonIgnore
    private static final Logger LOGGER = Logger.getLogger(ExtendedStorageProperties.class);

    // Relevant for all
    @JsonProperty("enabled")
    private Boolean enabled;

    // Relevant for most index storages
    @JsonIgnore
    private Path filepath;

    // Relevant for HGV profile
    @JsonProperty("restrictions")
    private Boolean restrictions;

    // Relevant for HereTraffic profile
    @JsonIgnore
    private Path streets;
    @JsonIgnore
    private Path refPattern;
    @JsonIgnore
    private Path pattern15Min;
    @JsonIgnore
    private Integer radius;
    @JsonIgnore
    private Boolean outputLog;
    @JsonIgnore
    private Path logLocation;

    // Relevant for HillIndex
    @JsonProperty("maximum_slope")
    private Integer maximumSlope;

    // Relevant for borders
    @JsonIgnore
    private Path boundaries;
    @JsonIgnore
    private Path ids;
    @JsonIgnore
    private Path openborders;
    @JsonIgnore
    private Boolean preprocessed = false;

    // Relevant for RoadAccessRestrictions
    @JsonProperty("use_for_warnings")
    private Boolean useForWarnings;

    // Relevant for Wheelchair
    @JsonProperty("kerbs_on_crossings")
    private Boolean kerbsOnCrossings;

    // Relevant for Csv
    @JsonProperty("column_names")
    private String[] columnNames;

    @JsonIgnore
    private String ghProfile;

    @JsonCreator
    public ExtendedStorageProperties() {
    }

    @JsonCreator
    public ExtendedStorageProperties(String ignoredEmpty) {
        // This constructor is used to create an empty object for the purpose of ignoring it in the JSON serialization.
    }

    public void merge(ExtendedStorageProperties other) {
        enabled = ofNullable(this.enabled).orElse(other.enabled);
        filepath = ofNullable(this.filepath).orElse(other.filepath);
        restrictions = ofNullable(this.restrictions).orElse(other.restrictions);
        streets = ofNullable(this.streets).orElse(other.streets);
        refPattern = ofNullable(this.refPattern).orElse(other.refPattern);
        pattern15Min = ofNullable(this.pattern15Min).orElse(other.pattern15Min);
        radius = ofNullable(this.radius).orElse(other.radius);
        outputLog = ofNullable(this.outputLog).orElse(other.outputLog);
        logLocation = ofNullable(this.logLocation).orElse(other.logLocation);
        maximumSlope = ofNullable(this.maximumSlope).orElse(other.maximumSlope);
        boundaries = ofNullable(this.boundaries).orElse(other.boundaries);
        ids = ofNullable(this.ids).orElse(other.ids);
        openborders = ofNullable(this.openborders).orElse(other.openborders);
        useForWarnings = ofNullable(this.useForWarnings).orElse(other.useForWarnings);
        kerbsOnCrossings = ofNullable(this.kerbsOnCrossings).orElse(other.kerbsOnCrossings);
    }


    // Write a function that sets every property to null but allows to set excluded properties to a value
    private void setAllPropertiesToNull(ArrayList<String> excludedProperties) {
        if (!excludedProperties.contains("filepath")) {
            this.filepath = null;
        }
        if (!excludedProperties.contains("restrictions")) {
            this.restrictions = null;
        }
        if (!excludedProperties.contains("streets")) {
            this.streets = null;
        }
        if (!excludedProperties.contains("ref_pattern")) {
            this.refPattern = null;
        }
        if (!excludedProperties.contains("pattern_15min")) {
            this.pattern15Min = null;
        }
        if (!excludedProperties.contains("radius")) {
            this.radius = null;
        }
        if (!excludedProperties.contains("output_log")) {
            this.outputLog = null;
        }
        if (!excludedProperties.contains("log_location")) {
            this.logLocation = null;
        }
        if (!excludedProperties.contains("maximum_slope")) {
            this.maximumSlope = null;
        }
        if (!excludedProperties.contains("boundaries")) {
            this.boundaries = null;
        }
        if (!excludedProperties.contains("ids")) {
            this.ids = null;
        }
        if (!excludedProperties.contains("openborders")) {
            this.openborders = null;
        }
        if (!excludedProperties.contains("use_for_warnings")) {
            this.useForWarnings = null;
        }
        if (!excludedProperties.contains("kerbs_on_crossings")) {
            this.kerbsOnCrossings = null;
        }
    }

    public void initialize(ExtendedStorageName storageName) {
        if (storageName == null) {
            this.setEnabled(false);
            return;
        }

        if (enabled == null) {
            enabled = true;
        }

        ArrayList<String> nonNullableProperties = new ArrayList<>();

        switch (storageName) {
            case HEAVY_VEHICLE:
                initializeHeavyVehicle(nonNullableProperties);
                break;
            case HILL_INDEX:
                initializeHillIndex(nonNullableProperties);
                break;
            case ROAD_ACCESS_RESTRICTIONS:
                initializeRoadAccessRestrictions(nonNullableProperties);
                break;
            case WHEELCHAIR:
                initializeWheelchair(nonNullableProperties);
                break;
            case NOISE_INDEX, GREEN_INDEX, SHADOW_INDEX, CSV:
                initializeFilepath(nonNullableProperties, storageName);
                break;
            case BORDERS:
                initializeBorders(nonNullableProperties, storageName);
                break;
            case HERE_TRAFFIC:
                initializeHereTraffic(nonNullableProperties, storageName);
                break;
            default:
        }
        setAllPropertiesToNull(nonNullableProperties);
    }

    private void initializeHeavyVehicle(ArrayList<String> nonNullableProperties) {
        if (restrictions == null) {
            restrictions = true;
        }
        nonNullableProperties.add("restrictions");
    }

    private void initializeHillIndex(ArrayList<String> nonNullableProperties) {
        nonNullableProperties.add("maximum_slope");
    }

    private void initializeRoadAccessRestrictions(ArrayList<String> nonNullableProperties) {
        if (useForWarnings == null) {
            this.useForWarnings = true;
        }
        nonNullableProperties.add("use_for_warnings");
    }

    private void initializeWheelchair(ArrayList<String> nonNullableProperties) {
        if (kerbsOnCrossings == null) {
            this.kerbsOnCrossings = true;
        }
        nonNullableProperties.add("kerbs_on_crossings");
    }

    private void initializeFilepath(ArrayList<String> nonNullableProperties, ExtendedStorageName storageName) {
        final Path emptyPath = Path.of("");
        if (filepath == null || filepath.equals(emptyPath)) {
            LOGGER.warn("Storage %s is missing filepath. Disabling storage.".formatted(storageName));
            enabled = false;
            filepath = Path.of("");
        } else {
            filepath = filepath.toAbsolutePath();
        }
        nonNullableProperties.add("filepath");
    }

    private void initializeBorders(ArrayList<String> nonNullableProperties, ExtendedStorageName storageName) {
        final Path emptyPath = Path.of("");
        if (boundaries == null || boundaries.equals(emptyPath)) {
            LOGGER.warn("Storage %s is missing boundaries. Disabling storage.".formatted(storageName));
            enabled = false;
            boundaries = Path.of("");
        } else {
            boundaries = boundaries.toAbsolutePath();
        }
        if (ids == null || ids.equals(emptyPath)) {
            LOGGER.warn("Storage %s is missing ids. Disabling storage.".formatted(storageName));
            enabled = false;
            ids = Path.of("");
        } else {
            ids = ids.toAbsolutePath();
        }
        if (openborders == null || openborders.equals(emptyPath)) {
            LOGGER.warn("Storage %s is missing openborders. Disabling storage.".formatted(storageName));
            enabled = false;
            openborders = Path.of("");
        } else {
            openborders = openborders.toAbsolutePath();
        }
        nonNullableProperties.add("boundaries");
        nonNullableProperties.add("ids");
        nonNullableProperties.add("openborders");
    }

    private void initializeHereTraffic(ArrayList<String> nonNullableProperties, ExtendedStorageName storageName) {
        final Path emptyPath = Path.of("");
        if (radius == null) {
            this.radius = 150;
        }
        if (outputLog == null) {
            this.outputLog = false;
        }
        if (logLocation == null || logLocation.equals(emptyPath)) {
            this.logLocation = Path.of("./here_matching.log");
        }
        if (streets == null || streets.equals(emptyPath)) {
            LOGGER.warn("Storage %s is missing streets. Disabling storage.".formatted(storageName));
            enabled = false;
            streets = Path.of("");
        } else {
            streets = streets.toAbsolutePath();
        }
        if (refPattern == null || refPattern.equals(emptyPath)) {
            LOGGER.warn("Storage %s is missing ref_pattern. Disabling storage.".formatted(storageName));
            enabled = false;
            refPattern = Path.of("");
        } else {
            refPattern = refPattern.toAbsolutePath();
        }
        if (pattern15Min == null || pattern15Min.equals(emptyPath)) {
            LOGGER.warn("Storage %s is missing pattern_15min. Disabling storage.".formatted(storageName));
            enabled = false;
            pattern15Min = Path.of("");
        } else {
            pattern15Min = pattern15Min.toAbsolutePath();
        }
        nonNullableProperties.add("radius");
        nonNullableProperties.add("output_log");
        nonNullableProperties.add("log_location");
        nonNullableProperties.add("streets");
        nonNullableProperties.add("ref_pattern");
        nonNullableProperties.add("pattern_15min");
    }
}


