package org.heigit.ors.api.config;

import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ExtendedStorageHereTrafficTest {

    void testDefaultConstructor() {
        ExtendedStorageHereTraffic storage = new ExtendedStorageHereTraffic();
        assertNotNull(storage, "Default constructor should initialize the object");
        assertTrue(storage.getEnabled(), "Default constructor should initialize 'enabled' to true");
    }
}