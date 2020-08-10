package org.heigit.ors.geojson;

import java.util.Arrays;
import java.util.Objects;
import org.heigit.ors.geojson.exception.GeoJSONException;
import org.json.JSONObject;

/**
 * Parser for GeoJSON according to <a href="https://tools.ietf.org/html/rfc7946">RFC7946</a>.
 *
 * This class throws {@link GeoJSONException} as {@link RuntimeException}, for all possible errors.
 */
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

  /**
   * Parses GeoJSON objects to
   *
   * It uses {@link GeometryJSON} to parse the {@code "Geometry"} objects.
   * @param input {@link org.json.JSONObject} to be parsed
   * @return {@link FeatureCollection}, {@link Feature}, or {@link Geometry} object.
   */
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

  /**
   * Returns the type of the GeoJSON object.
   * @return GeoJSON type as {@link String}
   */
  public String getGeoJSONType() {
    return geoJSONType;
  }

  /**
   * Returns {@link Feature Features} of the GeoJSON object.
   * @return Features as array.
   */
  public abstract Feature[] getFeatures();

  /**
   * Returns {@link Feature Features} of the GeoJSON object matching a certain geometry type.
   * @return Features as array.
   */
  public abstract Feature[] getFeatures(String type);

  /**
   * Returns the GeoJSON object as {@link JSONObject}.
   */
  public abstract JSONObject toJSON();

  /**
   * Returns the GeoJSON JSON representation as {@link String}.
   */
  public String toJSONString() {
    return toJSON().toString();
  }

  /**
   * Check if objects are equal.
   */
  @Override
  public abstract boolean equals(Object o);

  /**
   * Returns hash value for object.
   */
  @Override
  public abstract int hashCode();
}

