package org.heigit.ors.api.responses.centrality.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

public class JsonNodeScore {
    @Schema(description = "Id of the corresponding node in the graph", example = "1")
    @JsonProperty(value = "nodeId")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    protected Integer nodeId;

    @Schema(description = "Centrality Score of the corresponding node in the given bounding box",
            example = "123.45")
    @JsonProperty(value = "score")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT)
    protected Double score;

    JsonNodeScore(Map.Entry<Integer, Double> nodeScore) {
        this.nodeId = nodeScore.getKey();
        this.score = nodeScore.getValue();
    }
}
