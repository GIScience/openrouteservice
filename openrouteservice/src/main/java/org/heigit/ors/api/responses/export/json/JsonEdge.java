package org.heigit.ors.api.responses.export.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.heigit.ors.common.Pair;

import java.util.Map;

public class JsonEdge {
    @ApiModelProperty(value = "Id of the start point of the edge", example = "1")
    @JsonProperty(value = "fromId")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    protected Integer fromId;

    @ApiModelProperty(value = "Id of the end point of the edge", example = "2")
    @JsonProperty(value = "toId")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    protected Integer toId;

    @ApiModelProperty(value = "Weight of the corresponding edge in the given bounding box",
            example = "123.45")
    @JsonProperty(value = "weight")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT)
    protected Double weight;

    JsonEdge(Map.Entry<Pair<Integer, Integer>, Double> weightedEdge) {
        this.fromId = weightedEdge.getKey().first;
        this.toId = weightedEdge.getKey().second;
        this.weight = weightedEdge.getValue();
    }
}
