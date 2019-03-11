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

package heigit.ors.api.responses.routing.GeoJSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.util.shapes.BBox;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.common.BoundingBox.BoundingBoxFactory;
import heigit.ors.api.responses.routing.IndividualRouteResponse;
import heigit.ors.api.responses.routing.RouteResponse;
import heigit.ors.api.responses.routing.RouteResponseInfo;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.routing.RouteResult;
import heigit.ors.util.GeomUtility;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

public class GeoJSONRouteResponse extends RouteResponse {
    @JsonProperty("type")
    public final String TYPE = "FeatureCollection";

    @JsonProperty("bbox")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @ApiModelProperty(value = "Bounding box that covers all returned routes", example = "[49.414057, 8.680894, 49.420514, 8.690123]")
    public double[] getBBoxAsArray() {
        return bbox.getAsArray();
    }


    public GeoJSONRouteResponse(RouteResult[] routeResults, RouteRequest request) throws StatusCodeException {
        super(request);

        this.routeResults = new ArrayList<IndividualRouteResponse>();

        for(RouteResult result : routeResults) {
            this.routeResults.add(new GeoJSONIndividualRouteResponse(result, request));
        }

        List<BBox> bboxes = new ArrayList<>();
        for(RouteResult result : routeResults) {
            bboxes.add(result.getSummary().getBBox());
        }

        BBox bounding = GeomUtility.generateBoundingFromMultiple(bboxes.toArray(new BBox[bboxes.size()]));

        bbox = BoundingBoxFactory.constructBoundingBox(bounding, request);
    }

    @JsonProperty("features")
    public List getRoutes() {
        return routeResults;
    }

    @JsonProperty("metadata")
    public RouteResponseInfo getProperties() {
        return this.responseInformation;
    }
}
