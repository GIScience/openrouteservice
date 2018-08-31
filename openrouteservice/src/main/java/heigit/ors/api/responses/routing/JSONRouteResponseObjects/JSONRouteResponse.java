package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.IndividualRouteResponse;
import heigit.ors.api.responses.routing.RouteResponse;
import heigit.ors.api.responses.routing.RouteResponseInfo;
import heigit.ors.routing.RouteResult;

import java.util.ArrayList;
import java.util.List;

public class JSONRouteResponse extends RouteResponse {
    public JSONRouteResponse(RouteResult[] routeResults, RouteRequest request) {
        super(request);

        this.routeResults = new ArrayList<IndividualRouteResponse>();

        for(RouteResult result : routeResults) {
            this.routeResults.add(new JSONIndividualRouteResponse(result, request.getGeometryType()));
        }
    }

    @JsonProperty("routes")
    public List getRoutes() {
        return routeResults;
    }

    @JsonProperty("bbox")
    public double[][] getBBox() {
        return bbox;
    }

    @JsonProperty("info")
    public RouteResponseInfo getInfo() {
        return responseInformation;
    }
}
