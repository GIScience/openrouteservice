package heigit.ors.api.responses.routing.GPXRouteResponseObjects;

import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.IndividualRouteResponse;
import heigit.ors.routing.RouteResult;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "route")
public class GPXIndividualRouteResponse extends IndividualRouteResponse {
    private String test;

    public GPXIndividualRouteResponse() {
        super(null, null);
    }

    public GPXIndividualRouteResponse(RouteResult routeResult, RouteRequest request) {
        super(routeResult, request);
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }
}
