package org.heigit.ors.config;

import org.heigit.ors.config.profile.EncoderOptionsProperties;
import org.heigit.ors.config.profile.ExecutionProperties;
import org.heigit.ors.config.profile.PreparationProperties;
import org.heigit.ors.config.utils.NonEmptyObjectFilter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NonEmptyObjectFilterTest {

    NonEmptyObjectFilter filter = new NonEmptyObjectFilter();

    public static Stream<Arguments> params() {
        return Stream.of(
                Arguments.of(null, true, "Null should return true"),
                Arguments.of(new EncoderOptionsProperties(), true, "Empty EncoderOptionsProperties should return true"),
                Arguments.of(new PreparationProperties.MethodsProperties(), true, "Empty PreparationProperties.MethodsProperties should return true"),
                Arguments.of(new ExecutionProperties.MethodsProperties(), true, "Empty ExecutionProperties.MethodsProperties should return true"),
                Arguments.of(createEncoderOptionsWithBlockFords(), false, "EncoderOptionsProperties with a value should return false"),
                Arguments.of(createPreparationMethodsWithChEnabled(), false, "PreparationProperties.MethodsProperties with a value should return false"),
                Arguments.of(createExecutionMethodsWithCHSet(), false, "ExecutionProperties.MethodsProperties with a value should return false"),
                Arguments.of(new HelperClass(), true, "Helperclass with no valid methods should ignore the exception caused by privateMethod and return true")
        );
    }

    private static EncoderOptionsProperties createEncoderOptionsWithBlockFords() {
        EncoderOptionsProperties o1 = new EncoderOptionsProperties();
        o1.setBlockFords(true);
        return o1;
    }

    private static PreparationProperties.MethodsProperties createPreparationMethodsWithChEnabled() {
        PreparationProperties.MethodsProperties o1 = new PreparationProperties.MethodsProperties();
        o1.getCh().setEnabled(true);
        return o1;
    }

    private static ExecutionProperties.MethodsProperties createExecutionMethodsWithCHSet() {
        ExecutionProperties.MethodsProperties o1 = new ExecutionProperties.MethodsProperties();
        o1.getAstar().setApproximation("beeline");
        return o1;
    }

    private static class HelperClass {
        public HelperClass() {
        }

        private String privateMethod() {
            return "This should cause an IllegalAccessException.";
        }
    }

    @ParameterizedTest
    @MethodSource("params")
    public void testEquals(Object testObject, boolean expectedResult, String message) {
        assertEquals(expectedResult, filter.equals(testObject), message);
    }
}
