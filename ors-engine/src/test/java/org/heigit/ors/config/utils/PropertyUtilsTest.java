package org.heigit.ors.config.utils;

import lombok.Getter;
import org.heigit.ors.config.profile.ExtendedStorageProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PropertyUtilsTest {



    @Test
    void testPropertyTools() throws IllegalAccessException {
        TestClass test = new TestClass();
        HashSet<String> ignoreList = new HashSet<>();
        ignoreList.add("testString");
        PropertyUtils.assertAllNull(test, ignoreList);
    }

    static class TestExtendedStorageProperties extends ExtendedStorageProperties {
        @Getter
        private final String field3;
        public String field1;
        @Getter
        public int field2;
        TestPropertyNestedClass subclass;

        TestExtendedStorageProperties(String field1, int field2, String field3, Boolean enabled) {
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

    private static Stream<Arguments> provideDeepCompareFieldsAreUnequalTestCases() {
        return Stream.of(
                // Test case for Collection
                Arguments.of(new ArrayList<>(List.of(1, 2, 3)), new ArrayList<>(List.of(1, 2, 3)), new HashSet<>(), "collectionPath", true),
                Arguments.of(new ArrayList<>(List.of(1, 2, 3)), new ArrayList<>(List.of(1, 2, 3, 4)), new HashSet<>(), "collectionPath", false),

                // Test case for primitive or wrapper
                Arguments.of(1, 1, new HashSet<>(), "primitivePath", false),
                Arguments.of(1, 2, new HashSet<>(), "primitivePath", true),

                // Test case for Path
                Arguments.of(Path.of("/path1"), Path.of("/path1"), new HashSet<>(), "pathPath", false),
                Arguments.of(Path.of("/path1"), Path.of("/path2"), new HashSet<>(), "pathPath", true),

                // Test case for deep equality check
                Arguments.of(new TestClass("value1"), new TestClass("value1"), new HashSet<>(), "objectPath", false),
                Arguments.of(new TestClass("value1"), new TestClass("value2"), new HashSet<>(), "objectPath", true)
        );
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

    private static Stream<Arguments> providePrimitiveOrWrapperTypes() {
        return Stream.of(
                Arguments.of(int.class, true),
                Arguments.of(Integer.class, true),
                Arguments.of(boolean.class, true),
                Arguments.of(Boolean.class, true),
                Arguments.of(char.class, true),
                Arguments.of(Character.class, true),
                Arguments.of(byte.class, true),
                Arguments.of(Byte.class, true),
                Arguments.of(short.class, true),
                Arguments.of(Short.class, true),
                Arguments.of(double.class, true),
                Arguments.of(Double.class, true),
                Arguments.of(long.class, true),
                Arguments.of(Long.class, true),
                Arguments.of(float.class, true),
                Arguments.of(Float.class, true),
                Arguments.of(MyEnum.class, true),
                Arguments.of(String.class, true),
                Arguments.of(Object.class, false),
                Arguments.of(null, false)
        );
    }

    @Test
    void deepCompareImmutableFields() {
        Map<String, String> key1 = Map.of("key1", "value1");
        Map<String, String> key2 = Map.of("key1", "value1");
        assertThrows(RuntimeException.class, () -> PropertyUtils.deepCompareFieldsAreUnequal(key1, key2, new HashSet<>(), "arrayPath"));
    }

    @Test
    void testGetAllFields() {
        class SuperClass {
            private String superField1;
            private String superField2;
        }

        class SubClass extends SuperClass {
            private int subField;
        }

        List<Field> fields = PropertyUtils.getAllFields(SubClass.class);
        Set<String> fieldNames = new HashSet<>();
        for (Field field : fields) {
            fieldNames.add(field.getName());
        }

        assertTrue(fieldNames.contains("superField1"));
        assertTrue(fieldNames.contains("superField2"));
        assertTrue(fieldNames.contains("subField"));
    }

    @Test
    void testDeepEqualityCheckIsUnequal() {
        class DifferentSuperClass {
            private String superField99;
            private String superField98;
        }
        class SuperClass {
            private String superField1;
            private String superField2;

            SuperClass(String superField1, String superField2) {
                this.superField1 = superField1;
                this.superField2 = superField2;
            }
        }

        class SubClass extends SuperClass {
            private int subField;

            SubClass(String superField1, String superField2, int subField) {
                super(superField1, superField2);
                this.subField = subField;
            }
        }

        SubClass obj1 = new SubClass("value1", "value2", 10);
        SubClass obj2 = new SubClass("value1", "value2", 10);
        SubClass obj3 = new SubClass("value1", "value2", 20);
        DifferentSuperClass obj4 = new DifferentSuperClass();
        SubClass obj5 = new SubClass(null, "foo", 20);
        SubClass obj6 = new SubClass("value1", null, 20);
        Set<String> excludeFields = new HashSet<>();

        assertTrue(PropertyUtils.deepEqualityCheckIsUnequal(obj1, obj1, excludeFields));
        assertTrue(PropertyUtils.deepEqualityCheckIsUnequal(obj1, obj2, excludeFields));

        // One is null
        assertFalse(PropertyUtils.deepEqualityCheckIsUnequal(obj1, null, excludeFields));
        assertFalse(PropertyUtils.deepEqualityCheckIsUnequal(null, obj2, excludeFields));

        // Not equal content
        assertFalse(PropertyUtils.deepEqualityCheckIsUnequal(obj1, obj3, excludeFields));

        // DifferentSuperClass is not a subclass of SuperClass
        assertFalse(PropertyUtils.deepEqualityCheckIsUnequal(obj4, obj2, excludeFields));
        assertFalse(PropertyUtils.deepEqualityCheckIsUnequal(obj2, obj4, excludeFields));

        // Values are null
        assertFalse(PropertyUtils.deepEqualityCheckIsUnequal(obj5, obj6, excludeFields));
        assertFalse(PropertyUtils.deepEqualityCheckIsUnequal(obj6, obj5, excludeFields));
    }

    @ParameterizedTest
    @MethodSource("provideDeepCompareFieldsAreUnequalTestCases")
    void deepCompareFieldsAreUnequalWithVariousInputs(Object value1, Object value2, Set<String> excludeFields, String path, boolean expected) {
        boolean result = PropertyUtils.deepCompareFieldsAreUnequal(value1, value2, excludeFields, path);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("providePrimitiveOrWrapperTypes")
    void testIsPrimitiveOrWrapper(Class<?> type, boolean expected) {
        boolean result = PropertyUtils.isPrimitiveOrWrapper(type);
        assertEquals(expected, result);
    }

    @Test
    void testShouldExclude() {
        Set<String> excludeFields = new HashSet<>();
        excludeFields.add("field1");
        excludeFields.add("field2.subField");

        // Test cases
        assertTrue(PropertyUtils.shouldExclude("field1", excludeFields));
        assertTrue(PropertyUtils.shouldExclude("field2.subField", excludeFields));
        assertFalse(PropertyUtils.shouldExclude("field3", excludeFields));
        assertFalse(PropertyUtils.shouldExclude("field2.otherField", excludeFields));
    }

    @Test
    void testDeepCompareArrays() {
        Set<String> excludeFields = new HashSet<>();

        // Test case 1: Equal arrays
        Object[] arr1 = {1, 2, 3};
        Object[] arr2 = {1, 2, 3};
        assertTrue(PropertyUtils.deepCompareArrays(arr1, arr2, excludeFields, "path"));

        // Test case 2: Different lengths
        Object[] arr3 = {1, 2, 3};
        Object[] arr4 = {1, 2, 3, 4};
        assertFalse(PropertyUtils.deepCompareArrays(arr3, arr4, excludeFields, "path"));

        // Test case 3: Different content
        Object[] arr5 = {1, 2, 3};
        Object[] arr6 = {1, 2, 4};
        assertFalse(PropertyUtils.deepCompareArrays(arr5, arr6, excludeFields, "path"));

        // Test case 4: Nested arrays
        Object[] arr7 = {new int[]{1, 2}, new int[]{3, 4}};
        Object[] arr8 = {new int[]{1, 2}, new int[]{3, 4}};
        assertTrue(PropertyUtils.deepCompareArrays(arr7, arr8, excludeFields, "path"));

        // Test case 5: Arrays with null values
        Object[] arr9 = {1, null, 3};
        Object[] arr10 = {1, null, 3};
        assertTrue(PropertyUtils.deepCompareArrays(arr9, arr10, excludeFields, "path"));

        // Test case 6: Arrays with different null values
        Object[] arr11 = {1, null, 3};
        Object[] arr12 = {1, 2, 3};
        assertFalse(PropertyUtils.deepCompareArrays(arr11, arr12, excludeFields, "path"));

        // Test case 7: Null arrays
        assertFalse(PropertyUtils.deepCompareArrays(null, arr12, excludeFields, "path"));
        assertFalse(PropertyUtils.deepCompareArrays(arr11, null, excludeFields, "path"));
    }

    enum MyEnum {
        VALUE1, VALUE2
    }

    static class TestClass {
        public Boolean testEnum;
        public Boolean testBoolean;
        public Integer testInteger;
        public String testString;

        public TestClass() {
            this.testEnum = null;
            this.testBoolean = null;
            this.testInteger = null;
            this.testString = "notNull";
        }

        TestClass(String testString) {
            this.testString = testString;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestClass testClass = (TestClass) o;
            return Objects.equals(testString, testClass.testString);
        }

        @Override
        public int hashCode() {
            return Objects.hash(testString);
        }
    }
}