package org.heigit.ors.config.utils;

import lombok.Getter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertyUtilsTest {

    @Test
    void testUpdateObject() {
        TestProperty source = new TestProperty("testValue", 42, "foo");
        TestProperty target = new TestProperty("bar", 0, "bar");
        PropertyUtils.copyObjectProperties(source, target, true);

        assertEquals("testValue", target.field1);
        assertEquals(42, target.getField2());
        assertEquals("foo", target.getField3());
    }

    @Test
    void testUpdateWithSuperclass() {
        TestProperty source = new TestProperty("testValue", 42, "baz");
        TestPropertySubclass target = new TestPropertySubclass("foo", 0, "bar");
        PropertyUtils.copyObjectProperties(source, target, true);

        assertEquals("foo", target.field1);
        assertEquals(0, target.getField2());
        assertEquals("bar", target.getField3());
    }

    @Test
    void testUpdateWithSubclass() {
        TestPropertySubclass source = new TestPropertySubclass("foo", 0, "bar");
        TestProperty target = new TestProperty("testValue", 42, "baz");
        PropertyUtils.copyObjectProperties(source, target, true);

        assertEquals("foo", target.field1);
        assertEquals(0, target.getField2());
        assertEquals("bar", target.getField3());
    }

    @Test
    void testUpdateObjectWithNullSource() {
        TestProperty target = new TestProperty("testValue", 42, "foo");
        assertThrows(IllegalArgumentException.class, () -> PropertyUtils.copyObjectProperties(null, null, true));
        assertThrows(IllegalArgumentException.class, () -> PropertyUtils.copyObjectProperties(target, null, true));
        assertThrows(IllegalArgumentException.class, () -> PropertyUtils.copyObjectProperties(null, target, true));
    }

    class TestProperty {
        @Getter
        private final String field3;
        public String field1;
        @Getter
        public int field2;

        TestProperty(String field1, int field2, String field3) {
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
        }
    }

    class TestPropertySubclass extends TestProperty {
        TestPropertySubclass(String field1, int field2, String field3) {
            super(field1, field2, field3);
        }
    }

}