package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import heigit.ors.api.requests.routing.APIRoutingEnums;
import heigit.ors.api.responses.routing.GeometryResponse;
import heigit.ors.api.responses.routing.GeometryResponseFactory;
import heigit.ors.api.responses.routing.RouteResponse;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RouteSegment;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JSONRouteResponse extends RouteResponse {

    private Double[] bbox;

    @JsonUnwrapped
    private GeometryResponse geomResponse;

    private JSONSummary summary;

    private List<JSONSegment> segments;

    @JsonProperty("geometry_format")
    private APIRoutingEnums.RouteResponseGeometryType geometryFormat;

    public JSONRouteResponse() {
        super();
    }

    public JSONRouteResponse(RouteResult routeResult, APIRoutingEnums.RouteResponseGeometryType geomType) {

        super(routeResult);
        geomResponse = GeometryResponseFactory.createGeometryResponse(routeResult.getGeometry(), false, geomType);
        geometryFormat = geomType;
        summary = new JSONSummary(routeResult.getSummary().getDistance(), routeResult.getSummary().getDuration());
        segments = new ArrayList<>();
        for(RouteSegment routeSegment : routeResult.getSegments()) {
            segments.add(new JSONSegment(routeSegment));
        }
    }

    public Double[] getBbox() {
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


}
