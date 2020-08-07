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

/**
 * Contains all information for an augmentation. Internally, a {@link Geometry}, weight factor and a suiting {@link EdgeFilter} is stored.
 */
public class AugmentedWeight {
  private final Geometry geometry;
  private final double weight;
  private final EdgeFilter edgeFilter;
  public static final double MIN_WEIGHT = 0.0;
  public static final double MAX_WEIGHT = 2.0;

  /**
   * Create augmented weight and create a fitting {@link EdgeFilter}. Checks if the weight is proper and creates an {@link EdgeFilter}.
   * @param geometry given {@link Geometry}
   * @param weight given weight factor
   * @throws ParameterValueException thrown for a weight factor out of range
   */
  public AugmentedWeight(Geometry geometry, double weight) throws ParameterValueException {
    this.geometry = geometry;
    if ((MIN_WEIGHT < weight) && (weight <= MAX_WEIGHT)) {
      this.weight = weight;
    } else {
      throw new ParameterValueException(RoutingErrorCodes.INVALID_JSON_FORMAT, RouteRequest.PARAM_USER_WEIGHTS);
    }
    this.edgeFilter = createEdgeFilter();
  }

  /**
   * Returns geometry.
   */
  public Geometry getGeometry() {
    return geometry;
  }

  /**
   * Returns stored weight factor.
   */
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

  /**
   * Check if given edge applies to the {@link EdgeFilter}. Returns weight factor if yes.
   * @param edge internal edge id
   * @return weight factor or 1.0
   */
  public double getAugmentation(EdgeIteratorState edge) {
    return edgeFilter.accept(edge) ? weight : 1.0;
  }

  /**
   * Check if the weight factor is reducing the weight.
   *
   * This is important for routing algorithms like A*.
   */
  public boolean hasReducingWeight() {
    return weight < 1.0;
  }


  /**
   * Applies augmentations to all edges in a {@link GraphHopperStorage}.
   *
   * @deprecated Use more efficient methods to apply augmentations. See {@link AugmentationStorage} for an example.
   * @param ghs {@link GraphHopperStorage}
   */
  public void applyAugmentationToAll(GraphHopperStorage ghs) {
    EdgeExplorer edgeExplorer = ghs.createEdgeExplorer();
    EdgeIterator edges;

    HashSet<Integer> visitedEdges = new HashSet<>();
    // currently inefficient
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

  /**
   * Check if objects are equal.
   */
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

  /**
   * Returns hash value for object.
   */
  @Override
  public int hashCode() {
    return Objects.hash(geometry, weight);
  }
}
