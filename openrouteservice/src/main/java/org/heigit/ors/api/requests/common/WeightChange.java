package org.heigit.ors.api.requests.common;

import com.vividsolutions.jts.geom.Geometry;

public class WeightChange {
  private final Geometry geometry;
  private final double weight;

  public WeightChange(Geometry geometry, double weight) {
    this.geometry = geometry;
    this.weight = weight;
  }

  public Geometry getGeometry() {
    return geometry;
  }

  public double getWeight() {
    return weight;
  }
}
