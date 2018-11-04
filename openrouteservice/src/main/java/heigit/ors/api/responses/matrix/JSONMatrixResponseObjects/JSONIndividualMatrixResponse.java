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

package heigit.ors.api.responses.matrix.JSONMatrixResponseObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.requests.matrix.MatrixRequest;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBox;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBoxFactory;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONExtra;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONSegment;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONSummary;
import heigit.ors.common.DistanceUnit;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.matrix.MatrixResult;
import heigit.ors.routing.RouteExtraInfo;
import heigit.ors.routing.RouteResult;
import heigit.ors.util.DistanceUnitUtil;
import heigit.ors.util.PolylineEncoder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
// TODO not yet finished
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@ApiModel(value = "JSONIndividualRouteResponse", description = "An individual JSON based route created by the service")
public class JSONIndividualMatrixResponse extends JSONBasedIndividualMatrixResponse {
    @ApiModelProperty(value = "The durations of the matrix calculations.")
    @JsonProperty("durations")
    private List<Double> durations;

    @ApiModelProperty(value = "The individual destinations of the matrix calculations.")
    @JsonProperty("destinations")
    private JSONDestinations destinations;

    @ApiModelProperty(value = "The individual sources of the matrix calculations.")
    @JsonProperty("sources")
    private JSONSources sources;

    // TODO construct every matrix like in the usual get request
    public JSONIndividualMatrixResponse(MatrixResult routeResult, MatrixRequest request) throws StatusCodeException {
        super(routeResult, request);
        // TODO construction of the json results
        // 1. Construct Durations here!, 2. Construct Destinations in JSONBasedIndividualMatrixResponse, 3. Construct Sources in JSONBasedIndividualMatrixResponse
        // For "resolve_locations" add then to JSONBasedIndividualMatrixResponse
      /*  geomResponse = constructEncodedGeometry(this.routeCoordinates, this.includeElevation);

        if (this.includeElevation)
            summary = new JSONSummary(routeResult.getSummary().getDistance(), routeResult.getSummary().getDuration(), routeResult.getSummary().getAscent(), routeResult.getSummary().getDescent());
        else
            summary = new JSONSummary(routeResult.getSummary().getDistance(), routeResult.getSummary().getDuration());

        segments = constructSegments(routeResult, request);

        bbox = BoundingBoxFactory.constructBoundingBox(routeResult.getSummary().getBBox(), request);

        wayPoints = routeResult.getWayPointsIndices();

        extras = new HashMap<>();
        List<RouteExtraInfo> responseExtras = routeResult.getExtraInfo();
        if (responseExtras != null) {
            double routeLength = routeResult.getSummary().getDistance();
            DistanceUnit units = DistanceUnitUtil.getFromString(request.getUnits().toString(), DistanceUnit.Unknown);
            for (RouteExtraInfo extraInfo : responseExtras) {
                extras.put(extraInfo.getName(), new JSONExtra(extraInfo.getSegments(), extraInfo.getSummary(units, routeLength, true)));
            }
        }*/
    }

   /* private String constructEncodedGeometry(final Coordinate[] coordinates, boolean useElevation) {
        if (coordinates != null)
            return PolylineEncoder.encode(coordinates, includeElevation, new StringBuffer());
        else
            return "";
    }*/
}
