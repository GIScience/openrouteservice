package org.heigit.ors.weightaugmentation;

import com.vividsolutions.jts.geom.Geometry;
import org.heigit.ors.geojson.GeometryJSON;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AugmentedWeightTest {
  private AugmentedWeight augmentedWeight;
  private Geometry geometry;

  @Before
  public void setUp() throws Exception {
    geometry = GeometryJSON.parse(new JSONObject("{\"type\": \"Polygon\", \"coordinates\": [[[8.691, 49.415], [8.691, 49.413], [8.699, 49.413], [8.691, 49.415]]]}"));
    augmentedWeight = new AugmentedWeight(geometry, 0.5);
  }

  @Test
  public void testGetGeometry() {
    Assert.assertEquals(geometry, augmentedWeight.getGeometry());
  }

  @Test
  public void testGetWeight() {
    Assert.assertEquals(0.5, augmentedWeight.getWeight(), 0.0);
  }
}