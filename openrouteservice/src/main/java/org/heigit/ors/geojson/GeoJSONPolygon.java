/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.geojson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "GeoJSONPolygon", description = "GeoJSON Polygon API class.")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GeoJSONPolygon {
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_COORDINATES = "coordinates";
    private static final GeometryFactory geometryFactory = new GeometryFactory();

    @JsonProperty(PARAM_TYPE)
    private String type;

    @JsonProperty(PARAM_COORDINATES)
    @JsonSerialize(using = CoordinatesSerializer.class)
    private List<List<List<Double>>> coordinates;


    @JsonCreator
    public GeoJSONPolygon(@JsonProperty(PARAM_TYPE) String type, @JsonProperty(PARAM_COORDINATES) List<List<List<Double>>> coordinates) {
        setType(type);
        setCoordinates(coordinates);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<List<List<Double>>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<List<List<Double>>> coordinates) {
        this.coordinates = coordinates;
    }

//    private static class CoordinatesDeserializer extends JsonDeserializer<List<List<List<Double>>>> {
//        @Override
//        public List<List<List<Double>>> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
//            return jsonParser.readValueAs(List.class);
//        }
//    }

    private static class CoordinatesSerializer extends JsonSerializer<List<List<List<Double>>>> {
        @Override
        public void serialize(List<List<List<Double>>> coordinates, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartArray();
            for (List<List<Double>> polygon : coordinates) {
                jsonGenerator.writeStartArray();
                for (List<Double> point : polygon) {
                    jsonGenerator.writeStartArray();
                    jsonGenerator.writeNumber(point.get(0));
                    jsonGenerator.writeNumber(point.get(1));
                    jsonGenerator.writeEndArray();
                }
                jsonGenerator.writeEndArray();
            }
            jsonGenerator.writeEndArray();
        }
    }

    public Polygon[] convertToJTS() {
        List<Polygon> polygons = new ArrayList<>();

        List<List<List<Double>>> coordinatesDoubles = this.getCoordinates();

        for (List<List<Double>> linearRingCoords : coordinatesDoubles) {
            Coordinate[] jtsCoords = new Coordinate[linearRingCoords.size()];
            for (int i = 0; i < linearRingCoords.size(); i++) {
                List<Double> coord = linearRingCoords.get(i);
                jtsCoords[i] = new Coordinate(coord.get(0), coord.get(1));
            }

            LinearRing linearRing = geometryFactory.createLinearRing(jtsCoords);
            Polygon jtsPolygon = geometryFactory.createPolygon(linearRing);
            polygons.add(jtsPolygon);
        }
        return polygons.toArray(new Polygon[0]);
    }
}
