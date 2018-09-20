package heigit.ors.api.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.requests.routing.CoordinateListWrapper;

import java.io.IOException;

public class CoordinateListSerializer extends StdSerializer<CoordinateListWrapper> {
    public CoordinateListSerializer() {
        this(null);
    }

    public CoordinateListSerializer(Class<CoordinateListWrapper> listWrapper) {
        super(listWrapper);
    }

    @Override
    public void serialize(CoordinateListWrapper listWrapper, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException, JsonProcessingException {
        jsonGenerator.writeStartArray();

        for(Coordinate coord : listWrapper.getCoordinates()) {
            jsonGenerator.writeStartArray();

            jsonGenerator.writeNumber(coord.x);
            jsonGenerator.writeNumber(coord.y);

            jsonGenerator.writeEndArray();
        }

        jsonGenerator.writeEndArray();
    }
}
