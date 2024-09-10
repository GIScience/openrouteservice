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
public class ExtendedStorage {
    @JsonIgnore
    private static final Logger LOGGER = Logger.getLogger(ExtendedStorage.class);

    @JsonIgnore
    private ExtendedStorageName storageName;

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

    // Relevant for RoadAccessRestrictions
    @JsonProperty("use_for_warnings")
    private Boolean useForWarnings;

    // Relevant for Wheelchair
    @JsonProperty("kerbs_on_crossings")
    private Boolean kerbsOnCrossings;

    @JsonIgnore
    private String ghProfile;

    @JsonCreator
    public ExtendedStorage() {
    }

    @JsonCreator
    public ExtendedStorage(String ignoredEmpty) {
    }

    public void merge(ExtendedStorage other) {
        storageName = ofNullable(this.storageName).orElse(other.storageName);
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

        this.storageName = storageName;

        if (enabled == null) {
            enabled = true;
        }

        // Avoid initializing this multiple times
        final Path emptyPath = Path.of("");

        ArrayList<String> nonNullableProperties = new ArrayList<>();

        if (storageName == ExtendedStorageName.HEAVY_VEHICLE) {
            if (restrictions == null) {
                restrictions = true;
            }
            nonNullableProperties.add("restrictions");
        } else if (storageName == ExtendedStorageName.HILL_INDEX) {
            if (maximumSlope == null) {
                this.maximumSlope = null;
            }
            nonNullableProperties.add("maximum_slope");
        } else if (storageName == ExtendedStorageName.ROAD_ACCESS_RESTRICTIONS) {
            if (useForWarnings == null) {
                this.useForWarnings = true;
            }
            nonNullableProperties.add("use_for_warnings");
        } else if (storageName == ExtendedStorageName.WHEELCHAIR) {
            if (kerbsOnCrossings == null) {
                this.kerbsOnCrossings = true;
            }
            nonNullableProperties.add("kerbs_on_crossings");
        }

        if (storageName == ExtendedStorageName.NOISE_INDEX || storageName == ExtendedStorageName.GREEN_INDEX || storageName == ExtendedStorageName.SHADOW_INDEX || storageName == ExtendedStorageName.CSV) {
            if (filepath == null || filepath.equals(emptyPath)) {
                LOGGER.warn("Storage " + storageName + " is missing filepath. Disabling storage.");
                enabled = false;
                filepath = Path.of("");
            } else {
                filepath = filepath.toAbsolutePath();
            }
            nonNullableProperties.add("filepath");
        } else if (storageName == ExtendedStorageName.BORDERS) {
            if (boundaries == null || boundaries.equals(emptyPath)) {
                LOGGER.warn("Storage " + storageName + " is missing boundaries. Disabling storage.");
                enabled = false;
                boundaries = Path.of("");
            } else {
                boundaries = boundaries.toAbsolutePath();
            }
            if (ids == null || ids.equals(emptyPath)) {
                LOGGER.warn("Storage " + storageName + " is missing ids. Disabling storage.");
                enabled = false;
                ids = Path.of("");
            } else {
                ids = ids.toAbsolutePath();
            }
            if (openborders == null || openborders.equals(emptyPath)) {
                LOGGER.warn("Storage " + storageName + " is missing openborders. Disabling storage.");
                enabled = false;
                openborders = Path.of("");
            } else {
                openborders = openborders.toAbsolutePath();
            }
            nonNullableProperties.add("boundaries");
            nonNullableProperties.add("ids");
            nonNullableProperties.add("openborders");
        } else if (storageName == ExtendedStorageName.HERE_TRAFFIC) {
            // Here traffic if streets, ref_pattern or pattern_15min is not set, disable storage
            if (radius == null) {
                this.radius = 150;
            }

            if (outputLog == null) {
                this.outputLog = false;
            }

            if (logLocation == null || logLocation.equals(emptyPath)) {
                // TODO: check if we want to keep this functionality
                this.logLocation = Path.of("./here_matching.log");
            }

            if (streets == null || streets.equals(emptyPath)) {
                LOGGER.warn("Storage " + storageName + " is missing streets. Disabling storage.");
                enabled = false;
                streets = Path.of("");
            } else {
                streets = streets.toAbsolutePath();
            }
            if (refPattern == null || refPattern.equals(emptyPath)) {
                LOGGER.warn("Storage " + storageName + " is missing ref_pattern. Disabling storage.");
                enabled = false;
                refPattern = Path.of("");
            } else {
                refPattern = refPattern.toAbsolutePath();
            }
            if (pattern15Min == null || pattern15Min.equals(emptyPath)) {
                LOGGER.warn("Storage " + storageName + " is missing pattern_15min. Disabling storage.");
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
        setAllPropertiesToNull(nonNullableProperties);
    }
}


