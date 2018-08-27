package heigit.ors.api.dataTransferObjects;

import heigit.ors.api.responses.routing.GPXRouteResponse;
import heigit.ors.api.responses.routing.JSONRouteResponse;
import heigit.ors.api.responses.routing.RouteResponse;
import heigit.ors.routing.RouteResult;

public class RouteResponseFactory {
    public static RouteResponse constructResponse(RouteResult route, RouteRequestDTO request) {
        RouteResponse response = null;

        switch(request.getResponseType()) {
            case GPX:
                response = new GPXRouteResponse(route);
                break;
            case JSON:
                response = new JSONRouteResponse(route, request.getGeometryType());
        }

        return response;
    }
}
