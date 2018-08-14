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

package heigit.ors.api.responses.matrix.JSONMatrixResponseObjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.matrix.ResolvedLocation;
import heigit.ors.util.FormatUtility;

public class JSONLocation {
    final int COORDINATE_DECIMAL_PLACES = 6;
    private final int SNAPPED_DISTANCE_DECIMAL_PLACES = 2;
    @JsonProperty(value = "location")
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    protected Coordinate location;
    @JsonProperty(value = "name")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    protected String name;
    @JsonProperty(value = "snapped_distance")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "%.2d")
    private Double snapped_distance;

    JSONLocation(ResolvedLocation location, boolean includeResolveLocations) {
        this.snapped_distance = location.getSnappedDistance();
        this.location = location.getCoordinate();
        if (includeResolveLocations)
            this.name = location.getName();
    }

    public Double getSnapped_distance() {
        return FormatUtility.roundToDecimals(snapped_distance, SNAPPED_DISTANCE_DECIMAL_PLACES);
    }

    public Double[] getLocation() {
        return new Double[0];
    }
}
