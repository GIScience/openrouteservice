package heigit.ors.api.responses.routing;

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.routing.RouteResult;

public class IndividualRouteResponse {
    protected Coordinate[] routeCoordinates;
    protected boolean includeElevation = false;


    public IndividualRouteResponse(RouteResult result, RouteRequest request) {
        this.routeCoordinates = result.getGeometry();

        if(request.hasReturnElevationForPoints())
            includeElevation = request.getReturnElevationForPoints();
    }
}
