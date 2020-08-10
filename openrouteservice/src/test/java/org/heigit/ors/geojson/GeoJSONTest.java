package org.heigit.ors.geojson;

import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.util.Assert;

public class GeoJSONTest {
  private JSONArray featuresJson;
  private FeatureCollection featureCollection;
  private Feature feature;
  private Geometry geometry;

  @Before
  public void setUp() {
    // Feature Collection
    featuresJson = new JSONArray();
    featuresJson.put(HelperFunctions.buildFeatureJSON());
    featureCollection = (FeatureCollection) GeoJSON.parse(HelperFunctions.buildFeatureCollectionJSON(
        featuresJson));
    // Feature
    feature = (Feature) GeoJSON.parse(HelperFunctions.buildFeatureJSON());
    // Geometry
    geometry = (Geometry) GeoJSON.parse(HelperFunctions.buildGeometryJSON());
  }

  @Test
  public void parse() {
    Assert.equals(new FeatureCollection(HelperFunctions.buildFeatureCollectionJSON(featuresJson)), featureCollection);
    Assert.equals(new Feature(HelperFunctions.buildFeatureJSON()), feature);
    Assert.equals(new Geometry(HelperFunctions.buildGeometryJSON()), geometry);
  }

  @Test
  public void parseString() {
    Assert.equals(feature, GeoJSON.parse(HelperFunctions.buildFeatureJSON().toString()));
  }

  @Test
  public void parseSimpleJSON() throws ParseException {
    String jsonString = HelperFunctions.buildFeatureJSON().toString();
    JSONParser parser = new JSONParser();
    Assert.equals(feature, GeoJSON.parse((JSONObject) parser.parse(jsonString)));
  }

  @Test
  public void getGeoJSONType() {
    Assert.equals("FeatureCollection", featureCollection.getGeoJSONType());
    Assert.equals("Feature", feature.getGeoJSONType());
    Assert.equals("Geometry", geometry.getGeoJSONType());
  }

  @Test
  public void toJSONString() {
    Assert.equals(featureCollection.toJSON().toString(), featureCollection.toJSONString());
    Assert.equals(feature.toJSON().toString(), feature.toJSONString());
    Assert.equals(geometry.toJSON().toString(), geometry.toJSONString());
  }
}