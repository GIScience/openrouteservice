package org.heigit.ors.config.profile;

import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class EncoderOptionsPropertiesTest {

    @Test
    void testConstructor() throws IllegalAccessException {
        EncoderOptionsProperties properties = new EncoderOptionsProperties();
        // They must be null to be able to distinguish between set and not set
        Field[] declaredFields = properties.getClass().getDeclaredFields();
        assertEquals(9, declaredFields.length);
        for (Field field : declaredFields) {
            field.setAccessible(true);
            assertNull(field.get(properties));
        }
    }

    @Test
    void testUpdateObject() {
        TestProperty source = new TestProperty("testValue", 42, "foo");
        TestProperty target = new TestProperty("bar", 0, "bar");
        target.updateObject(source, true);

        assertEquals("testValue", target.field1);
        assertEquals(42, target.getField2());
        assertEquals("foo", target.getField3());
    }

    @Test
    void testUpdateObjectWithNullSource() {
        TestProperty target = new TestProperty("testValue", 42, "foo");
        assertThrows(IllegalArgumentException.class, () -> target.updateObject(null, true));
    }

    class TestProperty extends EncoderOptionsProperties {
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

    class TestProperty2 extends TestProperty {
        TestProperty2() {
            super("testValue", 42, "foo");
        }

        TestProperty2(String field1, int field2, String field3) {
            super(field1, field2, field3);
        }
    }
}