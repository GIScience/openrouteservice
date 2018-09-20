package heigit.ors.api.responses.routing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBox;
import io.swagger.annotations.ApiResponse;

import java.util.List;

public class RouteResponse {
    @JsonIgnore
    protected RouteResponseInfo responseInformation;

    @JsonIgnore
    protected BoundingBox bbox;

    @JsonIgnore
    protected List routeResults;

    public RouteResponse(RouteRequest request) {
        responseInformation = new RouteResponseInfo(request);
    }

    public RouteResponseInfo getResponseInformation() {
        return responseInformation;
    }

    public BoundingBox getBbox() {
        return bbox;
    }

    public List getRouteResults() {
        return routeResults;
    }
}
