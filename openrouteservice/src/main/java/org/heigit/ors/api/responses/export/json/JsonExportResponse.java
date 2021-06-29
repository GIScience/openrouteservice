package org.heigit.ors.api.responses.export.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import io.swagger.annotations.ApiModel;
import org.heigit.ors.api.responses.export.ExportResponse;
import org.heigit.ors.api.responses.routing.json.JSONWarning;
import org.heigit.ors.export.ExportResult;
import org.heigit.ors.centrality.CentralityWarning;
import org.heigit.ors.common.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApiModel(description = "The Export Response contains nodes and edge weights from the requested BBox")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonExportResponse extends ExportResponse {

    @JsonProperty("locations")
    public List<JsonExportLocation> locations;

    @JsonProperty("edgeWeights")
    public List<JsonEdgeWeight> edgeWeights;

    @JsonProperty("warning")
    public JSONWarning warning;

    public JsonExportResponse(ExportResult exportResult) {
        super(exportResult);

        this.locations = new ArrayList<>();
        for (Map.Entry<Integer, Coordinate> location : exportResult.getLocations().entrySet()) {
            this.locations.add(new JsonExportLocation(location));
        }

        this.edgeWeights = new ArrayList<>();
        for (Map.Entry<Pair<Integer, Integer>, Double> edgeWeight : exportResult.getEdgeWeigths().entrySet()) {
            this.edgeWeights.add(new JsonEdgeWeight(edgeWeight));
        }


        if (exportResult.hasWarning()) {
            CentralityWarning warning = exportResult.getWarning();
            this.warning = new JSONWarning(warning.getWarningCode(), warning.getWarningMessage());
        }
    }
}
