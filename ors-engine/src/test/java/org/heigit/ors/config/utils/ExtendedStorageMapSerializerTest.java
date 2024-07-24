package org.heigit.ors.config.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.heigit.ors.config.profile.storages.ExtendedStorage;
import org.heigit.ors.config.profile.storages.ExtendedStorageGreenIndex;
import org.heigit.ors.config.profile.storages.ExtendedStorageWayCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedStorageMapSerializerTest {
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(ExtendedStorageMapHelper.class, new ExtendedStorageMapSerializer());
        mapper.registerModule(module);
    }

    @Test
    void testSerializeExtendedStorage() throws IOException {
        HelperClass foo = new HelperClass();
        foo.setExtendedStorage(Map.of("WayCategory", new ExtendedStorageWayCategory()));
        String json = mapper.writeValueAsString(foo);
        String expectedJson = "{\"ext_storages\":{\"WayCategory\":{\"enabled\":true}}}";
        assertEquals(expectedJson, json);
    }

    @Test
    void testSerializeExtendedStorageWithMultipleEntries() throws IOException {
        HelperClass foo = new HelperClass();
        Map<String, ExtendedStorage> extendedStorage = new HashMap<>();
        extendedStorage.put("WayCategory", new ExtendedStorageWayCategory());
        extendedStorage.put("GreenIndex", new ExtendedStorageGreenIndex());
        extendedStorage.put("EmptyHelperStorage", new EmptyHelperStorage());
        extendedStorage.put("NestedHelperStorage", new NestedHelperStorage());
        foo.setExtendedStorage(extendedStorage);
        String json = mapper.writeValueAsString(foo);
        // Deserialize the JSON string to a map
        Map<?, ?> result = mapper.readValue(json, Map.class);
        // Check if the map contains the expected keys
        assertTrue(result.containsKey("ext_storages"));
        // Check size of the map
        assertEquals(4, ((Map<?, ?>) result.get("ext_storages")).size());
        // Check if the map contains the expected keys
        assertTrue(((Map<?, ?>) result.get("ext_storages")).containsKey("WayCategory"));
        assertTrue(((Map<?, ?>) result.get("ext_storages")).containsKey("GreenIndex"));
        // Check sizes of the inner maps
        assertEquals(1, ((Map<?, ?>) ((Map<?, ?>) result.get("ext_storages")).get("WayCategory")).size());
        assertEquals(2, ((Map<?, ?>) ((Map<?, ?>) result.get("ext_storages")).get("GreenIndex")).size());
        // Check if the map contains the expected values
        assertTrue(((Map<?, ?>) ((Map<?, ?>) result.get("ext_storages")).get("WayCategory")).containsKey("enabled"));
        assertTrue(((Map<?, ?>) ((Map<?, ?>) result.get("ext_storages")).get("GreenIndex")).containsKey("enabled"));
        // filepath in GreenIndex
        assertTrue(((Map<?, ?>) ((Map<?, ?>) result.get("ext_storages")).get("GreenIndex")).containsKey("filepath"));
        // Evaluate a storage that is empty, even without the enabled field when serialized
    }

    @Test
    void testSerializeWithException() throws IOException {
        ObjectMapper mockedObjectMapper = Mockito.mock(ObjectMapper.class);
        JsonGenerator mockedJsonGenerator = Mockito.mock(JsonGenerator.class);

        // Use RuntimeException since the method does not declare IOException
        Mockito.when(mockedObjectMapper.valueToTree(Mockito.any())).thenThrow(new RuntimeException("Mocked RuntimeException"));

        ExtendedStorageMapSerializer serializer = new ExtendedStorageMapSerializer(mockedObjectMapper);

        // This will fail if the serialize function cannot handle Exceptions properly when calling valueToTree
        serializer.serialize(Collections.singletonMap("test", new ExtendedStorage() {
        }), mockedJsonGenerator, null);
    }

    /**
     * Helper class to test the deserialization of a Path object.
     * The class has a single field of type Path.
     * The field setFilepath is annotated with @JsonDeserialize to use the PathDeserializer.
     */
    @JsonTypeName("HelperClass")
    private static class HelperClass {

        private Map<String, ExtendedStorage> extendedStorage;

        @JsonProperty("ext_storages")
        @JsonSerialize(using = ExtendedStorageMapSerializer.class)
        public Map<String, ExtendedStorage> getExtendedStorage() {
            return this.extendedStorage;
        }

        @JsonSetter("ext_storages")
        public void setExtendedStorage(Map<String, ExtendedStorage> extendedStorage) {
            this.extendedStorage = extendedStorage;
        }
    }

    private static class ExtendedStorageMapHelper extends HashMap<String, ExtendedStorage> {
    }

    private static class EmptyHelperStorage extends ExtendedStorage {
        Boolean enabled = true;

        @Override
        @JsonIgnore
        public Boolean getEnabled() {
            return this.enabled;
        }
    }

    private static class NestedHelperStorage extends ExtendedStorage {
        Map<String, String> nested = new HashMap<>();

        public NestedHelperStorage() {
            this.nested.put("key", "value");
        }

        @JsonProperty
        public Map<String, String> getNested() {
            return this.nested;
        }
    }
}


