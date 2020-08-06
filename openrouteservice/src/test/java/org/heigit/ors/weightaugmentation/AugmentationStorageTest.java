package org.heigit.ors.weightaugmentation;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

public class AugmentationStorageTest extends TestCase {
  private final AugmentationStorage augmentationStorage = new AugmentationStorage();

  @Before
  public void setUp() {
  }

  @Test
  public void testAugmentationStorage() {
    for (int i = 0; i < 20; i++) {
      assertEquals(1.0, augmentationStorage.get(i));
    }
    augmentationStorage.applyAugmentation(5, 0.75);
    assertEquals(0.75, augmentationStorage.get(5));
    augmentationStorage.applyAugmentation(5, 1.25);
    assertEquals(0.9375, augmentationStorage.get(5));
  }
}