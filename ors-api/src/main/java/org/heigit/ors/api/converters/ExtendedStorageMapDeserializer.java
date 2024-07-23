package org.heigit.ors.api.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.heigit.ors.api.config.ExtendedStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExtendedStorageMapDeserializer extends JsonDeserializer<Map<String, ExtendedStorage>> {
    private static final Logger logger = LoggerFactory.getLogger(ExtendedStorageMapDeserializer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, ExtendedStorage> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        Map<String, ExtendedStorage> result = new HashMap<>();
        JsonNode rootNode = jp.getCodec().readTree(jp);
        rootNode.fields().forEachRemaining(field -> {
            String key = field.getKey();
            JsonNode value = field.getValue().isNull() ? JsonNodeFactory.instance.objectNode() : field.getValue();
            ObjectNode combinedNode = JsonNodeFactory.instance.objectNode();
            combinedNode.set(key, value); // Combine key and value into a single node
            try {
                ExtendedStorage extendedStorage = objectMapper.treeToValue(combinedNode, ExtendedStorage.class);
                result.put(key, extendedStorage);
            } catch (Exception e) {
                logger.error("Error deserializing ExtendedStorage object", e);
            }
        });
        return result;
    }
}