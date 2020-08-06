package org.heigit.ors.weightaugmentation;

import java.util.HashMap;

public class AugmentationStorage {
  private final HashMap<Integer, Double> augmentations;

  AugmentationStorage() {
    this.augmentations = new HashMap<>();
  }

  public void applyAugmentation(int edge, double weight) {
    augmentations.put(edge, this.get(edge) * weight);
  }

  public double get(int edge) {
    return augmentations.getOrDefault(edge, 1.0);
  }
}
