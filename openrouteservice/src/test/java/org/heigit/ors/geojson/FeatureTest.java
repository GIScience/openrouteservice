package org.heigit.ors.geojson;

import java.util.Objects;
import org.heigit.ors.geojson.exception.GeoJSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FeatureTest {
  private Feature feature;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() {
    feature = (Feature) GeoJSON.parse(HelperFunctions.buildFeatureJSON());
  }

  @Test
  public void getFeatures() {
    Feature[] expectedFeatures = new Feature[]{new Feature(HelperFunctions.buildFeatureJSON())};
    Assert.assertArrayEquals(expectedFeatures, feature.getFeatures());
  }

  @Test
  public void getFeaturesWithFilter() {
    Assert.assertEquals(1, feature.getFeatures("Polygon").length);
    Assert.assertEquals(0, feature.getFeatures("LineString").length);
    thrown.expect(GeoJSONException.class);
    thrown.expectMessage("'Foobar' is no valid geometry type");
    Assert.assertEquals(0, feature.getFeatures("Foobar").length);
  }

  @Test
  public void toJSON() {
    Assert.assertEquals(HelperFunctions.buildFeatureJSON().toString(), feature.toJSON().toString());
  }

  @Test
  public void testEquals() {
    Feature expectedFeature = new Feature(HelperFunctions.buildFeatureJSON());
    Assert.assertEquals(expectedFeature, feature);
  }

  @Test
  public void testHashCode() {
    Assert.assertEquals(Objects.hash(feature.getGeometryObject(), feature.getProperties(), feature.getId(), feature.getGeoJSONType()), feature.hashCode());
  }
}