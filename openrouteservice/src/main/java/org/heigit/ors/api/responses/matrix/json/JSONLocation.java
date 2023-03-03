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

package org.heigit.ors.api.responses.matrix.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.locationtech.jts.geom.Coordinate;
import org.heigit.ors.matrix.ResolvedLocation;
import org.heigit.ors.util.FormatUtility;
import io.swagger.annotations.ApiModelProperty;

public class JSONLocation {
    protected static final int COORDINATE_DECIMAL_PLACES = 6;
    private static final int SNAPPED_DISTANCE_DECIMAL_PLACES = 2;

    @ApiModelProperty(value = "{longitude},{latitude} coordinates of the closest accessible point on the routing graph",
            example = "[8.678962, 49.40783]")
    @JsonProperty(value = "location")
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    protected Coordinate location;

    @ApiModelProperty(value = "Name of the street the closest accessible point is situated on. Only for `resolve_locations=true` and only if name is available." +
            "CUSTOM_KEYS:{'validWhen':{'ref':'resolve_locations','value':true}}",
            example = "Bergheimer Straße")
    @JsonProperty(value = "name")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    protected String name;

    @ApiModelProperty(value = "Distance between the `source/destination` Location and the used point on the routing graph.", example = "1.2")
    @JsonProperty(value = "snapped_distance")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "%.2d")
    private final Double snappedDistance;

    JSONLocation(ResolvedLocation location, boolean includeResolveLocations) {
        this.snappedDistance = location.getSnappedDistance();
        this.location = location.getCoordinate();
        if (includeResolveLocations)
            this.name = location.getName();
    }

    public Double getSnappedDistance() {
        return FormatUtility.roundToDecimals(snappedDistance, SNAPPED_DISTANCE_DECIMAL_PLACES);
    }

    public Double[] getLocation() {
        return new Double[0];
    }
}
