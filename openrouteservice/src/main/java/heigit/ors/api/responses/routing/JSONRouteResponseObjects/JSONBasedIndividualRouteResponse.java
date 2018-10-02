package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBox;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBoxFactory;
import heigit.ors.api.responses.routing.IndividualRouteResponse;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RouteSegment;

import java.util.ArrayList;
import java.util.List;

public class JSONBasedIndividualRouteResponse extends IndividualRouteResponse {
    BoundingBox bbox;

    public JSONBasedIndividualRouteResponse(RouteResult result, RouteRequest request) throws StatusCodeException {
        super(result, request);

        bbox = BoundingBoxFactory.constructBoundingBox(result.getSummary().getBBox(), request);
    }

    protected List<JSONSegment> constructSegments(RouteResult routeResult) {
        List segments = new ArrayList<>();
        for(RouteSegment routeSegment : routeResult.getSegments()) {
            segments.add(new JSONSegment(routeSegment));
        }

        return segments;
    }
}
