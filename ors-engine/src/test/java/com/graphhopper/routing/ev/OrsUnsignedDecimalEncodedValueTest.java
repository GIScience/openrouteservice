package com.graphhopper.routing.ev;

import com.graphhopper.storage.IntsRef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrsUnsignedDecimalEncodedValueTest {
    private static final double EPSILON = 1e-9;

    private final IntsRef flags = new IntsRef(1);

    @Test
    void unwrittenValueReadsAsDefault() {
        DecimalEncodedValue enc = initialized(Rest.create());

        assertEquals(-1, enc.getDecimal(false, flags), EPSILON);
        assertEquals(0, flags.ints[0]);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, 0.01, 0.5, 1, 1.26})
    void storesValuesShiftedByOne(double value) {
        DecimalEncodedValue enc = initialized(Rest.create());

        enc.setDecimal(false, flags, value);

        assertEquals(value, enc.getDecimal(false, flags), EPSILON);
        assertEquals(Math.round(value / 0.01) + 1, flags.ints[0]);
    }

    @Test
    void roundsToTheFactor() {
        DecimalEncodedValue enc = initialized(Rest.create());

        enc.setDecimal(false, flags, 0.256);

        assertEquals(0.26, enc.getDecimal(false, flags), EPSILON);
    }

    @Test
    void writingTheDefaultValueClearsAStoredValue() {
        DecimalEncodedValue enc = initialized(Rest.create());
        enc.setDecimal(false, flags, 0.5);

        enc.setDecimal(false, flags, -1);

        assertEquals(-1, enc.getDecimal(false, flags), EPSILON);
        assertEquals(0, flags.ints[0]);
    }

    @Test
    void usesAGivenDefaultValue() {
        DecimalEncodedValue enc = initialized(new OrsUnsignedDecimalEncodedValue("test", 7, 0.5, 7, false));

        assertEquals(7, enc.getDecimal(false, flags), EPSILON);
        enc.setDecimal(false, flags, 7);
        assertEquals(0, flags.ints[0]);
        enc.setDecimal(false, flags, 3.5);
        assertEquals(3.5, enc.getDecimal(false, flags), EPSILON);
    }

    @Test
    void storesBothDirectionsIndependently() {
        DecimalEncodedValue enc = initialized(new OrsUnsignedDecimalEncodedValue("test", 7, 0.01, true));

        enc.setDecimal(false, flags, 0.25);
        enc.setDecimal(true, flags, 0.75);

        assertEquals(0.25, enc.getDecimal(false, flags), EPSILON);
        assertEquals(0.75, enc.getDecimal(true, flags), EPSILON);
    }

    @Test
    void maximumLeavesTheZeroForTheDefault() {
        DecimalEncodedValue enc = initialized(Rest.create());

        assertEquals(1.26, enc.getMaxDecimal(), EPSILON);
    }

    @ParameterizedTest
    @ValueSource(doubles = {1.27, -0.5, Double.NaN})
    void rejectsValuesThatCannotBeStored(double value) {
        DecimalEncodedValue enc = initialized(Rest.create());

        assertThrows(IllegalArgumentException.class, () -> enc.setDecimal(false, flags, value));
    }

    @Test
    void requiresInitialization() {
        DecimalEncodedValue enc = Rest.create();

        assertThrows(IllegalStateException.class, () -> enc.setDecimal(false, flags, 0.5));
    }

    @Test
    void equalityAndVersionDependOnNameAndFactor() {
        OrsUnsignedDecimalEncodedValue rest = new OrsUnsignedDecimalEncodedValue("rest", 7, 0.01, false);
        OrsUnsignedDecimalEncodedValue sameRest = new OrsUnsignedDecimalEncodedValue("rest", 7, 0.01, false);
        OrsUnsignedDecimalEncodedValue coarser = new OrsUnsignedDecimalEncodedValue("rest", 7, 0.1, false);
        OrsUnsignedDecimalEncodedValue otherName = new OrsUnsignedDecimalEncodedValue("other", 7, 0.01, false);

        assertEquals(rest, sameRest);
        assertEquals(rest.getVersion(), sameRest.getVersion());
        assertNotEquals(rest, coarser);
        assertNotEquals(rest.getVersion(), coarser.getVersion());
        assertNotEquals(rest, otherName);
    }

    @Test
    void versionDependsOnTheDefaultValue() {
        OrsUnsignedDecimalEncodedValue rest = new OrsUnsignedDecimalEncodedValue("rest", 7, 0.01, -1, false);
        OrsUnsignedDecimalEncodedValue otherDefault = new OrsUnsignedDecimalEncodedValue("rest", 7, 0.01, -2, false);

        assertNotEquals(rest.getVersion(), otherDefault.getVersion());
    }

    private static DecimalEncodedValue initialized(DecimalEncodedValue enc) {
        enc.init(new EncodedValue.InitializerConfig());
        return enc;
    }
}
