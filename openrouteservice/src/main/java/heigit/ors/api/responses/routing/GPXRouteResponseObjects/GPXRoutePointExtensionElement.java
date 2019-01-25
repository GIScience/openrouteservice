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

import heigit.ors.routing.RouteStep;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "extensions")
public class GPXRoutePointExtensionElement {
    @XmlElement(name = "distance")
    private double distance;
    @XmlElement(name="duration")
    private double duration;
    @XmlElement(name = "type")
    private int type;
    @XmlElement(name = "step")
    private int step;

    public GPXRoutePointExtensionElement() {

    }

    public GPXRoutePointExtensionElement(RouteStep step, int stepNumber) {
        distance = step.getDistance();
        duration = step.getDuration();
        type = step.getType();
        this.step = stepNumber;
    }
}
