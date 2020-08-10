package org.heigit.ors.geojson;

import java.util.Arrays;
import java.util.Objects;
import org.heigit.ors.geojson.exception.GeoJSONException;
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
    if (!Arrays.asList(Geometry.ALLOWED_GEOMETRY_TYPES).contains(type)) {
      throw new GeoJSONException(String.format("'%s' is no valid geometry type", type));
    }
    if (getGeometry().getGeometryType().equals(type)) {
      return new Feature[]{this};
    } else {
      return new Feature[]{};
    }
  }

  public JSONObject getProperties() {
    if (properties == null) throw new GeoJSONException("Properties missing in GeoJSON.");
    return properties;
  }

  public String getId() {
    return id;
  }

  public JSONObject toJSON() {
    JSONObject geoJson = new JSONObject();
    geoJson.put("type", geoJSONType);
    geoJson.put("geometry", geometry.toJSON());
    geoJson.put("properties", properties);
    geoJson.put("id", id);
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
    Feature feature = (Feature) o;
    return Objects.equals(geometry, feature.geometry) &&
        Objects.equals(properties.toMap(), feature.properties.toMap()) &&
        Objects.equals(id, feature.id) &&
        Objects.equals(geoJSONType, feature.geoJSONType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(geometry, properties, id, geoJSONType);
  }
}
