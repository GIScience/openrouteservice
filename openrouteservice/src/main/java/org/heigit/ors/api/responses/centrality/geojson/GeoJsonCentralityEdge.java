package org.heigit.ors.api.responses.centrality.geojson;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import io.swagger.annotations.ApiModelProperty;
import org.json.JSONArray;
import org.json.simple.JSONObject;

public class GeoJsonCentralityEdge {
    @JsonProperty("type")
    public final String type = "Feature";

    @JsonProperty("properties")
    private JSONObject properties;

    public GeoJsonCentralityEdge(Coordinate from, Coordinate to, Double score) {
        setProperties(score);
        setGeometry(from, to);
    }

    @ApiModelProperty(dataType = "org.json.simple.JSONObject")
    @JsonProperty("geometry")
    public JSONObject geometry;

    public void setProperties(Double score) {
        JSONObject props = new JSONObject();
        props.put("score", score);

        this.properties = props;
    }

    public void setGeometry(Coordinate from, Coordinate to) {
        JSONObject geoJson = new JSONObject();
        geoJson.put("type", "LineString");
        JSONArray coordinates = new JSONArray(2);
        JSONArray fromArray = new JSONArray(2);
        JSONArray toArray = new JSONArray(2);
        fromArray.put(from.x);
        fromArray.put(from.y);
        toArray.put(to.x);
        toArray.put(to.y);

        coordinates.put(fromArray);
        coordinates.put(toArray);

        geoJson.put("coordinates", coordinates);

        this.geometry = geoJson;
    }

    public JSONObject getProperties() {
        return properties;
    }

    public JSONObject getGeometry() { return geometry; }

}
