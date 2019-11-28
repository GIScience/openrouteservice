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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.graphhopper.util.shapes.BBox;
import org.heigit.ors.api.responses.common.boundingbox.BoundingBox;
import org.heigit.ors.api.responses.common.boundingbox.BoundingBox3DBase;
import org.heigit.ors.util.FormatUtility;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder({"minLon", "minLat", "minEle", "maxLon", "maxLat", "maxEle"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JSON3DBoundingBox extends BoundingBox3DBase implements BoundingBox {
    private static final int ELEVATION_DECIMAL_PLACES = 2;

    public JSON3DBoundingBox(BBox bounds) {
        super(bounds);
    }

    @Override
    public double getMaxEle() {
        return FormatUtility.roundToDecimals(maxEle, ELEVATION_DECIMAL_PLACES);
    }

    @Override
    public double getMinEle() {
        return FormatUtility.roundToDecimals(minEle, ELEVATION_DECIMAL_PLACES);
    }

    @Override
    public double getMinLat() {
        return FormatUtility.roundToDecimals(minLat, COORDINATE_DECIMAL_PLACES);
    }

    @Override
    public double getMinLon() {
        return FormatUtility.roundToDecimals(minLon, COORDINATE_DECIMAL_PLACES);
    }

    @Override
    public double getMaxLat() {
        return FormatUtility.roundToDecimals(maxLat, COORDINATE_DECIMAL_PLACES);
    }

    @Override
    public double getMaxLon() {
        return FormatUtility.roundToDecimals(maxLon, COORDINATE_DECIMAL_PLACES);
    }
}