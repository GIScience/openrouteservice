package org.heigit.ors.api.responses.centrality.geojson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.util.shapes.BBox;
import io.swagger.annotations.ApiModelProperty;
import org.heigit.ors.api.requests.centrality.CentralityRequest;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.api.responses.centrality.CentralityResponse;
import org.heigit.ors.api.responses.common.boundingbox.BoundingBox;
import org.heigit.ors.api.responses.common.boundingbox.BoundingBoxFactory;
import org.heigit.ors.api.responses.routing.IndividualRouteResponse;
import org.heigit.ors.api.responses.routing.RouteResponseInfo;
import org.heigit.ors.api.responses.routing.geojson.GeoJSONIndividualRouteResponse;
import org.heigit.ors.centrality.CentralityResult;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.routing.RouteResult;
import org.heigit.ors.util.GeomUtility;

import java.util.ArrayList;
import java.util.List;

public class GeoJSONCentralityResponse extends CentralityResponse {
    @JsonProperty("type")
    public final String type = "FeatureCollection";

    @JsonProperty("bbox")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @ApiModelProperty(value = "Bounding box that covers all returned routes", example = "[49.414057, 8.680894, 49.420514, 8.690123]")
    public double[] getBBoxAsArray() {
        return bbox.getAsArray();
    }

    public GeoJSONCentralityResponse(CentralityResult result, CentralityRequest request) throws StatusCodeException {
        super(result);

        this.routeResults = new ArrayList<IndividualRouteResponse>();

        for(RouteResult result : routeResults) {
            this.routeResults.add(new GeoJSONIndividualRouteResponse(result, request));
            responseInformation.setGraphDate(result.getGraphDate());
        }

        List<BBox> bboxes = new ArrayList<>();
        for(RouteResult result : routeResults) {
            bboxes.add(result.getSummary().getBBox());
        }

        BBox bounding = GeomUtility.ge(bboxes.toArray(new BBox[bboxes.size()]));

        bbox = BoundingBox request.getBbox();
    }

    @JsonProperty("features")
    public List getRoutes() {
        return routeResults;
    }

    @JsonProperty("metadata")
    public RouteResponseInfo getProperties() {
        return this.responseInformation;
    }
}
