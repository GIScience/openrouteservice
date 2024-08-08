package org.heigit.ors.api.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.List;

public class InlineArraySerializer extends StdSerializer<List> {
    public InlineArraySerializer() {
        this(null);
    }

    public InlineArraySerializer(Class<List> t) {
        super(t);
    }

    public void serialize(List value, JsonGenerator generator, SerializerProvider var3) throws IOException {
        ((CustomYAMLGenerator) generator).writeRawString(String.join(", ", value));
    }
}