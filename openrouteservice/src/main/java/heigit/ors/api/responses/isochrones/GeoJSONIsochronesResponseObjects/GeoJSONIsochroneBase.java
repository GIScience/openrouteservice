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

package heigit.ors.api.responses.isochrones.GeoJSONIsochronesResponseObjects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import heigit.ors.geojson.GeometryJSON;
import io.swagger.annotations.ApiModelProperty;
import org.json.simple.JSONObject;

public abstract class GeoJSONIsochroneBase {
    @JsonProperty("type")
    public final String TYPE = "Feature";

    @JsonIgnore
    abstract Geometry getIsochroneGeometry();

    @ApiModelProperty(dataType = "org.json.simple.JSONObject")
    @JsonProperty("geometry")
    public JSONObject getGeometry() {
        JSONObject geoJson = new JSONObject();
        geoJson.put("type", "Polygon");
        Polygon isoPoly = (Polygon) getIsochroneGeometry();
        geoJson.put("coordinates", GeometryJSON.toJSON(isoPoly));
        return geoJson;
    }
}
