package org.heigit.ors.api.config;

import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ExtendedStorageRoadAccessRestrictionsTest {


    void testDefaultConstructor() {
        ExtendedStorageRoadAccessRestrictions storage = new ExtendedStorageRoadAccessRestrictions();
        assertNotNull(storage, "Default constructor should initialize the object");
        assertTrue(storage.getEnabled(), "Default constructor should initialize 'enabled' to true");
    }
}