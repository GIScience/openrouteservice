package org.heigit.ors.geojson;

import org.heigit.ors.geojson.exception.GeoJSONParseException;
import org.json.JSONObject;

public abstract class GeoJSON {

  public static String[] ALLOWED_TYPES = {"FeatureCollection", "Feature", "Geometry"};

  protected String geoJSONType;

  /**
   * Overloaded function for {@link #parse(JSONObject)} that receives a {@link String} instead of
   * a {@link JSONObject org.json.JSONObject}.
   */
  public static GeoJSON parse(String input) throws Exception {
    return parse(new JSONObject(input));
  }

  /**
   * Overloaded function for {@link #parse(JSONObject)} that receives a {@link
   * org.json.simple.JSONObject org.json.simple.JSONObject} instead of a {@link JSONObject
   * org.json.JSONObject}.
   */
  public static GeoJSON parse(org.json.simple.JSONObject input) throws Exception {
    return parse(input.toJSONString());
  }

  public static GeoJSON parse(JSONObject input) throws Exception {
    switch (input.getString("type")) {
      case "FeatureCollection":
        return new FeatureCollection(input);
      case "Feature":
        return new Feature(input);
      case "Geometry":
        return new Geometry(input);
      default:
        throw new GeoJSONParseException("Foo");
    }
  }

  public String getGeoJSONType() {
    return geoJSONType;
  }

  public abstract Feature[] getFeatures() throws GeoJSONParseException;

  public abstract JSONObject toJSON() throws Exception;

  public String toJSONString() throws Exception {
    return toJSON().toString();
  }
}

