package org.heigit.ors.api.responses.snapping.geojson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.util.shapes.BBox;
import io.swagger.v3.oas.annotations.media.Schema;
import org.heigit.ors.api.EndpointsProperties;
import org.heigit.ors.api.SystemMessageProperties;
import org.heigit.ors.api.requests.snapping.SnappingApiRequest;
import org.heigit.ors.api.responses.common.boundingbox.BoundingBox;
import org.heigit.ors.api.responses.common.boundingbox.BoundingBoxBase;
import org.heigit.ors.api.responses.matrix.json.JSON2DSources;
import org.heigit.ors.api.responses.routing.json.JSONBoundingBox;
import org.heigit.ors.api.responses.snapping.SnappingResponse;
import org.heigit.ors.api.responses.snapping.SnappingResponseInfo;
import org.heigit.ors.matrix.ResolvedLocation;
import org.heigit.ors.snapping.SnappingResult;
import org.heigit.ors.util.GeomUtility;

import java.util.ArrayList;
import java.util.List;

@Schema(name = "GeoJSONSnappingResponse", description = "The GeoJSON Snapping Response contains the snapped coordinates in GeoJSON format.")
public class GeoJSONSnappingResponse extends SnappingResponse {

    @JsonIgnore
    protected BoundingBox bbox;

    @JsonProperty("type")
    @Schema(description = "GeoJSON type", defaultValue = "FeatureCollection")
    public final String type = "FeatureCollection";

    @JsonProperty("bbox")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(description = "Bounding box that covers all returned snapping points", example = "[49.414057, 8.680894, 49.420514, 8.690123]")
    public double[] getBBoxAsArray() {
        return bbox.getAsArray();
    }

    @JsonProperty("features")
    @Schema(description = "Information about the service and request")
    public List<GeoJSONFeature> features;

    @JsonProperty("metadata")
    @Schema(description = "Information about the service and request")
    SnappingResponseInfo responseInformation;

    public GeoJSONSnappingResponse(SnappingResult result, SnappingApiRequest request, SystemMessageProperties systemMessageProperties, EndpointsProperties endpointsProperties) {
        super(result);
        this.features = new ArrayList<>();
        List<BBox> bBoxes = new ArrayList<>();
        for (int sourceId = 0; sourceId < result.getLocations().length; sourceId++){
            ResolvedLocation resolvedLocation = result.getLocations()[sourceId];
            if (resolvedLocation != null) {
                // create BBox for each point to use existing generateBoundingFromMultiple function
                double x = resolvedLocation.getCoordinate().x;
                double y = resolvedLocation.getCoordinate().y;
                bBoxes.add(new BBox(x,x,y,y));
                this.features.add(new GeoJSONFeature(sourceId, new JSON2DSources(resolvedLocation, true)));
            }
        }

        BBox[] boxes = bBoxes.toArray(new BBox[0]);
        if (boxes.length > 0) {
            this.bbox = new BoundingBoxBase(GeomUtility.generateBoundingFromMultiple(boxes));
        } else {
            this.bbox = new JSONBoundingBox(new BBox(0,0,0,0));
        }

        responseInformation = new SnappingResponseInfo(request, systemMessageProperties, endpointsProperties);
        responseInformation.setGraphDate(result.getGraphDate());
    }
}
