package org.heigit.ors.weightaugmentation;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import java.util.HashSet;
import java.util.Objects;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.routing.RoutingErrorCodes;

public class AugmentedWeight {
  private final Geometry geometry;
  private final double weight;
  private final EdgeFilter edgeFilter;

  public AugmentedWeight(Geometry geometry, double weight) throws ParameterValueException {
    this.geometry = geometry;
    if (weight > 0.0) {
      this.weight = weight;
    } else {
      throw new ParameterValueException(RoutingErrorCodes.INVALID_JSON_FORMAT, RouteRequest.PARAM_USER_WEIGHTS);
    }
    this.edgeFilter = createEdgeFilter();
  }

  public Geometry getGeometry() {
    return geometry;
  }

  public double getWeight() {
    return weight;
  }

  private EdgeFilter createEdgeFilter() {
    if (geometry instanceof Polygon) {
      return new PolygonEdgeFilter(new Polygon[]{(Polygon) geometry});
    } else {
      return null;
    }
  }

  public double getAugmentation(EdgeIteratorState edge) {
    return edgeFilter.accept(edge) ? weight : 1.0;
  }

  public boolean hasReducingWeight() {
    return weight < 1.0;
  }


  public void applyAugmentationToAll(GraphHopperStorage ghs) {
    EdgeExplorer edgeExplorer = ghs.createEdgeExplorer();
    EdgeIterator edges;

    HashSet<Integer> visitedEdges = new HashSet<>();
    // currently innefficient. If used in production: TODO optimize
    for (int i = 0; i < ghs.getNodes(); i++) {
      edges = edgeExplorer.setBaseNode(i);
      while (edges.next()) {
        if (visitedEdges.contains(edges.getEdge())) {
          continue;
        }
        // should be replaced
        edges.setDistance(edges.getDistance() * getAugmentation(edges));
        visitedEdges.add(edges.getEdge());
      }
    }
  }

  // doesn't work properly at the moment. If further used: TODO debug
  private static boolean nodeInPolygon(GraphHopperStorage ghs, int node, Polygon polygon) {
    GeometryFactory geometryFactory = new GeometryFactory();
    double x = ghs.getNodeAccess().getLon(node);
    double y = ghs.getNodeAccess().getLat(node);
    Coordinate[] c = new Coordinate[]{new Coordinate(x,y)};
    Point p = new Point(new CoordinateArraySequence(c), geometryFactory);
    return polygon.contains(p);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AugmentedWeight that = (AugmentedWeight) o;
    return Double.compare(that.weight, weight) == 0 &&
        Objects.equals(geometry, that.geometry);
  }

  @Override
  public int hashCode() {
    return Objects.hash(geometry, weight);
  }
}
