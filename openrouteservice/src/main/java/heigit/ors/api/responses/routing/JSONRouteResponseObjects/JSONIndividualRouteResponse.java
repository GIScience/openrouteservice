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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@ApiModel(value = "JSONIndividualRouteResponse", description = "An individual JSON based route created by the service")
public class JSONIndividualRouteResponse extends JSONBasedIndividualRouteResponse {

    private BoundingBox bbox;

    @JsonUnwrapped
    private GeometryResponse geomResponse;

    @ApiModelProperty("Summary information about the route")
    private JSONSummary summary;

    @ApiModelProperty("The segments of the route. These correspond to routing instructions")
    private List<JSONSegment> segments;

    @JsonProperty("way_points")
    @ApiModelProperty("A list of the locations of coordinates in the orute that correspond to the waypoints")
    private int[] wayPoints;

    @JsonProperty("geometry_format")
    @ApiModelProperty("The format of the geometry that is contained in the response")
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
