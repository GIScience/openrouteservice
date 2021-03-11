package org.heigit.ors.api.responses.centrality.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import io.swagger.annotations.ApiModel;
import org.heigit.ors.api.responses.centrality.CentralityResponse;
import org.heigit.ors.centrality.CentralityResult;
import org.heigit.ors.common.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApiModel(description = "The Centrality Response contains centrality values for nodes or edges in the requested BBox")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JSONCentralityResponse extends CentralityResponse {

    @JsonProperty("locations")
    public List<JSONCentralityLocation> locations;

    @JsonProperty("nodeScores")
    public List<JSONNodeScore> nodeScores;

    @JsonProperty("edgeScores")
    public List<JSONEdgeScore> edgeScores;

    public JSONCentralityResponse(CentralityResult centralityResult) {
        super(centralityResult);

        this.locations = new ArrayList<>();
        for (Map.Entry<Integer, Coordinate> location : centralityResult.getLocations().entrySet()) {
            this.locations.add(new JSONCentralityLocation(location));
        }

        if (centralityResult.hasNodeCentralityScores()) {
            this.nodeScores = new ArrayList<>();
            for (Map.Entry<Integer, Double> nodeScore : centralityResult.getNodeCentralityScores().entrySet()) {
                this.nodeScores.add(new JSONNodeScore(nodeScore));
            }
        }

        if (centralityResult.hasEdgeCentralityScores()) {
            this.edgeScores = new ArrayList<>();
            for (Map.Entry<Pair<Integer, Integer>, Double> edgeScore : centralityResult.getEdgeCentralityScores().entrySet()) {
                this.edgeScores.add(new JSONEdgeScore(edgeScore));
            }
        }
    }
}
