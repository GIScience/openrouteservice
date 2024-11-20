package org.heigit.ors.api.responses.export.topojson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TopoJsonPropertiesSerializer extends JsonSerializer<HashMap<String, Object>> {

    public TopoJsonPropertiesSerializer() {
        super();
    }

    @Override
    public void serialize(HashMap<String, Object> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        for (Map.Entry<String, Object> entry : value.entrySet()) {
            gen.writeFieldName(entry.getKey());
            if (entry.getValue() instanceof String stringValue) {
                gen.writeString(stringValue);
            } else if (entry.getValue() instanceof Integer integerValue) {
                gen.writeNumber(integerValue);
            } else if (entry.getValue() instanceof Double doubleValue) {
                gen.writeNumber(doubleValue);
            } else if (entry.getValue() instanceof Boolean booleanValue) {
                gen.writeBoolean(booleanValue);
            } else
                // This is a fallback for all other types
                gen.writeObject(entry.getValue());
        }
        gen.writeEndObject();
    }
}
