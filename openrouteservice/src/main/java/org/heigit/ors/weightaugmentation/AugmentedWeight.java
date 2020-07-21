package org.heigit.ors.weightaugmentation;

import com.vividsolutions.jts.geom.Geometry;
import java.util.Objects;

public class AugmentedWeight {
  private final Geometry geometry;
  private final double weight;

  public AugmentedWeight(Geometry geometry, double weight) {
    this.geometry = geometry;
    this.weight = weight;
  }

  public Geometry getGeometry() {
    return geometry;
  }

  public double getWeight() {
    return weight;
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
