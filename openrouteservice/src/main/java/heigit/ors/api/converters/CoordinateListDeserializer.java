package heigit.ors.api.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.requests.routing.CoordinateListWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CoordinateListDeserializer extends StdDeserializer<CoordinateListWrapper> {
    public CoordinateListDeserializer() {
        this(null);
    }

    public CoordinateListDeserializer(Class<CoordinateListWrapper> listWrapper) {
        super(listWrapper);
    }

    @Override
    public CoordinateListWrapper deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        List<Coordinate> convertedCoords = new ArrayList<>();

        if(node.isArray()) {
            Iterator<JsonNode> coordinates = node.iterator();
            JsonNode coord;
            while(coordinates.hasNext() && ( coord = coordinates.next()) != null) {
                convertedCoords.add(new Coordinate(coord.get(0).asDouble(), coord.get(1).asDouble()));
            }
        }

        try {
            return new CoordinateListWrapper(convertedCoords);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
