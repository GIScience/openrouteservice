package org.heigit.ors.weightaugmentation;

import static org.heigit.ors.util.HelperFunctions.convertCoordinateArray;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.DAType;
import com.graphhopper.storage.GHDirectory;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import junit.framework.TestCase;
import org.heigit.ors.routing.graphhopper.extensions.ORSDefaultFlagEncoderFactory;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import org.junit.Before;

public class PolygonEdgeFilterTest extends TestCase {
  private final EncodingManager encoder = EncodingManager.create(new ORSDefaultFlagEncoderFactory(), FlagEncoderNames.CAR_ORS, 4);
  private final GraphHopperStorage graphHopperStorage = new GraphHopperStorage(new GHDirectory("", DAType.RAM_STORE), encoder, false, new GraphExtension.NoOpExtension());
  private final GeometryFactory geometryFactory = new GeometryFactory();


  @Before
  public void setUp() {
    graphHopperStorage.create(3);
  }


  public void testAccept() {
    Polygon polygon = geometryFactory.createPolygon(convertCoordinateArray(new double[][]{{-1,1}, {1,1}, {1,2}, {-1,1}}));

    EdgeIteratorState intersectingEdge = graphHopperStorage.edge(0, 1);
    intersectingEdge.setWayGeometry(Helper.createPointList(0, -1, 3, 1));
    EdgeIteratorState includingEdge = graphHopperStorage.edge(2, 3);
    includingEdge.setWayGeometry(Helper.createPointList(1.2, 0, 1.5, 0.5));
    EdgeIteratorState externalEdge = graphHopperStorage.edge(4, 5);
    externalEdge.setWayGeometry(Helper.createPointList(0, 0, -1, -1));

    PolygonEdgeFilter polygonEdgeFilter = new PolygonEdgeFilter(new Polygon[]{polygon});
    assertTrue(polygonEdgeFilter.accept(intersectingEdge));
    assertTrue(polygonEdgeFilter.accept(includingEdge));
    assertFalse(polygonEdgeFilter.accept(externalEdge));
  }
}