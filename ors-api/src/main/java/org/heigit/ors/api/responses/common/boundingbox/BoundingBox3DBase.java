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

package org.heigit.ors.api.responses.common.boundingbox;

import com.graphhopper.util.shapes.BBox;
import org.heigit.ors.util.FormatUtility;

public class BoundingBox3DBase extends BoundingBoxBase {
    private static final int ELEVATION_DECIMAL_PLACES = 2;
    protected double minEle;
    protected double maxEle;

    public BoundingBox3DBase(BBox bounding) {
        super(bounding);
        this.maxEle = bounding.maxEle;
        this.minEle = bounding.minEle;
    }

    public double getMinEle() {
        return minEle;
    }

    public double getMaxEle() {
        return maxEle;
    }

    @Override
    public double[] getAsArray() {
        return new double[] {
                FormatUtility.roundToDecimals(minLon, COORDINATE_DECIMAL_PLACES),
                FormatUtility.roundToDecimals(minLat, COORDINATE_DECIMAL_PLACES),
                FormatUtility.roundToDecimals(minEle, ELEVATION_DECIMAL_PLACES),
                FormatUtility.roundToDecimals(maxLon, COORDINATE_DECIMAL_PLACES),
                FormatUtility.roundToDecimals(maxLat, COORDINATE_DECIMAL_PLACES),
                FormatUtility.roundToDecimals(maxEle, ELEVATION_DECIMAL_PLACES),
        };
    }
}
