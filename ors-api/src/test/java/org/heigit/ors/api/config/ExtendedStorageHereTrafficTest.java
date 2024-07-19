package org.heigit.ors.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedStorageHereTrafficTest {

    String streets = "./src/test/files/here-traffic-streets.csv";
    String patterns = "./src/test/files/here-traffic-patterns.csv";
    String ref_patterns = "/some/absolute/path/src/test/files/here-traffic-ref-patterns.csv";

    @Test
    void testDefaultConstructor() {
        ExtendedStorageHereTraffic storage = new ExtendedStorageHereTraffic();
        assertNotNull(storage, "Default constructor should initialize the object");
        assertTrue(storage.getEnabled(), "Default constructor should initialize 'enabled' to true");
    }


    @Test
    void serializationProducesCorrectJson() throws Exception {
        // Step 1: Create and configure an instance of ExtendedStorageHereTraffic
        ExtendedStorageHereTraffic storage = new ExtendedStorageHereTraffic();
        Path streets_path = Path.of(streets);
        Path patterns_path = Path.of(patterns);
        Path ref_patterns_path = Path.of(ref_patterns);
        storage.setStreets(streets_path);
        storage.setRefPattern(ref_patterns_path);

        // Step 2: Serialize the object to JSON
        ObjectMapper mapper = new ObjectMapper();
        String jsonResult = mapper.writeValueAsString(storage);

        // Step 3: Assert JSON structure and values including enabled
        assertTrue(jsonResult.contains("\"HereTraffic\""), "Serialized JSON should have 'HereTraffic' key");
        assertTrue(jsonResult.contains("\"enabled\":true"), "Serialized JSON should have 'enabled' set to true");
        assertTrue(jsonResult.contains("\"streets\":\"" + streets_path.toAbsolutePath() + "\""), "Serialized JSON should have 'streets' set to the absolute path.");
        assertTrue(jsonResult.contains("\"ref_pattern\":\"" + ref_patterns_path + "\""), "Serialized JSON should have 'ref_pattern' set to the absolute path.");
        assertTrue(jsonResult.contains("\"pattern\":\"" + patterns_path.toAbsolutePath() + "\""), "Serialized JSON should have 'pattern' set to the absolute path.");


    }

    @Test
    void deSerializationDisabledCorrectJson() throws Exception {
        Path streets_path = Paths.get(streets);
        Path patterns_path = Paths.get(patterns);
        Path ref_patterns_path = Paths.get(ref_patterns);


        // Step 1: Create and configure an instance of ExtendedStorageHereTraffic
        String json = "{\"HereTraffic\":{\"enabled\":false,\"streets\":\"" + streets_path +
                "\",\"ref_pattern\":\"" + ref_patterns_path +
                "\",\"pattern\":\"" + patterns_path + "\"}}";

        // Step 2: Deserialize the JSON to an object
        ObjectMapper mapper = new ObjectMapper();
        ExtendedStorageHereTraffic storage = mapper.readValue(json, ExtendedStorageHereTraffic.class);

        // Step 3: Assert JSON structure and values including enabled
        assertFalse(storage.getEnabled(), "Deserialized object should have 'enabled' set to false");
        assertEquals(streets_path.toAbsolutePath(), storage.getStreets(), "Deserialized object should have 'streets' set to the absolute path.");
        assertEquals(ref_patterns_path.toAbsolutePath(), storage.getRefPattern(), "Deserialized object should have 'ref_pattern' set to the absolute path.");
        assertEquals(patterns_path.toAbsolutePath(), storage.getPattern(), "Deserialized object should have 'pattern' set to the absolute path.");
    }

    @Test
    void deSerializationWithoutEnabledCorrectJson() throws Exception {

        // Step 1: Create and configure an instance of ExtendedStorageHereTraffic
        String json = "{\"HereTraffic\":{\"streets\":\"" + streets +
                "\",\"ref_pattern\":\"" + ref_patterns +
                "\",\"pattern\":\"" + patterns + "\"}}";

        // Step 2: Deserialize the JSON to an object
        ObjectMapper mapper = new ObjectMapper();
        ExtendedStorageHereTraffic storage = mapper.readValue(json, ExtendedStorageHereTraffic.class);

        // Step 3: Assert JSON structure and values including enabled
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
        assertEquals(Paths.get(streets).toAbsolutePath(), storage.getStreets(), "Deserialized object should have 'streets' set to the absolute path.");
        assertEquals(Paths.get(ref_patterns).toAbsolutePath(), storage.getRefPattern(), "Deserialized object should have 'ref_pattern' set to the absolute path.");
        assertEquals(Paths.get(patterns).toAbsolutePath(), storage.getPattern(), "Deserialized object should have 'pattern' set to the absolute path.");
    }

    private static Stream<Arguments> provideJsonStrings() {
        return Stream.of(
                Arguments.of("{\"HereTraffic\":{\"enabled\":true,\"streets\":\"./src/test/files/here-traffic-streets.csv\",\"ref_pattern\":\"./src/test/files/here-traffic-ref-patterns.csv\",\"pattern\":\"./src/test/files/here-traffic-patterns.csv\"}}"),
                Arguments.of("{\"HereTraffic\":{\"enabled\":false,\"streets\":\"./src/test/files/here-traffic-streets.csv\",\"ref_pattern\":\"./src/test/files/here-traffic-ref-patterns.csv\",\"pattern\":\"./src/test/files/here-traffic-patterns.csv\"}}"),
                Arguments.of("{\"HereTraffic\":{\"streets\":\"./src/test/files/here-traffic-streets.csv\",\"ref_pattern\":\"./src/test/files/here-traffic-ref-patterns.csv\",\"pattern\":\"./src/test/files/here-traffic-patterns.csv\"}}")
        );
    }

    @ParameterizedTest
    @MethodSource("provideJsonStrings")
    void deSerializationCorrectJson(String json) throws Exception {
        // Step 1: Deserialize the JSON to an object
        ObjectMapper mapper = new ObjectMapper();
        ExtendedStorageHereTraffic storage = mapper.readValue(json, ExtendedStorageHereTraffic.class);

        // Step 2: Assert JSON structure and values including enabled
        if (json.contains("enabled\":false")) {
            assertFalse(storage.getEnabled(), "Deserialized object should have 'enabled' set to false");
        } else {
            assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
        }
        assertEquals(Paths.get("./src/test/files/here-traffic-streets.csv").toAbsolutePath(), storage.getStreets(), "Deserialized object should have 'streets' set to the absolute path.");
        assertEquals(Paths.get("./src/test/files/here-traffic-ref-patterns.csv").toAbsolutePath(), storage.getRefPattern(), "Deserialized object should have 'ref_pattern' set to the absolute path.");
        assertEquals(Paths.get("./src/test/files/here-traffic-patterns.csv").toAbsolutePath(), storage.getPattern(), "Deserialized object should have 'pattern' set to the absolute path.");
    }
}