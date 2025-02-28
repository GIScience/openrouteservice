package org.heigit.ors.generators;

import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.ParseException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CoordinateGeneratorSnappingCLITest {

    @Test
    void testValidCliArguments() throws ParseException {
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
    void testCustomOutputFile() throws ParseException {
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
    void testProfileParsing(String profileInput) throws ParseException {
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
        MissingOptionException exception = assertThrows(MissingOptionException.class,
                () -> new CoordinateGeneratorSnappingCLI(args));
        assertNotNull(exception);
        assertEquals("org.apache.commons.cli.MissingOptionException: Missing required options: n, e, p", exception.toString());
    }

    @Test
    void testMissingRequiredArgument() {
        String[] args = {
            "-n", "100",
            "-e", "8.6", "49.3", "8.7", "49.4"
            // Missing required -p argument
        };

        ParseException exception = assertThrows(ParseException.class, () -> new CoordinateGeneratorSnappingCLI(args));
        assertNotNull(exception);
    }

    @Test
    void testInvalidNumberFormat() throws ParseException {
        String[] args = {
            "-n", "invalid",
            "-e", "8.6", "49.3", "8.7", "49.4",
            "-p", "driving-car"
        };

        CoordinateGeneratorSnappingCLI cli = new CoordinateGeneratorSnappingCLI(args);
        NumberFormatException exception = assertThrows(NumberFormatException.class, () -> cli.createGenerator());
        assertNotNull(exception);
    }

    @Test
    void testInvalidExtentValues() {
        String[] args = {
            "-n", "100",
            "-e", "8.6", "49.3", "8.7", // Missing one extent value
            "-p", "driving-car"
        };

        ParseException exception = assertThrows(ParseException.class, () -> new CoordinateGeneratorSnappingCLI(args));
        assertNotNull(exception);
    }

    @Test
    void testEmptyProfileList() {
        String[] args = {
            "-n", "100",
            "-e", "8.6", "49.3", "8.7", "49.4",
            "-p", ""
        };

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            CoordinateGeneratorSnappingCLI cli = new CoordinateGeneratorSnappingCLI(args);
            cli.createGenerator();
        });
        assertNotNull(exception);
    }
}
