package org.heigit.ors.weightaugmentation;

import com.vividsolutions.jts.geom.Geometry;
import junit.framework.TestCase;
import org.heigit.ors.geojson.GeometryJSON;
import org.json.JSONObject;

public class AugmentedWeightTest extends TestCase {
  private AugmentedWeight augmentedWeight;
  private Geometry geometry;

  public void setUp() throws Exception {
    super.setUp();
    geometry = GeometryJSON.parse(new JSONObject("{\"type\": \"Polygon\", \"coordinates\": [[[8.691, 49.415], [8.691, 49.413], [8.699, 49.413], [8.691, 49.415]]]}"));
    augmentedWeight = new AugmentedWeight(geometry, 0.5);
  }

  public void testGetGeometry() {
    assertEquals(geometry, augmentedWeight.getGeometry());
  }

  public void testGetWeight() {
    assertEquals(0.5, augmentedWeight.getWeight());
  }
}