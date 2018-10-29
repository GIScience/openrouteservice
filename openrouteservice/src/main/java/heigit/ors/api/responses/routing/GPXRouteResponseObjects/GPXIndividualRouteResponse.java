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

import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.IndividualRouteResponse;
import heigit.ors.routing.RouteResult;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "route")
public class GPXIndividualRouteResponse extends IndividualRouteResponse {

    public GPXIndividualRouteResponse() {
        super(null, null);
    }

    public GPXIndividualRouteResponse(RouteResult routeResult, RouteRequest request) {
        super(routeResult, request);
    }
}
