package org.heigit.ors.api.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.heigit.ors.api.config.profile.storages.ExtendedStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class ExtendedStorageMapSerializer extends JsonSerializer<Map<String, ExtendedStorage>> {
    Logger logger = LoggerFactory.getLogger(ExtendedStorageMapSerializer.class);
    private ObjectMapper objectMapper;

    public ExtendedStorageMapSerializer() {
        this.objectMapper = new ObjectMapper();
    }

    public ExtendedStorageMapSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @Override
    public void serialize(Map<String, ExtendedStorage> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        value.forEach((key, storage) -> {
            try {
                ObjectNode node = objectMapper.valueToTree(storage);
                if (node.elements().hasNext()) {
                    var firstField = node.fields().next();
                    if (!firstField.getValue().isNull()) {
                        gen.writeObjectField(key, firstField.getValue());
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to serialize ExtendedStorage: {}", key);
            }
        });
        gen.writeEndObject();
    }
}