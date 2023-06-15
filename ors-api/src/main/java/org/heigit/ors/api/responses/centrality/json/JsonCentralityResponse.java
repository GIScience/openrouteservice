package org.heigit.ors.api.responses.centrality.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.locationtech.jts.geom.Coordinate;
import io.swagger.annotations.ApiModel;
import org.heigit.ors.api.responses.centrality.CentralityResponse;
import org.heigit.ors.api.responses.routing.json.JSONWarning;
import org.heigit.ors.centrality.CentralityResult;
import org.heigit.ors.centrality.CentralityWarning;
import org.heigit.ors.common.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApiModel(description = "The Centrality Response contains centrality values for nodes or edges in the requested BBox")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonCentralityResponse extends CentralityResponse {

    @JsonProperty("locations")
    public List<JsonCentralityLocation> locations;

    @JsonProperty("nodeScores")
    public List<JsonNodeScore> nodeScores;

    @JsonProperty("edgeScores")
    public List<JsonEdgeScore> edgeScores;

    @JsonProperty("warning")
    public JSONWarning warning;

    public JsonCentralityResponse(CentralityResult centralityResult) {
        super(centralityResult);

        this.locations = new ArrayList<>();
        for (Map.Entry<Integer, Coordinate> location : centralityResult.getLocations().entrySet()) {
            this.locations.add(new JsonCentralityLocation(location));
        }

        if (centralityResult.hasNodeCentralityScores()) {
            this.nodeScores = new ArrayList<>();
            for (Map.Entry<Integer, Double> nodeScore : centralityResult.getNodeCentralityScores().entrySet()) {
                this.nodeScores.add(new JsonNodeScore(nodeScore));
            }
        }

        if (centralityResult.hasEdgeCentralityScores()) {
            this.edgeScores = new ArrayList<>();
            for (Map.Entry<Pair<Integer, Integer>, Double> edgeScore : centralityResult.getEdgeCentralityScores().entrySet()) {
                this.edgeScores.add(new JsonEdgeScore(edgeScore));
            }
        }

        if (centralityResult.hasWarning()) {
            CentralityWarning warning = centralityResult.getWarning();
            this.warning = new JSONWarning(warning.getWarningCode(), warning.getWarningMessage());
        }
    }
}
