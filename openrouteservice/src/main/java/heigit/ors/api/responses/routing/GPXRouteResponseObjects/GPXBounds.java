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

package heigit.ors.api.responses.routing.GPXRouteResponseObjects;

import com.graphhopper.util.shapes.BBox;
import heigit.ors.api.responses.common.BoundingBox.BoundingBox;
import heigit.ors.api.responses.common.BoundingBox.BoundingBoxBase;
import heigit.ors.util.FormatUtility;

import javax.xml.bind.annotation.XmlAttribute;

public class GPXBounds extends BoundingBoxBase implements BoundingBox {
    public GPXBounds() {
        super();
    }

    public GPXBounds(BBox bounding) {
        super(bounding);
    }

    @XmlAttribute(name = "minLat")
    public double getMinLat() {
        return FormatUtility.roundToDecimals(this.minLat, COORDINATE_DECIMAL_PLACES);
    }
    @XmlAttribute(name = "minLon")
    public double getMinLon() {
        return FormatUtility.roundToDecimals(this.minLon, COORDINATE_DECIMAL_PLACES);
    }
    @XmlAttribute(name = "maxLat")
    public double getMaxLat() {
        return FormatUtility.roundToDecimals(this.maxLat, COORDINATE_DECIMAL_PLACES);
    }
    @XmlAttribute(name = "maxLon")
    public double getMaxLon() {
        return FormatUtility.roundToDecimals(this.maxLon, COORDINATE_DECIMAL_PLACES);
    }
}