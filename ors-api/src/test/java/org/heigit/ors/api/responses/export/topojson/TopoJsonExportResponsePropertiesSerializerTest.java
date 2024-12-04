package org.heigit.ors.api.responses.export.topojson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TopoJsonExportResponsePropertiesSerializerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testSerialize() throws IOException {
        HelperClass helperClass = new HelperClass();
        helperClass.properties = new HashMap<>();
        helperClass.properties.put("string", "string");
        helperClass.properties.put("integer", 1);
        helperClass.properties.put("double", 1.0);
        helperClass.properties.put("boolean", true);
        helperClass.properties.put("object", new HashMap<>());

        String jsonString = objectMapper.writeValueAsString(helperClass);

        assertTrue(jsonString.contains("\"string\":\"string\""));
        assertTrue(jsonString.contains("\"integer\":1"));
        assertTrue(jsonString.contains("\"double\":1.0"));
        assertTrue(jsonString.contains("\"boolean\":true"));
        assertTrue(jsonString.contains("\"object\":{}"));


    }

    class HelperClass {
        @JsonSerialize(using = TopoJsonPropertiesSerializer.class)
        Map<String, Object> properties;
    }
}