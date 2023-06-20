package org.heigit.ors.api.responses.export.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.heigit.ors.common.Pair;

import java.util.Map;

public class JsonEdgeExtra {

    @Schema(description = "Id of the corresponding edge in the graph", example = "1")
    @JsonProperty(value = "edgeId")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    protected String nodeId;

    @Schema(description = "Extra info stored on the edge",
            example = "{\"surface_quality_known\" : \"true\"}")
    @JsonProperty(value = "extra")
    @JsonFormat(shape = JsonFormat.Shape.ANY)
    protected Object extra;

    JsonEdgeExtra(Map.Entry<Pair<Integer, Integer>, Map<String, Object>> edge) {
        this.nodeId = edge.getKey().first.toString() + "->" + edge.getKey().second.toString();
        this.extra = edge.getValue();
    }
}




