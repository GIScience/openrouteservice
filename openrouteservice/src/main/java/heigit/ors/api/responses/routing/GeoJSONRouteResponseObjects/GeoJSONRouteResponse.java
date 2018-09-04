package heigit.ors.api.responses.routing.GeoJSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.IndividualRouteResponse;
import heigit.ors.api.responses.routing.RouteResponse;
import heigit.ors.api.responses.routing.RouteResponseInfo;
import heigit.ors.routing.RouteResult;

import java.util.ArrayList;
import java.util.List;

public class GeoJSONRouteResponse extends RouteResponse {
    @JsonProperty("type")
    public final String type = "FeatureSet";

    public GeoJSONRouteResponse(RouteResult[] routeResults, RouteRequest request) {
        super(request);

        this.routeResults = new ArrayList<IndividualRouteResponse>();

        for(RouteResult result : routeResults) {
            this.routeResults.add(new GeoJSONIndividualRouteResponse(result, request));
        }
    }

    @JsonProperty("features")
    public List getRoutes() {
        return routeResults;
    }

    @JsonProperty("properties")
    public RouteResponseInfo getProperties() {
        return this.responseInformation;
    }
}
