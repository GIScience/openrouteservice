package org.heigit.ors.benchmark.util;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NameUtilsTest {

    @ParameterizedTest(name = "{index}: {0} -> {1}")
    @MethodSource("fileNameTestCases")
    @DisplayName("Test getFileNameWithoutExtension with various inputs")
    void testGetFileNameWithoutExtension(String filePath, String expected) {
        String actual = NameUtils.getFileNameWithoutExtension(filePath);
        assertEquals(expected, actual);
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> fileNameTestCases() {
        return Stream.of(
            // Normal file path
            arguments("/path/to/file.txt", "file"),
            // Multiple dots in file name
            arguments("/path/to/file.name.txt", "file.name"),
            // No extension
            arguments("/path/to/file", "file"),
            // Ending with a dot
            arguments("/path/to/file.", "file"),
            // Just filename with extension
            arguments("file.txt", "file"),
            // Just filename without extension
            arguments("file", "file"),
            // Empty string
            arguments("", ""),
            // Null
            arguments(null, null)
        );
    }
}
