package org.heigit.ors.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataAccessEnumTest {

    @Test
    void testEnum() {
        DataAccessEnum[] dataAccessEnums = DataAccessEnum.values();
        assertEquals(3, dataAccessEnums.length);
        assertEquals(DataAccessEnum.RAM_STORE, dataAccessEnums[0]);
        assertEquals(DataAccessEnum.MMAP, dataAccessEnums[1]);
        assertEquals(DataAccessEnum.MMAP_RO, dataAccessEnums[2]);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(DataAccessEnum.RAM_STORE, DataAccessEnum.valueOf("RAM_STORE"));
        assertEquals(DataAccessEnum.MMAP, DataAccessEnum.valueOf("MMAP"));
        assertEquals(DataAccessEnum.MMAP_RO, DataAccessEnum.valueOf("MMAP_RO"));
    }

    @Test
    void testGetStore() {
        assertEquals("RAM_STORE", DataAccessEnum.RAM_STORE.getStore());
        assertEquals("MMAP", DataAccessEnum.MMAP.getStore());
        assertEquals("MMAP_RO", DataAccessEnum.MMAP_RO.getStore());
    }

    @Test
    void testToString() {
        assertEquals("RAM_STORE", DataAccessEnum.RAM_STORE.toString());
        assertEquals("MMAP", DataAccessEnum.MMAP.toString());
        assertEquals("MMAP_RO", DataAccessEnum.MMAP_RO.toString());
    }
}