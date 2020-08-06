package org.heigit.ors.mapmatching.polygon;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.heigit.ors.mapmatching.LocationIndexMatch;

public class PolygonMatcher {
  private final GeometryFactory gf = new GeometryFactory();
  private GraphHopper graphHopper;
  private FlagEncoder encoder;
  private LocationIndexMatch locationIndex;
  private double searchRadius = 50; // in meters
  private double nodeGridStepSize = 0.001;

  public void setGraphHopper(GraphHopper graphHopper) {
    this.graphHopper = graphHopper;
  }

  public void setLocationIndex() {
    encoder = graphHopper.getEncodingManager().fetchEdgeEncoders().get(0);
    locationIndex = new LocationIndexMatch(graphHopper.getGraphHopperStorage(),
        (com.graphhopper.storage.index.LocationIndexTree) graphHopper.getLocationIndex(), (int) searchRadius);
  }

  public void setSearchRadius(double radius) {
    searchRadius = radius;
    if (locationIndex != null)
      locationIndex.setGpxAccuracy(radius);
  }

  public void setNodeGridStepSize(double stepSize) {
    this.nodeGridStepSize = stepSize;
  }

  public Set<Integer> match(Polygon polygon) {
    Set<Integer> edges = new HashSet<>();
    EdgeExplorer edgeExplorer = graphHopper.getGraphHopperStorage().createEdgeExplorer();

    Set<Integer> nodesInPolygon = getNodesInPolygon(polygon);
    for (Integer node: nodesInPolygon) {
      if (nodeInPolygon(node, polygon)) {
        // here an additional check can be added if the edge should really be added to the set
        EdgeIterator edgeIterator = edgeExplorer.setBaseNode(node);
        while (edgeIterator.next()) {
          edges.add(edgeIterator.getEdge());
        }
      }
    }

    return edges;
  }

  private boolean nodeInPolygon(int node, Polygon polygon) {
    GeometryFactory geometryFactory = new GeometryFactory();
    double x = graphHopper.getGraphHopperStorage().getNodeAccess().getLon(node);
    double y = graphHopper.getGraphHopperStorage().getNodeAccess().getLat(node);
    Coordinate[] coordinates = new Coordinate[]{new Coordinate(x,y)};
    Point point = new Point(new CoordinateArraySequence(coordinates), geometryFactory);
    return point.within(polygon) || point.intersects(polygon);
  }

  private Set<Integer> getNodesInPolygon(Polygon polygon) {
    Set<Integer> nodes = new HashSet<>();
    Geometry boundary = polygon.getBoundary();
    List<Coordinate> coordinates = generatePoints(getBoundingBox(boundary));
    for (Coordinate coord: coordinates) {
      List<QueryResult> qResults = locationIndex.findNClosest(coord.y, coord.x, EdgeFilter.ALL_EDGES);
      if (!qResults.isEmpty()) {
        nodes.add(qResults.get(0).getClosestNode());
      }
    }
    return nodes;
  }

  private List<Coordinate> generatePoints(double[] bbox) {
    List<Coordinate> coordinates = new ArrayList<>();
    for (double x = bbox[0]; x <= bbox[2]; x += nodeGridStepSize) {
      for (double y = bbox[1]; y <= bbox[3]; y += nodeGridStepSize) {
        coordinates.add(new Coordinate(PointList.round6(x), PointList.round6(y)));
      }
    }
    return coordinates;
  }

  private static double[] getBoundingBox(Geometry geometry) {
    Coordinate min = new Coordinate(90.0, 180.0);
    Coordinate max = new Coordinate(-90.0, -180.0);
    for (Coordinate coord: geometry.getCoordinates()) {
      if (coord.x < min.x) {
        min.x = coord.x;
      }
      if (coord.y < min.y) {
        min.y = coord.y;
      }
      if (coord.x > max.x) {
        max.x = coord.x;
      }
      if (coord.y > max.y) {
        max.y = coord.y;
      }
    }
    return new double[]{min.x, min.y, max.x, max.y};
  }
}
