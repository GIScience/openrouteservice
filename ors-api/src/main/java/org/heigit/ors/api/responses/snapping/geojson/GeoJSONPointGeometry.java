package org.heigit.ors.api.responses.snapping.geojson;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.heigit.ors.api.responses.matrix.json.JSON2DSources;

public class GeoJSONPointGeometry {
    @JsonProperty("type")
    @Schema(description = "GeoJSON type", defaultValue = "Point")
    public final String type = "Point";

    @JsonProperty("coordinates")
    @Schema(description = "Lon/Lat coordinates of the snapped location", example = "[8.681495,49.41461]")
    public Double[] coordinates;

    public GeoJSONPointGeometry(JSON2DSources source) {
        this.coordinates = source.getLocation();
    }

    public String getType() {
        return type;
    }

    public Double[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Double[] coordinates) {
        this.coordinates = coordinates;
    }
}
