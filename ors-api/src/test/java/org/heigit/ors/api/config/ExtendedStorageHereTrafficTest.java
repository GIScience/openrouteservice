package org.heigit.ors.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedStorageHereTrafficTest {

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
        storage.setStreets("./src/test/files/here-traffic-streets.csv");
        storage.setRefPattern("/some/absolute/path/src/test/files/here-traffic-ref-patterns.csv");
        storage.setPattern("./src/test/files/here-traffic-patterns.csv");

        // Step 2: Serialize the object to JSON
        ObjectMapper mapper = new ObjectMapper();
        String jsonResult = mapper.writeValueAsString(storage);

        // Step 3: Assert JSON structure and values including enabled
        assertTrue(jsonResult.contains("\"HereTraffic\""), "Serialized JSON should have 'HereTraffic' key");
        assertTrue(jsonResult.contains("\"enabled\":true"), "Serialized JSON should have 'enabled' set to true");
        assertTrue(jsonResult.contains("\"streets\":\"" + Paths.get("./src/test/files/here-traffic-streets.csv").toAbsolutePath() + "\""), "Serialized JSON should have 'streets' set to the absolute path.");
        assertTrue(jsonResult.contains("\"ref_pattern\":\"/some/absolute/path/src/test/files/here-traffic-ref-patterns.csv\""), "Serialized JSON should have 'ref_pattern' set to the absolute path.");
        assertTrue(jsonResult.contains("\"pattern\":\"" + Paths.get("./src/test/files/here-traffic-patterns.csv").toAbsolutePath() + "\""), "Serialized JSON should have 'pattern' set to the absolute path.");


    }

    @Test
    void deSerializationDisabledCorrectJson() throws Exception {
        // Step 1: Create and configure an instance of ExtendedStorageHereTraffic
        String json = "{\"HereTraffic\":{\"enabled\":false,\"streets\":\"./src/test/files/here-traffic-streets.csv\",\"ref_pattern\":\"./src/test/files/here-traffic-ref-patterns.csv\",\"pattern\":\"./src/test/files/here-traffic-patterns.csv\"}}";

        // Step 2: Deserialize the JSON to an object
        ObjectMapper mapper = new ObjectMapper();
        ExtendedStorageHereTraffic storage = mapper.readValue(json, ExtendedStorageHereTraffic.class);

        // Step 3: Assert JSON structure and values including enabled
        assertFalse(storage.getEnabled(), "Deserialized object should have 'enabled' set to false");
        assertEquals(Paths.get("./src/test/files/here-traffic-streets.csv").toAbsolutePath().toString(), storage.getStreets(), "Deserialized object should have 'streets' set to the absolute path.");
        assertEquals(Paths.get("./src/test/files/here-traffic-ref-patterns.csv").toAbsolutePath().toString(), storage.getRefPattern(), "Deserialized object should have 'ref_pattern' set to the absolute path.");
        assertEquals(Paths.get("./src/test/files/here-traffic-patterns.csv").toAbsolutePath().toString(), storage.getPattern(), "Deserialized object should have 'pattern' set to the absolute path.");
    }

    @Test
    void deSerializationWithoutEnabledCorrectJson() throws Exception {
        // Step 1: Create and configure an instance of ExtendedStorageHereTraffic
        String json = "{\"HereTraffic\":{\"streets\":\"./src/test/files/here-traffic-streets.csv\",\"ref_pattern\":\"./src/test/files/here-traffic-ref-patterns.csv\",\"pattern\":\"./src/test/files/here-traffic-patterns.csv\"}}";

        // Step 2: Deserialize the JSON to an object
        ObjectMapper mapper = new ObjectMapper();
        ExtendedStorageHereTraffic storage = mapper.readValue(json, ExtendedStorageHereTraffic.class);

        // Step 3: Assert JSON structure and values including enabled
        assertTrue(storage.getEnabled(), "Deserialized object should have 'enabled' set to true");
        assertEquals(Paths.get("./src/test/files/here-traffic-streets.csv").toAbsolutePath().toString(), storage.getStreets(), "Deserialized object should have 'streets' set to the absolute path.");
        assertEquals(Paths.get("./src/test/files/here-traffic-ref-patterns.csv").toAbsolutePath().toString(), storage.getRefPattern(), "Deserialized object should have 'ref_pattern' set to the absolute path.");
        assertEquals(Paths.get("./src/test/files/here-traffic-patterns.csv").toAbsolutePath().toString(), storage.getPattern(), "Deserialized object should have 'pattern' set to the absolute path.");
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
        assertEquals(Paths.get("./src/test/files/here-traffic-streets.csv").toAbsolutePath().toString(), storage.getStreets(), "Deserialized object should have 'streets' set to the absolute path.");
        assertEquals(Paths.get("./src/test/files/here-traffic-ref-patterns.csv").toAbsolutePath().toString(), storage.getRefPattern(), "Deserialized object should have 'ref_pattern' set to the absolute path.");
        assertEquals(Paths.get("./src/test/files/here-traffic-patterns.csv").toAbsolutePath().toString(), storage.getPattern(), "Deserialized object should have 'pattern' set to the absolute path.");
    }
}