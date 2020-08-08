package org.heigit.ors.mapmatching.point;

import static org.heigit.ors.util.HelperFunctions.convertCoordinateArray;

import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import junit.framework.TestCase;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import org.junit.Before;
import org.junit.Test;

public class PointMatcherTest extends TestCase {
  private final GeometryFactory geometryFactory = new GeometryFactory();
  private PointMatcher pointMatcher;
  private final CarFlagEncoder carEncoder = new CarFlagEncoder();
  private final EncodingManager encodingManager = EncodingManager.create(carEncoder);
  private ORSGraphHopper graphHopper;

  private GraphHopperStorage createGHStorage() {
    return new GraphBuilder(encodingManager).create();
  }

  public GraphHopperStorage createMediumGraph() {
    //    3---4--5
    //   /\   |  |
    //  2--0  6--7
    //  | / \   /
    //  |/   \ /
    //  1-----8
    GraphHopperStorage g = createGHStorage();
    g.edge(0, 1, 1, true);
    g.edge(0, 2, 1, true);
    g.edge(0, 3, 5, true);
    g.edge(0, 8, 1, true);
    g.edge(1, 2, 1, true);
    g.edge(1, 8, 2, true);
    g.edge(2, 3, 2, true);
    g.edge(3, 4, 2, true);
    g.edge(4, 5, 1, true);
    g.edge(4, 6, 1, true);
    g.edge(5, 7, 1, true);
    g.edge(6, 7, 2, true);
    g.edge(7, 8, 3, true);
    //Set test lat lon
    g.getBaseGraph().getNodeAccess().setNode(0, 3, 3);
    g.getBaseGraph().getNodeAccess().setNode(1, 1, 1);
    g.getBaseGraph().getNodeAccess().setNode(2, 3, 1);
    g.getBaseGraph().getNodeAccess().setNode(3, 4, 2);
    g.getBaseGraph().getNodeAccess().setNode(4, 4, 4);
    g.getBaseGraph().getNodeAccess().setNode(5, 4, 5);
    g.getBaseGraph().getNodeAccess().setNode(6, 3, 4);
    g.getBaseGraph().getNodeAccess().setNode(7, 3, 5);
    g.getBaseGraph().getNodeAccess().setNode(8, 1, 4);
    return g;
  }

  @Before
  public void setUp() {
    graphHopper = new ORSGraphHopper();
    graphHopper.setCHEnabled(false);
    graphHopper.setCoreEnabled(false);
    graphHopper.setCoreLMEnabled(false);
    graphHopper.setEncodingManager(encodingManager);
    graphHopper.setGraphHopperStorage(createMediumGraph());
    graphHopper.postProcessing();

    pointMatcher = new PointMatcher();
    pointMatcher.setGraphHopper(graphHopper);
    pointMatcher.setLocationIndex();
  }

  @Test
  public void testMatch() {
    testPointMatch(3.5, 2.0, 3);
  }

  @Test
  public void testExactMatch() {
    testPointMatch(4.0, 3.0, 9);
  }

  private void testPointMatch(double x, double y, int expectedEdge) {
    Point point = geometryFactory.createPoint(new Coordinate(x, y));
    Set<Integer> expectedSet = new HashSet<>();
    expectedSet.add(expectedEdge);
    assertEquals(expectedSet, pointMatcher.match(point));
  }
}