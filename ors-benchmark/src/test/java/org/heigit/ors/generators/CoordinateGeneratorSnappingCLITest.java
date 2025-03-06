package org.heigit.ors.generators;

import org.heigit.ors.exceptions.CommandLineParsingException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class CoordinateGeneratorSnappingCLITest {

    @Test
    void testValidCliArguments() {
        String[] args = {
            "-n", "100",
                "-e", "8.6,49.3,8.7,49.4",
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
                "-e", "8.6,49.3,8.7,49.4",
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
                "-e", "8.6,49.3,8.7,49.4",
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
                "-e", "8.6,49.3,8.7,49.4"
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
                "-e", "8.6,49.3,8.7,49.4",
                    "-p", "driving-car"
        };

        CoordinateGeneratorSnappingCLI cli = new CoordinateGeneratorSnappingCLI(args);
        NumberFormatException exception = assertThrows(NumberFormatException.class, cli::createGenerator);
        assertNotNull(exception);
    }

    @ParameterizedTest
    @CsvSource({
            // Format: extent string, expected values
            "'8.6,49.3,8.7,49.4', 8.6, 49.3, 8.7, 49.4",
            "'8.6 49.3 8.7 49.4', 8.6, 49.3, 8.7, 49.4",
            "'8.6, 49.3, 8.7, 49.4', 8.6, 49.3, 8.7, 49.4"
    })
    void testExtentParsing(String extentInput, double minLon, double minLat, double maxLon, double maxLat) {
        String[] args = {
                "-n", "100",
                "-e", extentInput,
                "-p", "driving-car"
        };
        CoordinateGeneratorSnappingCLI cli = new CoordinateGeneratorSnappingCLI(args);
        double[] extent = cli.parseExtent(extentInput);

        assertEquals(minLon, extent[0], 0.001, "Min longitude should match");
        assertEquals(minLat, extent[1], 0.001, "Min latitude should match");
        assertEquals(maxLon, extent[2], 0.001, "Max longitude should match");
        assertEquals(maxLat, extent[3], 0.001, "Max latitude should match");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "8.6,49.3,8.7", // Too few values
            "8.6,49.3,8.7,49.4,8.8", // Too many values
            "8.6,invalid,8.7,49.4", // Non-numeric value
            "", // Empty string
            "   " // Blank string
    })
    void testInvalidExtentParsing(String extentInput) {
        String[] args = {
                "-n", "100",
                "-e", extentInput,
                "-p", "driving-car"
        };
        CoordinateGeneratorSnappingCLI cli = new CoordinateGeneratorSnappingCLI(args);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cli.parseExtent(extentInput),
                "Should throw exception for invalid extent format");
        assertNotNull(exception);
    }

    @Test
    void testFlexibleExtentCommandLine() {
        String[] args = {
                "-n", "100",
                "-e", "8.6,49.3,8.7,49.4", // comma-separated extent
                "-p", "driving-car"
        };

        CoordinateGeneratorSnappingCLI cli = new CoordinateGeneratorSnappingCLI(args);
        CoordinateGeneratorSnapping generator = cli.createGenerator();
        assertNotNull(generator);
    }

    @Test
    void testSpaceExtentCommandLine() {
        String[] args = {
            "-n", "100",
                "-e", "8.6 49.3 8.7 49.4", // space-separated extent
            "-p", "driving-car"
        };

        CoordinateGeneratorSnappingCLI cli = new CoordinateGeneratorSnappingCLI(args);
        CoordinateGeneratorSnapping generator = cli.createGenerator();
        assertNotNull(generator);
    }

    @Test
    void testEmptyProfileList() {
        String[] args = {
            "-n", "100",
                "-e", "8.6 49.3 8.7 49.4",
                    "-p", ""
        };
        CoordinateGeneratorSnappingCLI cli = new CoordinateGeneratorSnappingCLI(args);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                cli::createGenerator);
        assertNotNull(exception);
    }
}
