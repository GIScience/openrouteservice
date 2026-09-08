package org.heigit.ors.api.responses.export.json;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.text.DecimalFormat;

public class WeightSerializer<T> extends ValueSerializer<T> {
    @Override
    public void serialize(Object o, JsonGenerator jsonGenerator, SerializationContext serializationContext) {
        jsonGenerator.writeString(new DecimalFormat("#.###").format(o));
    }
}
