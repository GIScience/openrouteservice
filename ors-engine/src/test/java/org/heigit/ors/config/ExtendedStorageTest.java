package org.heigit.ors.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.config.profile.storages.ExtendedStorage;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedStorageTest {

    Boolean testConstructorObjectIsEmpty(ExtendedStorage storage) {
        assertNull(storage.getEnabled(), "Default constructor should initialize 'enabled' to true");
        assertNull(storage.getFilepath(), "Default constructor should initialize 'filepath' to null");
        assertNull(storage.getRestrictions(), "Default constructor should initialize 'restrictions' to null");
        assertNull(storage.getStreets(), "Default constructor should initialize 'streets' to null");
        assertNull(storage.getRef_pattern(), "Default constructor should initialize 'ref_pattern' to null");
        assertNull(storage.getPattern_15min(), "Default constructor should initialize 'pattern_15min' to null");
        assertNull(storage.getRadius(), "Default constructor should initialize 'radius' to null");
        assertNull(storage.getOutput_log(), "Default constructor should initialize 'output_log' to null");
        assertNull(storage.getLog_location(), "Default constructor should initialize 'log_location' to null");
        assertNull(storage.getMaximumSlope(), "Default constructor should initialize 'maximum_slope' to null");
        assertNull(storage.getBoundaries(), "Default constructor should initialize 'boundaries' to null");
        return true;
    }

    @Test
    void testDefaultConstructor() {
        ExtendedStorage storage = new ExtendedStorage();
        assertTrue(testConstructorObjectIsEmpty(storage), "Default constructor should initialize all fields to null");
    }

    @Test
    void testStringConstructor() {
        ExtendedStorage storage = new ExtendedStorage("");
        assertTrue(testConstructorObjectIsEmpty(storage), "String constructor should initialize all fields to null");
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

        //language=JSON
        String expectedJson = """
                {
                    "enabled":null,
                    "restrictions":null,
                    "streets":null,
                    "ref_pattern":null,
                    "pattern_15min":null,
                    "radius":null,
                    "output_log":null,
                    "log_location":null,
                    "ghProfile":null,
                    "filepath":null,
                    "maximum_slope":null,
                    "boundaries":null,
                    "ids":null,
                    "openborders":null,
                    "use_for_warnings":null,
                    "KerbsOnCrossings":null
                }
                """;
        // Step 3: Assert JSON structure and values including enabled
        assertEquals(mapper.readTree(expectedJson), mapper.readTree(jsonResult), "Serialized JSON should match the expected JSON");
    }

    @Test
    void testSerializationNonEmptyStorageProducesCorrectJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

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
                     "ghProfile":"car",
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
}
