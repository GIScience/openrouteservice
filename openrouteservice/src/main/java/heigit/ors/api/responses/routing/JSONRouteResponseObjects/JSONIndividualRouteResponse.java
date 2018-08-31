package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.graphhopper.util.shapes.BBox;
import heigit.ors.api.requests.routing.APIRoutingEnums;
import heigit.ors.api.responses.routing.GeometryResponse;
import heigit.ors.api.responses.routing.GeometryResponseFactory;
import heigit.ors.api.responses.routing.IndividualRouteResponse;
import heigit.ors.api.responses.routing.RouteResponse;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RouteSegment;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class JSONIndividualRouteResponse extends IndividualRouteResponse {

    private double[][] bbox;

    @JsonUnwrapped
    private GeometryResponse geomResponse;

    private JSONSummary summary;

    private List<JSONSegment> segments;

    @JsonProperty("geometry_format")
    private APIRoutingEnums.RouteResponseGeometryType geometryFormat;

    public JSONIndividualRouteResponse() {
        super();
    }

    public JSONIndividualRouteResponse(RouteResult routeResult, APIRoutingEnums.RouteResponseGeometryType geomType) {
        super();
        geomResponse = GeometryResponseFactory.createGeometryResponse(routeResult.getGeometry(), false, geomType);
        geometryFormat = geomType;
        summary = new JSONSummary(routeResult.getSummary().getDistance(), routeResult.getSummary().getDuration());
        segments = constructSegments(routeResult);
        bbox = convertBBox(routeResult.getSummary().getBBox());
    }

    public double[][] getBbox() {
        return bbox;
    }

    public GeometryResponse getGeomResponse() {
        return geomResponse;
    }

    public JSONSummary getSummary() {
        return summary;
    }

    public APIRoutingEnums.RouteResponseGeometryType getGeometryFormat() {
        return geometryFormat;
    }

    public List<JSONSegment> getSegments() {
        return segments;
    }


    private List<JSONSegment> constructSegments(RouteResult routeResult) {
        List segments = new ArrayList<>();
        for(RouteSegment routeSegment : routeResult.getSegments()) {
            segments.add(new JSONSegment(routeSegment));
        }

        return segments;
    }

    private double[][] convertBBox(BBox boundingIn) {

        double[][] bbox = new double[2][2];
        bbox[0][0] = boundingIn.minLon;
        bbox[0][1] = boundingIn.minLat;
        bbox[1][0] = boundingIn.maxLon;
        bbox[1][1] = boundingIn.maxLat;

        return bbox;
    }
}
