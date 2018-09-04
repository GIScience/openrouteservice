package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import com.graphhopper.util.shapes.BBox;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.IndividualRouteResponse;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RouteSegment;

import java.util.ArrayList;
import java.util.List;

public class JSONBasedIndividualRouteResponse extends IndividualRouteResponse {
    public JSONBasedIndividualRouteResponse(RouteResult result, RouteRequest request) {
        super(result, request);
    }

    protected List<JSONSegment> constructSegments(RouteResult routeResult) {
        List segments = new ArrayList<>();
        for(RouteSegment routeSegment : routeResult.getSegments()) {
            segments.add(new JSONSegment(routeSegment));
        }

        return segments;
    }
}
