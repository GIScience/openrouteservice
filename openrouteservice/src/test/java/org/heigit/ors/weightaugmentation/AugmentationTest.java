package org.heigit.ors.weightaugmentation;

import static org.heigit.ors.util.HelperFunctions.convertCoordinateArray;
import static org.heigit.ors.util.HelperFunctions.printEdges;
import static org.heigit.ors.util.HelperFunctions.printNodes;

import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIterator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.util.DebugUtility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

// TODO extend tests with other polygon types
public class AugmentationTest {
  private final CarFlagEncoder carEncoder = new CarFlagEncoder();
  private final EncodingManager encodingManager = EncodingManager.create(carEncoder);
  private final UserWeightParser userWeightParser = new UserWeightParser();

  @Before
  public void setUp() {
  }

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


  @Test
  public void testPolygonChange() throws ParameterValueException {
    GraphHopperStorage actualGhs = createMediumGraph();

    Coordinate[] coordinates = convertCoordinateArray(new double[][]{{1,2},{1,5},{3,5},{3,2},{1,2}});
    double weight = 0.75;
    GeometryFactory geometryFactory = new GeometryFactory();
    Polygon polygon = geometryFactory.createPolygon(coordinates);
    List<AugmentedWeight> weightAugmentations = userWeightParser.parse(new Geometry[]{polygon}, new double[]{weight});

    // apply augmentations
    for (AugmentedWeight augmentedWeight: weightAugmentations) {
      augmentedWeight.applyAugmentationToAll(actualGhs);
    }

    if (DebugUtility.isDebug()) {
      // print nodes
      printNodes(actualGhs);
      System.out.println();

      // print edges
      printEdges(actualGhs);
    }

    // apply changes to reference/expected graph
    HashSet<Integer> edgesToChange = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 6, 7));
    GraphHopperStorage expectedGhs = createMediumGraph();
    EdgeIterator edges = expectedGhs.getAllEdges();
    while (edges.next()) {
      if (edgesToChange.contains(edges.getEdge())) {
        edges.setDistance(edges.getDistance() * weight);
      }
    }

    // check nodes
    Assert.assertEquals(expectedGhs.getNodes(), actualGhs.getNodes());
    NodeAccess actualNodes = actualGhs.getNodeAccess();
    NodeAccess expectedNodes = expectedGhs.getNodeAccess();
    for (int i = 0; i < actualGhs.getNodes(); i++) {
      Assert.assertEquals(expectedNodes.getLat(i), actualNodes.getLat(i), 0.0);
      Assert.assertEquals(expectedNodes.getLon(i), actualNodes.getLon(i), 0.0);
    }

    // check edges
    Assert.assertEquals(expectedGhs.getEdges(), actualGhs.getEdges());
    AllEdgesIterator actualEdges = actualGhs.getAllEdges();
    AllEdgesIterator expectedEdges = expectedGhs.getAllEdges();
    while (actualEdges.next()) {
      expectedEdges.next();
      Assert.assertEquals(expectedEdges.getBaseNode(), actualEdges.getBaseNode());
      Assert.assertEquals(expectedEdges.getAdjNode(), actualEdges.getAdjNode());
      Assert.assertEquals(expectedEdges.getDistance(), actualEdges.getDistance(), 0.0);
    }
  }
}
