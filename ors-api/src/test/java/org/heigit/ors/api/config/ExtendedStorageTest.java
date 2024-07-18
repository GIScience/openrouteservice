package org.heigit.ors.api.config;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedStorageTest {

    @Test
    void testDefaultConstructor() {
        ExtendedStorage storage = new ExtendedStorage();
        assertTrue(storage.getEnabled(), "Default constructor should initialize 'enabled' to true");
    }

    @Test
    void testStringConstructor() {
        ExtendedStorage storage = new ExtendedStorage("");
        assertTrue(storage.getEnabled(), "String constructor should not alter 'enabled' state");
    }

    @Test
    void testLinkedHashMapConstructor() {
        ExtendedStorage storage = new ExtendedStorage(new LinkedHashMap<>());
        assertTrue(storage.getEnabled(), "LinkedHashMap constructor should not alter 'enabled' state");
    }

    @Test
    void testGetterAndSetter() {
        ExtendedStorage storage = new ExtendedStorage();
        storage.setEnabled(false);
        assertFalse(storage.getEnabled(), "setEnabled(false) should result in 'enabled' being false");
        storage.setEnabled(true);
        assertTrue(storage.getEnabled(), "setEnabled(true) should result in 'enabled' being true");
    }
}