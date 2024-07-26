package org.heigit.ors.config.utils;

import lombok.Getter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertyUtilsTest {

    @Test
    void testUpdateObject() {
        TestProperty source = new TestProperty("testValue", 42, "foo", true);
        TestProperty target = new TestProperty("bar", 0, "bar", false);
        PropertyUtils.copyObjectPropertiesDeep(source, target, true);

        assertEquals("testValue", target.field1);
        assertEquals(42, target.getField2());
        assertEquals("foo", target.getField3());
        assertEquals(true, target.subclass.enabled);
    }

    @Test
    void testUpdateObjectNoOverwrite() {
        TestProperty source = new TestProperty("testValue", 42, "foo", true);
        TestProperty target = new TestProperty("bar", 0, null, false);
        PropertyUtils.copyObjectPropertiesDeep(source, target, false);

        assertEquals("bar", target.field1);
        assertEquals(0, target.getField2());
        // Correctly set as the target field is null.
        assertEquals("foo", target.getField3());
        assertEquals(false, target.subclass.enabled);
    }

    @Test
    void testUpdateObjectNoOverwriteInSubclass() {
        TestProperty source = new TestProperty("testValue", 42, "foo", true);
        TestProperty target = new TestProperty("bar", 0, "baz", null);
        PropertyUtils.copyObjectPropertiesDeep(source, target, false);

        assertEquals("bar", target.field1);
        assertEquals(0, target.getField2());
        assertEquals("baz", target.getField3());
        // Correctly set as the target field is null.
        assertEquals(true, target.subclass.enabled);
    }

    @Test
    void testUpdateWithSuperclass() {
        // This is not working! The subclass is not updated by the superclass.
        TestProperty source = new TestProperty("testValue", 42, "baz", true);
        TestPropertySubclass target = new TestPropertySubclass("foo", 0, "bar", false);
        PropertyUtils.copyObjectPropertiesDeep(source, target, true);

        assertEquals("foo", target.field1);
        assertEquals(0, target.getField2());
        assertEquals("bar", target.getField3());
        assertEquals(false, target.subclass.enabled);
    }

    @Test
    void testUpdateWithSubclass() {
        TestPropertySubclass source = new TestPropertySubclass("foo", 0, "bar", true);
        TestProperty target = new TestProperty("testValue", 42, "baz", false);
        PropertyUtils.copyObjectPropertiesDeep(source, target, true);

        assertEquals("foo", target.field1);
        assertEquals(0, target.getField2());
        assertEquals("bar", target.getField3());
        assertEquals(true, target.subclass.enabled);
    }

    @Test
    void testUpdateObjectWithNullSource() {
        TestProperty target = new TestProperty("testValue", 42, "foo", true);
        assertThrows(IllegalArgumentException.class, () -> PropertyUtils.copyObjectPropertiesDeep(null, null, true));
        assertThrows(IllegalArgumentException.class, () -> PropertyUtils.copyObjectPropertiesDeep(target, null, true));
        assertThrows(IllegalArgumentException.class, () -> PropertyUtils.copyObjectPropertiesDeep(null, target, true));
    }

    class TestProperty {
        @Getter
        private final String field3;
        public String field1;
        @Getter
        public int field2;
        TestPropertyNestedClass subclass;

        TestProperty(String field1, int field2, String field3, Boolean enabled) {
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
            this.subclass = new TestPropertyNestedClass(enabled);
        }

        public static class TestPropertyNestedClass {
            Boolean enabled;

            TestPropertyNestedClass(Boolean enabled) {
                ;
                this.enabled = enabled;
            }
        }
    }

    class TestPropertySubclass extends TestProperty {


        TestPropertySubclass(String field1, int field2, String field3, Boolean enabled) {
            super(field1, field2, field3, enabled);
            this.subclass = new TestPropertyNestedClass(enabled);
        }

    }

}