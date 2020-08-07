package org.heigit.ors.mapmatching.polygon;

import com.carrotsearch.hppc.ObjectHashSet;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PolygonMatcher {
  private final GeometryFactory gf = new GeometryFactory();
  private GraphHopper graphHopper;
  private FlagEncoder encoder;
  private LocationIndexTree locationIndex;
  private double searchRadius = 50; // in meters
  private double nodeGridStepSize = 0.001;

  public void setGraphHopper(GraphHopper graphHopper) {
    this.graphHopper = graphHopper;
    encoder = graphHopper.getEncodingManager().fetchEdgeEncoders().get(0);
  }

  public void setLocationIndex() {
    locationIndex = (LocationIndexTree) graphHopper.getLocationIndex();
  }

  public void setSearchRadius(double searchRadius) {
    this.searchRadius = searchRadius;
  }

  public void setNodeGridStepSize(double stepSize) {
    this.nodeGridStepSize = stepSize;
  }

  public Set<Integer> match(Polygon polygon) {
    Set<Integer> edges = new HashSet<>();
    EdgeExplorer edgeExplorer = graphHopper.getGraphHopperStorage().createEdgeExplorer();

    Set<Integer> nodesInPolygonBBox = getNodesInPolygonBBox(polygon);
    for (Integer node: nodesInPolygonBBox) {
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
    double x = graphHopper.getGraphHopperStorage().getNodeAccess().getLon(node);
    double y = graphHopper.getGraphHopperStorage().getNodeAccess().getLat(node);
    return nodeInPolygon(new Coordinate(x,y), polygon);
  }

  private boolean nodeInPolygon(Coordinate coordinate, Polygon polygon) {
    Point point = gf.createPoint(coordinate);
    return point.within(polygon) || point.intersects(polygon);
  }

  private Set<Integer> getNodesInPolygonBBox(Polygon polygon) {
    Set<Integer> nodes = new HashSet<>();
    Geometry boundary = polygon.getBoundary();
    ObjectHashSet<Coordinate> coordinates = generatePointsInPolygon(getBoundingBox(boundary), polygon);
    for (ObjectCursor<Coordinate> coord: coordinates) {
      List<QueryResult> qResults = locationIndex.findNClosest(coord.value.y, coord.value.x, EdgeFilter.ALL_EDGES, searchRadius);
      if (qResults.isEmpty()) continue;
      coordinates.removeAll(c -> distance(coord.value, c) < searchRadius);
      for (QueryResult qResult: qResults) {
        nodes.add(qResult.getClosestNode());
      }
    }
    return nodes;
  }

  private ObjectHashSet<Coordinate> generatePointsInPolygon(double[] bbox, Polygon polygon) {
    // bbox = [minX, minY, maxX, maxY]
    int dimX = (int) Math.ceil((bbox[2]-bbox[0]) / nodeGridStepSize) + 1;
    int dimY = (int) Math.ceil((bbox[3]-bbox[1]) / nodeGridStepSize) + 1;
    ObjectHashSet<Coordinate> coordinates = new ObjectHashSet<>(dimX * dimY);
    for (double x = bbox[0]; x <= bbox[2]; x += nodeGridStepSize) {
      for (double y = bbox[1]; y <= bbox[3]; y += nodeGridStepSize) {
        Coordinate coord = new Coordinate(PointList.round6(x), PointList.round6(y));
        if (nodeInPolygon(coord, polygon)) coordinates.add(coord);
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

  /*
   Calculates the distance between two coordinates in meters
    */
  public static double distance(Coordinate coord1, Coordinate coord2) {
    double lat1 = coord1.y;
    double lon1 = coord1.x;
    double lat2 = coord2.y;
    double lon2 = coord2.x;

    final int R = 6371; // Radius of the earth

    double latDistance = Math.toRadians(lat2 - lat1);
    double lonDistance = Math.toRadians(lon2 - lon1);
    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double distance = R * c * 1000; // convert to meters

    distance = Math.pow(distance, 2);

    return Math.sqrt(distance);
  }
}
