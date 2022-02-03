package org.heigit.ors.api.responses.centrality.geojson;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import io.swagger.annotations.ApiModelProperty;
import org.json.JSONArray;
import org.json.simple.JSONObject;

public class GeoJsonCentralityNode {
    @JsonProperty("type")
    public static final String type = "Feature";

    @JsonProperty("properties")
    private JSONObject properties;

    public GeoJsonCentralityNode(Coordinate coord, Double score) {
        setProperties(score);
        setGeometry(coord);
    }

    @ApiModelProperty(dataType = "org.json.simple.JSONObject")
    @JsonProperty("geometry")
    public JSONObject geometry;

    public void setProperties(Double score) {
        JSONObject props = new JSONObject();
        props.put("score", score);

        this.properties = props;
    }

    public void setGeometry(Coordinate coord) {
        JSONObject geoJson = new JSONObject();
        geoJson.put("type", "Point");
        JSONArray coordinates = new JSONArray(2);
        coordinates.put(coord.x);
        coordinates.put(coord.y);

        geoJson.put("coordinates", coordinates);

        this.geometry = geoJson;
    }

    public JSONObject getProperties() {
        return properties;
    }

    public JSONObject getGeometry() { return geometry; }

}
