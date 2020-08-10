package org.heigit.ors.geojson;

import org.heigit.ors.geojson.exception.GeoJSONParseException;
import org.json.JSONObject;

public class Feature extends GeoJSON {
  private Geometry geometry;
  private JSONObject properties;
  private String id;

  public Feature(JSONObject input) {
    this.geoJSONType = "Feature";
    this.geometry = new Geometry(input.getJSONObject("geometry"));
    this.properties = input.has("properties") ? input.getJSONObject("properties") : null;
    this.id = input.has("id") ? input.getString("id") : null;
  }

  public com.vividsolutions.jts.geom.Geometry getGeometry() {
    return geometry.getGeometry();
  }

  public Geometry getGeometryObject() {
    return geometry;
  }

  public Feature[] getFeatures() {
    return new Feature[]{this};
  }

  public Feature[] getFeatures(String type) {
    if (getGeometry().getGeometryType().equals(type)) {
      return new Feature[]{this};
    } else {
      return new Feature[]{};
    }
  }

  public JSONObject getProperties() {
    if (properties == null) throw new GeoJSONParseException("Properties missing in GeoJSON.");
    return properties;
  }

  public JSONObject toJSON() {
    JSONObject geoJson = new JSONObject();
    geoJson.put("type", geoJSONType);
    geoJson.put("geometry", geometry.toJSON());
    geoJson.put("properties", properties);
    geoJson.put("id", id);
    return geoJson;
  }
}
