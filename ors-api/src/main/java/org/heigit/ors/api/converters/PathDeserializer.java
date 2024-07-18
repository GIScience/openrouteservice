package org.heigit.ors.api.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import java.io.IOException;



public class PathDeserializer extends JsonDeserializer<Path> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PathDeserializer.class);

    @Override
    public Path deserialize(JsonParser p, DeserializationContext ctxt) {
        Path deserializePath = null;

        try {
            deserializePath = Path.of(p.getText());
        } catch (IOException e) {
            LOGGER.warn("Error deserializing path: {}", e.getMessage());
        }

        // Check if null or "null"
        if (deserializePath == null || deserializePath.toString().equals("null")) {
            deserializePath = Path.of("");
        }
        return deserializePath;
    }
}
