package heigit.ors.api.responses.routing;

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.geojson.GeometryJSON;
import org.json.simple.JSONObject;

public class GeoJSONGeometryResponse extends GeometryResponse {

    public GeoJSONGeometryResponse(Coordinate[] coords, boolean includeElevation) {
        super(coords, includeElevation);
    }

    @Override
    public Object getGeometry() {

        JSONObject geoJson = new JSONObject();
        geoJson.put("type", "LineString");
        geoJson.put("coordinates", GeometryJSON.toJSON(coordinates, includeElevation));

        return geoJson;
    }

}
