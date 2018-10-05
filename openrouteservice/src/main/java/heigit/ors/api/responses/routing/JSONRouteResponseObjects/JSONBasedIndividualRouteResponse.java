package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBox;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBoxFactory;
import heigit.ors.api.responses.routing.IndividualRouteResponse;
import heigit.ors.common.DistanceUnit;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.routing.RouteExtraInfo;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RouteSegment;
import heigit.ors.util.DistanceUnitUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONBasedIndividualRouteResponse extends IndividualRouteResponse {
    protected BoundingBox bbox;

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

    protected Map<String, JSONExtra> constructExtras(RouteRequest routeRequest, RouteResult routeResult) throws StatusCodeException {
        Map<String, JSONExtra> extras = new HashMap<>();
        List<RouteExtraInfo> responseExtras = routeResult.getExtraInfo();
        if(responseExtras != null) {
            double routeLength = routeResult.getSummary().getDistance();
            DistanceUnit units =  DistanceUnitUtil.getFromString(routeRequest.getUnits().toString(), DistanceUnit.Unknown);
            for (RouteExtraInfo extraInfo : responseExtras) {
                extras.put(extraInfo.getName(), new JSONExtra(extraInfo.getSegments(), extraInfo.getSummary(units, routeLength, true)));

            }
        }

        return extras;
    }
}
