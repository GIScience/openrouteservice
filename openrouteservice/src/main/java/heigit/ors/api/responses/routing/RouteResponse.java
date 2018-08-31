package heigit.ors.api.responses.routing;

import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.routing.RouteResult;

import java.util.List;

public class RouteResponse {
    @JsonProperty("info")
    protected RouteResponseInfo responseInformation;

    @JsonProperty("bbox")
    protected double[][] bbox;

    @JsonProperty("routes")
    protected List<IndividualRouteResponse> routeResults;

    public RouteResponse(RouteRequest request) {
        responseInformation = new RouteResponseInfo(request);
    }

    public RouteResponseInfo getResponseInformation() {
        return responseInformation;
    }

    public double[][] getBbox() {
        return bbox;
    }

    public List<IndividualRouteResponse> getRouteResults() {
        return routeResults;
    }
}
