package org.heigit.ors.geojson;

import java.util.Arrays;
import java.util.Objects;
import org.heigit.ors.geojson.exception.GeoJSONException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * GeoJSON FeatureCollection representation.
 */
public class FeatureCollection extends GeoJSON {
  private final Feature[] features;

  /**
   * Create FeatureCollection from JSON.
   * @param input {@link JSONObject} representing a FeatureCollection.
   */
  public FeatureCollection(JSONObject input) {
    this.geoJSONType = "FeatureCollection";
    JSONArray jsonFeatures = input.getJSONArray("features");
    features = new Feature[jsonFeatures.length()];
    for (int i = 0; i < jsonFeatures.length(); i++) {
      features[i] = new Feature(jsonFeatures.getJSONObject(i));
    }
  }

  /**
   * Returns all {@link Feature Features} of the FeatureCollection.
   * @return Features as array.
   */
  public Feature[] getFeatures() {
    return features;
  }

  /**
   * Returns all {@link Feature Features} of the FeatureCollection matching a certain geometry type.
   * @param type Geometry type to filter the features.
   * @return Features as array.
   */
  public Feature[] getFeatures(String type) {
    if (!Arrays.asList(Geometry.ALLOWED_GEOMETRY_TYPES).contains(type)) {
      throw new GeoJSONException(String.format("'%s' is no valid geometry type", type));
    }
    return Arrays.stream(features)
        .filter(f -> f.getGeometry().getGeometryType().equals(type))
        .toArray(Feature[]::new);
  }

  @Override
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
