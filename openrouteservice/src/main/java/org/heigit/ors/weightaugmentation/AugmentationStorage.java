package org.heigit.ors.weightaugmentation;

import java.util.HashMap;

/**
 * This class stores augmentations. It consists of a HashMap storing internal edge ids and their weight factor.
 */
public class AugmentationStorage {
  private final HashMap<Integer, Double> augmentations;

  /**
   * Initializes new storage.
   */
  AugmentationStorage() {
    this.augmentations = new HashMap<>();
  }

  /**
   * Applies given augmentation to the store by multiplying the new weight factor onto the existing.
   * @param edge internal edge id
   * @param weight weight factor
   */
  public void applyAugmentation(int edge, double weight) {
    augmentations.put(edge, this.get(edge) * weight);
  }

  /**
   * Returns the stored augmentation for a given internal edge id.
   * @param edge internal edge id
   * @return stored weight factor or 1.0 as default
   */
  public double get(int edge) {
    return augmentations.getOrDefault(edge, 1.0);
  }
}
