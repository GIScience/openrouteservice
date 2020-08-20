package org.heigit.ors.weightaugmentation;

import com.graphhopper.routing.util.EdgeFilter;
import com.vividsolutions.jts.geom.Geometry;
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
  /** weight should not be 0.0 or lower */
  public static final double MIN_WEIGHT = 0.0;
  /** weights higher than 10.0 are treated as positive infinity weight factor */
  public static final double MAX_WEIGHT = 10.0;

  /**
   * Create augmented weight and create a fitting {@link EdgeFilter}. Checks if the weight is proper and creates an {@link EdgeFilter}.
   * @param geometry given {@link Geometry}
   * @param weight given weight factor. Allowed values: {@link #MIN_WEIGHT} ({@value #MIN_WEIGHT}) &lt; {@code weight} &leq; {@link #MAX_WEIGHT} ({@value #MAX_WEIGHT}). All values above this range are treated as {@link Double#POSITIVE_INFINITY}.
   * @throws ParameterValueException thrown for a weight factor out of range
   */
  public AugmentedWeight(Geometry geometry, double weight) throws ParameterValueException {
    this.geometry = geometry;
    if (MIN_WEIGHT < weight) {
      if (weight < MAX_WEIGHT) {
        this.weight = weight;
      } else {
        this.weight = Double.POSITIVE_INFINITY;
      }
    } else {
      throw new ParameterValueException(RoutingErrorCodes.INVALID_JSON_FORMAT, RouteRequest.PARAM_USER_WEIGHTS, String.valueOf(weight), "Weight has to be between " + MIN_WEIGHT + " and " + MAX_WEIGHT + "!");
    }
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

  /**
   * Check if the weight factor is reducing the weight.
   *
   * This is important for routing algorithms like A*.
   */
  public boolean hasReducingWeight() {
    return weight < 1.0;
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
