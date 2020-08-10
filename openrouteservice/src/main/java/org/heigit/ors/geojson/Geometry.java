package org.heigit.ors.geojson;

import java.util.Arrays;
import java.util.stream.Stream;
import org.heigit.ors.geojson.exception.GeoJSONParseException;
import org.json.JSONObject;

public class Geometry extends GeoJSON {
  public static final String[] ALLOWED_SIMPLE_GEOMETRY_TYPES = {"Point", "LineString", "Polygon"};
  public static final String[] ALLOWED_MULTI_GEOMETRY_TYPES = {"MultiPoint", "MultiLineString", "MultiPolygon", "GeometryCollection"};
  public static final String[] ALLOWED_GEOMETRY_TYPES = Stream.concat(Arrays.stream(ALLOWED_SIMPLE_GEOMETRY_TYPES), Arrays.stream(ALLOWED_MULTI_GEOMETRY_TYPES)).toArray(String[]::new);

  private com.vividsolutions.jts.geom.Geometry geometry;

  public Geometry(JSONObject input) throws Exception {
    this.geometry = GeometryJSON.parse(input);
    this.geoJSONType = "Geometry";
  }

  public com.vividsolutions.jts.geom.Geometry getGeometry() {
    return geometry;
  }

  public String getGeometryType() {
    return geometry.getGeometryType();
  }

  public Feature[] getFeatures() throws GeoJSONParseException {
    throw new GeoJSONParseException("Geometry does not contain any features.");
  }

  public JSONObject toJSON() throws Exception {
    if (!Arrays.asList(ALLOWED_GEOMETRY_TYPES).contains(getGeometryType())) {
      throw new GeoJSONParseException("Invalid geometry type building GeoJSON.");
    }
    JSONObject geoJson = new JSONObject();
    geoJson.put("type", getGeometryType());
    geoJson.put("coordinates", GeometryJSON.toJSON(geometry));
    return geoJson;
  }
}
