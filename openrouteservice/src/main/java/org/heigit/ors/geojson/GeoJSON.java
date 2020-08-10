package org.heigit.ors.geojson;

import java.util.Arrays;
import org.heigit.ors.geojson.exception.GeoJSONException;
import org.json.JSONObject;

public abstract class GeoJSON {

  public static String[] ALLOWED_TYPES = {"FeatureCollection", "Feature", "Geometry"};

  protected String geoJSONType;

  /**
   * Overloaded function for {@link #parse(JSONObject)} that receives a {@link String} instead of
   * a {@link JSONObject org.json.JSONObject}.
   */
  public static GeoJSON parse(String input) {
    return parse(new JSONObject(input));
  }

  /**
   * Overloaded function for {@link #parse(JSONObject)} that receives a {@link
   * org.json.simple.JSONObject org.json.simple.JSONObject} instead of a {@link JSONObject
   * org.json.JSONObject}.
   */
  public static GeoJSON parse(org.json.simple.JSONObject input) {
    return parse(input.toJSONString());
  }

  public static GeoJSON parse(JSONObject input) {
    String type = input.getString("type");
    if (type.equals("FeatureCollection")) {
      return new FeatureCollection(input);
    } else if (type.equals("Feature")) {
      return new Feature(input);
    } else if (Arrays.asList(Geometry.ALLOWED_GEOMETRY_TYPES).contains(type)) {
      return new Geometry(input);
    } else {
      throw new GeoJSONException("Invalid GeoJSON type: " + type);
    }
  }

  public String getGeoJSONType() {
    return geoJSONType;
  }

  public abstract Feature[] getFeatures();

  public abstract JSONObject toJSON();

  public String toJSONString() {
    return toJSON().toString();
  }
}

