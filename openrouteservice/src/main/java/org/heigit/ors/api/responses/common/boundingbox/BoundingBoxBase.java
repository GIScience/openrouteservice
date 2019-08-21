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

public class BoundingBoxBase implements BoundingBox {
    protected static final int COORDINATE_DECIMAL_PLACES = 6;
    protected double minLat;
    protected double minLon;
    protected double maxLat;
    protected double maxLon;

    public BoundingBoxBase() {}

    public BoundingBoxBase(BBox bounding) {
        minLat = bounding.minLat;
        minLon = bounding.minLon;
        maxLat = bounding.maxLat;
        maxLon = bounding.maxLon;
    }

    public double getMinLat() {
        return minLat;
    }

    public double getMinLon() {
        return minLon;
    }

    public double getMaxLat() {
        return maxLat;
    }

    public double getMaxLon() {
        return maxLon;
    }

    public double[] getAsArray() {
        return new double[] {
                FormatUtility.roundToDecimals(minLon, COORDINATE_DECIMAL_PLACES),
                FormatUtility.roundToDecimals(minLat, COORDINATE_DECIMAL_PLACES),
                FormatUtility.roundToDecimals(maxLon, COORDINATE_DECIMAL_PLACES),
                FormatUtility.roundToDecimals(maxLat, COORDINATE_DECIMAL_PLACES)};
    }
}
