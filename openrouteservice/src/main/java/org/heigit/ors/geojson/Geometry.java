package org.heigit.ors.geojson;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import org.heigit.ors.geojson.exception.GeoJSONException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * GeoJSON Geometry representation.
 */
// Class may be merged with GeometryJSON in the future.
public class Geometry extends GeoJSON {
  public static final String[] ALLOWED_SIMPLE_GEOMETRY_TYPES = {"Point", "LineString", "Polygon"};
  public static final String[] ALLOWED_MULTI_GEOMETRY_TYPES = {"MultiPoint", "MultiLineString", "MultiPolygon", "GeometryCollection"};
  public static final String[] ALLOWED_GEOMETRY_TYPES = Stream.concat(Arrays.stream(ALLOWED_SIMPLE_GEOMETRY_TYPES), Arrays.stream(ALLOWED_MULTI_GEOMETRY_TYPES)).toArray(String[]::new);

  private com.vividsolutions.jts.geom.Geometry geometry;

  /**
   * Create Geometry from JSON.
   *
   * {@link GeometryJSON} is used to parse the actual geometry.
   * @param input {@link JSONObject} representing a Geometry.
   */
  public Geometry(JSONObject input) {
    try {
      this.geometry = GeometryJSON.parse(input);
    } catch (JSONException e) {
      throw new GeoJSONException("Geometry could not be parsed.\n" + e.getMessage());
    }
    this.geoJSONType = "Geometry";
  }

  /**
   * Returns the actual {@link com.vividsolutions.jts.geom.Geometry}.
   * @return {@link com.vividsolutions.jts.geom.Geometry}.
   */
  public com.vividsolutions.jts.geom.Geometry getGeometry() {
    return geometry;
  }

  /**
   * Get geometry type.
   * @return Geometry type as {@link String}.
   */
  public String getGeometryType() {
    return geometry.getGeometryType();
  }

  /**
   * Implemented for compatiblity reasons. Only throws an exception.
   */
  public Feature[] getFeatures() {
    throw new GeoJSONException("Geometry does not contain any features.");
  }

  /**
   * Implemented for compatiblity reasons. Only throws an exception.
   */
  public Feature[] getFeatures(String type) {
    throw new GeoJSONException("Geometry does not contain any features.");
  }

  @Override
  public JSONObject toJSON() {
    if (!Arrays.asList(ALLOWED_GEOMETRY_TYPES).contains(getGeometryType())) {
      throw new GeoJSONException("Invalid geometry type building GeoJSON.");
    }
    JSONObject geoJson = new JSONObject();
    geoJson.put("type", getGeometryType());
    geoJson.put("coordinates", GeometryJSON.toJSON(geometry));
    return geoJson;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Geometry geometry1 = (Geometry) o;
    return Objects.equals(geometry, geometry1.geometry) &&
        Objects.equals(geoJSONType, geometry1.geoJSONType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(geometry, geoJSONType);
  }
}
