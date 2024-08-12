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
        properties.setMinNetworkSize(null);
        properties.setMinOneWayNetworkSize(null);
        properties.getMethods().setCh(null);
        properties.getMethods().setLm(null);
        properties.getMethods().setCore(null);
        properties.getMethods().setFastisochrones(null);

        assertTrue(properties.isEmpty());
    }

    @Test
    void chPropertiesIsEnabledReturnsFalseWhenEnabledIsNull() {
        PreparationProperties.MethodsProperties.CHProperties ch = new PreparationProperties.MethodsProperties.CHProperties();

        assertFalse(ch.isEnabled());
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

    @Test
    void copyPropertiesToEmptyTarget() {
        PreparationProperties source = new PreparationProperties();
        PreparationProperties target = new PreparationProperties();

        // Set properties in source
        source.setMinNetworkSize(10);
        source.setMinOneWayNetworkSize(20);
        source.getMethods().setCh(new PreparationProperties.MethodsProperties.CHProperties());
        source.getMethods().setLm(new PreparationProperties.MethodsProperties.LMProperties());
        source.getMethods().setCore(new PreparationProperties.MethodsProperties.CoreProperties());
        source.getMethods().setFastisochrones(new PreparationProperties.MethodsProperties.FastIsochroneProperties());

        source.getMethods().getCh().setEnabled(true);
        source.getMethods().getCh().setWeightings("foo");
        source.getMethods().getCh().setThreads(99);

        source.getMethods().getLm().setEnabled(true);
        source.getMethods().getLm().setWeightings("bar");
        source.getMethods().getLm().setLandmarks(100);
        source.getMethods().getLm().setThreads(98);

        source.getMethods().getCore().setEnabled(true);
        source.getMethods().getCore().setWeightings("baz");
        source.getMethods().getCore().setLandmarks(101);
        source.getMethods().getCore().setThreads(97);
        source.getMethods().getCore().setLmsets("lmsets");

        source.getMethods().getFastisochrones().setEnabled(true);
        source.getMethods().getFastisochrones().setWeightings("qux");
        source.getMethods().getFastisochrones().setThreads(96);
        source.getMethods().getFastisochrones().setMaxcellnodes(95);


        // Prepare and check target
        assertNull(target.getMinNetworkSize());
        assertNull(target.getMinOneWayNetworkSize());
        assertNull(target.getMethods().getCh());
        assertNull(target.getMethods().getLm());
        assertNull(target.getMethods().getCore());
        assertNull(target.getMethods().getFastisochrones());

        assertNotEquals(source, target);

        // Copy properties
        target.copyProperties(source, false);

        // Check target properties
        assertEquals(source, target);
    }

    @Test
    void copyPropertiesWithOverwrite() {
        PreparationProperties source = new PreparationProperties();
        PreparationProperties target = new PreparationProperties();

        // Set properties in source
        source.setMethods(new PreparationProperties.MethodsProperties());
        source.getMethods().setCh(new PreparationProperties.MethodsProperties.CHProperties());
        source.getMethods().setLm(new PreparationProperties.MethodsProperties.LMProperties());
        source.getMethods().setCore(new PreparationProperties.MethodsProperties.CoreProperties());
        source.getMethods().setFastisochrones(new PreparationProperties.MethodsProperties.FastIsochroneProperties());

        // Set properties in target
        target.setMethods(new PreparationProperties.MethodsProperties());
        target.getMethods().setCh(new PreparationProperties.MethodsProperties.CHProperties());
        target.getMethods().setLm(new PreparationProperties.MethodsProperties.LMProperties());
        target.getMethods().setCore(new PreparationProperties.MethodsProperties.CoreProperties());
        target.getMethods().setFastisochrones(new PreparationProperties.MethodsProperties.FastIsochroneProperties());

        // Set different properties in source
        source.setMinNetworkSize(10);
        source.setMinOneWayNetworkSize(20);

        source.getMethods().getCh().setEnabled(true);
        source.getMethods().getCh().setWeightings("foo");
        source.getMethods().getCh().setThreads(99);

        source.getMethods().getLm().setEnabled(true);
        source.getMethods().getLm().setWeightings("bar");
        source.getMethods().getLm().setLandmarks(100);
        source.getMethods().getLm().setThreads(98);

        source.getMethods().getCore().setEnabled(true);
        source.getMethods().getCore().setWeightings("baz");
        source.getMethods().getCore().setLandmarks(101);
        source.getMethods().getCore().setThreads(97);
        source.getMethods().getCore().setLmsets("lmsets");

        source.getMethods().getFastisochrones().setEnabled(true);
        source.getMethods().getFastisochrones().setWeightings("baz");
        source.getMethods().getFastisochrones().setThreads(96);
        source.getMethods().getFastisochrones().setMaxcellnodes(95);


        // Set different properties in target
        target.setMinNetworkSize(20);
        target.setMinOneWayNetworkSize(30);
        target.getMethods().getCh().setEnabled(false);
        target.getMethods().getCh().setWeightings("bar");
        target.getMethods().getCh().setThreads(98);

        target.getMethods().getLm().setEnabled(false);
        target.getMethods().getLm().setWeightings("baz");
        target.getMethods().getLm().setLandmarks(101);
        target.getMethods().getLm().setThreads(97);

        target.getMethods().getCore().setEnabled(false);
        target.getMethods().getCore().setWeightings("qux");
        target.getMethods().getCore().setLandmarks(96);
        target.getMethods().getCore().setThreads(95);
        target.getMethods().getCore().setLmsets("lmsets");

        target.getMethods().getFastisochrones().setEnabled(false);
        target.getMethods().getFastisochrones().setWeightings("qux");
        target.getMethods().getFastisochrones().setThreads(96);
        target.getMethods().getFastisochrones().setMaxcellnodes(95);

        assertNotEquals(source, target);

        // Copy properties without overwrite
        target.copyProperties(source, false);

        // Check target properties remain unchanged
        assertNotEquals(source, target);
        assertEquals(20, target.getMinNetworkSize());
        assertEquals(30, target.getMinOneWayNetworkSize());
        assertFalse(target.getMethods().getCh().isEnabled());
        assertEquals("bar", target.getMethods().getCh().getWeightings());
        assertEquals(98, target.getMethods().getCh().getThreads());

        assertFalse(target.getMethods().getLm().isEnabled());
        assertEquals("baz", target.getMethods().getLm().getWeightings());
        assertEquals(101, target.getMethods().getLm().getLandmarks());
        assertEquals(97, target.getMethods().getLm().getThreads());

        assertFalse(target.getMethods().getCore().isEnabled());
        assertEquals("qux", target.getMethods().getCore().getWeightings());
        assertEquals(96, target.getMethods().getCore().getLandmarks());
        assertEquals(95, target.getMethods().getCore().getThreads());
        assertEquals("lmsets", target.getMethods().getCore().getLmsets());

        assertFalse(target.getMethods().getFastisochrones().isEnabled());
        assertEquals("qux", target.getMethods().getFastisochrones().getWeightings());
        assertEquals(96, target.getMethods().getFastisochrones().getThreads());
        assertEquals(95, target.getMethods().getFastisochrones().getMaxcellnodes());


        // Copy properties with overwrite
        target.copyProperties(source, true);

        // Check target properties are updated
        assertEquals(source, target);
    }

    @Test
    void copyPropertiesWithNullSource() {
        PreparationProperties target = new PreparationProperties();

        // Set properties in target
        target.getMethods().setCh(new PreparationProperties.MethodsProperties.CHProperties());
        target.getMethods().getCh().setEnabled(true);

        // Copy properties from null source
        target.copyProperties(null, false);

        // Check target properties remain unchanged
        assertNotNull(target.getMethods().getCh());
        assertTrue(target.getMethods().getCh().isEnabled());
    }

    @Test
    void copyPropertiesWithNullMethods() {
        PreparationProperties source = new PreparationProperties();
        PreparationProperties target = new PreparationProperties();

        // Set properties in target
        source.setMethods(null);
        target.setMethods(null);

        // Copy properties from null source
        target.copyProperties(source, false);

        // Check target properties remain unchanged
        assertNull(target.getMethods());
    }

    @Test
    void copyPropertiesWithNullSourceAlgorithms() {
        PreparationProperties source = new PreparationProperties();
        PreparationProperties target = new PreparationProperties();

        // Set properties in target
        target.setMinNetworkSize(10);
        target.setMinOneWayNetworkSize(20);
        target.setMethods(new PreparationProperties.MethodsProperties());
        target.getMethods().setCh(new PreparationProperties.MethodsProperties.CHProperties());
        target.getMethods().setLm(new PreparationProperties.MethodsProperties.LMProperties());
        target.getMethods().setCore(new PreparationProperties.MethodsProperties.CoreProperties());
        target.getMethods().setFastisochrones(new PreparationProperties.MethodsProperties.FastIsochroneProperties());

        target.getMethods().getCh().setEnabled(false);
        target.getMethods().getLm().setEnabled(false);
        target.getMethods().getCore().setEnabled(false);
        target.getMethods().getFastisochrones().setEnabled(false);

        // Copy properties from null source
        target.copyProperties(source, false);

        // Check target properties remain unchanged
        assertNotNull(target.getMethods());
        assertEquals(10, target.getMinNetworkSize());
        assertEquals(20, target.getMinOneWayNetworkSize());
        assertFalse(target.getMethods().getCh().isEnabled());
        assertFalse(target.getMethods().getLm().isEnabled());
        assertFalse(target.getMethods().getCore().isEnabled());
        assertFalse(target.getMethods().getFastisochrones().isEnabled());
    }

    @Test
    void copyPropertiesWithOverwriteAndNullSourceAlgorithms() {
        PreparationProperties source = new PreparationProperties();
        PreparationProperties target = new PreparationProperties();

        // Set properties in source
        source.setMethods(new PreparationProperties.MethodsProperties());
        source.getMethods().setCh(new PreparationProperties.MethodsProperties.CHProperties());
        source.getMethods().setLm(new PreparationProperties.MethodsProperties.LMProperties());
        source.getMethods().setCore(new PreparationProperties.MethodsProperties.CoreProperties());
        source.getMethods().setFastisochrones(new PreparationProperties.MethodsProperties.FastIsochroneProperties());

        // Set properties in target
        target.setMethods(new PreparationProperties.MethodsProperties());
        target.getMethods().setCh(new PreparationProperties.MethodsProperties.CHProperties());
        target.getMethods().setLm(new PreparationProperties.MethodsProperties.LMProperties());
        target.getMethods().setCore(new PreparationProperties.MethodsProperties.CoreProperties());
        target.getMethods().setFastisochrones(new PreparationProperties.MethodsProperties.FastIsochroneProperties());

        // Set different properties in target
        target.setMinNetworkSize(20);
        target.setMinOneWayNetworkSize(30);
        target.getMethods().getCh().setEnabled(false);
        target.getMethods().getLm().setEnabled(false);
        target.getMethods().getCore().setEnabled(false);
        target.getMethods().getFastisochrones().setEnabled(false);

        // Copy properties without overwrite
        target.copyProperties(source, false);

        // Check target properties remain unchanged
        assertNotEquals(source, target);
        assertEquals(20, target.getMinNetworkSize());
        assertEquals(30, target.getMinOneWayNetworkSize());
        assertFalse(target.getMethods().getCh().isEnabled());
        assertFalse(target.getMethods().getLm().isEnabled());
        assertFalse(target.getMethods().getCore().isEnabled());
        assertFalse(target.getMethods().getFastisochrones().isEnabled());

        // Copy properties with overwrite
        target.copyProperties(source, true);

        // Check target properties are not updated
        assertNotEquals(source, target);
        assertEquals(20, target.getMinNetworkSize());
        assertEquals(30, target.getMinOneWayNetworkSize());
        assertFalse(target.getMethods().getCh().isEnabled());
        assertFalse(target.getMethods().getLm().isEnabled());
        assertFalse(target.getMethods().getCore().isEnabled());
        assertFalse(target.getMethods().getFastisochrones().isEnabled());
    }

    @Test
    void testCopyPropertiesWithEmptyTargetAlgorithms() {
        PreparationProperties source = new PreparationProperties();
        PreparationProperties target = new PreparationProperties();

        // Set properties in source
        source.setMethods(new PreparationProperties.MethodsProperties());
        source.getMethods().setCh(new PreparationProperties.MethodsProperties.CHProperties());
        source.getMethods().setLm(new PreparationProperties.MethodsProperties.LMProperties());
        source.getMethods().setCore(new PreparationProperties.MethodsProperties.CoreProperties());
        source.getMethods().setFastisochrones(new PreparationProperties.MethodsProperties.FastIsochroneProperties());

        // Set properties in target
        target.setMethods(new PreparationProperties.MethodsProperties());
        target.getMethods().setCh(new PreparationProperties.MethodsProperties.CHProperties());
        target.getMethods().setLm(new PreparationProperties.MethodsProperties.LMProperties());
        target.getMethods().setCore(new PreparationProperties.MethodsProperties.CoreProperties());
        target.getMethods().setFastisochrones(new PreparationProperties.MethodsProperties.FastIsochroneProperties());

        // Set different properties in source
        source.setMinNetworkSize(10);
        source.setMinOneWayNetworkSize(20);
        source.getMethods().getCh().setEnabled(true);
        source.getMethods().getLm().setEnabled(true);
        source.getMethods().getCore().setEnabled(true);
        source.getMethods().getFastisochrones().setEnabled(true);

        assertNotEquals(source, target);

        // Copy properties
        target.copyProperties(source, false);

        // Check target properties
        assertEquals(source, target);
    }
}