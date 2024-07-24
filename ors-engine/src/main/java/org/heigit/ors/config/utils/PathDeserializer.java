package org.heigit.ors.config.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public class PathDeserializer extends JsonDeserializer<Path> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PathDeserializer.class);

    @Override
    public Path deserialize(JsonParser p, DeserializationContext ctxt) {
        Path fallbackPath = Path.of("");
        try {
            String pathStr = p.getText();
            if (pathStr.equals("null") || pathStr.isEmpty()) {
                return fallbackPath;
            }
            return Path.of(pathStr).toAbsolutePath();
        } catch (IOException e) {
            LOGGER.error("Error deserializing path: {}", e.getMessage(), e);
            return fallbackPath;
        }
    }
}
