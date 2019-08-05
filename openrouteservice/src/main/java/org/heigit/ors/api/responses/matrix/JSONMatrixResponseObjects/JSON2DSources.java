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

import com.fasterxml.jackson.annotation.JsonInclude;
import heigit.ors.matrix.ResolvedLocation;
import heigit.ors.util.FormatUtility;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class JSON2DSources extends JSONLocation {
    JSON2DSources(ResolvedLocation source, boolean includeResolveLocations) {
        super(source, includeResolveLocations);
    }

    @Override
    public Double[] getLocation() {
        Double[] location2D = new Double[2];
        location2D[0] = FormatUtility.roundToDecimals(location.x, COORDINATE_DECIMAL_PLACES);
        location2D[1] = FormatUtility.roundToDecimals(location.y, COORDINATE_DECIMAL_PLACES);
        // location2D[3] = location.z; --> example for third dimension
        return location2D;
    }
}
