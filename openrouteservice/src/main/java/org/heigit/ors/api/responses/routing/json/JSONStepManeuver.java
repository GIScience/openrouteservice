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

package org.heigit.ors.api.responses.routing.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.locationtech.jts.geom.Coordinate;
import org.heigit.ors.routing.RouteStepManeuver;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.heigit.ors.util.FormatUtility;

@ApiModel(description = "Maneuver object of the step")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JSONStepManeuver {
    private static final int COORDINATE_PRECISION = 6;
    private static final int ELEVATION_DECIMAL_PLACES = 2;


    @ApiModelProperty(value = "The coordinate of the point where a maneuver takes place.", example = "[8.678962,49.407819]")
    @JsonProperty("location")
    private Double[] location;
    @ApiModelProperty(value = "The azimuth angle (in degrees) of the direction right before the maneuver.", example = "24")
    @JsonProperty("bearing_before")
    private final Integer bearingBefore;
    @ApiModelProperty(value = "The azimuth angle (in degrees) of the direction right after the maneuver.", example = "96")
    @JsonProperty("bearing_after")
    private final Integer bearingAfter;

    public JSONStepManeuver(RouteStepManeuver maneuver) {
        Coordinate coordinate = maneuver.getLocation();
        if(coordinate != null) {
            if (!Double.isNaN(coordinate.z)) {
                location = new Double[3];
                location[2] = FormatUtility.roundToDecimals(coordinate.z, ELEVATION_DECIMAL_PLACES);
            } else {
                location = new Double[2];
            }
            location[0] = FormatUtility.roundToDecimals(coordinate.x, COORDINATE_PRECISION);
            location[1] = FormatUtility.roundToDecimals(coordinate.y, COORDINATE_PRECISION);
        }

        bearingAfter = maneuver.getBearingAfter();
        bearingBefore = maneuver.getBearingBefore();
    }

    public Double[] getLocation() {
        return location;
    }

    public Integer getBearingBefore() {
        return bearingBefore;
    }

    public Integer getBearingAfter() {
        return bearingAfter;
    }
}
