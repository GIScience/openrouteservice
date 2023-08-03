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

package org.heigit.ors.api.responses.routing.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.util.shapes.BBox;
import io.swagger.v3.oas.annotations.media.Schema;
import org.heigit.ors.api.EndpointsProperties;
import org.heigit.ors.api.SystemMessageProperties;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.api.responses.common.boundingbox.BoundingBoxFactory;
import org.heigit.ors.api.responses.routing.RouteResponse;
import org.heigit.ors.api.responses.routing.RouteResponseInfo;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.routing.RouteResult;
import org.heigit.ors.util.GeomUtility;

import java.util.ArrayList;
import java.util.List;

@Schema(name = "JSONRouteResponse")
public class JSONRouteResponse extends RouteResponse {
    public JSONRouteResponse(RouteResult[] routeResults, RouteRequest request, SystemMessageProperties systemMessageProperties, EndpointsProperties endpointsProperties) throws StatusCodeException {
        super(request, systemMessageProperties, endpointsProperties);

        this.routeResults = new ArrayList<>();

        List<BBox> bboxes = new ArrayList<>();
        for (RouteResult result : routeResults) {
            this.routeResults.add(new JSONIndividualRouteResponse(result, request));
            bboxes.add(result.getSummary().getBBox());
            responseInformation.setGraphDate(result.getGraphDate());
        }

        BBox bounding = GeomUtility.generateBoundingFromMultiple(bboxes.toArray(new BBox[0]));
        bbox = BoundingBoxFactory.constructBoundingBox(bounding, request);
    }

    @JsonProperty("routes")
    @Schema(description = "A list of routes returned from the request")
    public JSONIndividualRouteResponse[] getRoutes() {
        return routeResults.toArray(new JSONIndividualRouteResponse[0]);
    }

    @JsonProperty("bbox")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(description = "Bounding box that covers all returned routes", example = "[49.414057, 8.680894, 49.420514, 8.690123]")
    public double[] getBBoxsArray() {
        return bbox.getAsArray();
    }

    @JsonProperty("metadata")
    @Schema(description = "Information about the service and request")
    public RouteResponseInfo getInfo() {
        return responseInformation;
    }
}
