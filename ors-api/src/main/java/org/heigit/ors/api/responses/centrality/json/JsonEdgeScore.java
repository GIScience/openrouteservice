package org.heigit.ors.api.responses.centrality.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.heigit.ors.common.Pair;

import java.util.Map;

public class JsonEdgeScore {
    @ApiModelProperty(value = "Id of the start point of the edge", example = "1")
    @JsonProperty(value = "fromId")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    protected Integer fromId;

    @ApiModelProperty(value = "Id of the end point of the edge", example = "2")
    @JsonProperty(value = "toId")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    protected Integer toId;

    @ApiModelProperty(value = "Centrality Score of the corresponding edge in the given bounding box",
            example = "123.45")
    @JsonProperty(value = "score")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT)
    protected Double score;

    JsonEdgeScore(Map.Entry<Pair<Integer, Integer>, Double> edgeScore) {
        this.fromId = edgeScore.getKey().first;
        this.toId = edgeScore.getKey().second;
        this.score = edgeScore.getValue();
    }
}
