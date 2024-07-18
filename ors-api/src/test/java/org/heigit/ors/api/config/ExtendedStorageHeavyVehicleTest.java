package org.heigit.ors.api.config;

import org.heigit.ors.api.config.ExtendedStorageHeavyVehicle;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ExtendedStorageHeavyVehicleTest {

    @Test
    void testDefaultConstructor() {
        ExtendedStorageHeavyVehicle storage = new ExtendedStorageHeavyVehicle();
        assertNotNull(storage, "Default constructor should initialize the object");
        assertTrue(storage.getEnabled(), "Default constructor should initialize 'enabled' to true");
    }
}