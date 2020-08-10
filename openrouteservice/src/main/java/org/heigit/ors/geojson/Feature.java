package org.heigit.ors.geojson;

import org.json.JSONObject;

public class Feature extends GeoJSON {
  private Geometry geometry;
  private JSONObject properties;
  private String id;

  public Feature(JSONObject input) throws Exception {
    this.geoJSONType = "Feature";
    this.geometry = new Geometry(input.getJSONObject("geometry"));
    this.properties = input.getJSONObject("properties");
    this.id = input.getString("id");
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
    return properties;
  }

  public Object getProperty(String key) {
    return properties.get(key);
  }

  public double getPropertyDouble(String key) {
    return properties.getDouble(key);
  }

  public JSONObject toJSON() throws Exception {
    JSONObject geoJson = new JSONObject();
    geoJson.put("type", geoJSONType);
    geoJson.put("geometry", geometry.toJSON());
    geoJson.put("properties", properties);
    geoJson.put("id", id);
    return geoJson;
  }
}
