package org.heigit.ors.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.config.profile.storages.ExtendedStorage;
import org.heigit.ors.config.profile.storages.ExtendedStorageName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedStorageTest {
    ObjectMapper mapper = new ObjectMapper();

    Boolean testStorageObjectIsEmpty(ExtendedStorage storage, ArrayList<String> nonNullFields) {
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
            assertNull(storage.getRef_pattern(), "ref_pattern should be null");
        } else {
            assertNotNull(storage.getRef_pattern(), "ref_pattern should not be null");
        }
        if (!nonNullFields.contains("pattern_15min")) {
            assertNull(storage.getPattern_15min(), "pattern_15min should be null");
        } else {
            assertNotNull(storage.getPattern_15min(), "pattern_15min should not be null");
        }
        if (!nonNullFields.contains("radius")) {
            assertNull(storage.getRadius(), "radius should be null");
        } else {
            assertNotNull(storage.getRadius(), "radius should not be null");
        }
        if (!nonNullFields.contains("output_log")) {
            assertNull(storage.getOutput_log(), "output_log should be null");
        } else {
            assertNotNull(storage.getOutput_log(), "output_log should not be null");
        }
        if (!nonNullFields.contains("log_location")) {
            assertNull(storage.getLog_location(), "log_location should be null");
        } else {
            assertNotNull(storage.getLog_location(), "log_location should not be null");
        }
        if (!nonNullFields.contains("maximum_slope")) {
            assertNull(storage.getMaximumSlope(), "maximum_slope should be null");
        } else {
            assertNotNull(storage.getMaximumSlope(), "maximum_slope should not be null");
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
            assertNull(storage.getUse_for_warnings(), "use_for_warnings should be null");
        } else {
            assertNotNull(storage.getUse_for_warnings(), "use_for_warnings should not be null");
        }
        return true;
    }

    @ParameterizedTest
    @ValueSource(strings = {"WayCategory", "WaySurfaceType", "HeavyVehicle", "RoadAccessRestrictions", "Tollways", "HillIndex", "TrailDifficulty", "Wheelchair", "OsmId"})
    void testStorageName(String storageName) {
        ExtendedStorage storage = new ExtendedStorage();
        storage.initialize(ExtendedStorageName.getEnum(storageName));
        assertEquals(ExtendedStorageName.getEnum(storageName), storage.getStorageName(), "Storage name should be set correctly");
    }

    @Test
    void testDefaultConstructor() {
        ExtendedStorage storage = new ExtendedStorage();
        assertTrue(testStorageObjectIsEmpty(storage, new ArrayList<>()), "Default constructor should initialize all fields to null");
    }

    @Test
    void testStringConstructor() {
        ExtendedStorage storage = new ExtendedStorage("");
        assertTrue(testStorageObjectIsEmpty(storage, new ArrayList<>()), "String constructor should initialize all fields to null");
    }

    @Test
    void testEnumConstructor() {
        ExtendedStorage storage = new ExtendedStorage(ExtendedStorageName.WAY_CATEGORY);
        assertTrue(testStorageObjectIsEmpty(storage, new ArrayList<>() {{
            add("enabled");
        }}), "Enum constructor should initialize all fields to null except enabled");
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
        Path testBoundariesPath = Paths.get("src/test/resources/boundaries.csv");
        storage.setBoundaries(testBoundariesPath);
        assertEquals(testBoundariesPath.toAbsolutePath(), storage.getBoundaries());

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
        ExtendedStorage storage = new ExtendedStorage();

        // Step 2: Serialize the object to JSON
        ObjectMapper mapper = new ObjectMapper();
        String jsonResult = mapper.writeValueAsString(storage);

        // Step 3: Assert JSON structure and values including enabled
        assertEquals(mapper.readTree("{}"), mapper.readTree(jsonResult), "Serialized JSON should match the expected JSON");
    }

    @Test
    void testSerializationNonEmptyStorageProducesCorrectJson() throws Exception {

        //language=JSON
        String expectedJson = """
                {
                     "enabled":true,
                     "restrictions":true,
                     "streets":"/src/test/resources/streets.csv",
                     "ref_pattern":"/src/test/resources/ref_pattern.csv",
                     "pattern_15min":"/src/test/resources/pattern_15min.csv",
                     "radius":10,
                     "output_log":true,
                     "log_location":"/src/test/resources/log_location.csv",
                     "filepath":"/src/test/resources/filepath.csv",
                     "maximum_slope":5,
                     "boundaries":"/src/test/resources/boundaries.csv",
                     "ids":"/src/test/resources/ids.csv",
                     "openborders":"/src/test/resources/openborders.csv",
                     "use_for_warnings":true,
                     "KerbsOnCrossings":true
                 }
                """;
        // Serialize the object to JSON
        ExtendedStorage storage = mapper.readValue(expectedJson, ExtendedStorage.class);

        // Step 2: Serialize the object to JSON
        String jsonResult = mapper.writeValueAsString(storage);
        // Step 3: Assert JSON structure and values including enabled
        assertEquals(mapper.readTree(expectedJson), mapper.readTree(jsonResult), "Serialized JSON should match the expected JSON");
    }

    static class HelperClass extends ExtendedStorage {
        public HelperClass() {
            super();
        }

        public void setRestrictions(String restrictions) {
            super.setRestrictions(restrictions);
        }

        public void setBoundaries(Path boundaries) {
            super.setBoundaries(boundaries);
        }

        public void setIds(Path ids) {
            super.setIds(ids);
        }

        public void setOpenborders(Path openborders) {
            super.setOpenborders(openborders);
        }

        public void setFilepath(Path filepath) {
            super.setFilepath(filepath);
        }
    }

    @Test
    void testInitializeWithOnlyNullValuesButEnabled() {
        ExtendedStorage storage = new ExtendedStorage();
        storage.initialize(ExtendedStorageName.OSM_ID);
        assertTrue(storage.getEnabled());
        testStorageObjectIsEmpty(storage, new ArrayList<>() {{
            add("enabled");
        }});

        storage.initialize(null);
        assertFalse(storage.getEnabled());
    }

    @Test
    void initializeSetsRestrictionsToTrueForHeavyVehicle() throws JsonProcessingException {
        ExtendedStorage storage = new ExtendedStorage();
        assertNull(storage.getRestrictions(), "restrictions should be null before initialize");

        // Test Default value
        storage.initialize(ExtendedStorageName.HEAVY_VEHICLE);
        assertTrue(storage.getRestrictions(), "initialize should set restrictions to true for HEAVY_VEHICLE if it is null");

        // Variable set and not the default value. It should be left as is.
        String json = """
                {
                    "enabled": false,
                    "restrictions": false,
                    "filepath": "/custom/path.csv"
                }
                """;
        storage = mapper.readValue(json, ExtendedStorage.class);
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
    void initializeSetsUseForWarningsToTrueForRoadAccessRestrictions() throws JsonProcessingException {
        ExtendedStorage storage = new ExtendedStorage();
        assertNull(storage.getUse_for_warnings(), "use_for_warnings should be null before initialize");

        // Test Default value
        storage.initialize(ExtendedStorageName.ROAD_ACCESS_RESTRICTIONS);
        assertTrue(storage.getUse_for_warnings(), "initialize should set use_for_warnings to true for ROAD_ACCESS_RESTRICTIONS if it is null");

        // Variable set and not the default value. It should be left as is.
        String json = """
                {
                    "use_for_warnings": false
                }
                """;
        storage = mapper.readValue(json, ExtendedStorage.class);
        assertFalse(storage.getUse_for_warnings(), "initialize should not change use_for_warnings if it is not null");
        storage.initialize(ExtendedStorageName.ROAD_ACCESS_RESTRICTIONS);
        assertFalse(storage.getUse_for_warnings(), "initialize should not change use_for_warnings if it is not null");

        // Assert everything else was set to null
        testStorageObjectIsEmpty(storage, new ArrayList<>() {{
            add("enabled");
            add("use_for_warnings");
        }});
    }

    @Test
    void initializeSetsKerbsOnCrossingsToTrueForWheelchair() throws JsonProcessingException {
        ExtendedStorage storage = new ExtendedStorage();
        assertNull(storage.getKerbs_on_crossings(), "kerbs_on_crossings should be null before initialize");

        // Test Default value
        storage.initialize(ExtendedStorageName.WHEELCHAIR);
        assertTrue(storage.getKerbs_on_crossings(), "initialize should set kerbs_on_crossings to true for WHEELCHAIR if it is null");

        // Variable set and not the default value. It should be left as is.
        String json = """
                {
                    "KerbsOnCrossings": false
                }
                """;
        storage = mapper.readValue(json, ExtendedStorage.class);
        assertFalse(storage.getKerbs_on_crossings(), "initialize should not change kerbs_on_crossings if it is not null");
        storage.initialize(ExtendedStorageName.WHEELCHAIR);
        assertFalse(storage.getKerbs_on_crossings(), "initialize should not change kerbs_on_crossings if it is not null");

        // Assert everything else was set to null
        testStorageObjectIsEmpty(storage, new ArrayList<>() {{
            add("enabled");
            add("KerbsOnCrossings");
        }});
    }

    @Test
    void initializeSetsRadiusTo150ForHereTrafficIfNull() throws JsonProcessingException {
        ExtendedStorage storage = new ExtendedStorage();
        assertNull(storage.getRadius(), "radius should be null before initialize");

        // Test Default value
        storage.initialize(ExtendedStorageName.HERE_TRAFFIC);
        assertEquals(150, storage.getRadius(), "initialize should set radius to 150 for HERE_TRAFFIC if it is null");

        // Variable set and not the default value. It should be left as is.
        String json = """
                {
                    "radius": 100
                }
                """;
        storage = mapper.readValue(json, ExtendedStorage.class);
        assertEquals(100, storage.getRadius(), "initialize should not change radius if it is not null");
        storage.initialize(ExtendedStorageName.HERE_TRAFFIC);
        assertEquals(100, storage.getRadius(), "initialize should not change radius if it is not null");

        // Assert everything else was set to null
        testStorageObjectIsEmpty(storage, new ArrayList<>() {{
            add("enabled");
            add("radius");
            add("output_log");
            add("log_location");
        }});
    }

// Hillindex maximum slope

    @Test
    void initializeSetsMaximumSlopeToNullForHillIndexIfNull() throws JsonProcessingException {
        ExtendedStorage storage = new ExtendedStorage();
        assertNull(storage.getMaximumSlope(), "maximum_slope should be null before initialize");

        // Test Default value
        storage.initialize(ExtendedStorageName.HILL_INDEX);
        assertNull(storage.getMaximumSlope(), "initialize should set maximum_slope to null for HILL_INDEX if it is null");

        // Variable set and not the default value. It should be left as is.
        String json = """
                {
                    "maximum_slope": 5
                }
                """;
        storage = mapper.readValue(json, ExtendedStorage.class);
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
    void initializeSetsOutputLogToFalseForHereTrafficIfNull() throws JsonProcessingException {
        ExtendedStorage storage = new ExtendedStorage();
        assertNull(storage.getOutput_log(), "output_log should be null before initialize");

        // Test Default value
        storage.initialize(ExtendedStorageName.HERE_TRAFFIC);
        assertFalse(storage.getOutput_log(), "initialize should set output_log to false for HERE_TRAFFIC if it is null");

        // Variable set and not the default value. It should be left as is.
        String json = """
                {
                    "output_log": true
                }
                """;
        storage = mapper.readValue(json, ExtendedStorage.class);
        assertTrue(storage.getOutput_log(), "initialize should not change output_log if it is not null");
        storage.initialize(ExtendedStorageName.HERE_TRAFFIC);
        assertTrue(storage.getOutput_log(), "initialize should not change output_log if it is not null");

        // Assert everything else was set to null
        testStorageObjectIsEmpty(storage, new ArrayList<>() {{
            add("enabled");
            add("output_log");
            add("radius");
            add("log_location");
        }});
    }

    @Test
    void initializeSetsLogLocationToDefaultForHereTrafficIfNull() throws JsonProcessingException {
        ExtendedStorage storage = new ExtendedStorage();
        assertNull(storage.getLog_location(), "log_location should be null before initialize");

        // Test Default value
        storage.initialize(ExtendedStorageName.HERE_TRAFFIC);
        assertEquals(Path.of("./here_matching.log"), storage.getLog_location(), "initialize should set log_location to default for HERE_TRAFFIC if it is null");

        // Variable set and not the default value. It should be left as is.
        String json = """
                {
                    "log_location": "/custom/path.log"
                }
                """;
        storage = mapper.readValue(json, ExtendedStorage.class);
        assertEquals(Path.of("/custom/path.log"), storage.getLog_location(), "initialize should not change log_location if it is not null");
        storage.initialize(ExtendedStorageName.HERE_TRAFFIC);
        assertEquals(Path.of("/custom/path.log"), storage.getLog_location(), "initialize should not change log_location if it is not null");

        // Assert everything else was set to null
        testStorageObjectIsEmpty(storage, new ArrayList<>() {{
            add("enabled");
            add("radius");
            add("output_log");
            add("log_location");
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
    void assertSetHereTrafficPathLogic(String streets, String refPattern, String pattern15min) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ExtendedStorage storage;

        // Test null values
        String json = """
                {
                    "streets":null,
                    "ref_pattern":null,
                    "pattern_15min":null
                }
                """;

        storage = mapper.readValue(json, ExtendedStorage.class);
        storage.initialize(ExtendedStorageName.HERE_TRAFFIC);
        assertFalse(storage.getEnabled(), "initialize should disable storage if all paths are null");

        // Create JSON string based on parameters
        json = String.format("""
                {
                    "streets": "%s",
                    "ref_pattern": "%s",
                    "pattern_15min": "%s"
                }
                """, streets, refPattern, pattern15min);

        storage = mapper.readValue(json, ExtendedStorage.class);
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
            assertEquals(emptyPath, storage.getRef_pattern(), "initialize should set ref_pattern to empty path if it is null");
        } else {
            assertEquals(Path.of(refPattern).toAbsolutePath(), storage.getRef_pattern(), "initialize should not change ref_pattern if it is not null");
        }

        if (pattern15min.isEmpty()) {
            assertEquals(emptyPath, storage.getPattern_15min(), "initialize should set pattern_15min to empty path if it is null");
        } else {
            assertEquals(Path.of(pattern15min).toAbsolutePath(), storage.getPattern_15min(), "initialize should not change pattern_15min if it is not null");
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
    @CsvSource({"'', '', ''", // All paths null
            "'/custom/path.csv', '', ''", // Only boundaries set
            "'', '/custom/path.csv', ''", // Only ids set
            "'', '', '/custom/path.csv'", // Only openborders set
            "'/custom/path.csv', '/custom/path.csv', ''", // boundaries and ids set
            "'/custom/path.csv', '', '/custom/path.csv'", // boundaries and openborders set
            "'', '/custom/path.csv', '/custom/path.csv'", // ids and openborders set
            "'/custom/path.csv', '/custom/path.csv', '/custom/path.csv'" // All paths set -> Enabled!
    })
    void assertSetBordersPathLogic(String boundaries, String ids, String openborders) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ExtendedStorage storage;
        // Test null values
        String json = """
                {
                    "boundaries":null,
                    "ids":null,
                    "openborders":null
                }
                """;

        storage = mapper.readValue(json, ExtendedStorage.class);
        storage.initialize(ExtendedStorageName.BORDERS);
        assertFalse(storage.getEnabled(), "initialize should disable storage if all paths are null");

        // Create JSON string based on parameters
        json = String.format("""
                {
                    "boundaries": "%s",
                    "ids": "%s",
                    "openborders": "%s"
                }
                """, boundaries, ids, openborders);

        storage = mapper.readValue(json, ExtendedStorage.class);
        storage.initialize(ExtendedStorageName.BORDERS);

        // Check if storage is enabled or disabled based on paths
        boolean shouldBeEnabled = !boundaries.isEmpty() && !ids.isEmpty() && !openborders.isEmpty();
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
            add("boundaries");
            add("ids");
            add("openborders");
        }});
    }

    @ParameterizedTest
    @ValueSource(strings = {"NoiseIndex", "GreenIndex", "ShadowIndex"})
    void initializeDisablesStorageIfFilepathIsNull(String storageName) throws JsonProcessingException {
        ExtendedStorage storage = new ExtendedStorage();
        assertNull(storage.getFilepath(), "filepath should be null before initialize");

        // Initialize with empty path
        storage.initialize(ExtendedStorageName.getEnum(storageName));
        assertFalse(storage.getEnabled(), "initialize should disable storage if filepath is null");

        //language=JSON
        String json = """
                { 
                  "enabled": true,
                  "filepath": ""
                }  
                """;

        storage = mapper.readValue(json, ExtendedStorage.class);
        storage.initialize(ExtendedStorageName.getEnum(storageName));
        assertFalse(storage.getEnabled(), "initialize should disable storage if filepath is empty");
        assertEquals(Path.of(""), storage.getFilepath(), "initialize should not change filepath if it is empty");

        // Initialize with non-empty path
        //language=JSON
        json = """
                {
                    "filepath": "custom/path.csv"
                }
                """;

        storage = mapper.readValue(json, ExtendedStorage.class);
        storage.initialize(ExtendedStorageName.getEnum(storageName));
        assertTrue(storage.getEnabled(), "initialize should not disable storage if filepath is not null");
        assertEquals(Path.of("custom/path.csv").toAbsolutePath(), storage.getFilepath(), "initialize should not change filepath if it is not null");

        // Assert everything else was set to null
        testStorageObjectIsEmpty(storage, new ArrayList<>() {{
            add("enabled");
            add("filepath");
        }});
    }
}
