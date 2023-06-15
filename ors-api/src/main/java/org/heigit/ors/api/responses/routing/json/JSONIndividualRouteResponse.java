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
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.locationtech.jts.geom.Coordinate;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.api.responses.common.boundingbox.BoundingBoxFactory;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.routing.RouteExtraInfo;
import org.heigit.ors.routing.RouteResult;
import org.heigit.ors.routing.RouteWarning;
import org.heigit.ors.util.DistanceUnitUtil;
import org.heigit.ors.util.PolylineEncoder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@ApiModel(value = "JSONIndividualRouteResponse", description = "An individual JSON based route created by the service")
public class JSONIndividualRouteResponse extends JSONBasedIndividualRouteResponse {
    @ApiModelProperty(value = "The geometry of the route. For JSON route responses this is an encoded polyline.", example = "yuqlH{i~s@gaUe@VgEQFcBRbB_C")
    @JsonProperty("geometry")
    @JsonUnwrapped
    private final String geomResponse;

    @ApiModelProperty("Summary information about the route")
    private final JSONSummary summary;

    @ApiModelProperty("List containing the segments and its corresponding steps which make up the route.")
    private final List<JSONSegment> segments;

    @JsonProperty("way_points")
    @ApiModelProperty(value = "List containing the indices of way points corresponding to the *geometry*.", example = "[0,23]")
    private final List<Integer> wayPoints;

    @JsonProperty("warnings")
    @ApiModelProperty("List of warnings that have been generated for the route")
    private List<JSONWarning> warnings;

    @ApiModelProperty("List containing the legs the route consists of.")
    @JsonProperty("legs")
    @JsonInclude()
    private final List<JSONLeg> legs;
    private final Map<String, JSONExtra> extras;

    @ApiModelProperty(value = "Departure date and time" +
            "CUSTOM_KEYS:{'validWhen':{'ref':'departure','value':true}}", example = "2020-01-31T12:45:00+01:00")
    @JsonProperty(value = "departure")
    protected ZonedDateTime departure;
    @ApiModelProperty(value = "Arrival date and time" +
            "CUSTOM_KEYS:{'validWhen':{'ref':'arrival','value':true}}", example = "2020-01-31T13:15:00+01:00")
    @JsonProperty(value = "arrival")
    protected ZonedDateTime arrival;

    public JSONIndividualRouteResponse(RouteResult routeResult, RouteRequest request) throws StatusCodeException {
        super(routeResult, request);

        geomResponse = constructEncodedGeometry(this.routeCoordinates);

        if(this.includeElevation)
            summary = new JSONSummary(routeResult.getSummary().getDistance(), routeResult.getSummary().getDuration(), routeResult.getSummary().getAscent(), routeResult.getSummary().getDescent());
        else
            summary = new JSONSummary(routeResult.getSummary().getDistance(), routeResult.getSummary().getDuration());

        if (this.isPtRequest) {
            summary.setTransfers(routeResult.getSummary().getTransfers());
            summary.setFare(routeResult.getSummary().getFare());
        }

        if(routeResult.hasDepartureAndArrival()) {
            departure = routeResult.getDeparture();
            arrival = routeResult.getArrival();
        }

        segments = constructSegments(routeResult, request);

        bbox = BoundingBoxFactory.constructBoundingBox(routeResult.getSummary().getBBox(), request);

        wayPoints = routeResult.getWayPointsIndices();

        extras = new HashMap<>();
        List<RouteExtraInfo> responseExtras = routeResult.getExtraInfo();
        if(responseExtras != null) {
            double routeLength = routeResult.getSummary().getDistance();
            DistanceUnit units = DistanceUnit.METERS;
            if (request.hasUnits())
                units =  DistanceUnitUtil.getFromString(request.getUnits().toString(), DistanceUnit.UNKNOWN);
            for (RouteExtraInfo extraInfo : responseExtras) {
                extras.put(extraInfo.getName(), new JSONExtra(extraInfo.getSegments(), extraInfo.getSummary(units, routeLength, true)));
            }
        }

        if (routeResult.getWarnings() != null && !routeResult.getWarnings().isEmpty()) {
            warnings= new ArrayList<>();
            for (RouteWarning warning : routeResult.getWarnings()) {
                warnings.add(new JSONWarning(warning));
            }
        }

        legs = constructLegs(routeResult);
    }

    private String constructEncodedGeometry(final Coordinate[] coordinates) {
        if(coordinates != null)
            return PolylineEncoder.encode(coordinates, includeElevation, new StringBuilder());
        else
            return "";
    }

    @ApiModelProperty(value = "A bounding box which contains the entire route", example = "[49.414057, 8.680894, 49.420514, 8.690123]")
    @JsonProperty("bbox")
    public double[] getBbox() {
        return bbox.getAsArray();
    }

    public String getGeomResponse() {
        return geomResponse;
    }

    @ApiModelProperty(value = "List of extra info objects representing the extra info items that were requested for the route.")
    @JsonProperty("extras")
    public Map<String, JSONExtra> getExtras() {
        return extras;
    }

    public JSONSummary getSummary() {
        return summary;
    }

    public List<JSONSegment> getSegments() {
        return segments;
    }

    public List<Integer> getWayPoints() {
        return wayPoints;
    }
}
