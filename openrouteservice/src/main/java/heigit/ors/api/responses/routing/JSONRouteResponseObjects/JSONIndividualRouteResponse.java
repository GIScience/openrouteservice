package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import heigit.ors.api.requests.routing.APIRoutingEnums;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.*;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBox;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBoxFactory;
import heigit.ors.common.DistanceUnit;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.routing.RouteExtraInfo;
import heigit.ors.routing.RouteResult;
import heigit.ors.util.DistanceUnitUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@ApiModel(value = "JSONIndividualRouteResponse", description = "An individual JSON based route created by the service")
public class JSONIndividualRouteResponse extends JSONBasedIndividualRouteResponse {

    private BoundingBox bbox;

    @JsonUnwrapped
    private EncodedPolylineGeometryResponse geomResponse;

    @ApiModelProperty("Summary information about the route")
    private JSONSummary summary;

    @ApiModelProperty("The segments of the route. These correspond to routing instructions")
    private List<JSONSegment> segments;

    @JsonProperty("way_points")
    @ApiModelProperty("A list of the locations of coordinates in the route that correspond to the waypoints")
    private int[] wayPoints;

    private Map<String, JSONExtra> extras;

    public JSONIndividualRouteResponse(RouteResult routeResult, RouteRequest request) throws StatusCodeException {
        super(routeResult, request);

        geomResponse = new EncodedPolylineGeometryResponse(this.routeCoordinates, this.includeElevation);
        summary = new JSONSummary(routeResult.getSummary().getDistance(), routeResult.getSummary().getDuration());
        segments = constructSegments(routeResult);

        bbox = BoundingBoxFactory.constructBoundingBox(routeResult.getSummary().getBBox(), request);

        wayPoints = routeResult.getWayPointsIndices();

        extras = new HashMap<>();
        List<RouteExtraInfo> responseExtras = routeResult.getExtraInfo();
        if(responseExtras != null) {
            double routeLength = routeResult.getSummary().getDistance();
            DistanceUnit units =  DistanceUnitUtil.getFromString(request.getUnits().toString(), DistanceUnit.Unknown);
            for (RouteExtraInfo extraInfo : responseExtras) {
                extras.put(extraInfo.getName(), new JSONExtra(extraInfo.getSegments(), extraInfo.getSummary(units, routeLength, true)));

            }
        }
    }

    @ApiModelProperty(value = "A bounding box which contains the entire route", example = "[49.414057, 8.680894, 49.420514, 8.690123]")
    @JsonProperty("bbox")
    public double[] getBbox() {
        return bbox.getAsArray();
    }

    @ApiModelProperty(value = "The geometry of the route", dataType = "heigit.ors.api.responses.routing.GeoJSONRouteResponseObjects.GeoJSONGeometryResponse")
    @JsonProperty("geometry")
    public GeometryResponse getGeomResponse() {
        return geomResponse;
    }

    @JsonProperty("extras")
    public Map<String, JSONExtra> getExtras() {
        return extras;
    }

    @JsonProperty("geometry_format")
    public String getGeometryFormat() {
        return geomResponse.FORMAT;
    }

    public JSONSummary getSummary() {
        return summary;
    }

    public List<JSONSegment> getSegments() {
        return segments;
    }

    public int[] getWayPoints() {
        return wayPoints;
    }
}
