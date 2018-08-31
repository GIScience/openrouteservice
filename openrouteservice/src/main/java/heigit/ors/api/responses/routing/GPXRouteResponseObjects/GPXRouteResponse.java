package heigit.ors.api.responses.routing.GPXRouteResponseObjects;

import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.RouteResponse;
import heigit.ors.routing.RouteResult;

public class GPXRouteResponse extends RouteResponse {
    public GPXRouteResponse(RouteResult[] routeResult, RouteRequest request) {
        super(request);
    }
}
