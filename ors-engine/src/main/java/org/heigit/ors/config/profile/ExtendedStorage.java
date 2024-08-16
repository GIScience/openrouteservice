package org.heigit.ors.config.profile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.utils.PathSerializer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This the base class for the extended storage configuration. It contains a boolean field to enable or disable the storage.
 */
@Getter
@Setter
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtendedStorage {
    @JsonIgnore
    private static final Logger LOGGER = Logger.getLogger(ExtendedStorage.class);

    @JsonIgnore
    private ExtendedStorageName storageName;

    // Relevant for all
    @JsonProperty
    private Boolean enabled;

    // Relevant for most index storages
    @JsonProperty
    @JsonSerialize(using = PathSerializer.class)
    private Path filepath;

    // Relevant for HGV profile
    @JsonProperty
    private Boolean restrictions;

    @JsonProperty
    // Relevant for HereTraffic profile
    @JsonSerialize(using = PathSerializer.class)
    private Path streets;
    @JsonSerialize(using = PathSerializer.class)
    private Path ref_pattern;
    @JsonProperty
    @JsonSerialize(using = PathSerializer.class)
    private Path pattern_15min;
    @JsonProperty
    private Integer radius;
    @JsonProperty
    private Boolean output_log;
    @JsonProperty
    @JsonSerialize(using = PathSerializer.class)
    private Path log_location;

    // Relevant for HillIndex
    @JsonProperty("maximum_slope")
    private Integer maximumSlope;

    // Relevant for borders
    @JsonProperty
    @JsonSerialize(using = PathSerializer.class)
    private Path boundaries;
    @JsonProperty
    @JsonSerialize(using = PathSerializer.class)
    private Path ids;
    @JsonProperty
    @JsonSerialize(using = PathSerializer.class)
    private Path openborders;

    // Relevant for RoadAccessRestrictions
    @JsonProperty("use_for_warnings")
    private Boolean use_for_warnings;

    // Relevant for Wheelchair
    @JsonProperty("KerbsOnCrossings")
    private Boolean kerbs_on_crossings;

    @Getter
    @Setter
    @JsonIgnore
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
            this.ref_pattern = null;
        }
        if (!excludedProperties.contains("pattern_15min")) {
            this.pattern_15min = null;
        }
        if (!excludedProperties.contains("radius")) {
            this.radius = null;
        }
        if (!excludedProperties.contains("output_log")) {
            this.output_log = null;
        }
        if (!excludedProperties.contains("log_location")) {
            this.log_location = null;
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
            this.use_for_warnings = null;
        }
        if (!excludedProperties.contains("kerbs_on_crossings")) {
            this.kerbs_on_crossings = null;
        }
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
            if (use_for_warnings == null) {
                this.use_for_warnings = true;
            }
            nonNullableProperties.add("use_for_warnings");
        } else if (storageName == ExtendedStorageName.WHEELCHAIR) {
            if (kerbs_on_crossings == null) {
                this.kerbs_on_crossings = true;
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
                streets = Path.of("");
            } else {
                streets = streets.toAbsolutePath();
            }
            if (ref_pattern == null || ref_pattern.equals(emptyPath)) {
                LOGGER.warn("Storage " + storageName + " is missing ref_pattern. Disabling storage.");
                enabled = false;
                ref_pattern = Path.of("");
            } else {
                ref_pattern = ref_pattern.toAbsolutePath();
            }
            if (pattern_15min == null || pattern_15min.equals(emptyPath)) {
                LOGGER.warn("Storage " + storageName + " is missing pattern_15min. Disabling storage.");
                enabled = false;
                pattern_15min = Path.of("");
            } else {
                pattern_15min = pattern_15min.toAbsolutePath();
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

    public static Map<String, ExtendedStorage> getDefaultExtStoragesMap(EncoderNameEnum encoderName) {
        // TODO: check why Julian has changed defaults and if the changes are good
        Map<String, ExtendedStorage> extStorages = new LinkedHashMap<>();
        switch (encoderName) {
            case DRIVING_CAR -> {
                extStorages.put("WayCategory", new ExtendedStorage(ExtendedStorageName.WAY_CATEGORY));
                extStorages.put("WaySurfaceType", new ExtendedStorage(ExtendedStorageName.WAY_SURFACE_TYPE));
                extStorages.put("HeavyVehicle", new ExtendedStorage(ExtendedStorageName.HEAVY_VEHICLE));
                extStorages.put("RoadAccessRestrictions", new ExtendedStorage(ExtendedStorageName.ROAD_ACCESS_RESTRICTIONS));
            }
            case DRIVING_HGV -> {
                extStorages.put("WayCategory", new ExtendedStorage(ExtendedStorageName.WAY_CATEGORY));
                extStorages.put("WaySurfaceType", new ExtendedStorage(ExtendedStorageName.WAY_SURFACE_TYPE));
                extStorages.put("HeavyVehicle", new ExtendedStorage(ExtendedStorageName.HEAVY_VEHICLE));
                extStorages.put("Tollways", new ExtendedStorage(ExtendedStorageName.TOLLWAYS));
            }
            case CYCLING_REGULAR, CYCLING_MOUNTAIN, CYCLING_ROAD, CYCLING_ELECTRIC, FOOT_WALKING, FOOT_HIKING -> {
                extStorages.put("WayCategory", new ExtendedStorage(ExtendedStorageName.WAY_CATEGORY));
                extStorages.put("WaySurfaceType", new ExtendedStorage(ExtendedStorageName.WAY_SURFACE_TYPE));
                extStorages.put("HillIndex", new ExtendedStorage(ExtendedStorageName.HILL_INDEX));
                extStorages.put("TrailDifficulty", new ExtendedStorage(ExtendedStorageName.TRAIL_DIFFICULTY));
            }
            case WHEELCHAIR -> {
                extStorages.put("WayCategory", new ExtendedStorage(ExtendedStorageName.WAY_CATEGORY));
                extStorages.put("WaySurfaceType", new ExtendedStorage(ExtendedStorageName.WAY_SURFACE_TYPE));
                extStorages.put("Wheelchair", new ExtendedStorage(ExtendedStorageName.WHEELCHAIR));
                extStorages.put("OsmId", new ExtendedStorage(ExtendedStorageName.OSM_ID));
            }
            case PUBLIC_TRANSPORT -> {
            }
            default -> {
                extStorages.put("WayCategory", new ExtendedStorage(ExtendedStorageName.WAY_CATEGORY));
                extStorages.put("WaySurfaceType", new ExtendedStorage(ExtendedStorageName.WAY_SURFACE_TYPE));
            }
        }
        return extStorages;
    }
}


