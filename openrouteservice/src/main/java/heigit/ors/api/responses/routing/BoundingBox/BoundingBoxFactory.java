package heigit.ors.api.responses.routing.BoundingBox;

import com.graphhopper.util.shapes.BBox;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.GPXRouteResponseObjects.GPXBounds;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSON3DBoundingBox;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONBoundingBox;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.routing.RoutingErrorCodes;

public class BoundingBoxFactory {
    public static BoundingBox constructBoundingBox(BBox bounds, RouteRequest request) throws ParameterValueException {
        switch(request.getResponseType()) {
            case GEOJSON:
            case JSON:
                if(request.hasReturnElevationForPoints() && request.getReturnElevationForPoints())
                    return new JSON3DBoundingBox(bounds);
                return new JSONBoundingBox(bounds);
            case GPX:
                return new GPXBounds(bounds);
            default:
                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "format", request.getResponseType().toString());
        }
    }
}
