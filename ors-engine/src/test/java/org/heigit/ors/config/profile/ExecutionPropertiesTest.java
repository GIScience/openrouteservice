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
        assertNotNull(executionProperties.getMethods().getAstar());
        assertNotNull(executionProperties.getMethods().getLm());
        assertNotNull(executionProperties.getMethods().getCore());
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
}