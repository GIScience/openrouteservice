package org.heigit.ors.api.responses.centrality.geojson;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import io.swagger.annotations.ApiModelProperty;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.api.responses.routing.geojson.GeoJSONSummary;
import org.heigit.ors.api.responses.routing.json.JSONSegment;
import org.heigit.ors.centrality.CentralityResult;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.geojson.GeometryJSON;
import org.heigit.ors.routing.RouteResult;
import org.json.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

public class GeoJsonCentralityNode {
    @JsonProperty("type")
    public final String type = "Feature";

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
