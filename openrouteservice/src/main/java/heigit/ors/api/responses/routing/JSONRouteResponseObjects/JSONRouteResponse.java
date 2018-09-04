package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.util.shapes.BBox;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.*;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBox;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBoxFactory;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.routing.RouteResult;
import heigit.ors.util.GeomUtility;

import java.util.ArrayList;
import java.util.List;

public class JSONRouteResponse extends RouteResponse {
    public JSONRouteResponse(RouteResult[] routeResults, RouteRequest request) throws StatusCodeException {
        super(request);

        this.routeResults = new ArrayList<IndividualRouteResponse>();

        List<BBox> bboxes = new ArrayList<>();
        for(RouteResult result : routeResults) {
            this.routeResults.add(new JSONIndividualRouteResponse(result, request));
            bboxes.add(result.getSummary().getBBox());
        }

        BBox bounding = GeomUtility.generateBoundingFromMultiple(bboxes.toArray(new BBox[bboxes.size()]));

        bbox = BoundingBoxFactory.constructBoundingBox(bounding, request);
    }

    @JsonProperty("routes")
    public List getRoutes() {
        return routeResults;
    }

    @JsonProperty("bbox")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public BoundingBox getBBox() {
        return bbox;
    }

    @JsonProperty("info")
    public RouteResponseInfo getInfo() {
        return responseInformation;
    }
}
