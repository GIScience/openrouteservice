package org.heigit.ors.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedStorageOsmIdTest {

    @Test
    void testDefaultConstructor() {
        ExtendedStorageOsmId storage = new ExtendedStorageOsmId();
        assertNotNull(storage, "Default constructor should initialize the object");
        assertTrue(storage.getEnabled(), "Default constructor should initialize 'enabled' to true");
    }
}