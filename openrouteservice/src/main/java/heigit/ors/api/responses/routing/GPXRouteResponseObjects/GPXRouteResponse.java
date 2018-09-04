package heigit.ors.api.responses.routing.GPXRouteResponseObjects;

import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.RouteResponse;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.routing.RouteResult;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "gpx")
public class GPXRouteResponse extends RouteResponse {

    @XmlAttribute(name = "version")
    private String gpxVersion = "1.0";

    @XmlAttribute(name = "creator")
    private String gpxCreator = "openrouteservice";

    @XmlElement(name = "metadata")
    private GPXMetadata metadata;

    @XmlElement(name = "rte")
    private List<GPXRouteElement> routes;

    //@XmlElement(name = "extensions")

    public GPXRouteResponse() throws StatusCodeException {
        super(null);
        init(null, null);
    }

    public GPXRouteResponse(RouteResult[] routeResult, RouteRequest request) throws StatusCodeException {
        super(request);
        init(routeResult, request);
        for(RouteResult result : routeResult) {
            routes.add(new GPXRouteElement(result));
        }
    }

    private void init(RouteResult[] result, RouteRequest request) throws StatusCodeException {
        metadata = new GPXMetadata(result, request);
        routes = new ArrayList<>();
    }

    public List<GPXRouteElement> getGpxRouteElements() {
        return routes;
    }
}
