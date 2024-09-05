package org.heigit.ors.config.profile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PreparationPropertiesTest {

    static Stream<Object[]> providePreparationProperties() {
        return Stream.of(
                new Object[]{null, null, new PreparationProperties.MethodsProperties(), true},
                new Object[]{10, null, new PreparationProperties.MethodsProperties(), false},
                new Object[]{null, 10, new PreparationProperties.MethodsProperties(), false},
                new Object[]{10, 10, new PreparationProperties.MethodsProperties(), false},
                new Object[]{null, null, createEmptyMethodsProperties(true, true, true, true), true},
                new Object[]{10, null, createEmptyMethodsProperties(true, true, true, true), false},
                new Object[]{null, 10, createEmptyMethodsProperties(true, true, true, true), false},
                new Object[]{10, 10, createEmptyMethodsProperties(true, true, true, true), false},
                new Object[]{null, null, createEmptyMethodsProperties(false, false, false, false), true},
                new Object[]{null, null, createEmptyMethodsProperties(true, false, false, false), true},
                new Object[]{null, null, createEmptyMethodsProperties(false, true, false, false), true},
                new Object[]{null, null, createEmptyMethodsProperties(false, false, true, false), true},
                new Object[]{null, null, createEmptyMethodsProperties(false, false, false, true), true},
                new Object[]{null, null, createEmptyMethodsProperties(true, true, false, false), true},
                new Object[]{null, null, createEnabledMethodsProperties(true, true, true, true), false},
                new Object[]{null, null, createEnabledMethodsProperties(false, false, false, false), true},
                new Object[]{null, null, createEnabledMethodsProperties(true, false, false, false), false},
                new Object[]{null, null, createEnabledMethodsProperties(false, true, false, false), false},
                new Object[]{null, null, createEnabledMethodsProperties(false, false, true, true), false}
        );
    }

    private static PreparationProperties.MethodsProperties createEmptyMethodsProperties(boolean ch, boolean lm, boolean core, boolean fastisochrones) {
        PreparationProperties.MethodsProperties methods = new PreparationProperties.MethodsProperties();
        if (ch) methods.setCh(new PreparationProperties.MethodsProperties.CHProperties());
        if (lm) methods.setLm(new PreparationProperties.MethodsProperties.LMProperties());
        if (core) methods.setCore(new PreparationProperties.MethodsProperties.CoreProperties());
        if (fastisochrones)
            methods.setFastisochrones(new PreparationProperties.MethodsProperties.FastIsochroneProperties());
        return methods;
    }

    private static PreparationProperties.MethodsProperties createEnabledMethodsProperties(boolean ch, boolean lm, boolean core, boolean fastisochrones) {
        PreparationProperties.MethodsProperties methods = new PreparationProperties.MethodsProperties();
        if (ch) {
            methods.setCh(new PreparationProperties.MethodsProperties.CHProperties());
            methods.getCh().setEnabled(true);
        }
        if (lm) {
            methods.setLm(new PreparationProperties.MethodsProperties.LMProperties());
            methods.getLm().setEnabled(true);
        }
        if (core) {
            methods.setCore(new PreparationProperties.MethodsProperties.CoreProperties());
            methods.getCore().setEnabled(true);
        }
        if (fastisochrones) {
            methods.setFastisochrones(new PreparationProperties.MethodsProperties.FastIsochroneProperties());
            methods.getFastisochrones().setEnabled(true);
        }

        return methods;
    }

    @Test
    void isEmptyReturnsTrueWhenAllFieldsAreNull() {
        PreparationProperties properties = new PreparationProperties();
        assertTrue(properties.isEmpty());
    }

    @Test
    void chPropertiesGetThreadsSaveReturnsOneWhenThreadsIsNull() {
        PreparationProperties.MethodsProperties.CHProperties ch = new PreparationProperties.MethodsProperties.CHProperties();

        assertNull(ch.getThreads());
        assertEquals(1, ch.getThreadsSave());
    }

    @Test
    void chPropertiesGetThreadsSaveReturnsOneWhenThreadsIsLessThanOne() {
        PreparationProperties.MethodsProperties.CHProperties ch = new PreparationProperties.MethodsProperties.CHProperties();
        ch.setThreads(0);

        assertEquals(1, ch.getThreadsSave());
    }

    @Test
    void chPropertiesGetThreadsSaveReturnsThreadsWhenThreadsIsGreaterThanZero() {
        PreparationProperties.MethodsProperties.CHProperties ch = new PreparationProperties.MethodsProperties.CHProperties();
        ch.setThreads(5);

        assertEquals(5, ch.getThreadsSave());
    }

    @ParameterizedTest
    @MethodSource("providePreparationProperties")
    void isEmptyTests(Integer minNetworkSize, Integer minOneWayNetworkSize, PreparationProperties.MethodsProperties methods, boolean expectedEmpty) {
        PreparationProperties properties = new PreparationProperties();
        properties.setMinNetworkSize(minNetworkSize);
        properties.setMinOneWayNetworkSize(minOneWayNetworkSize);
        properties.setMethods(methods);

        assertEquals(expectedEmpty, properties.isEmpty());
    }
}