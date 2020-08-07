package org.heigit.ors.weightaugmentation;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import java.util.List;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSPMap;

/**
 * Weighting method altering given weights of {@link #superWeighting}.
 * @deprecated Use {@link AugmentedStorageWeighting} instead.
 */
public class AugmentedWeighting implements Weighting {
  private final Weighting superWeighting;
  private final List<AugmentedWeight> augmentedWeights;

  /**
   * Constructor of the class.
   * @param additionalHints additionalHints (as {@link ORSPMap}) created in {@link org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper} including the augmentations by the user in the key {@code "user_weights"}
   * @param weighting already existing weighting that will be augmented
   */
  public AugmentedWeighting(PMap additionalHints, Weighting weighting) {
    ORSPMap params = (ORSPMap) additionalHints;
    this.superWeighting = weighting;
    //noinspection unchecked
    this.augmentedWeights = (List<AugmentedWeight>) params.getObj("user_weights");
  }

  /**
   * Get the augmentation by checking all {@link AugmentedWeight} objects and applying their factor if necessary.
   * @param edge internal edge id
   * @return weight factor
   */
  public double getAugmentations(final EdgeIteratorState edge) {
    double factor = 1.0;
    for (AugmentedWeight augmentedWeight: augmentedWeights) {
      factor *= augmentedWeight.getAugmentation(edge);
    }
    return factor;
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
    double minAugmentationWeight = 1.0;
    for (AugmentedWeight augmentedWeight: augmentedWeights) {
      minAugmentationWeight = Math.min(minAugmentationWeight, augmentedWeight.getWeight());
    }
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
