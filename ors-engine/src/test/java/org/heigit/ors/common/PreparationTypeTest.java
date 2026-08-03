package org.heigit.ors.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PreparationTypeTest {

    @Test
    void testEnum() {
        PreparationType[] preparationTypes = PreparationType.values();
        assertEquals(2, preparationTypes.length);
        assertEquals(PreparationType.FOLDER, preparationTypes[0]);
        assertEquals(PreparationType.ARCHIVE, preparationTypes[1]);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(PreparationType.FOLDER, PreparationType.valueOf("FOLDER"));
        assertEquals(PreparationType.ARCHIVE, PreparationType.valueOf("ARCHIVE"));
    }

    @Test
    void testGetType() {
        assertEquals("FOLDER", PreparationType.FOLDER.getType());
        assertEquals("ARCHIVE", PreparationType.ARCHIVE.getType());
    }

    @Test
    void testToString() {
        assertEquals("FOLDER", PreparationType.FOLDER.toString());
        assertEquals("ARCHIVE", PreparationType.ARCHIVE.toString());
    }
}
