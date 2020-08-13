package org.heigit.ors.weightaugmentation;

import java.util.HashMap;
import org.heigit.ors.exceptions.AugmentationStorageException;

/**
 * This class stores augmentations. It consists of a HashMap storing internal edge ids and their weight factor.
 */
public class AugmentationStorage {
  public static final int MAX_AUGMENTATIONS = 50000;
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
  public void applyAugmentation(int edge, double weight) throws AugmentationStorageException {
    augmentations.put(edge, this.get(edge) * weight);
    if (augmentations.size() > MAX_AUGMENTATIONS) {
      throw new AugmentationStorageException(String.format("Augmentations exceeded the maximum number of edges: %s > %s", augmentations.size(), MAX_AUGMENTATIONS));
    }
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
