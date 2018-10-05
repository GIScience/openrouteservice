package heigit.ors.api.responses.routing.GeoJSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONBasedIndividualRouteResponse;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONSegment;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.routing.RouteResult;

import java.util.List;
import java.util.Map;

public class GeoJSONIndividualRouteResponse extends JSONBasedIndividualRouteResponse {
    @JsonUnwrapped
    private GeoJSONGeometryResponse geomResponse;

    @JsonProperty("type")
    public final String type = "Feature";

    @JsonProperty("properties")
    private GeoJSONSummary properties;

    public GeoJSONIndividualRouteResponse(RouteResult routeResult, RouteRequest request) throws StatusCodeException {
        super(routeResult, request);
        geomResponse = new GeoJSONGeometryResponse(this.routeCoordinates, this.includeElevation);
        List<JSONSegment> segments = constructSegments(routeResult);

        Map extras = constructExtras(request, routeResult);

        properties = new GeoJSONSummary(routeResult, segments, extras);
    }

    public GeoJSONGeometryResponse getGeomResponse() {
        return geomResponse;
    }

    public GeoJSONSummary getProperties() {
        return properties;
    }

    @JsonProperty("bbox")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public double[] getBBox() {
        return bbox.getAsArray();
    }
}
