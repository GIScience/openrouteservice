package org.heigit.ors.geojson;

import java.util.Arrays;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONObject;

public class FeatureCollection extends GeoJSON {
  private final Feature[] features;

  public FeatureCollection(JSONObject input) {
    this.geoJSONType = "FeatureCollection";
    JSONArray jsonFeatures = input.getJSONArray("features");
    features = new Feature[jsonFeatures.length()];
    for (int i = 0; i < jsonFeatures.length(); i++) {
      features[i] = new Feature(jsonFeatures.getJSONObject(i));
    }
  }

  public Feature[] getFeatures() {
    return features;
  }

  public Feature[] getFeatures(String type) {
    return Arrays.stream(features)
        .filter(f -> f.getGeometry().getGeometryType().equals(type))
        .toArray(Feature[]::new);
  }

  public JSONObject toJSON() {
    JSONObject geoJson = new JSONObject();
    geoJson.put("type", geoJSONType);
    JSONArray featuresJson = new JSONArray(features.length);
    for (Feature feature: features) { featuresJson.put(feature.toJSON()); }
    geoJson.put("features", featuresJson);
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
    FeatureCollection that = (FeatureCollection) o;
    return Arrays.equals(features, that.features) &&
        Objects.equals(geoJSONType, that.geoJSONType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(features, geoJSONType);
  }
}
