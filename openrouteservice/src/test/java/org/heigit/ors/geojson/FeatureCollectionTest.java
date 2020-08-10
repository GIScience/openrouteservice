package org.heigit.ors.geojson;

import java.util.Objects;
import org.heigit.ors.geojson.exception.GeoJSONException;
import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FeatureCollectionTest {
  private JSONArray featuresJson;
  private FeatureCollection featureCollection;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() {
    featuresJson = new JSONArray();
    featuresJson.put(HelperFunctions.buildFeatureJSON());
    featureCollection = (FeatureCollection) GeoJSON.parse(HelperFunctions.buildFeatureCollectionJSON(
        featuresJson));
  }

  @Test
  public void getFeatures() {
    Feature[] expectedFeatures = new Feature[featuresJson.length()];
    for (int i = 0; i < featuresJson.length(); i++) {
      expectedFeatures[i] = new Feature(featuresJson.getJSONObject(i));
    }
    Assert.assertArrayEquals(expectedFeatures, featureCollection.getFeatures());
  }

  @Test
  public void getFeaturesWithFilter() {
    Assert.assertEquals(1, featureCollection.getFeatures("Polygon").length);
    Assert.assertEquals(0, featureCollection.getFeatures("LineString").length);
    thrown.expect(GeoJSONException.class);
    thrown.expectMessage("'Foobar' is no valid geometry type");
    Assert.assertEquals(0, featureCollection.getFeatures("Foobar").length);
  }

  @Test
  public void toJSON() {
    Assert.assertEquals(HelperFunctions.buildFeatureCollectionJSON(featuresJson).toString(), featureCollection.toJSON().toString());
  }

  @Test
  public void testEquals() {
    FeatureCollection expectedFeatureCollection = new FeatureCollection(HelperFunctions.buildFeatureCollectionJSON(featuresJson));
    Assert.assertEquals(expectedFeatureCollection, featureCollection);
  }

  @Test
  public void testHashCode() {
    Assert.assertEquals(Objects.hash(featureCollection.getFeatures(), featureCollection.getGeoJSONType()), featureCollection.hashCode());
  }
}