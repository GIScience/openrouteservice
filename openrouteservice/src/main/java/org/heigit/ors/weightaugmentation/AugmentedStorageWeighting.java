package org.heigit.ors.weightaugmentation;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import com.vividsolutions.jts.geom.Polygon;
import java.util.List;
import java.util.Set;
import org.heigit.ors.mapmatching.polygon.PolygonMatcher;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSPMap;

/**
 * Weighting method using the {@link AugmentationStorage} that is created first and then used altering given weights of {@link #superWeighting}.
 */
public class AugmentedStorageWeighting implements Weighting {
  private final Weighting superWeighting;
  private final AugmentationStorage augmentationStorage;
  private final PolygonMatcher polygonMatcher;
  private double minAugmentationWeight = 1.0;

  /**
   * Constructor of the class with custom stepSize and searchRadius.
   * @param additionalHints additionalHints (as {@link ORSPMap}) created in {@link org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper} including the augmentations by the user in the key {@code "user_weights"}
   * @param weighting already existing weighting that will be augmented
   * @param graphHopper {@link GraphHopper} instance
   * @param stepSize custom step size for generating the node search grid
   * @param searchRadius custom search radius for the node search
   */
  public AugmentedStorageWeighting(PMap additionalHints, Weighting weighting, GraphHopper graphHopper, double stepSize, double searchRadius) {
    ORSPMap params = (ORSPMap) additionalHints;
    this.superWeighting = weighting;
    this.augmentationStorage = new AugmentationStorage();
    this.polygonMatcher = new PolygonMatcher();
    polygonMatcher.setGraphHopper(graphHopper);
    polygonMatcher.setLocationIndex();
    if (stepSize > 0) {
      polygonMatcher.setNodeGridStepSize(stepSize);
    }
    if (searchRadius > 0) {
      polygonMatcher.setSearchRadius(searchRadius);
    }
    //noinspection unchecked
    fillAugmentationStorage((List<AugmentedWeight>) params.getObj("user_weights"));
  }

  /**
   * Overload of the {@link #AugmentedStorageWeighting(PMap, Weighting, GraphHopper, double, double)} with a default {@code stepSize} and {@code searchRadius}.
   */
  public AugmentedStorageWeighting(PMap additionalHints, Weighting weighting, GraphHopper graphHopper) {
    this(additionalHints, weighting, graphHopper, -1, -1);
  }

  private void fillAugmentationStorage(List<AugmentedWeight> augmentedWeights) {
    for (AugmentedWeight augmentedWeight: augmentedWeights) {
      Polygon polygon = (Polygon) augmentedWeight.getGeometry();
      Set<Integer> edges = polygonMatcher.match(polygon);
      minAugmentationWeight = Math.min(minAugmentationWeight, augmentedWeight.getWeight());
      for (int edge: edges) {
        augmentationStorage.applyAugmentation(edge, augmentedWeight.getWeight());
      }
    }
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
