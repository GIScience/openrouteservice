package org.heigit.ors.config.profile;

import lombok.Getter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertyBaseTest {

    @Test
    void testUpdateObject() {
        TestProperty source = new TestProperty("testValue", 42, "foo");
        TestProperty target = new TestProperty("bar", 0, "bar");
        target.updateObject(source);

        assertEquals("testValue", target.field1);
        assertEquals(42, target.getField2());
        assertEquals("foo", target.getField3());
    }

    @Test
    void testUpdateObjectWithNullSource() {
        TestProperty target = new TestProperty("testValue", 42, "foo");
        assertThrows(IllegalArgumentException.class, () -> target.updateObject(null));
    }

    @Test
    void testUpdateObjectWithDifferentClass() {
        TestProperty target = new TestProperty("testValue", 42, "foo");
        assertThrows(IllegalArgumentException.class, () -> target.updateObject(new WrongTestProperty() {
        }));
    }

    class TestProperty extends PropertyBase {
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

    class WrongTestProperty {
    }
}
