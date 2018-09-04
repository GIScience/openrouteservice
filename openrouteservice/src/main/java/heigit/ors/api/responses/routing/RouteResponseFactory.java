package heigit.ors.api.responses.routing;

import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.GPXRouteResponseObjects.GPXRouteResponse;
import heigit.ors.api.responses.routing.GeoJSONRouteResponseObjects.GeoJSONRouteResponse;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONRouteResponse;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.routing.RouteResult;

public class RouteResponseFactory {
    public static RouteResponse constructResponse(RouteResult[] route, RouteRequest request) throws StatusCodeException {
        RouteResponse response = null;

        switch(request.getResponseType()) {
            case GPX:
                response = new GPXRouteResponse(route, request);
                break;
            case JSON:
                response = new JSONRouteResponse(route, request);
                break;
            case GEOJSON:
                response = new GeoJSONRouteResponse(route, request);
                break;
        }

        return response;
    }
}
