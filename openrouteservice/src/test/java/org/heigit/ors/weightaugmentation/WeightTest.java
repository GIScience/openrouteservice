package org.heigit.ors.weightaugmentation;

import static org.heigit.ors.util.HelperFunctions.convertCoordinateArray;

import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class WeightTest { // TODO naming
  private final CarFlagEncoder carEncoder = new CarFlagEncoder();
  private final EncodingManager encodingManager = EncodingManager.create(carEncoder);

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

  // TODO implement properly, not in tests
  // TODO naming
  @Test
  public void testSomething() {
    GraphHopperStorage graphHopperStorage = createMediumGraph();

    Coordinate[] coordinates = convertCoordinateArray(new double[][]{{1,2},{1,5},{3,5},{3,2},{1,2}});
    double weight = 0.75;

    GeometryFactory geometryFactory = new GeometryFactory();
    Polygon polygon = geometryFactory.createPolygon(coordinates);
    EdgeFilter edgeFilter = new PolygonEdgeFilter(new Polygon[]{polygon});

    EdgeExplorer edgeExplorer = graphHopperStorage.createEdgeExplorer(edgeFilter);
    EdgeIterator edges;
    List<Integer> changedIds = new ArrayList<>();
    for (int i = 0; i < graphHopperStorage.getNodes(); i++) {
      edges = edgeExplorer.setBaseNode(i);
      while (edges.next()) {
        if (changedIds.contains(edges.getEdge())) {
          continue;
        }
        double origDistance = edges.getDistance();
        edges.setDistance(origDistance * weight);
        changedIds.add(edges.getEdge());
        System.out.print("Changed edge (");
        System.out.print(edges);
        System.out.print(") from ");
        System.out.print(origDistance);
        System.out.print(" to ");
        System.out.print(edges.getDistance());
        System.out.println(".");
      }
    }

    System.out.println("NODES");
    NodeAccess nodes = graphHopperStorage.getNodeAccess();
    for (int i = 0; i < graphHopperStorage.getNodes(); i++) {
      System.out.print(i);
      System.out.print(" ");
      System.out.print(nodes.getLat(i));
      System.out.print(",");
      System.out.println(nodes.getLon(i));
    }

    System.out.println();
    System.out.println("EDGES");
    AllEdgesIterator allEdges = graphHopperStorage.getAllEdges();
    while (allEdges.next()) {
      System.out.print(allEdges);
      System.out.print(": ");
      System.out.print(allEdges.getDistance());
      if (changedIds.contains(allEdges.getEdge())) {
        System.out.print(" <- changed");
      }
      System.out.println();
    }
  }
}
