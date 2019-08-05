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

package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.common.BoundingBox.BoundingBox;
import heigit.ors.api.responses.common.BoundingBox.BoundingBoxFactory;
import heigit.ors.api.responses.routing.IndividualRouteResponse;
import heigit.ors.common.DistanceUnit;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.routing.RouteExtraInfo;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RouteSegment;
import heigit.ors.util.DistanceUnitUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONBasedIndividualRouteResponse extends IndividualRouteResponse {
    protected BoundingBox bbox;

    public JSONBasedIndividualRouteResponse(RouteResult result, RouteRequest request) throws StatusCodeException {
        super(result, request);
        if(request.hasUseElevation() && request.getUseElevation())
            includeElevation = true;

        bbox = BoundingBoxFactory.constructBoundingBox(result.getSummary().getBBox(), request);
    }

    protected List<JSONSegment> constructSegments(RouteResult routeResult, RouteRequest request) {
        List segments = new ArrayList<>();
        for(RouteSegment routeSegment : routeResult.getSegments()) {
            segments.add(new JSONSegment(routeSegment, request, routeResult.getSummary().getDistance()));
        }

        return segments;
    }

    protected Map<String, JSONExtra> constructExtras(RouteRequest routeRequest, RouteResult routeResult) throws StatusCodeException {
        Map<String, JSONExtra> extras = new HashMap<>();
        List<RouteExtraInfo> responseExtras = routeResult.getExtraInfo();
        if(responseExtras != null) {
            double routeLength = routeResult.getSummary().getDistance();
            DistanceUnit units = DistanceUnit.Meters;
            if (routeRequest.hasUnits())
                DistanceUnitUtil.getFromString(routeRequest.getUnits().toString(), DistanceUnit.Unknown);
            for (RouteExtraInfo extraInfo : responseExtras) {
                extras.put(extraInfo.getName(), new JSONExtra(extraInfo.getSegments(), extraInfo.getSummary(units, routeLength, true)));

            }
        }

        return extras;
    }
}
