package org.heigit.ors.config.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.heigit.ors.config.profile.storages.ExtendedStorage;
import org.heigit.ors.config.profile.storages.ExtendedStorageIndex;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExtendedStorageMapDeserializer extends JsonDeserializer<Map<String, ExtendedStorage>> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, ExtendedStorage> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        Map<String, ExtendedStorage> result = new HashMap<>();
        JsonNode rootNode = jp.getCodec().readTree(jp);
        rootNode.fields().forEachRemaining(field -> {
            String key = field.getKey();
            JsonNode value = field.getValue().isNull() ? JsonNodeFactory.instance.objectNode() : field.getValue();
            ObjectNode combinedNode = JsonNodeFactory.instance.objectNode();
            combinedNode.set(key, value);
            try {
                ExtendedStorage extendedStorage;
                try {
                    // First try to deserialize as ExtendedStorageIndex
                    extendedStorage = objectMapper.treeToValue(combinedNode, ExtendedStorageIndex.class);
                } catch (Exception e) {
                    // If the first attempt fails, try to deserialize as ExtendedStorage
                    extendedStorage = objectMapper.treeToValue(combinedNode, ExtendedStorage.class);
                }
                result.put(key, extendedStorage);
            } catch (IOException e) {
                // If both attempts fail, throw an exception
                throw new RuntimeException(new ExtendedStorageDeserializationException("Failed to deserialize ExtendedStorage object for key: " + key, e));
            }
        });
        return result;
    }
}

class ExtendedStorageDeserializationException extends Exception {
    public ExtendedStorageDeserializationException(String message, Throwable cause) {
        super(message, cause);
    }
}