package heigit.ors.api.responses.isochrones.GeoJSONIsochronesResponseObjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import heigit.ors.geojson.GeometryJSON;
import heigit.ors.isochrones.Isochrone;
import io.swagger.annotations.ApiModelProperty;
import org.json.simple.JSONObject;

public class GeoJSONIsochrone {
    private Isochrone isochrone;

    @JsonProperty("type")
    public final String type = "Feature";

    @JsonProperty("properties")
    public GeoJSONIsochroneProperties properties;


    public GeoJSONIsochrone(Isochrone isochrone, Coordinate center, int travellerId) {
        this.isochrone = isochrone;
        properties = new GeoJSONIsochroneProperties(this.isochrone, center, travellerId);
    }

    @ApiModelProperty(dataType = "org.json.simple.JSONObject")
    @JsonProperty("geometry")
    public JSONObject getGeometry() {
        JSONObject geoJson = new JSONObject();
        geoJson.put("type", "Polygon");
        Polygon isoPoly = (Polygon) isochrone.getGeometry();
        geoJson.put("coordinates", GeometryJSON.toJSON(isoPoly));
        return geoJson;
    }

    public GeoJSONIsochroneProperties getProperties() {
        return properties;
    }


}
