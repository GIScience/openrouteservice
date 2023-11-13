package org.heigit.ors.api.responses.snapping.geojson;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.heigit.ors.api.responses.matrix.json.JSON2DSources;

public class GeoJSONFeatureProperties {
    @JsonProperty("name")
    @Schema(description = "\"Name of the street the closest accessible point is situated on. Only for `resolve_locations=true` and only if name is available.",
            extensions = {@Extension(name = "validWhen", properties = {
                    @ExtensionProperty(name = "ref", value = "resolve_locations"),
                    @ExtensionProperty(name = "value", value = "true", parseValue = true)}
            )},
            example = "Gerhart-Hauptmann-Straße")
    public String name;
    @JsonProperty("snapped_distance")
    @Schema(description = "Distance between the `source/destination` Location and the used point on the routing graph in meters.",
    example = "0.02")
    public double dist;

    @JsonProperty("source_id")
    @Schema(description = "Index of the requested location")
    public int sourceId;

    public GeoJSONFeatureProperties(int sourceId, JSON2DSources source) {
        this.sourceId = sourceId;
        this.dist = source.getSnappedDistance();
        this.name = source.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDist() {
        return dist;
    }

    public void setDist(double dist) {
        this.dist = dist;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }
}
