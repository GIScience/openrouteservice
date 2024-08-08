package org.heigit.ors.config.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.nio.file.Path;

public class PathSerializer extends JsonSerializer<Path> {

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
