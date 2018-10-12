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

package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.responses.routing.GeometryResponse;
import heigit.ors.util.PolylineEncoder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("EncodedPolyLineGeometryResponse")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EncodedPolylineGeometryResponse extends GeometryResponse {
    public final String FORMAT = "encodedpolyline";

    public EncodedPolylineGeometryResponse(Coordinate[]coordinates, boolean includeElevation) {
        super(coordinates, includeElevation);
    }

    @ApiModelProperty(dataType = "String")
    @JsonProperty("geometry")
    @Override
    public Object getGeometry() {
        StringBuffer strBuffer = new StringBuffer();
        if(coordinates != null)
            return PolylineEncoder.encode(coordinates, includeElevation, strBuffer);
        else
            return null;
    }
}
