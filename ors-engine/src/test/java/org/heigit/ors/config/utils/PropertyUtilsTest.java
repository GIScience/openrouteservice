package org.heigit.ors.config.utils;

import lombok.Getter;
import org.heigit.ors.config.profile.storages.ExtendedStorage;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PropertyUtilsTest {

    @Test
    void testUpdateObject() {
        TestProperty source = new TestProperty("testValue", 42, "foo", true);
        TestProperty target = new TestProperty("bar", 0, "bar", false);
        PropertyUtils.deepCopyObjectsProperties(source, target, true, false);

        assertEquals("testValue", target.field1);
        assertEquals(42, target.getField2());
        assertEquals("foo", target.getField3());
        assertEquals(true, target.subclass.enabled);
    }

    @Test
    void testUpdateObjectNoOverwrite() {
        TestProperty source = new TestProperty("testValue", 42, "foo", true);
        TestProperty target = new TestProperty("bar", 0, null, false);
        PropertyUtils.deepCopyObjectsProperties(source, target, false, false);

        assertEquals("bar", target.field1);
        assertEquals(0, target.getField2());
        // Correctly set as the target field is null.
        assertEquals("foo", target.getField3());
        assertEquals(false, target.subclass.enabled);
    }

    @Test
    void testUpdateObjectNoOverwriteInSubclass() {
        TestProperty source = new TestProperty("testValue", 42, "foo", true);
        TestProperty target = new TestProperty("bar", 0, "baz", null, false);
        PropertyUtils.deepCopyObjectsProperties(source, target, false, false);

        assertEquals("bar", target.field1);
        assertEquals(0, target.getField2());
        assertEquals("baz", target.getField3());
        // Correctly set as the target field is null.
        assertNull(target.subclass);
    }

    @Test
    void testUpdateObjectNoCopyEmptyMemberClasses() {
        TestProperty source = new TestProperty("testValue", 42, "foo", true);
        TestProperty target = new TestProperty("bar", 0, "baz", null, false);
        PropertyUtils.deepCopyObjectsProperties(source, target, true, false);

        assertEquals("testValue", target.field1);
        assertEquals(42, target.getField2());
        assertEquals("foo", target.getField3());
        // Correctly set as the target field is null.
        assertNull(target.subclass);
    }

    @Test
    void testUpdateWithSuperclass() {
        // This is not working! The subclass is not updated by the superclass.
        TestProperty source = new TestProperty("testValue", 42, "baz", true);
        TestPropertySubclass target = new TestPropertySubclass("foo", 0, "bar", false);
        PropertyUtils.deepCopyObjectsProperties(source, target, true, false);

        assertEquals("foo", target.field1);
        assertEquals(0, target.getField2());
        assertEquals("bar", target.getField3());
        assertEquals(false, target.subclass.enabled);
    }

    @Test
    void testUpdateWithSubclass() {
        TestPropertySubclass source = new TestPropertySubclass("foo", 0, "bar", true);
        TestProperty target = new TestProperty("testValue", 42, "baz", false);
        PropertyUtils.deepCopyObjectsProperties(source, target, true, false);

        assertEquals("foo", target.field1);
        assertEquals(0, target.getField2());
        assertEquals("bar", target.getField3());
        assertEquals(true, target.subclass.enabled);
    }

    @Test
    void testUpdateObjectWithNullSource() {
        TestProperty target = new TestProperty("testValue", 42, "foo", true);
        assertThrows(IllegalArgumentException.class, () -> PropertyUtils.deepCopyObjectsProperties(null, null, true, false));
        assertThrows(IllegalArgumentException.class, () -> PropertyUtils.deepCopyObjectsProperties(target, null, true, false));
        assertThrows(IllegalArgumentException.class, () -> PropertyUtils.deepCopyObjectsProperties(null, target, true, false));
    }


    @Test
    void testUpdateObjectsWithEmptyMemberClasses() {
        TestProperty source = new TestProperty("testValue", 42, "foo", true);
        TestProperty target = new TestProperty("bar", 0, "baz", null, false);
        assertNull(target.subclass);
        PropertyUtils.deepCopyObjectsProperties(source, target, true, true);

        assertEquals("testValue", target.field1);
        assertEquals(42, target.getField2());
        assertEquals("foo", target.getField3());
        assertInstanceOf(TestProperty.TestPropertyNestedClass.class, target.subclass);
        assertEquals(true, target.subclass.enabled);
    }

    @Test
    void testUpdateMapObjectsWithOverwrite() {
        // Create a map with two TestExtendedStorage objects
        Map<String, ExtendedStorage> source = Map.of("test1", new TestExtendedStorage("testValue1", 42, "foo1", true), "test2", new TestExtendedStorage("testValue2", 43, "foo2", false));
        Map<String, ExtendedStorage> target = Map.of("test1", new TestExtendedStorage("bar", 0, null, false), "test2", new TestExtendedStorage("bar", 0, "bar", false));
        Map<String, ExtendedStorage> returnedTarget = PropertyUtils.deepCopyMapsProperties(source, target, true, false, false);

        // Check if the values are updated
        assertEquals(((TestExtendedStorage) source.get("test1")).field1, ((TestExtendedStorage) returnedTarget.get("test1")).field1);
        assertEquals(((TestExtendedStorage) source.get("test1")).getField2(), ((TestExtendedStorage) returnedTarget.get("test1")).getField2());
        assertEquals(((TestExtendedStorage) source.get("test1")).getField3(), ((TestExtendedStorage) returnedTarget.get("test1")).getField3());
        assertEquals(source.get("test1").getEnabled(), returnedTarget.get("test1").getEnabled());
        assertEquals(((TestExtendedStorage) source.get("test1")).subclass.enabled, ((TestExtendedStorage) returnedTarget.get("test1")).subclass.enabled);

        assertEquals(((TestExtendedStorage) source.get("test2")).field1, ((TestExtendedStorage) returnedTarget.get("test2")).field1);
        assertEquals(((TestExtendedStorage) source.get("test2")).getField2(), ((TestExtendedStorage) returnedTarget.get("test2")).getField2());
        assertEquals(((TestExtendedStorage) source.get("test2")).getField3(), ((TestExtendedStorage) returnedTarget.get("test2")).getField3());
        assertEquals(source.get("test2").getEnabled(), returnedTarget.get("test2").getEnabled());
        assertEquals(((TestExtendedStorage) source.get("test2")).subclass.enabled, ((TestExtendedStorage) returnedTarget.get("test2")).subclass.enabled);
    }

    @Test
    void testUpdateMapObjectsWithNoOverwrite() {
        // Create a map with two TestExtendedStorage objects
        Map<String, ExtendedStorage> source = Map.of("test1", new TestExtendedStorage("testValue1", 42, "foo1", true), "test2", new TestExtendedStorage("testValue2", 43, "foo2", true));
        Map<String, ExtendedStorage> target = Map.of("test1", new TestExtendedStorage("bar", 0, null, false), "test2", new TestExtendedStorage(null, 0, "bar", false));
        Map<String, ExtendedStorage> returnedTarget = PropertyUtils.deepCopyMapsProperties(source, target, false, false, false);

        // Check if the values are updated
        assertEquals("bar", ((TestExtendedStorage) returnedTarget.get("test1")).field1);
        assertEquals(0, ((TestExtendedStorage) returnedTarget.get("test1")).getField2());
        assertEquals("foo1", ((TestExtendedStorage) returnedTarget.get("test1")).getField3());
        assertEquals(false, ((TestExtendedStorage) returnedTarget.get("test1")).subclass.enabled);

        assertEquals("testValue2", ((TestExtendedStorage) returnedTarget.get("test2")).field1);
        assertEquals(0, ((TestExtendedStorage) returnedTarget.get("test2")).getField2());
        assertEquals("bar", ((TestExtendedStorage) returnedTarget.get("test2")).getField3());
        assertEquals(false, ((TestExtendedStorage) returnedTarget.get("test2")).subclass.enabled);
    }

    @Test
    void testUpdateMapObjectsWithUnequalTarget() {
        // Create a map with two TestExtendedStorage objects
        Map<String, ExtendedStorage> source = Map.of("test1", new TestExtendedStorage("testValue1", 42, "foo1", true), "test2", new TestExtendedStorage("testValue2", 43, "foo2", true));
        Map<String, ExtendedStorage> target = Map.of("test1", new TestExtendedStorage("bar", 0, null, false));
        assertEquals(1, target.size());
        Map<String, ExtendedStorage> targetUpdate = PropertyUtils.deepCopyMapsProperties(source, target, true, false, true);

        assertEquals(2, targetUpdate.size());

        // Check if the values are updated
        assertEquals("testValue1", ((TestExtendedStorage) targetUpdate.get("test1")).field1);
        assertEquals(42, ((TestExtendedStorage) targetUpdate.get("test1")).getField2());
        assertEquals("foo1", ((TestExtendedStorage) targetUpdate.get("test1")).getField3());
        assertEquals(true, ((TestExtendedStorage) targetUpdate.get("test1")).subclass.enabled);

        assertEquals("testValue2", ((TestExtendedStorage) targetUpdate.get("test2")).field1);
        assertEquals(43, ((TestExtendedStorage) targetUpdate.get("test2")).getField2());
        assertEquals("foo2", ((TestExtendedStorage) targetUpdate.get("test2")).getField3());
        assertEquals(true, ((TestExtendedStorage) targetUpdate.get("test2")).subclass.enabled);
    }

    @Test
    void testUpdateMapObjectsWithUnequalSource() {
        // Create a map with two TestExtendedStorage objects
        Map<String, ExtendedStorage> source = Map.of("test1", new TestExtendedStorage("testValue1", 42, "foo1", true));
        Map<String, ExtendedStorage> target = Map.of("test1", new TestExtendedStorage("bar", 0, null, false), "test2", new TestExtendedStorage("foo", 1, "bar", false));
        assertEquals(2, target.size());
        Map<String, ExtendedStorage> targetUpdate = PropertyUtils.deepCopyMapsProperties(source, target, true, false, false);

        assertEquals(2, targetUpdate.size());

        // Check if the values are updated
        assertEquals("testValue1", ((TestExtendedStorage) targetUpdate.get("test1")).field1);
        assertEquals(42, ((TestExtendedStorage) targetUpdate.get("test1")).getField2());
        assertEquals("foo1", ((TestExtendedStorage) targetUpdate.get("test1")).getField3());
        assertEquals(true, ((TestExtendedStorage) targetUpdate.get("test1")).subclass.enabled);

        assertEquals("foo", ((TestExtendedStorage) targetUpdate.get("test2")).field1);
        assertEquals(1, ((TestExtendedStorage) targetUpdate.get("test2")).getField2());
        assertEquals("bar", ((TestExtendedStorage) targetUpdate.get("test2")).getField3());
        assertEquals(false, ((TestExtendedStorage) targetUpdate.get("test2")).subclass.enabled);
    }

    @Test
    void testUpdateMapObjectsWithNoCopyEmptyStorage() {
        // Create a map with two TestExtendedStorage objects
        Map<String, ExtendedStorage> source = Map.of("test1", new TestExtendedStorage("testValue1", 42, "foo1", true), "test2", new TestExtendedStorage("testValue2", 43, "foo2", true));
        Map<String, ExtendedStorage> target = Map.of("test1", new TestExtendedStorage("bar", 0, null, false));
        assertEquals(1, target.size());
        Map<String, ExtendedStorage> targetUpdate = PropertyUtils.deepCopyMapsProperties(source, target, true, false, false);

        assertEquals(1, targetUpdate.size());

        // Check if the values are updated
        assertEquals("testValue1", ((TestExtendedStorage) targetUpdate.get("test1")).field1);
        assertEquals(42, ((TestExtendedStorage) targetUpdate.get("test1")).getField2());
        assertEquals("foo1", ((TestExtendedStorage) targetUpdate.get("test1")).getField3());
        assertEquals(true, ((TestExtendedStorage) targetUpdate.get("test1")).subclass.enabled);
    }

    @Test
    void testUpdateMapObjectsWithUnequalSourceNonOverwriteNonEmptyFieldsAndCopyEmptyStorages() {
        // Create a map with two TestExtendedStorage objects
        Map<String, ExtendedStorage> source = Map.of("test1", new TestExtendedStorage("bar", 0, null, false), "test2",
                new TestExtendedStorage("foo", 1, "bar", false));
        Map<String, ExtendedStorage> target = Map.of("test1", new TestExtendedStorage("testValue1", 42, "foo1", true));
        assertEquals(1, target.size());
        Map<String, ExtendedStorage> targetUpdate = PropertyUtils.deepCopyMapsProperties(source, target, false, false, true);

        assertEquals(2, targetUpdate.size());

        // Check if the values are updated
        assertEquals("testValue1", ((TestExtendedStorage) targetUpdate.get("test1")).field1);
        assertEquals(42, ((TestExtendedStorage) targetUpdate.get("test1")).getField2());
        assertEquals("foo1", ((TestExtendedStorage) targetUpdate.get("test1")).getField3());
        assertEquals(true, ((TestExtendedStorage) targetUpdate.get("test1")).subclass.enabled);

        assertEquals("foo", ((TestExtendedStorage) targetUpdate.get("test2")).field1);
        assertEquals(1, ((TestExtendedStorage) targetUpdate.get("test2")).getField2());
        assertEquals("bar", ((TestExtendedStorage) targetUpdate.get("test2")).getField3());
        assertEquals(false, ((TestExtendedStorage) targetUpdate.get("test2")).subclass.enabled);
    }

    @Test
    void testUpdateMapObjectsWithNullSource() {
        Map<String, ExtendedStorage> target = Map.of("test1", new TestExtendedStorage("testValue1", 42, "foo1", true), "test2", new TestExtendedStorage("testValue2", 43, "foo2", true));
        Map<String, ExtendedStorage> returnedTarget = PropertyUtils.deepCopyMapsProperties(null, target, true, false, false);

        assertEquals(2, returnedTarget.size());
        assertEquals(target.get("test1"), returnedTarget.get("test1"));
        assertEquals(target.get("test2"), returnedTarget.get("test2"));
    }

    @Test
    void testUpdateMapObjectsWithNullTarget() {
        Map<String, ExtendedStorage> source = Map.of("test1", new TestExtendedStorage("testValue1", 42, "foo1", true), "test2", new TestExtendedStorage("testValue2", 43, "foo2", true));
        Map<String, ExtendedStorage> returnedTarget = PropertyUtils.deepCopyMapsProperties(source, null, true, false, false);

        assertEquals(source.get("test1"), returnedTarget.get("test1"));
    }

    static class TestExtendedStorage extends ExtendedStorage {
        @Getter
        private final String field3;
        public String field1;
        @Getter
        public int field2;
        TestPropertyNestedClass subclass;

        TestExtendedStorage(String field1, int field2, String field3, Boolean enabled) {
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

    static class TestProperty {
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

        TestProperty(String field1, int field2, String field3, Boolean enabled, Boolean enableSubclass) {
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
            if (enableSubclass) this.subclass = new TestPropertyNestedClass(enabled);
        }

        public static class TestPropertyNestedClass {
            Boolean enabled;

            TestPropertyNestedClass(Boolean enabled) {
                ;
                this.enabled = enabled;
            }
        }
    }

    static class TestPropertySubclass extends TestProperty {


        TestPropertySubclass(String field1, int field2, String field3, Boolean enabled) {
            super(field1, field2, field3, enabled);
            this.subclass = new TestPropertyNestedClass(enabled);
        }

    }

}