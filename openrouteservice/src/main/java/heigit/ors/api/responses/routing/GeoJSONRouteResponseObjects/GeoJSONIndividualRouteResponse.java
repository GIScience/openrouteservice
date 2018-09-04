package heigit.ors.api.responses.routing.GeoJSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import heigit.ors.api.requests.routing.APIRoutingEnums;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.GeometryResponse;
import heigit.ors.api.responses.routing.GeometryResponseFactory;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONBasedIndividualRouteResponse;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONSegment;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONSummary;
import heigit.ors.routing.RouteResult;

import java.util.List;

public class GeoJSONIndividualRouteResponse extends JSONBasedIndividualRouteResponse {
    @JsonUnwrapped
    private GeometryResponse geomResponse;

    @JsonProperty("type")
    public final String type = "Feature";

    @JsonProperty("properties")
    private JSONSummary properties;

    public GeoJSONIndividualRouteResponse(RouteResult routeResult, RouteRequest request) {
        super(routeResult, request);
        geomResponse = GeometryResponseFactory.createGeometryResponse(this.routeCoordinates, includeElevation, APIRoutingEnums.RouteResponseGeometryType.GEOJSON);
        List<JSONSegment> segments = constructSegments(routeResult);
        properties = new GeoJSONSummary(routeResult, segments);
    }

    public GeometryResponse getGeomResponse() {
        return geomResponse;
    }

    public JSONSummary getProperties() {
        return properties;
    }
}
