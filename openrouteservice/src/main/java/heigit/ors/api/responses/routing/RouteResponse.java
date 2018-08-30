package heigit.ors.api.responses.routing;

import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.routing.RouteResult;

public class RouteResponse {
    @JsonProperty("request")
    private RouteRequest request;

    public RouteResponse() {};
    public RouteResponse(RouteResult routeResult) {

    }

    public RouteRequest getRequest() {
        return request;
    }

    public void setRequest(RouteRequest request) {
        this.request = request;
    }
}
