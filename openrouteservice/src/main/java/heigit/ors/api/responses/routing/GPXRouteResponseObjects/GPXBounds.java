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
import heigit.ors.api.responses.routing.BoundingBox.BoundingBox;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBoxBase;

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
        return this.minLat;
    }
    @XmlAttribute(name = "minLon")
    public double getMinLon() {
        return this.minLon;
    }
    @XmlAttribute(name = "maxLat")
    public double getMaxLat() {
        return this.maxLat;
    }
    @XmlAttribute(name = "maxLon")
    public double getMaxLon() {
        return this.maxLon;
    }
}
