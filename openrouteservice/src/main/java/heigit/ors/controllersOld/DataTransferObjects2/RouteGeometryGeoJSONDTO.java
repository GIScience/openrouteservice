package heigit.ors.controllersOld.DataTransferObjects2;

import com.fasterxml.jackson.databind.JsonNode;
import heigit.ors.util.JsonUtility;
import org.json.JSONObject;

public class RouteGeometryGeoJSONDTO extends RouteGeometryJSONDTO {

    @Override
    public JsonNode getGeometry() {
        JSONObject json = new JSONObject();

        json.put("type", "GeoJSON");

        return JsonUtility.convertJsonFormat(json);
    }
}
