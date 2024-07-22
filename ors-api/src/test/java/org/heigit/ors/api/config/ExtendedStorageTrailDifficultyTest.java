package org.heigit.ors.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedStorageTrailDifficultyTest {

    @Test
    void testDefaultConstructor() {
        ExtendedStorageTrailDifficulty storage = new ExtendedStorageTrailDifficulty();
        assertNotNull(storage, "Default constructor should initialize the object");
        assertTrue(storage.getEnabled(), "Default constructor should initialize 'enabled' to true");
    }
}