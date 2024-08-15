package org.heigit.ors.config.profile.storages;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.heigit.ors.config.utils.PathDeserializer;
import org.heigit.ors.config.utils.PathSerializer;

import java.nio.file.Path;

/**
 * This the base class for the extended storage configuration. It contains a boolean field to enable or disable the storage.
 */
@Getter
@Setter(AccessLevel.PACKAGE)
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtendedStorage {
    @JsonIgnore
    private static final Logger LOGGER = Logger.getLogger(ExtendedStorage.class);

    @JsonIgnore
    @Setter(AccessLevel.PUBLIC)
    private ExtendedStorageName storageName;

    // Relevant for all
    @JsonProperty
    private Boolean enabled;

    // Relevant for most index storages
    @JsonProperty
    @JsonSerialize(using = PathSerializer.class)
    @JsonDeserialize(using = PathDeserializer.class)
    private Path filepath;

    // Relevant for HGV profile
    @JsonProperty
    private Boolean restrictions;

    @JsonProperty
    // Relevant for HereTraffic profile
    @JsonSerialize(using = PathSerializer.class)
    @JsonDeserialize(using = PathDeserializer.class)
    private Path streets;
    @JsonSerialize(using = PathSerializer.class)
    @JsonDeserialize(using = PathDeserializer.class)
    private Path ref_pattern;
    @JsonProperty
    @JsonSerialize(using = PathSerializer.class)
    @JsonDeserialize(using = PathDeserializer.class)
    private Path pattern_15min;
    @JsonProperty
    private Integer radius;
    @JsonProperty
    private Boolean output_log;
    @JsonProperty
    @JsonSerialize(using = PathSerializer.class)
    @JsonDeserialize(using = PathDeserializer.class)
    private Path log_location;

    // Relevant for HillIndex
    @JsonProperty("maximum_slope")
    private Integer maximumSlope;

    // Relevant for borders
    @JsonProperty
    @JsonSerialize(using = PathSerializer.class)
    @JsonDeserialize(using = PathDeserializer.class)
    private Path boundaries;
    @JsonProperty
    @JsonSerialize(using = PathSerializer.class)
    @JsonDeserialize(using = PathDeserializer.class)
    private Path ids;
    @JsonProperty
    @JsonSerialize(using = PathSerializer.class)
    @JsonDeserialize(using = PathDeserializer.class)
    private Path openborders;

    // Relevant for RoadAccessRestrictions
    @JsonProperty("use_for_warnings")
    @Setter(AccessLevel.PROTECTED)
    private Boolean use_for_warnings;

    // Relevant for Wheelchair
    @JsonProperty("KerbsOnCrossings")
    private Boolean kerbs_on_crossings;

    @Getter
    @Setter
    private String ghProfile;

    @JsonCreator
    public ExtendedStorage() {
    }

    @JsonCreator
    public ExtendedStorage(String ignoredEmpty) {
    }

    @JsonIgnore
    public ExtendedStorage(ExtendedStorageName storageName) {
        super();
        this.initialize(storageName);
    }

    @JsonSetter
    protected void setRestrictions(String restrictions) {
        this.restrictions = Boolean.parseBoolean(restrictions);
    }

    @JsonIgnore
    public void initialize(ExtendedStorageName storageName) {
        if (storageName == null) {
            this.setEnabled(false);
            return;
        }

        this.storageName = storageName;

        if (enabled == null) {
            enabled = true;
        }

        if (storageName == ExtendedStorageName.HEAVY_VEHICLE) {
            if (restrictions == null) {
                restrictions = true;
            }
        }

        if (storageName == ExtendedStorageName.HILL_INDEX) {
            if (maximumSlope == null) {
                this.maximumSlope = null;
            }
        }

        if (storageName == ExtendedStorageName.ROAD_ACCESS_RESTRICTIONS) {
            if (use_for_warnings == null) {
                this.use_for_warnings = true;
            }
        }

        if (storageName == ExtendedStorageName.WHEELCHAIR) {
            if (kerbs_on_crossings == null) {
                this.kerbs_on_crossings = true;
            }
        }

        // Avoid initializing this multiple times
        final Path emptyPath = Path.of("");
        if (storageName == ExtendedStorageName.NOISE_INDEX || storageName == ExtendedStorageName.GREEN_INDEX || storageName == ExtendedStorageName.SHADOW_INDEX) {
            if (filepath == null || filepath.equals(emptyPath)) {
                LOGGER.warn("Storage " + storageName + " is missing filepath. Disabling storage.");
                enabled = false;
            }
        }

        if (storageName == ExtendedStorageName.BORDERS) {
            if (boundaries == null || boundaries.equals(emptyPath)) {
                LOGGER.warn("Storage " + storageName + " is missing boundaries. Disabling storage.");
                enabled = false;
            }
            if (ids == null || ids.equals(emptyPath)) {
                LOGGER.warn("Storage " + storageName + " is missing ids. Disabling storage.");
                enabled = false;
            }
            if (openborders == null || openborders.equals(emptyPath)) {
                LOGGER.warn("Storage " + storageName + " is missing openborders. Disabling storage.");
                enabled = false;
            }
        }

        // Here traffic if streets, ref_pattern or pattern_15min is not set, disable storage
        if (storageName == ExtendedStorageName.HERE_TRAFFIC) {
            if (radius == null) {
                this.radius = 150;
            }

            if (output_log == null) {
                this.output_log = false;
            }

            if (log_location == null || log_location.equals(emptyPath)) {
                // TODO: check if we want to keep this functionality
                this.log_location = Path.of("./here_matching.log");
            }

            if (streets == null || streets.equals(emptyPath)) {
                LOGGER.warn("Storage " + storageName + " is missing streets. Disabling storage.");
                enabled = false;
            }
            if (ref_pattern == null || ref_pattern.equals(emptyPath)) {
                LOGGER.warn("Storage " + storageName + " is missing ref_pattern. Disabling storage.");
                enabled = false;
            }
            if (pattern_15min == null || pattern_15min.equals(emptyPath)) {
                LOGGER.warn("Storage " + storageName + " is missing pattern_15min. Disabling storage.");
                enabled = false;
            }
        }
    }

    @JsonSetter("boundaries")
    protected void setBoundaries(Path boundaries) {
        this.boundaries = boundaries != null && !boundaries.toString().isEmpty() ? boundaries.toAbsolutePath() : boundaries;
    }

    @JsonSetter("ids")
    @JsonDeserialize(using = PathDeserializer.class)
    protected void setIds(Path ref_pattern) {
        this.ids = ref_pattern != null && !ref_pattern.toString().isEmpty() ? ref_pattern.toAbsolutePath() : ref_pattern;
    }

    @JsonSetter("openborders")
    @JsonDeserialize(using = PathDeserializer.class)
    protected void setOpenborders(Path openborders) {
        this.openborders = openborders != null && !openborders.toString().isEmpty() ? openborders.toAbsolutePath() : openborders;
    }

    @JsonSetter("filepath")
    @JsonDeserialize(using = PathDeserializer.class)
    protected void setFilepath(Path filepath) {
        this.filepath = filepath != null && !filepath.toString().isEmpty() ? filepath.toAbsolutePath() : filepath;
    }

}


