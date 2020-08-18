package org.heigit.ors.weightaugmentation;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import java.util.Objects;

/**
 * Weighting method using the {@link AugmentationStorage} that is created first and then used altering given weights of {@link #superWeighting}.
 */
public class AugmentedWeighting implements Weighting {
  private final Weighting superWeighting;
  private final AugmentationStorage augmentationStorage;

  /**
   * Constructor of the class.
   * @param weighting already existing weighting that will be augmented
   * @param augmentationStorage storage with the augmentations {@link AugmentationStorage}
   */
  public AugmentedWeighting(Weighting weighting, AugmentationStorage augmentationStorage) {
    this.superWeighting = weighting;
    this.augmentationStorage = augmentationStorage;
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
    return superWeighting.getMinWeight(distance) * augmentationStorage.getMinAugmentationWeight();
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
    AugmentedWeighting that = (AugmentedWeighting) o;
    return Objects.equals(superWeighting, that.superWeighting) &&
        augmentationStorage.equals(that.augmentationStorage);
  }

  /**
   * Returns hash value for object.
   */
  @Override
  public int hashCode() {
    return Objects.hash(superWeighting, augmentationStorage);
  }
}
