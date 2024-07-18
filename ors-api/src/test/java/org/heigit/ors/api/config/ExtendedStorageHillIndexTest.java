package org.heigit.ors.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ExtendedStorageHillIndexTest {

    @Test
    void testDefaultConstructor() {
        ExtendedStorageHillIndex storage = new ExtendedStorageHillIndex();
        assertNotNull(storage, "Default constructor should initialize the object");
        assertTrue(storage.getEnabled(), "Default constructor should initialize 'enabled' to true");
    }
}