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

package org.heigit.ors.api.responses.routing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.api.responses.common.boundingbox.BoundingBox;

import java.util.List;

public class RouteResponse {
    @JsonIgnore
    protected RouteResponseInfo responseInformation;

    @JsonIgnore
    protected BoundingBox bbox;

    @JsonIgnore
    protected List<IndividualRouteResponse> routeResults;

    public RouteResponse(RouteRequest request) {
        responseInformation = new RouteResponseInfo(request);
    }

    public RouteResponseInfo getResponseInformation() {
        return responseInformation;
    }

    public BoundingBox getBbox() {
        return bbox;
    }

    public List<IndividualRouteResponse> getRouteResults() {
        return routeResults;
    }
}
