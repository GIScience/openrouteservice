package org.heigit.ors.coordinates_generator.cli;

import org.heigit.ors.benchmark.exceptions.CommandLineParsingException;
import org.heigit.ors.coordinates_generator.generators.CoordinateGeneratorSnapping;
import org.heigit.ors.coordinates_generator.service.CoordinateSnapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class SnappingCommandLineParserTest {

    @Test
    void testValidCliArguments() {
        String[] args = {
            "-n", "100",
                "-e", "8.6,49.3,8.7,49.4",
                    "-p", "driving-car,cycling-regular",
                "-sr", "350",
                "-u", "http://localhost:8080/ors"
        };

        SnappingCommandLineParser cli = new SnappingCommandLineParser(args);
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

        SnappingCommandLineParser cli = new SnappingCommandLineParser(args);
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

        SnappingCommandLineParser cli = new SnappingCommandLineParser(args);
        CoordinateGeneratorSnapping generator = cli.createGenerator();
        assertNotNull(generator);
    }

    @Test
    void testHelpFlag() {
        String[] args = { "-h" };
        // assert throws MissingOptionException
        CommandLineParsingException exception = assertThrows(
                        CommandLineParsingException.class,
                () -> new SnappingCommandLineParser(args));
        assertNotNull(exception);
        assertEquals("org.heigit.ors.benchmark.exceptions.CommandLineParsingException: Failed to parse command line arguments",
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
                CommandLineParsingException.class, () -> new SnappingCommandLineParser(args));
        assertNotNull(exception);
    }

    @Test
    void testInvalidNumberFormat() {
        String[] args = {
            "-n", "invalid",
                "-e", "8.6,49.3,8.7,49.4",
                    "-p", "driving-car"
        };

        SnappingCommandLineParser cli = new SnappingCommandLineParser(args);
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
        SnappingCommandLineParser cli = new SnappingCommandLineParser(args);
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
        SnappingCommandLineParser cli = new SnappingCommandLineParser(args);
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

        SnappingCommandLineParser cli = new SnappingCommandLineParser(args);
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

        SnappingCommandLineParser cli = new SnappingCommandLineParser(args);
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
        SnappingCommandLineParser cli = new SnappingCommandLineParser(args);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                cli::createGenerator);
        assertNotNull(exception);
    }

    @Test
    void testMaxAttempts() {
        String[] args = {
                "-n", "100",
                "-e", "8.6 49.3 8.7 49.4",
                "-p", "driving-car",
                "-ma", "2000"
        };

        SnappingCommandLineParser cli = new SnappingCommandLineParser(args);
        CoordinateGeneratorSnapping generator = cli.createGenerator();
        assertNotNull(generator);

        // No direct way to test maxAttempts after generation, as it's just a parameter
        // passed to generate()
        // We're verifying that the CLI parser correctly reads the value and the
        // generator is created successfully
    }

    @Test
    void testSnapRadius() {
        String[] args = {
                "-n", "100",
                "-e", "8.6 49.3 8.7 49.4",
                "-p", "driving-car",
                "-sr", "500"
        };

        SnappingCommandLineParser cli = new SnappingCommandLineParser(args);
        CoordinateGeneratorSnapping generator = cli.createGenerator();
        assertNotNull(generator);

        // Test snapRadius using reflection to verify it was correctly passed to the
        // coordinate snapper
        assertDoesNotThrow(() -> {
            Field coordinateSnapperField = CoordinateGeneratorSnapping.class.getDeclaredField("coordinateSnapper");
            coordinateSnapperField.setAccessible(true);
            CoordinateSnapper snapper = (CoordinateSnapper) coordinateSnapperField.get(generator);

            Field snapRadiusField = CoordinateSnapper.class.getDeclaredField("snapRadius");
            snapRadiusField.setAccessible(true);
            assertEquals(500.0, snapRadiusField.getDouble(snapper), 0.001, "Snap radius should be set correctly");
        });
    }

    @Test
    void testDefaultSnapRadius() {
        String[] args = {
                "-n", "100",
                "-e", "8.6 49.3 8.7 49.4",
                "-p", "driving-car"
                // No -sr parameter, should use default value
        };

        SnappingCommandLineParser cli = new SnappingCommandLineParser(args);
        CoordinateGeneratorSnapping generator = cli.createGenerator();
        assertNotNull(generator);

        // Test snapRadius using reflection to verify the default value was used
        assertDoesNotThrow(() -> {
            Field coordinateSnapperField = CoordinateGeneratorSnapping.class.getDeclaredField("coordinateSnapper");
            coordinateSnapperField.setAccessible(true);
            CoordinateSnapper snapper = (CoordinateSnapper) coordinateSnapperField.get(generator);

            Field snapRadiusField = CoordinateSnapper.class.getDeclaredField("snapRadius");
            snapRadiusField.setAccessible(true);
            assertEquals(350.0, snapRadiusField.getDouble(snapper), 0.001, "Default snap radius should be 350");
        });
    }
}
