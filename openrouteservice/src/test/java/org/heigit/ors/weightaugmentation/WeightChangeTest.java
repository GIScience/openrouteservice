package org.heigit.ors.weightaugmentation;

import com.vividsolutions.jts.geom.Geometry;
import junit.framework.TestCase;
import org.heigit.ors.geojson.GeometryJSON;
import org.json.JSONObject;

public class WeightChangeTest extends TestCase {
  private WeightChange weightChange;
  private Geometry geometry;

  public void setUp() throws Exception {
    super.setUp();
    geometry = GeometryJSON.parse(new JSONObject("{\"type\": \"Polygon\", \"coordinates\": [[[8.691, 49.415], [8.691, 49.413], [8.699, 49.413], [8.691, 49.415]]]}"));
    weightChange = new WeightChange(geometry, 0.5);
  }

  public void testGetGeometry() {
    assertEquals(geometry, weightChange.getGeometry());
  }

  public void testGetWeight() {
    assertEquals(0.5, weightChange.getWeight());
  }
}