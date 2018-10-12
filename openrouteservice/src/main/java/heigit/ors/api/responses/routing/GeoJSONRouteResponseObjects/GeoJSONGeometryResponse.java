/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package heigit.ors.api.responses.routing.GeoJSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.responses.routing.GeometryResponse;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.geojson.GeometryJSON;
import heigit.ors.routing.RoutingErrorCodes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.json.simple.JSONObject;

@ApiModel(value = "GeoJSONGeometryResponse")
public class GeoJSONGeometryResponse extends GeometryResponse {

    public GeoJSONGeometryResponse(Coordinate[] coords, boolean includeElevation) {
        super(coords, includeElevation);
    }

    @ApiModelProperty(dataType = "org.json.simple.JSONObject")
    @JsonProperty("geometry")
    @Override
    public JSONObject getGeometry() {
        JSONObject geoJson = new JSONObject();
        geoJson.put("type", "LineString");
        geoJson.put("coordinates", GeometryJSON.toJSON(coordinates, includeElevation));

        return geoJson;
    }

}
