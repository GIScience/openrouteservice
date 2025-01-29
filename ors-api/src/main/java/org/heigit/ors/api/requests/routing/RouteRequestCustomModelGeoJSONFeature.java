package org.heigit.ors.api.requests.routing;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RouteRequestCustomModelGeoJSONFeature {
    @JsonProperty("type")
    @Schema(description = "GeoJSON type", defaultValue = "Feature")
    public final String type = "Feature";

    @JsonProperty("geometry")
    @Schema(description = "Feature geometry")
    public RouteRequestCustomModelGeoJSONPolygonGeometry geometry;
}
