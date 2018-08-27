package heigit.ors.api.responses.routing;

import heigit.ors.api.dataTransferObjects.RouteResponseDTO;
import heigit.ors.routing.RouteResult;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "route")
public class GPXRouteResponse extends RouteResponse {
    private String test;

    public GPXRouteResponse() { test="blah2"; }

    public GPXRouteResponse(RouteResult routeResult) {
        super(routeResult);
        test="blah";
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }
}
