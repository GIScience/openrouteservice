package org.heigit.ors.api.responses.export.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.heigit.ors.util.FormatUtility;
import org.locationtech.jts.geom.Coordinate;

import java.util.Map;

public class JsonNode {
    protected static final int COORDINATE_DECIMAL_PLACES = 6;

    @Schema(description = "Id of the corresponding node in the graph", example = "1")
    @JsonProperty(value = "nodeId")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    protected Integer nodeId;

    @Schema(description = "{longitude},{latitude} coordinates of the closest accessible point on the routing graph",
            example = "[8.678962, 49.40783]")
    @JsonProperty(value = "location")
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    protected Coordinate location;

    JsonNode(Map.Entry<Integer, Coordinate> location) {
        this.nodeId = location.getKey();
        this.location = location.getValue();
    }

    public Double[] getLocation() {
        Double[] coord2D = new Double[2];
        coord2D[0] = FormatUtility.roundToDecimals(location.x, COORDINATE_DECIMAL_PLACES);
        coord2D[1] = FormatUtility.roundToDecimals(location.y, COORDINATE_DECIMAL_PLACES);
        // coord2D[3] = location.z; --> example for third dimension
        return coord2D;
    }
}




