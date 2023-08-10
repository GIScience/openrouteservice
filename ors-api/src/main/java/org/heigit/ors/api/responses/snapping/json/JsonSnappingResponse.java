package org.heigit.ors.api.responses.snapping.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.v3.oas.annotations.media.Schema;
import org.heigit.ors.api.responses.matrix.json.JSON2DSources;
import org.heigit.ors.api.responses.matrix.json.JSONIndividualMatrixResponse;
import org.heigit.ors.api.responses.matrix.json.JSONLocation;
import org.heigit.ors.api.responses.snapping.SnappingResponse;
import org.heigit.ors.matrix.ResolvedLocation;
import org.heigit.ors.snapping.SnappingResult;

import java.util.ArrayList;
import java.util.List;

@Schema(name = "SnappingResponse", description = "The Snapping Response contains the snapped coordinates.")
public class JsonSnappingResponse extends SnappingResponse {
    @Schema(description = "The snapped locations as coordinates and snapping distance.")
    @JsonProperty("locations")
    List<JSONLocation> locations;
    public JsonSnappingResponse(SnappingResult result) {
        super(result);
        locations = constructLocations(result);
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
