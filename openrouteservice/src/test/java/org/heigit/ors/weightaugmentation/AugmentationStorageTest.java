package org.heigit.ors.weightaugmentation;

import static org.junit.Assert.assertEquals;

import org.heigit.ors.exceptions.AugmentationStorageException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AugmentationStorageTest {
  private final AugmentationStorage augmentationStorage = new AugmentationStorage();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() {
  }

  @Test
  public void testAugmentationStorage() throws AugmentationStorageException {
    for (int i = 0; i < 20; i++) {
      assertEquals(1.0, augmentationStorage.get(i), 0.0);
    }
    augmentationStorage.applyAugmentation(5, 0.75);
    assertEquals(0.75, augmentationStorage.get(5), 0.0);
    augmentationStorage.applyAugmentation(5, 1.25);
    assertEquals(0.9375, augmentationStorage.get(5), 0.0);
  }

  @Test
  public void testMax() throws AugmentationStorageException {
    thrown.expect(AugmentationStorageException.class);
    thrown.expectMessage("Augmentations exceeded the maximum number of edges:");
    for (int i = 0; i < 100000; i++) {
      augmentationStorage.applyAugmentation(i, 1.55);
    }
  }
}