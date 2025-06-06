package org.heigit.ors.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.config.profile.ExtendedStorageName;
import org.heigit.ors.config.profile.ExtendedStorageProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedStoragePropertiesTest {

    Boolean testStorageObjectIsEmpty(ExtendedStorageProperties storage, ArrayList<String> nonNullFields) {
        if (!nonNullFields.contains("enabled")) {
            assertNull(storage.getEnabled(), "enabled should be null");
        } else {
            assertNotNull(storage.getEnabled(), "enabled should not be null");
        }
        if (!nonNullFields.contains("filepath")) {
            assertNull(storage.getFilepath(), "filepath should be null");
        } else {
            assertNotNull(storage.getFilepath(), "filepath should not be null");
        }
        if (!nonNullFields.contains("restrictions")) {
            assertNull(storage.getRestrictions(), "restrictions should be null");
        } else {
            assertNotNull(storage.getRestrictions(), "restrictions should not be null");
        }
        if (!nonNullFields.contains("streets")) {
            assertNull(storage.getStreets(), "streets should be null");
        } else {
            assertNotNull(storage.getStreets(), "streets should not be null");
        }
        if (!nonNullFields.contains("ref_pattern")) {
            assertNull(storage.getRefPattern(), "ref_pattern should be null");
        } else {
            assertNotNull(storage.getRefPattern(), "ref_pattern should not be null");
        }
        if (!nonNullFields.contains("pattern_15min")) {
            assertNull(storage.getPattern15Min(), "pattern_15min should be null");
        } else {
            assertNotNull(storage.getPattern15Min(), "pattern_15min should not be null");
        }
        if (!nonNullFields.contains("radius")) {
            assertNull(storage.getRadius(), "radius should be null");
        } else {
            assertNotNull(storage.getRadius(), "radius should not be null");
        }
        if (!nonNullFields.contains("output_log")) {
            assertNull(storage.getOutputLog(), "output_log should be null");
        } else {
            assertNotNull(storage.getOutputLog(), "output_log should not be null");
        }
        if (!nonNullFields.contains("log_location")) {
            assertNull(storage.getLogLocation(), "log_location should be null");
        } else {
            assertNotNull(storage.getLogLocation(), "log_location should not be null");
        }
        if (!nonNullFields.contains("maximum_slope")) {
            assertNull(storage.getMaximumSlope(), "maximum_slope should be null");
        } else {
            assertNotNull(storage.getMaximumSlope(), "maximum_slope should not be null");
        }
        if (!nonNullFields.contains("preprocessed")) {
            assertNull(storage.getPreprocessed(), "preprocessed should be null");
        } else {
            assertNotNull(storage.getPreprocessed(), "preprocessed should not be null");
        }
        if (!nonNullFields.contains("boundaries")) {
            assertNull(storage.getBoundaries(), "boundaries should be null");
        } else {
            assertNotNull(storage.getBoundaries(), "boundaries should not be null");
        }
        if (!nonNullFields.contains("ids")) {
            assertNull(storage.getIds(), "ids should be null");
        } else {
            assertNotNull(storage.getIds(), "ids should not be null");
        }
        if (!nonNullFields.contains("openborders")) {
            assertNull(storage.getOpenborders(), "openborders should be null");
        } else {
            assertNotNull(storage.getOpenborders(), "openborders should not be null");
        }
        if (!nonNullFields.contains("use_for_warnings")) {
            assertNull(storage.getUseForWarnings(), "use_for_warnings should be null");
        } else {
            assertNotNull(storage.getUseForWarnings(), "use_for_warnings should not be null");
        }
        if (!nonNullFields.contains("kerbs_on_crossings")) {
            assertNull(storage.getKerbsOnCrossings(), "kerbs_on_crossings should be null");
        } else {
            assertNotNull(storage.getKerbsOnCrossings(), "kerbs_on_crossings should not be null");
        }
        return true;
    }

    @Test
    void testDefaultConstructor() {
        ExtendedStorageProperties storage = new ExtendedStorageProperties();
        assertTrue(testStorageObjectIsEmpty(storage, new ArrayList<>()), "Default constructor should initialize all fields to null");
    }

    @Test
    void testStringConstructor() {
        ExtendedStorageProperties storage = new ExtendedStorageProperties("");
        assertTrue(testStorageObjectIsEmpty(storage, new ArrayList<>()), "String constructor should initialize all fields to null");
    }

    @Test
    void testSetRestrictions() {
        HelperClass storage = new HelperClass();

        assertNull(storage.getRestrictions());
        storage.setRestrictions("true");
        assertTrue(storage.getRestrictions());

        storage.setRestrictions("false");
        assertFalse(storage.getRestrictions());
    }

    @Test
    void testSetBoundaries() {
        HelperClass storage = new HelperClass();

        assertNull(storage.getBoundaries());

        storage.setBoundaries(null);
        assertNull(storage.getBoundaries());

        storage.setBoundaries(Paths.get(""));
        assertEquals(Path.of(""), storage.getBoundaries());
    }



    @Test
    void testSetIds() {
        HelperClass storage = new HelperClass();
        Path path = Path.of("/path/to/ids");
        storage.setIds(path);
        assertEquals(path.toAbsolutePath(), storage.getIds(), "setIds should correctly set the absolute path");

        storage.setIds(null);
        assertNull(storage.getIds(), "setIds should set the path to null if the new path is null");

        storage.setIds(Paths.get(""));
        assertEquals(Path.of(""), storage.getIds(), "setIds should not change the path if the new path is empty");
    }

    @Test
    void testSetOpenborders() {
        HelperClass storage = new HelperClass();
        Path path = Path.of("/path/to/openborders");
        storage.setOpenborders(path);
        assertEquals(path.toAbsolutePath(), storage.getOpenborders(), "setOpenborders should correctly set the absolute path");

        storage.setOpenborders(null);
        assertNull(storage.getOpenborders(), "setOpenborders should set the path to null if the new path is null");
        storage.setOpenborders(Paths.get(""));
        assertEquals(Path.of(""), storage.getOpenborders(), "setOpenborders should not change the path if the new path is empty");
    }

    @Test
    void testSetFilepath() {
        HelperClass storage = new HelperClass();
        Path path = Path.of("/path/to/filepath");
        storage.setFilepath(path);
        assertEquals(path.toAbsolutePath(), storage.getFilepath(), "setFilepath should correctly set the path");

        storage.setFilepath(null);
        assertNull(storage.getFilepath(), "setFilepath should set the path to null if the new path is null");

        storage.setFilepath(Paths.get(""));
        assertEquals(Path.of(""), storage.getFilepath(), "setFilepath should not change the path if the new path is empty");
    }

    @Test
    void testSerializationEmptyStorageProducesCorrectJson() throws Exception {
        // Step 1: Create and configure an instance of ExtendedStorage
        ExtendedStorageProperties storage = new ExtendedStorageProperties();

        // Step 2: Serialize the object to JSON
        ObjectMapper mapper = new ObjectMapper();
        String jsonResult = mapper.writeValueAsString(storage);

        // Step 3: Assert JSON structure and values including enabled
        assertEquals(mapper.readTree("{}"), mapper.readTree(jsonResult), "Serialized JSON should match the expected JSON");
    }

    @Test
    void initializeSetsRadiusTo150ForHereTrafficIfNull() {
        ExtendedStorageProperties storage = new ExtendedStorageProperties();
        assertNull(storage.getRadius(), "radius should be null before initialize");

        // Test Default value
        storage.initialize(ExtendedStorageName.HERE_TRAFFIC);
        assertEquals(150, storage.getRadius(), "initialize should set radius to 150 for HERE_TRAFFIC if it is null");

        // Variable set and not the default value. It should be left as is.
        storage = new ExtendedStorageProperties();
        storage.setRadius(100);
        assertEquals(100, storage.getRadius(), "initialize should not change radius if it is not null");
        storage.initialize(ExtendedStorageName.HERE_TRAFFIC);
        assertEquals(100, storage.getRadius(), "initialize should not change radius if it is not null");

        // Assert everything else was set to null
        testStorageObjectIsEmpty(storage, new ArrayList<>() {{
            add("enabled");
            add("radius");
            add("output_log");
            add("log_location");
            add("streets");
            add("ref_pattern");
            add("pattern_15min");
        }});
    }

    @Test
    void testInitializeWithOnlyNullValuesButEnabled() {
        ExtendedStorageProperties storage = new ExtendedStorageProperties();
        storage.initialize(ExtendedStorageName.OSM_ID);
        assertTrue(storage.getEnabled());
        testStorageObjectIsEmpty(storage, new ArrayList<>() {{
            add("enabled");
        }});

        storage.initialize(null);
        assertFalse(storage.getEnabled());
    }

    @Test
    void initializeSetsRestrictionsToTrueForHeavyVehicle() {
        ExtendedStorageProperties storage = new ExtendedStorageProperties();
        assertNull(storage.getRestrictions(), "restrictions should be null before initialize");

        // Test Default value
        storage.initialize(ExtendedStorageName.HEAVY_VEHICLE);
        assertTrue(storage.getRestrictions(), "initialize should set restrictions to true for HEAVY_VEHICLE if it is null");

        // Variable set and not the default value. It should be left as is.
        storage = new ExtendedStorageProperties();
        storage.setEnabled(true);
        storage.setRestrictions(false);
        storage.setFilepath(Path.of("/custom/path.csv"));
        assertFalse(storage.getRestrictions(), "initialize should not change restrictions if it is not null");
        storage.initialize(ExtendedStorageName.HEAVY_VEHICLE);
        assertFalse(storage.getRestrictions(), "initialize should not change restrictions if it is not null");

        // Assert everything else was set to null
        testStorageObjectIsEmpty(storage, new ArrayList<>() {{
            add("enabled");
            add("restrictions");
        }});
    }

    @Test
    void initializeSetsUseForWarningsToTrueForRoadAccessRestrictions() {
        ExtendedStorageProperties storage = new ExtendedStorageProperties();
        assertNull(storage.getUseForWarnings(), "use_for_warnings should be null before initialize");

        // Test Default value
        storage.initialize(ExtendedStorageName.ROAD_ACCESS_RESTRICTIONS);
        assertTrue(storage.getUseForWarnings(), "initialize should set use_for_warnings to true for ROAD_ACCESS_RESTRICTIONS if it is null");

        // Variable set and not the default value. It should be left as is.
        storage = new ExtendedStorageProperties();
        storage.setUseForWarnings(false);
        assertFalse(storage.getUseForWarnings(), "initialize should not change use_for_warnings if it is not null");
        storage.initialize(ExtendedStorageName.ROAD_ACCESS_RESTRICTIONS);
        assertFalse(storage.getUseForWarnings(), "initialize should not change use_for_warnings if it is not null");

        // Assert everything else was set to null
        testStorageObjectIsEmpty(storage, new ArrayList<>() {{
            add("enabled");
            add("use_for_warnings");
        }});
    }

    @Test
    void initializeSetsOutputLogToFalseForHereTrafficIfNull() {
        ExtendedStorageProperties storage = new ExtendedStorageProperties();
        assertNull(storage.getOutputLog(), "output_log should be null before initialize");

        // Test Default value
        storage.initialize(ExtendedStorageName.HERE_TRAFFIC);
        assertFalse(storage.getOutputLog(), "initialize should set output_log to false for HERE_TRAFFIC if it is null");

        // Variable set and not the default value. It should be left as is.
        storage = new ExtendedStorageProperties();
        storage.setOutputLog(true);
        assertTrue(storage.getOutputLog(), "initialize should not change output_log if it is not null");
        storage.initialize(ExtendedStorageName.HERE_TRAFFIC);
        assertTrue(storage.getOutputLog(), "initialize should not change output_log if it is not null");

        // Assert everything else was set to null
        testStorageObjectIsEmpty(storage, new ArrayList<>() {{
            add("enabled");
            add("output_log");
            add("radius");
            add("log_location");
            add("streets");
            add("ref_pattern");
            add("pattern_15min");
        }});
    }

// Hillindex maximum slope

    @Test
    void initializeSetsMaximumSlopeToNullForHillIndexIfNull() {
        ExtendedStorageProperties storage = new ExtendedStorageProperties();
        assertNull(storage.getMaximumSlope(), "maximum_slope should be null before initialize");

        // Test Default value
        storage.initialize(ExtendedStorageName.HILL_INDEX);
        assertNull(storage.getMaximumSlope(), "initialize should set maximum_slope to null for HILL_INDEX if it is null");

        // Variable set and not the default value. It should be left as is.
        storage.setMaximumSlope(5);
        assertEquals(5, storage.getMaximumSlope(), "initialize should not change maximum_slope if it is not null");
        storage.initialize(ExtendedStorageName.HILL_INDEX);
        assertEquals(5, storage.getMaximumSlope(), "initialize should not change maximum_slope if it is not null");

        // Assert everything else was set to null
        testStorageObjectIsEmpty(storage, new ArrayList<>() {{
            add("enabled");
            add("maximum_slope");
        }});
    }

    @Test
    void initializeSetsLogLocationToDefaultForHereTrafficIfNull() {
        ExtendedStorageProperties storage = new ExtendedStorageProperties();
        assertNull(storage.getLogLocation(), "log_location should be null before initialize");

        // Test Default value
        storage.initialize(ExtendedStorageName.HERE_TRAFFIC);
        assertEquals(Path.of("./here_matching.log"), storage.getLogLocation(), "initialize should set log_location to default for HERE_TRAFFIC if it is null");

        // Variable set and not the default value. It should be left as is.
        storage = new ExtendedStorageProperties();
        storage.setLogLocation(Path.of("/custom/path.log"));
        assertEquals(Path.of("/custom/path.log"), storage.getLogLocation(), "initialize should not change log_location if it is not null");
        storage.initialize(ExtendedStorageName.HERE_TRAFFIC);
        assertEquals(Path.of("/custom/path.log"), storage.getLogLocation(), "initialize should not change log_location if it is not null");

        // Assert everything else was set to null
        testStorageObjectIsEmpty(storage, new ArrayList<>() {{
            add("enabled");
            add("radius");
            add("output_log");
            add("log_location");
            add("streets");
            add("ref_pattern");
            add("pattern_15min");
        }});
    }

    @ParameterizedTest
    @CsvSource({"'', '', ''", // All paths null
            "'/custom/path.csv', '', ''", // Only streets set
            "'', '/custom/path.csv', ''", // Only ref_pattern set
            "'', '', '/custom/path.csv'", // Only pattern_15min set
            "'/custom/path.csv', '/custom/path.csv', ''", // streets and ref_pattern set
            "'/custom/path.csv', '', '/custom/path.csv'", // streets and pattern_15min set
            "'', '/custom/path.csv', '/custom/path.csv'", // ref_pattern and pattern_15min set
            "'/custom/path.csv', '/custom/path.csv', '/custom/path.csv'" // All paths set -> Enabled!
    })
    void assertSetHereTrafficPathLogic(String streets, String refPattern, String pattern15min) {
        ExtendedStorageProperties storage;

        // Test null values
        storage = new ExtendedStorageProperties();
        storage.initialize(ExtendedStorageName.HERE_TRAFFIC);
        assertFalse(storage.getEnabled(), "initialize should disable storage if all paths are null");

        // Create JSON string based on parameters
        storage = new ExtendedStorageProperties();
        storage.setStreets(Path.of(streets));
        storage.setRefPattern(Path.of(refPattern));
        storage.setPattern15Min(Path.of(pattern15min));
        storage.initialize(ExtendedStorageName.HERE_TRAFFIC);

        // Check if storage is enabled or disabled based on paths
        boolean shouldBeEnabled = !streets.isEmpty() && !refPattern.isEmpty() && !pattern15min.isEmpty();
        assertEquals(shouldBeEnabled, storage.getEnabled(), "initialize should disable storage if one of the paths is null");

        // Check paths
        Path emptyPath = Path.of("");
        if (streets.isEmpty()) {
            assertEquals(emptyPath, storage.getStreets(), "initialize should set streets to empty path if it is null");
        } else {
            assertEquals(Path.of(streets).toAbsolutePath(), storage.getStreets(), "initialize should not change streets if it is not null");
        }

        if (refPattern.isEmpty()) {
            assertEquals(emptyPath, storage.getRefPattern(), "initialize should set ref_pattern to empty path if it is null");
        } else {
            assertEquals(Path.of(refPattern).toAbsolutePath(), storage.getRefPattern(), "initialize should not change ref_pattern if it is not null");
        }

        if (pattern15min.isEmpty()) {
            assertEquals(emptyPath, storage.getPattern15Min(), "initialize should set pattern_15min to empty path if it is null");
        } else {
            assertEquals(Path.of(pattern15min).toAbsolutePath(), storage.getPattern15Min(), "initialize should not change pattern_15min if it is not null");
        }

        // Assert everything else was set to null
        testStorageObjectIsEmpty(storage, new ArrayList<>() {{
            add("enabled");
            add("streets");
            add("ref_pattern");
            add("pattern_15min");
            add("radius");
            add("output_log");
            add("log_location");
        }});
    }

    // Same for borders
    @ParameterizedTest
    @CsvSource({"'','', '', '', ''", // All paths null
            "'','/custom/path.csv', '', ''", // Only boundaries set
            "'','', '/custom/path.csv', ''", // Only ids set
            "'','', '', '/custom/path.csv'", // Only openborders set
            "'','/custom/path.csv', '/custom/path.csv', ''", // boundaries and ids set
            "'','/custom/path.csv', '', '/custom/path.csv'", // boundaries and openborders set
            "'','', '/custom/path.csv', '/custom/path.csv'", // ids and openborders set
            "'true','', '/custom/path.csv', '/custom/path.csv'",
            "'','/custom/path.csv', '/custom/path.csv', '/custom/path.csv'", // All paths set -> Enabled!
            "'true','/custom/path.csv', '/custom/path.csv', '/custom/path.csv'"
    })
    void assertSetBordersPathLogic(String preprocessed, String boundaries, String ids, String openborders) {
        ExtendedStorageProperties storage;
        // Test null values
        storage = new ExtendedStorageProperties();
        storage.initialize(ExtendedStorageName.BORDERS);
        assertFalse(storage.getEnabled(), "initialize should disable storage if all paths are null");

        // Create JSON string based on parameters
        storage = new ExtendedStorageProperties();
        var isPreprocessed = Boolean.parseBoolean(preprocessed);
        storage.setPreprocessed(isPreprocessed);
        storage.setBoundaries(Path.of(boundaries));
        storage.setIds(Path.of(ids));
        storage.setOpenborders(Path.of(openborders));
        storage.initialize(ExtendedStorageName.BORDERS);

        // Check if storage is enabled or disabled based on paths
        boolean shouldBeEnabled = (isPreprocessed || !boundaries.isEmpty()) && !ids.isEmpty() && !openborders.isEmpty();
        assertEquals(shouldBeEnabled, storage.getEnabled(), "initialize should disable storage if one of the paths is null");

        // Check paths
        Path emptyPath = Path.of("");
        if (boundaries.isEmpty()) {
            assertEquals(emptyPath, storage.getBoundaries(), "initialize should set boundaries to empty path if it is null");
        } else {
            assertEquals(Path.of(boundaries).toAbsolutePath(), storage.getBoundaries(), "initialize should not change boundaries if it is not null");
        }

        if (ids.isEmpty()) {
            assertEquals(emptyPath, storage.getIds(), "initialize should set ids to empty path if it is null");
        } else {
            assertEquals(Path.of(ids).toAbsolutePath(), storage.getIds(), "initialize should not change ids if it is not null");
        }

        if (openborders.isEmpty()) {
            assertEquals(emptyPath, storage.getOpenborders(), "initialize should set openborders to empty path if it is null");
        } else {
            assertEquals(Path.of(openborders).toAbsolutePath(), storage.getOpenborders(), "initialize should not change openborders if it is not null");
        }

        // Assert everything else was set to null
        testStorageObjectIsEmpty(storage, new ArrayList<>() {{
            add("enabled");
            add("preprocessed");
            add("boundaries");
            add("ids");
            add("openborders");
        }});
    }

    @ParameterizedTest
    @ValueSource(strings = {"NoiseIndex", "GreenIndex", "ShadowIndex", "Csv"})
    void initializeDisablesStorageIfFilepathIsNull(String storageName) {
        ExtendedStorageProperties storage = new ExtendedStorageProperties();
        assertNull(storage.getFilepath(), "filepath should be null before initialize");

        // Initialize with empty path
        storage.initialize(ExtendedStorageName.getEnum(storageName));
        assertFalse(storage.getEnabled(), "initialize should disable storage if filepath is null");

        storage = new ExtendedStorageProperties();
        storage.setEnabled(true);
        storage.setFilepath(Path.of(""));
        storage.initialize(ExtendedStorageName.getEnum(storageName));
        assertFalse(storage.getEnabled(), "initialize should disable storage if filepath is empty");
        assertEquals(Path.of(""), storage.getFilepath(), "initialize should not change filepath if it is empty");

        // Initialize with non-empty path
        storage = new ExtendedStorageProperties();
        storage.setFilepath(Path.of("custom/path.csv"));
        storage.initialize(ExtendedStorageName.getEnum(storageName));
        assertTrue(storage.getEnabled(), "initialize should not disable storage if filepath is not null");
        assertEquals(Path.of("custom/path.csv").toAbsolutePath(), storage.getFilepath(), "initialize should not change filepath if it is not null");

        // Assert everything else was set to null
        testStorageObjectIsEmpty(storage, new ArrayList<>() {{
            add("enabled");
            add("filepath");
        }});
    }

    @Test
    void testInitializeWheelchair() {
        ExtendedStorageProperties storage = new ExtendedStorageProperties();
        storage.initialize(ExtendedStorageName.WHEELCHAIR);
        assertTrue(storage.getEnabled());
        testStorageObjectIsEmpty(storage, new ArrayList<>() {{
            add("enabled");
            add("kerbs_on_crossings");
        }});

        storage.setKerbsOnCrossings(false);
        storage.initialize(ExtendedStorageName.WHEELCHAIR);
        assertFalse(storage.getKerbsOnCrossings());
        testStorageObjectIsEmpty(storage, new ArrayList<>() {{
            add("enabled");
            add("kerbs_on_crossings");
        }});
    }

    @Test
    void testInitializeEmptyHereLogLocation() {
        ExtendedStorageProperties storage = new ExtendedStorageProperties();
        storage.setLogLocation(Path.of(""));
        storage.initialize(ExtendedStorageName.HERE_TRAFFIC);
        assertEquals(Path.of("./here_matching.log"), storage.getLogLocation(), "initialize should set log_location to default for HERE_TRAFFIC if it is null");

        storage.setLogLocation(null);
        storage.initialize(ExtendedStorageName.HERE_TRAFFIC);
        assertEquals(Path.of("./here_matching.log"), storage.getLogLocation(), "initialize should set log_location to default for HERE_TRAFFIC if it is null");
    }

    static class HelperClass extends ExtendedStorageProperties {
        private Map<String, ExtendedStorageProperties> extendedStorage;

        public HelperClass() {
            super();
        }

        public void setRestrictions(String restrictions) {
            super.setRestrictions(Boolean.parseBoolean(restrictions));
        }

        @Override
        public void setBoundaries(Path boundaries) {
            super.setBoundaries(boundaries);
        }

        @Override
        public void setIds(Path ids) {
            super.setIds(ids);
        }

        @Override
        public void setOpenborders(Path openborders) {
            super.setOpenborders(openborders);
        }

        @Override
        public void setFilepath(Path filepath) {
            super.setFilepath(filepath);
        }

        @JsonProperty("ext_storages")
        public Map<String, ExtendedStorageProperties> getExtendedStorage() {
            return this.extendedStorage;
        }

        @JsonSetter("ext_storages")
        public void setExtendedStorage(Map<String, ExtendedStorageProperties> extendedStorage) {
            this.extendedStorage = extendedStorage;
        }
    }
}
