package heigit.ors.controllersOld.DataTransferObjects2;

import com.fasterxml.jackson.databind.JsonNode;
import heigit.ors.util.JsonUtility;
import org.json.JSONObject;

public class RouteGeometryJSONDTO implements RouteGeometryDTO<JsonNode> {
    public JsonNode getGeometry() {
        JSONObject json = new JSONObject();

        json.put("type", "JSON");

        return JsonUtility.convertJsonFormat(json);
    }
}
