package org.heigit.ors.api.requests.common;

import com.vividsolutions.jts.geom.Geometry;

public class WeightChange {
  private final Geometry geometry;

  private final Double weight;

  public WeightChange(Geometry geometry, Double weight) {
    this.geometry = geometry;
    this.weight = weight;
  }

  public final Geometry getGeometry() {
    return geometry;
  }

  public final Double getWeight() {
    return weight;
  }
}
