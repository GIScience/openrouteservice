package org.heigit.ors.api.responses.snapping.geojson;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.heigit.ors.api.responses.matrix.json.JSON2DSources;

public class GeoJSONFeature {
    @JsonProperty("type")
    @Schema(description = "GeoJSON type", defaultValue = "Feature")
    public final String type = "Feature";

    @JsonProperty("properties")
    @Schema(description = "Feature properties")
    public GeoJSONFeatureProperties props;

    public GeoJSONFeature(int sourceId, JSON2DSources source) {
        this.geometry = new GeoJSONPointGeometry(source);
        this.props = new GeoJSONFeatureProperties(sourceId, source);
    }

    @JsonProperty("geometry")
    @Schema(description = "Feature geometry")
    public GeoJSONPointGeometry geometry;

    public String getType() {
        return type;
    }

    public GeoJSONFeatureProperties getProps() {
        return props;
    }

    public void setProps(GeoJSONFeatureProperties props) {
        this.props = props;
    }

    public GeoJSONPointGeometry getGeometry() {
        return geometry;
    }

    public void setGeometry(GeoJSONPointGeometry geometry) {
        this.geometry = geometry;
    }

}
