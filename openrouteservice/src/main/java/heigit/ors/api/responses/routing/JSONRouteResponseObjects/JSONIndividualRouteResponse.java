package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import heigit.ors.api.requests.routing.APIRoutingEnums;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.*;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBox;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBoxFactory;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.routing.RouteResult;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class JSONIndividualRouteResponse extends JSONBasedIndividualRouteResponse {

    @JsonProperty("bbox")
    private BoundingBox bbox;

    @JsonUnwrapped
    private GeometryResponse geomResponse;

    private JSONSummary summary;

    private List<JSONSegment> segments;

    @JsonProperty("way_points")
    private int[] wayPoints;

    @JsonProperty("geometry_format")
    private APIRoutingEnums.RouteResponseGeometryType geometryFormat;

    public JSONIndividualRouteResponse(RouteResult routeResult, RouteRequest request) throws StatusCodeException {
        super(routeResult, request);

        geometryFormat = request.getGeometryType();
        geomResponse = GeometryResponseFactory.createGeometryResponse(this.routeCoordinates, this.includeElevation, geometryFormat);
        summary = new JSONSummary(routeResult.getSummary().getDistance(), routeResult.getSummary().getDuration());
        segments = constructSegments(routeResult);

        bbox = BoundingBoxFactory.constructBoundingBox(routeResult.getSummary().getBBox(), request);

        wayPoints = routeResult.getWayPointsIndices();
    }

    public BoundingBox getBbox() {
        return bbox;
    }

    public GeometryResponse getGeomResponse() {
        return geomResponse;
    }

    public JSONSummary getSummary() {
        return summary;
    }

    public APIRoutingEnums.RouteResponseGeometryType getGeometryFormat() {
        return geometryFormat;
    }

    public List<JSONSegment> getSegments() {
        return segments;
    }

    public int[] getWayPoints() {
        return wayPoints;
    }
}
