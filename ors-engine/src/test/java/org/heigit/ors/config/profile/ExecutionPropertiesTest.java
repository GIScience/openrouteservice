package org.heigit.ors.config.profile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionPropertiesTest {
    static Stream<Object[]> provideEmptyExecutionProperties() {
        return Stream.of(
                new Object[]{createEmptyMethodsProperties(false, false, false)},
                new Object[]{createEmptyMethodsProperties(true, true, true)},
                new Object[]{createEmptyMethodsProperties(false, false, false)},
                new Object[]{createEmptyMethodsProperties(true, true, true)},
                new Object[]{createEmptyMethodsProperties(false, false, false)},
                new Object[]{createEmptyMethodsProperties(true, true, true)},
                new Object[]{createEmptyMethodsProperties(false, false, false)},
                new Object[]{createEmptyMethodsProperties(true, true, true)},
                new Object[]{createEmptyMethodsProperties(false, false, false)},
                new Object[]{createEmptyMethodsProperties(true, true, true)},
                new Object[]{createEmptyMethodsProperties(false, false, false)},
                new Object[]{createEmptyMethodsProperties(true, true, true)}
        );
    }

    static Stream<Object[]> provideEnabledExecutionProperties() {
        return Stream.of(
                new Object[]{null, null, null, createEnabledMethodsProperties(), true},
                new Object[]{5, null, null, createEnabledMethodsProperties(), false},
                new Object[]{null, "some_approximation", null, createEnabledMethodsProperties(), false},
                new Object[]{null, null, 0.0, createEnabledMethodsProperties(), false},
                new Object[]{5, "some_approximation", 0.0, createEnabledMethodsProperties(), false}
        );
    }

    private static ExecutionProperties.MethodsProperties createEmptyMethodsProperties(boolean astar, boolean lm,
                                                                                      boolean core) {
        ExecutionProperties.MethodsProperties methods = new ExecutionProperties.MethodsProperties();
        if (astar) methods.setAstar(new ExecutionProperties.MethodsProperties.AStarProperties());
        if (lm) methods.setLm(new ExecutionProperties.MethodsProperties.LMProperties());
        if (core) methods.setCore(new ExecutionProperties.MethodsProperties.CoreProperties());
        return methods;
    }

    private static ExecutionProperties.MethodsProperties createEnabledMethodsProperties() {
        ExecutionProperties.MethodsProperties methods = new ExecutionProperties.MethodsProperties();
        methods.setAstar(new ExecutionProperties.MethodsProperties.AStarProperties());
        methods.setLm(new ExecutionProperties.MethodsProperties.LMProperties());
        methods.setCore(new ExecutionProperties.MethodsProperties.CoreProperties());

        return methods;
    }

    @Test
    void defaultExecutionPropertiesShouldInitializeMethods() {
        ExecutionProperties executionProperties = new ExecutionProperties();
        assertNotNull(executionProperties.getMethods());
        assertNull(executionProperties.getMethods().getAstar());
        assertNull(executionProperties.getMethods().getLm());
        assertNull(executionProperties.getMethods().getCore());
    }

    @ParameterizedTest
    @MethodSource("provideEmptyExecutionProperties")
    void isEmptyTestsWithEmptyExecutions(ExecutionProperties.MethodsProperties methods) {
        ExecutionProperties properties = new ExecutionProperties();
        properties.setMethods(methods);

        assertTrue(properties.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("provideEnabledExecutionProperties")
    void isEmptyTestsWithEnabledExecutions(Integer activeLandmarks, String approximation, Double
            epsilon, ExecutionProperties.MethodsProperties methods, boolean expectedEmpty) {
        ExecutionProperties properties = new ExecutionProperties();
        methods.getCore().setActiveLandmarks(activeLandmarks);
        methods.getAstar().setApproximation(approximation);
        methods.getAstar().setEpsilon(epsilon);
        methods.getLm().setActiveLandmarks(activeLandmarks);

        properties.setMethods(methods);

        assertEquals(expectedEmpty, properties.isEmpty());
    }

    @Test
    void copyPropertiesToEmptyTarget() {
        ExecutionProperties source = new ExecutionProperties();
        ExecutionProperties target = new ExecutionProperties();

        // Set properties in source
        source.getMethods().setAstar(new ExecutionProperties.MethodsProperties.AStarProperties());
        source.getMethods().getAstar().setApproximation("approximation1");
        source.getMethods().getAstar().setEpsilon(1.0);

        source.getMethods().setLm(new ExecutionProperties.MethodsProperties.LMProperties());
        source.getMethods().getLm().setActiveLandmarks(5);

        source.getMethods().setCore(new ExecutionProperties.MethodsProperties.CoreProperties());
        source.getMethods().getCore().setActiveLandmarks(10);

        // Prepare and check target
        assertNull(target.getMethods().getAstar());
        assertNull(target.getMethods().getLm());
        assertNull(target.getMethods().getCore());

        // Copy properties
        target.copyProperties(source, false);

        // Check target properties
        assertEquals(source, target);
        assertEquals(source.getMethods().getAstar(), target.getMethods().getAstar());
        assertEquals(source.getMethods().getLm(), target.getMethods().getLm());
        assertEquals(source.getMethods().getCore(), target.getMethods().getCore());
    }

    @Test
    void copyPropertiesWithOverwrite() {
        ExecutionProperties source = new ExecutionProperties();
        ExecutionProperties target = new ExecutionProperties();

        // Set properties in source
        source.setMethods(new ExecutionProperties.MethodsProperties());
        source.getMethods().setAstar(new ExecutionProperties.MethodsProperties.AStarProperties());
        source.getMethods().setLm(new ExecutionProperties.MethodsProperties.LMProperties());
        source.getMethods().setCore(new ExecutionProperties.MethodsProperties.CoreProperties());

        // Set properties in target
        target.setMethods(new ExecutionProperties.MethodsProperties());
        target.getMethods().setAstar(new ExecutionProperties.MethodsProperties.AStarProperties());
        target.getMethods().setLm(new ExecutionProperties.MethodsProperties.LMProperties());
        target.getMethods().setCore(new ExecutionProperties.MethodsProperties.CoreProperties());

        // Set different properties in source
        source.getMethods().getAstar().setApproximation("approximation1");
        source.getMethods().getAstar().setEpsilon(1.0);
        source.getMethods().getLm().setActiveLandmarks(8);
        source.getMethods().getCore().setActiveLandmarks(6);

        // Set different properties in target
        target.getMethods().getAstar().setApproximation("approximation2");
        target.getMethods().getAstar().setEpsilon(2.0);
        target.getMethods().getLm().setActiveLandmarks(5);
        target.getMethods().getCore().setActiveLandmarks(10);

        // Copy properties without overwrite
        target.copyProperties(source, false);

        // Check target properties remain unchanged
        assertNotNull(target.getMethods());
        assertEquals("approximation2", target.getMethods().getAstar().getApproximation());
        assertEquals(2.0, target.getMethods().getAstar().getEpsilon());
        assertEquals(5, target.getMethods().getLm().getActiveLandmarks());
        assertEquals(10, target.getMethods().getCore().getActiveLandmarks());

        // Copy properties with overwrite
        target.copyProperties(source, true);

        // Check target properties are updated
        assertEquals(source, target);
    }

    @Test
    void copyPropertiesWithNullSource() {
        ExecutionProperties target = new ExecutionProperties();

        // Set properties in target
        target.getMethods().setAstar(new ExecutionProperties.MethodsProperties.AStarProperties());
        target.getMethods().getAstar().setApproximation("approximation2");
        target.getMethods().getAstar().setEpsilon(2.0);

        // Copy properties from null source
        target.copyProperties(null, false);

        // Check target properties remain unchanged
        assertNotNull(target.getMethods().getAstar());
        assertEquals("approximation2", target.getMethods().getAstar().getApproximation());
        assertEquals(2.0, target.getMethods().getAstar().getEpsilon());
    }

    @Test
    void copyPropertiesWithNullMethods() {
        ExecutionProperties source = new ExecutionProperties();
        ExecutionProperties target = new ExecutionProperties();

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
        ExecutionProperties source = new ExecutionProperties();
        ExecutionProperties target = new ExecutionProperties();

        // Set properties in target
        target.setMethods(new ExecutionProperties.MethodsProperties());
        target.getMethods().setAstar(new ExecutionProperties.MethodsProperties.AStarProperties());
        target.getMethods().setLm(new ExecutionProperties.MethodsProperties.LMProperties());
        target.getMethods().setCore(new ExecutionProperties.MethodsProperties.CoreProperties());

        target.getMethods().getAstar().setApproximation("approximation2");
        target.getMethods().getAstar().setEpsilon(2.0);
        target.getMethods().getLm().setActiveLandmarks(5);
        target.getMethods().getCore().setActiveLandmarks(10);

        // Copy properties from null source
        target.copyProperties(source, false);

        // Check target properties remain unchanged
        assertNotNull(target.getMethods());
        assertEquals("approximation2", target.getMethods().getAstar().getApproximation());
        assertEquals(2.0, target.getMethods().getAstar().getEpsilon());
        assertEquals(5, target.getMethods().getLm().getActiveLandmarks());
        assertEquals(10, target.getMethods().getCore().getActiveLandmarks());
    }

    @Test
    void copyPropertiesWithOverwriteAndNullSourceAlgorithms() {
        ExecutionProperties source = new ExecutionProperties();
        ExecutionProperties target = new ExecutionProperties();

        // Set properties in source
        source.setMethods(new ExecutionProperties.MethodsProperties());
        source.getMethods().setAstar(new ExecutionProperties.MethodsProperties.AStarProperties());
        source.getMethods().setLm(new ExecutionProperties.MethodsProperties.LMProperties());
        source.getMethods().setCore(new ExecutionProperties.MethodsProperties.CoreProperties());

        // Set properties in target
        target.setMethods(new ExecutionProperties.MethodsProperties());
        target.getMethods().setAstar(new ExecutionProperties.MethodsProperties.AStarProperties());
        target.getMethods().setLm(new ExecutionProperties.MethodsProperties.LMProperties());
        target.getMethods().setCore(new ExecutionProperties.MethodsProperties.CoreProperties());

        // Set different properties in target
        target.getMethods().getAstar().setApproximation("approximation2");
        target.getMethods().getAstar().setEpsilon(2.0);
        target.getMethods().getLm().setActiveLandmarks(5);
        target.getMethods().getCore().setActiveLandmarks(10);

        // Copy properties without overwrite
        target.copyProperties(source, false);

        // Check target properties remain unchanged
        assertEquals("approximation2", target.getMethods().getAstar().getApproximation());
        assertEquals(2.0, target.getMethods().getAstar().getEpsilon());
        assertEquals(5, target.getMethods().getLm().getActiveLandmarks());
        assertEquals(10, target.getMethods().getCore().getActiveLandmarks());

        // Copy properties with overwrite
        target.copyProperties(source, true);

        // Check target properties are not updated
        assertNotEquals(source, target);
        assertEquals("approximation2", target.getMethods().getAstar().getApproximation());
        assertEquals(2.0, target.getMethods().getAstar().getEpsilon());
        assertEquals(5, target.getMethods().getLm().getActiveLandmarks());
        assertEquals(10, target.getMethods().getCore().getActiveLandmarks());
    }

    @Test
    void testCopyPropertiesWithEmptyTargetAlgorithms() {
        ExecutionProperties source = new ExecutionProperties();
        ExecutionProperties target = new ExecutionProperties();

        // Set properties in source
        source.setMethods(new ExecutionProperties.MethodsProperties());
        source.getMethods().setAstar(new ExecutionProperties.MethodsProperties.AStarProperties());
        source.getMethods().setLm(new ExecutionProperties.MethodsProperties.LMProperties());
        source.getMethods().setCore(new ExecutionProperties.MethodsProperties.CoreProperties());

        // Set properties in target
        target.setMethods(new ExecutionProperties.MethodsProperties());
        target.getMethods().setAstar(new ExecutionProperties.MethodsProperties.AStarProperties());
        target.getMethods().setLm(new ExecutionProperties.MethodsProperties.LMProperties());
        target.getMethods().setCore(new ExecutionProperties.MethodsProperties.CoreProperties());

        // Set different properties in source
        source.getMethods().getAstar().setApproximation("approximation1");
        source.getMethods().getAstar().setEpsilon(1.0);
        source.getMethods().getLm().setActiveLandmarks(8);
        source.getMethods().getCore().setActiveLandmarks(6);

        // Copy properties
        target.copyProperties(source, false);

        // Check target properties
        assertEquals(source, target);
    }


}