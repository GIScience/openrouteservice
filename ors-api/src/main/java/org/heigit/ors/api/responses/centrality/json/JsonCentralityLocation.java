package org.heigit.ors.api.responses.centrality.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.locationtech.jts.geom.Coordinate;
import io.swagger.v3.oas.annotations.media.Schema;
import org.heigit.ors.util.FormatUtility;

import java.util.Map;

public class JsonCentralityLocation {
    protected static final int COORDINATE_DECIMAL_PLACES = 6;

    @Schema(description = "Id of the corresponding node in the graph", example = "1")
    @JsonProperty(value = "nodeId")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    protected Integer nodeId;

    @Schema(description = "{longitude},{latitude} coordinates of the closest accessible point on the routing graph",
            example = "[8.678962, 49.40783]")
    @JsonProperty(value = "coord")
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    protected Coordinate coord;

    JsonCentralityLocation(Map.Entry<Integer, Coordinate> location) {
        this.nodeId = location.getKey();
        this.coord = location.getValue();
    }

    public Double[] getCoord() {
        Double[] coord2D = new Double[2];
        coord2D[0] = FormatUtility.roundToDecimals(coord.x, COORDINATE_DECIMAL_PLACES);
        coord2D[1] = FormatUtility.roundToDecimals(coord.y, COORDINATE_DECIMAL_PLACES);
        // coord2D[3] = location.z; --> example for third dimension
        return coord2D;
    }
}




