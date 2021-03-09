package org.heigit.ors.api.responses.centrality.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

public class JSONCentralityLocation {
    @ApiModelProperty(value = "Id of the corresponding node in the graph", example = "1")
    @JsonProperty(value = "nodeId")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    protected Integer nodeId;

    @ApiModelProperty(value = "{longitude},{latitude} coordinates of the closest accessible point on the routing graph",
            example = "[8.678962, 49.40783]")
    @JsonProperty(value = "coord")
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    protected Coordinate coord;

    JSONCentralityLocation(Map.Entry<Integer, Coordinate> location) {
        this.nodeId = location.getKey();
        this.coord = location.getValue();
    }
}




