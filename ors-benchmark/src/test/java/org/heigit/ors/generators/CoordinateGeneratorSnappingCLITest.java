package org.heigit.ors.generators;

import org.heigit.ors.exceptions.CommandLineParsingException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CoordinateGeneratorSnappingCLITest {

    @Test
    void testValidCliArguments() {
        String[] args = {
            "-n", "100",
            "-e", "8.6", "49.3", "8.7", "49.4",
            "-p", "driving-car,cycling-regular",
            "-r", "350",
            "-u", "http://localhost:8080/ors"
        };

        CoordinateGeneratorSnappingCLI cli = new CoordinateGeneratorSnappingCLI(args);
        CoordinateGeneratorSnapping generator = cli.createGenerator();

        assertNotNull(generator);
        assertEquals("snapped_coordinates.csv", cli.getOutputFile());
        assertFalse(cli.hasHelp());
    }

    @Test
    void testCustomOutputFile() {
        String[] args = {
            "-n", "100",
            "-e", "8.6", "49.3", "8.7", "49.4",
            "-p", "driving-car",
            "-o", "custom_output.csv"
        };

        CoordinateGeneratorSnappingCLI cli = new CoordinateGeneratorSnappingCLI(args);
        assertEquals("custom_output.csv", cli.getOutputFile());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "driving-car cycling-regular",
        "driving-car,cycling-regular",
        "driving-car, cycling-regular",
        "driving-car,cycling-regular,walking"
    })
    void testProfileParsing(String profileInput) {
        String[] args = {
            "-n", "100",
            "-e", "8.6", "49.3", "8.7", "49.4",
            "-p", profileInput
        };

        CoordinateGeneratorSnappingCLI cli = new CoordinateGeneratorSnappingCLI(args);
        CoordinateGeneratorSnapping generator = cli.createGenerator();
        assertNotNull(generator);
    }

    @Test
    void testHelpFlag() {
        String[] args = { "-h" };
        // assert throws MissingOptionException
        CommandLineParsingException exception = assertThrows(
                        CommandLineParsingException.class,
                () -> new CoordinateGeneratorSnappingCLI(args));
        assertNotNull(exception);
        assertEquals("org.heigit.ors.exceptions.CommandLineParsingException: Failed to parse command line arguments",
                exception.toString());
    }

    @Test
    void testMissingRequiredArgument() {
        String[] args = {
            "-n", "100",
            "-e", "8.6", "49.3", "8.7", "49.4"
            // Missing required -p argument
        };

        CommandLineParsingException exception = assertThrows(
                CommandLineParsingException.class, () -> new CoordinateGeneratorSnappingCLI(args));
        assertNotNull(exception);
    }

    @Test
    void testInvalidNumberFormat() {
        String[] args = {
            "-n", "invalid",
            "-e", "8.6", "49.3", "8.7", "49.4",
            "-p", "driving-car"
        };

        CoordinateGeneratorSnappingCLI cli = new CoordinateGeneratorSnappingCLI(args);
        NumberFormatException exception = assertThrows(NumberFormatException.class, cli::createGenerator);
        assertNotNull(exception);
    }

    @Test
    void testInvalidExtentValues() {
        String[] args = {
            "-n", "100",
            "-e", "8.6", "49.3", "8.7", // Missing one extent value
            "-p", "driving-car"
        };

        CommandLineParsingException exception = assertThrows(
                CommandLineParsingException.class, () -> new CoordinateGeneratorSnappingCLI(args));
        assertNotNull(exception);
    }

    @Test
    void testEmptyProfileList() {
        String[] args = {
            "-n", "100",
            "-e", "8.6", "49.3", "8.7", "49.4",
            "-p", ""
        };
        CoordinateGeneratorSnappingCLI coordinateGeneratorSnappingCLI = new CoordinateGeneratorSnappingCLI(args);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                coordinateGeneratorSnappingCLI::createGenerator);
        assertNotNull(exception);
    }
}
