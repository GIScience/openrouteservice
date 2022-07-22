package org.heigit.ors.api.responses.export.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import io.swagger.annotations.ApiModelProperty;
import org.heigit.ors.util.FormatUtility;

import java.util.Map;

public class JsonEdgeExtra {

    @ApiModelProperty(value = "Id of the corresponding edge in the graph", example = "1")
    @JsonProperty(value = "edgeId")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    protected Integer nodeId;

    @ApiModelProperty(value = "Extra info stored on the edge",
            example = "{\"surface_quality_known\" : \"true\"}")
    @JsonProperty(value = "extra")
    @JsonFormat(shape = JsonFormat.Shape.ANY)
    protected Object extra;

    JsonEdgeExtra(Map.Entry<Integer, Map<String, Object>> edge) {
        this.nodeId = edge.getKey();
        this.extra = edge.getValue();
    }
}




