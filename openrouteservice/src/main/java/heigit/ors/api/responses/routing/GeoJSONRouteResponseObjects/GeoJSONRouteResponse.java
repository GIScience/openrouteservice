package heigit.ors.api.responses.routing.GeoJSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.util.shapes.BBox;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBoxFactory;
import heigit.ors.api.responses.routing.IndividualRouteResponse;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONIndividualRouteResponse;
import heigit.ors.api.responses.routing.RouteResponse;
import heigit.ors.api.responses.routing.RouteResponseInfo;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.routing.RouteResult;
import heigit.ors.util.GeomUtility;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

public class GeoJSONRouteResponse extends RouteResponse {
    @JsonProperty("type")
    public final String type = "FeatureSet";

    @JsonProperty("bbox")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @ApiModelProperty(value = "Bounding box that covers all returned routes", example = "[49.414057, 8.680894, 49.420514, 8.690123]")
    public double[] getBBox() {
        return bbox.getAsArray();
    }


    public GeoJSONRouteResponse(RouteResult[] routeResults, RouteRequest request) throws StatusCodeException {
        super(request);

        this.routeResults = new ArrayList<IndividualRouteResponse>();

        for(RouteResult result : routeResults) {
            this.routeResults.add(new GeoJSONIndividualRouteResponse(result, request));
        }

        List<BBox> bboxes = new ArrayList<>();
        for(RouteResult result : routeResults) {
            bboxes.add(result.getSummary().getBBox());
        }

        BBox bounding = GeomUtility.generateBoundingFromMultiple(bboxes.toArray(new BBox[bboxes.size()]));

        bbox = BoundingBoxFactory.constructBoundingBox(bounding, request);
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
