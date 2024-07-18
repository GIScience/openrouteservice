package org.heigit.ors.api.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public class PathSerializer extends JsonSerializer<Path> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathSerializer.class);

    @Override
    public void serialize(Path value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        String path;
        path = value.toString();
        if (path.equals("null")) {
            path = "";
        }
        gen.writeString(path);
    }
}
