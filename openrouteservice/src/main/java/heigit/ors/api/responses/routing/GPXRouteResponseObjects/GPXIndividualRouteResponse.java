package heigit.ors.api.responses.routing.GPXRouteResponseObjects;

import heigit.ors.api.responses.routing.IndividualRouteResponse;
import heigit.ors.routing.RouteResult;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "route")
public class GPXIndividualRouteResponse extends IndividualRouteResponse {
    private String test;

    public GPXIndividualRouteResponse() { test="blah2"; }

    public GPXIndividualRouteResponse(RouteResult routeResult) {
        super();
        test="blah";
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }
}
