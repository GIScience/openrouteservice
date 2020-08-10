package org.heigit.ors.geojson;

import java.util.Objects;
import org.heigit.ors.geojson.exception.GeoJSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class GeometryTest {
  private Geometry geometry;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() {
    geometry = (Geometry) GeoJSON.parse(HelperFunctions.buildGeometryJSON());
  }

  @Test
  public void getFeatures() {
    thrown.expect(GeoJSONException.class);
    thrown.expectMessage("Geometry does not contain any features.");
    geometry.getFeatures();
  }

  @Test
  public void getFeaturesWithFilter() {
    thrown.expect(GeoJSONException.class);
    thrown.expectMessage("Geometry does not contain any features.");
    geometry.getFeatures("Polygon");
  }

  @Test
  public void toJSON() {
    Assert.assertEquals(HelperFunctions.buildGeometryJSON().toString(), geometry.toJSON().toString());
  }

  @Test
  public void testEquals() {
    Geometry expectedGeometry = new Geometry(HelperFunctions.buildGeometryJSON());
    Assert.assertEquals(expectedGeometry, geometry);
  }

  @Test
  public void testHashCode() {
    Assert.assertEquals(Objects.hash(geometry.getGeometry(), geometry.getGeoJSONType()), geometry.hashCode());
  }
}