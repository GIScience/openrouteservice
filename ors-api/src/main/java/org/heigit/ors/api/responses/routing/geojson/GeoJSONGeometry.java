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

package org.heigit.ors.api.responses.routing.geojson;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.json.simple.JSONObject;

public class GeoJSONGeometry {

    private final JSONObject json;

    public GeoJSONGeometry(String type, Object coordinates) {
        this.json = new JSONObject();
        this.json.put("type", type);
        this.json.put("coordinates", coordinates);
    }

    @Schema(description = "The geometry type", example = "LineString")
    @JsonProperty("type")
    public String getType() {
        return (String) json.get("type");
    }

    @Schema(description = "The coordinates array for the geometry")
    @JsonProperty("coordinates")
    public Object getCoordinates() {
        return json.get("coordinates");
    }

    @JsonAnyGetter
    public JSONObject getJson() {
        return json;
    }
}