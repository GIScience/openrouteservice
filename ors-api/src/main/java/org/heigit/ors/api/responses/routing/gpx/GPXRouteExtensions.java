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

package org.heigit.ors.api.responses.routing.gpx;

import org.heigit.ors.routing.RouteResult;
import org.heigit.ors.routing.RouteSummary;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "extensions")
public class GPXRouteExtensions {
    @XmlElement(name = "distance")
    private double distance;
    @XmlElement(name = "duration")
    private double duration;
    @XmlElement(name = "ascent")
    private double ascent;
    @XmlElement(name = "descent")
    private double descent;
    @XmlElement(name = "avgspeed")
    private double avgSpeed;
    @XmlElement(name = "bounds")
    private GPXBounds bounds;

    public GPXRouteExtensions() {}

    public GPXRouteExtensions(RouteResult result) {
        RouteSummary summary = result.getSummary();
        this.distance = summary.getDistance();
        this.duration = summary.getDuration();
        this.ascent = summary.getAscent();
        this.descent = summary.getDescent();
        this.avgSpeed = summary.getAverageSpeed();
        this.bounds = new GPXBounds(result.getSummary().getBBox());
    }
}
