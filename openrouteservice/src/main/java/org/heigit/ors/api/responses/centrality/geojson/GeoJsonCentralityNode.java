package org.heigit.ors.api.responses.centrality.geojson;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.api.responses.routing.geojson.GeoJSONSummary;
import org.heigit.ors.api.responses.routing.json.JSONSegment;
import org.heigit.ors.centrality.CentralityResult;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.geojson.GeometryJSON;
import org.heigit.ors.routing.RouteResult;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

public class GeoJsonCentralityNode {
    @JsonProperty("type")
    public final String type = "Feature";

    @JsonProperty("properties")
    private GeoJsonScore properties;

    public GeoJSONCentralityNode(CentralityResult centralityResult) throws StatusCodeException {
        properties = new GeoJsonScore(centralityResult);
    }

    @ApiModelProperty(dataType = "org.json.simple.JSONObject")
    @JsonProperty("geometry")
    public JSONObject getGeometry() {
        JSONObject geoJson = new JSONObject();
        geoJson.put("type", "LineString");
        geoJson.put("coordinates", GeometryJSON.toJSON(this.routeCoordinates, includeElevation));

        return geoJson;
    }

    public GeoJSONSummary getProperties() {
        return properties;
    }

}
