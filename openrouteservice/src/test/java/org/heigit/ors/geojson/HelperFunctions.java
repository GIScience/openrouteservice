package org.heigit.ors.geojson;

import static org.heigit.ors.util.HelperFunctions.convertCoordinateArray;

import org.json.JSONArray;
import org.json.JSONObject;

public class HelperFunctions {
  public static JSONObject buildFeatureCollectionJSON(JSONArray featuresJson) {
    JSONObject inputJson = new JSONObject();
    inputJson.put("type", "FeatureCollection");
    inputJson.put("features", featuresJson);
    return inputJson;
  }

  public static JSONObject buildFeatureJSON() {
    JSONObject inputJson = new JSONObject();
    inputJson.put("type", "Feature");
    JSONObject properties = new JSONObject();
    properties.put("foo", "bar");
    properties.put("someNumber", 1.4);
    inputJson.put("properties", properties);
    inputJson.put("id", "1285-16834-1908-3");
    inputJson.put("geometry", buildGeometryJSON());
    return inputJson;
  }

  public static JSONObject buildGeometryJSON() {
    JSONObject inputJson = new JSONObject();
    inputJson.put("type", "Polygon");
    JSONArray coordinates = new JSONArray();
    coordinates.put(GeometryJSON.toJSON(convertCoordinateArray(new double[][]{{-1,1}, {1,1}, {1,2}, {-1,1}}), false));
    inputJson.put("coordinates", coordinates);
    return inputJson;
  }
}
