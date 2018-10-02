package heigit.ors.api.responses.routing.GeoJSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.responses.routing.GeometryResponse;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.geojson.GeometryJSON;
import heigit.ors.routing.RoutingErrorCodes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.json.simple.JSONObject;

@ApiModel(value = "GeoJSONGeometryResponse")
public class GeoJSONGeometryResponse extends GeometryResponse {

    public GeoJSONGeometryResponse(Coordinate[] coords, boolean includeElevation) {
        super(coords, includeElevation);
    }

    @ApiModelProperty(dataType = "org.json.simple.JSONObject")
    @JsonProperty("geometry")
    @Override
    public JSONObject getGeometry() {
        JSONObject geoJson = new JSONObject();
        geoJson.put("type", "LineString");
        geoJson.put("coordinates", GeometryJSON.toJSON(coordinates, includeElevation));

        return geoJson;
    }

}
