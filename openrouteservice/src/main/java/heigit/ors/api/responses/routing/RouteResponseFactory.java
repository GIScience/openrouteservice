package heigit.ors.api.responses.routing;

import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONRouteResponse;
import heigit.ors.routing.RouteResult;

public class RouteResponseFactory {
    public static RouteResponse constructResponse(RouteResult route, RouteRequest request) {
        RouteResponse response = null;

        switch(request.getResponseType()) {
            case GPX:
                response = new GPXRouteResponse(route);
                break;
            case JSON:
                response = new JSONRouteResponse(route, request.getGeometryType());
        }

        response.setRequest(request);

        return response;
    }
}
