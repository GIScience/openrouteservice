package org.heigit.ors.api.responses.snapping.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.heigit.ors.api.EndpointsProperties;
import org.heigit.ors.api.SystemMessageProperties;
import org.heigit.ors.api.requests.snapping.SnappingApiRequest;
import org.heigit.ors.api.responses.matrix.json.JSON2DSources;
import org.heigit.ors.api.responses.matrix.json.JSONLocation;
import org.heigit.ors.api.responses.snapping.SnappingResponse;
import org.heigit.ors.api.responses.snapping.SnappingResponseInfo;
import org.heigit.ors.matrix.ResolvedLocation;
import org.heigit.ors.snapping.SnappingResult;

import java.util.ArrayList;
import java.util.List;

@Schema(name = "SnappingResponse", description = "The Snapping Response contains the snapped coordinates.")
public class JsonSnappingResponse extends SnappingResponse {
    @Schema(description = "The snapped locations as coordinates and snapping distance.")
    @JsonProperty("locations")
    List<JSONLocation> locations;

    @JsonProperty("metadata")
    @Schema(description = "Information about the service and request")
    SnappingResponseInfo responseInformation;
    public JsonSnappingResponse(SnappingResult result, SnappingApiRequest request, SystemMessageProperties systemMessageProperties, EndpointsProperties endpointsProperties) {
        super(result);
        locations = constructLocations(result);
        responseInformation = new SnappingResponseInfo(request, systemMessageProperties, endpointsProperties);
        responseInformation.setGraphDate(result.getGraphDate());
    }

    private List<JSONLocation> constructLocations(SnappingResult result) {
        List<JSONLocation> locs = new ArrayList<JSONLocation>();
        for (ResolvedLocation location: result.getLocations()) {
            if (location == null) {
                locs.add(null);
            } else {
                locs.add(new JSON2DSources(location, true));
            }
        }
        return locs;
    }
}
