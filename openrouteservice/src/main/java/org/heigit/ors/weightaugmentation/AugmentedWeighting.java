package org.heigit.ors.weightaugmentation;

import static org.heigit.ors.weightaugmentation.UserWeightParser.ALLOWED_MULTI_GEOMETRY_TYPES;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.heigit.ors.exceptions.AugmentationStorageException;
import org.heigit.ors.mapmatching.RouteSegmentInfo;
import org.heigit.ors.mapmatching.hmm.HiddenMarkovMapMatcher;
import org.heigit.ors.mapmatching.point.PointMatcher;
import org.heigit.ors.mapmatching.polygon.PolygonMatcher;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSPMap;

/**
 * Weighting method using the {@link AugmentationStorage} that is created first and then used altering given weights of {@link #superWeighting}.
 */
public class AugmentedWeighting implements Weighting {
  private final Weighting superWeighting;
  private final AugmentationStorage augmentationStorage;
  private final PolygonMatcher polygonMatcher;
  private final PointMatcher pointMatcher;
  private final HiddenMarkovMapMatcher lineStringMatcher;
  private double minAugmentationWeight = 1.0;

  /**
   * Constructor of the class with custom stepSize and searchRadius.
   * @param additionalHints additionalHints (as {@link ORSPMap}) created in {@link org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper} including the augmentations by the user in the key {@code "user_weights"}
   * @param weighting already existing weighting that will be augmented
   * @param graphHopper {@link GraphHopper} instance
   * @param stepSize custom step size for generating the node search grid
   * @param searchRadius custom search radius for the node search
   */
  public AugmentedWeighting(PMap additionalHints, Weighting weighting, GraphHopper graphHopper, double stepSize, double searchRadius) throws AugmentationStorageException {
    ORSPMap params = (ORSPMap) additionalHints;
    this.superWeighting = weighting;
    this.augmentationStorage = new AugmentationStorage();
    this.polygonMatcher = new PolygonMatcher();
    this.pointMatcher = new PointMatcher();
    this.lineStringMatcher = new HiddenMarkovMapMatcher();
    polygonMatcher.setGraphHopper(graphHopper);
    pointMatcher.setGraphHopper(graphHopper);
    lineStringMatcher.setGraphHopper(graphHopper);
    polygonMatcher.setLocationIndex();
    pointMatcher.setLocationIndex();
    lineStringMatcher.setEdgeFilter(null);
    if (stepSize > 0) {
      polygonMatcher.setNodeGridStepSize(stepSize);
    }
    if (searchRadius > 0) {
      polygonMatcher.setSearchRadius(searchRadius);
      lineStringMatcher.setSearchRadius(searchRadius);
    }
    //noinspection unchecked
    getEdgesAndFillStorage((List<AugmentedWeight>) params.getObj("user_weights"));
  }

  /**
   * Overload of the {@link #AugmentedWeighting(PMap, Weighting, GraphHopper, double, double)} with a default {@code stepSize} and {@code searchRadius}.
   */
  public AugmentedWeighting(PMap additionalHints, Weighting weighting, GraphHopper graphHopper) throws AugmentationStorageException {
    this(additionalHints, weighting, graphHopper, -1, -1);
  }

  private void getEdgesAndFillStorage(List<AugmentedWeight> augmentedWeights)
      throws AugmentationStorageException {
    for (AugmentedWeight augmentedWeight: augmentedWeights) {
      Set<Integer> edges = getMatchedEdges(augmentedWeight.getGeometry());
      minAugmentationWeight = Math.min(minAugmentationWeight, augmentedWeight.getWeight());
      augmentationStorage.applyAllAugmentation(edges, augmentedWeight.getWeight());
    }
  }

  private Set<Integer> getMatchedEdges(Geometry geometry) {
    Set<Integer> edges = new HashSet<>();
    if (geometry.getGeometryType().equals("Polygon")) {
      edges.addAll(polygonMatcher.match((Polygon) geometry));
    } else if (geometry.getGeometryType().equals("Point")) {
      edges.addAll(pointMatcher.match((Point) geometry));
    } else if (geometry.getGeometryType().equals("LineString")) {
      LineString lineString = (LineString) geometry;
      RouteSegmentInfo[] routeSegments = lineStringMatcher
          .match(lineString.getCoordinates(), true);
      for (RouteSegmentInfo routeSegment: routeSegments) {
        if (routeSegment != null)
          edges.addAll(routeSegment.getEdges());
      }
    } else if (Arrays.asList(ALLOWED_MULTI_GEOMETRY_TYPES).contains(geometry.getGeometryType())) {
      for (int g = 0; g < geometry.getNumGeometries(); g++) {
        edges.addAll(getMatchedEdges(geometry.getGeometryN(g)));
      }
    } else {
      throw new UnsupportedOperationException("AugmentationStorage is not implemented for " + geometry.getGeometryType());
    }
    return edges;
  }

  /**
   * Get the augmentation of the {@link #augmentationStorage}. For details see {@link AugmentationStorage#get(int)}.
   * @param edge internal edge id
   * @return weight factor
   */
  public double getAugmentations(final EdgeIteratorState edge) {
    return augmentationStorage.get(edge.getEdge());
  }

  /**
   * Calculate weight by applying the augmentation to existing weights.
   * @param edge internal edge id
   * @param reverse see {@link Weighting#calcWeight(EdgeIteratorState, boolean, int)}
   * @param prevOrNextEdgeId see {@link Weighting#calcWeight(EdgeIteratorState, boolean, int)}
   * @return calculated weight
   */
  @Override
  public double calcWeight(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId) {
    return superWeighting.calcWeight(edge, reverse, prevOrNextEdgeId) * getAugmentations(edge);
  }

  /**
   * Calculate milliseconds by applying the augmentation to existing milliseconds.
   * @param edge internal edge id
   * @param reverse see {@link Weighting#calcMillis(EdgeIteratorState, boolean, int)}
   * @param prevOrNextEdgeId see {@link Weighting#calcMillis(EdgeIteratorState, boolean, int)}
   * @return calculated milliseconds
   */
  @Override
  public long calcMillis(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId) {
    return (long) (superWeighting.calcMillis(edge, reverse, prevOrNextEdgeId) * getAugmentations(edge));
  }

  /**
   * Get the minimum weight for a given distance used for A*. The minimum augmentation is applied to the given {@link Weighting#getMinWeight(double) minWeight}. Only minimum augmentations &leq; 1.0 are applied.
   * @param distance given distance
   * @return minimum weight
   */
  @Override
  public double getMinWeight(double distance) {
    return superWeighting.getMinWeight(distance) * minAugmentationWeight;
  }

  /**
   * See {@link Weighting#getFlagEncoder()}.
   */
  @Override
  public FlagEncoder getFlagEncoder() {
    return superWeighting.getFlagEncoder();
  }

  /**
   * See {@link Weighting#matches(HintsMap)}.
   */
  @Override
  public boolean matches(HintsMap weightingMap) {
    return superWeighting.matches(weightingMap);
  }

  /**
   * Returns all weightings as string with {@code "augmented|"} prepended.
   */
  @Override
  public String toString() {
    return "augmented|" + superWeighting.toString();
  }

  /**
   * Returns all weightings as string with {@code "augmented|"} prepended.
   */
  @Override
  public String getName() {
    return "augmented|" + superWeighting.getName();
  }
}
