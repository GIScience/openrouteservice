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

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RouteSegment;
import heigit.ors.routing.RouteStep;
import io.swagger.annotations.ApiModel;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "rte")
@ApiModel(value = "rte")
public class GPXRouteElement {
    @XmlElement(name = "rtept")
    List<GPXRoutePointElement> routePoints;
    @XmlElement(name = "extensions")
    GPXRouteExtensions extensions;

    public GPXRouteElement() {
    }

    public GPXRouteElement(RouteResult result) {
        routePoints = new ArrayList<>();
        Coordinate[] routeCoordinates = result.getGeometry();
        List<RouteSegment> segments = result.getSegments();
        List<RouteStep> steps = new ArrayList<>();

        for (RouteSegment segment : segments) {
            steps.addAll(segment.getSteps());
        }

        for (int i = 0; i < steps.size(); i++) {
            RouteStep step = steps.get(i);
            int coordStartId = step.getWayPoints()[0];
            int coordEndId = step.getWayPoints()[1];
            int coordinateId = coordStartId;
            while (coordinateId >= coordStartId && coordinateId <= coordEndId) {
                if (coordStartId == coordEndId) {
                    Coordinate c = routeCoordinates[coordinateId];
                    routePoints.add(new GPXRoutePointElement(step, c.x, c.y, c.z, i));
                    break;
                } else if (coordinateId < coordEndId) {
                    Coordinate c = routeCoordinates[coordinateId];
                    routePoints.add(new GPXRoutePointElement(step, c.x, c.y, c.z, i));
                }
                coordinateId++;
            }
        }

        // it may be the case that we did not ask for instructions so there will be no steps
        if (steps.isEmpty() && routeCoordinates.length > 0) {
            for (Coordinate coord : routeCoordinates) {
                routePoints.add(new GPXRoutePointElement(null, coord.x, coord.y, coord.z, -1));
            }
        }

        extensions = new GPXRouteExtensions(result);
    }
}
