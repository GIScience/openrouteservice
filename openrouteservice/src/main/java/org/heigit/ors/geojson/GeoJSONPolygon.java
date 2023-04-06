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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiModel;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "GeoJSONPolygon", description = "Input GeoJSON specification.")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GeoJSONPolygon extends GeometryJSON {
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_COORDINATES = "coordinates";
    private static final GeometryFactory geometryFactory = new GeometryFactory();
    ObjectMapper mapper = new ObjectMapper();

    @JsonProperty(value = PARAM_TYPE)
    private String type;

    @JsonProperty(value = PARAM_COORDINATES)
    private List<List<List<Double>>> coordinates;

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

    public JSONObject toJsonObject() throws JsonProcessingException {
        String json = mapper.writeValueAsString(this);
        return new JSONObject(json);
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
