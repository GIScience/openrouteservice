package org.heigit.ors.mapmatching.polygon;

import static org.heigit.ors.util.HelperFunctions.convertCoordinateArray;

import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import junit.framework.TestCase;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import org.junit.Before;
import org.junit.Test;

public class PolygonMatcherTest extends TestCase {
  private PolygonMatcher polygonMatcher;
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

    polygonMatcher = new PolygonMatcher();
    polygonMatcher.setGraphHopper(graphHopper);
    polygonMatcher.setLocationIndex();
    polygonMatcher.setSearchRadius(100);
    polygonMatcher.setNodeGridStepSize(0.1);
  }

  @Test
  public void testMatch() {
    Coordinate[] coordinates = convertCoordinateArray(new double[][]{{0.9,1.9},{0.9,5.1},{3.1,5.1},{3.1,1.9},{0.9,1.9}});
    Polygon polygon = new GeometryFactory().createPolygon(coordinates);

    Set<Integer> actualEdges = polygonMatcher.match(polygon);
    Set<Integer> expectedEdges = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 6, 7));
    assertEquals(expectedEdges, actualEdges);
  }
}