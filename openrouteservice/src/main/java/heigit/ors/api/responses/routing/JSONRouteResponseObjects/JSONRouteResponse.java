package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.util.shapes.BBox;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBoxFactory;
import heigit.ors.api.responses.routing.IndividualRouteResponse;
import heigit.ors.api.responses.routing.RouteResponse;
import heigit.ors.api.responses.routing.RouteResponseInfo;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.routing.RouteResult;
import heigit.ors.util.GeomUtility;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "JSONRouteResponse")
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
    @ApiModelProperty(value = "A list of routes returned from the request")
    public JSONIndividualRouteResponse[] getRoutes() {
        return (JSONIndividualRouteResponse[]) routeResults.toArray(new JSONIndividualRouteResponse[routeResults.size()]);
    }

    @JsonProperty("bbox")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @ApiModelProperty(value = "Bounding box that covers all returned routes", example = "[49.414057, 8.680894, 49.420514, 8.690123]")
    public double[] getBBox() {
        return bbox.getAsArray();
    }

    @JsonProperty("info")
    @ApiModelProperty("Information about the service and request")
    public RouteResponseInfo getInfo() {
        return responseInformation;
    }
}
