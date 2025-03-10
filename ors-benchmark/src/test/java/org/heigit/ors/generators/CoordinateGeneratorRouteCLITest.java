package org.heigit.ors.generators;

import java.lang.reflect.Field;
import java.util.Map;

import org.heigit.ors.exceptions.CommandLineParsingException;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class CoordinateGeneratorRouteCLITest {

    @Test
    void testValidCliArguments() {
        String[] args = {
                "-n", "50",
                "-e", "8.6 49.3 8.7 49.4",
                "-p", "driving-car,cycling-regular",
                "-u", "http://localhost:8080/ors",
                "-d", "100",
                "-m", "5000,3000",
                "-t", "4"
        };

        CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);
        CoordinateGeneratorRoute generator = cli.createGenerator();

        assertNotNull(generator);
        assertEquals("route_coordinates.csv", cli.getOutputFile());
        assertFalse(cli.hasHelp());
    }

    @Test
    void testCustomOutputFile() {
        String[] args = {
                "-n", "50",
                "-e", "8.6 49.3 8.7 49.4",
                "-p", "driving-car",
                "-o", "custom_routes.csv"
        };

        CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);
        assertEquals("custom_routes.csv", cli.getOutputFile());
    }

    @ParameterizedTest
    @CsvSource({
            "driving-car,5000",
            "'driving-car,cycling-regular','5000,3000'",
            "'driving-car,cycling-regular,foot-walking','5000,3000,1000'"
    })
    void testProfileParsing(String profileInput, String maxDistances) {
        String[] args = {
                "-n", "50",
                "-e", "8.6 49.3 8.7 49.4",
                "-m", maxDistances,
                "-p", profileInput
        };

        CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);
        CoordinateGeneratorRoute generator = cli.createGenerator();
        assertNotNull(generator);
    }

    @Test
    void testHelpFlag() {
        String[] args = { "-h" };
        CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);
        assertTrue(cli.hasHelp());

    }

    @Test
    void testMissingRequiredArgument() {
        String[] args = {
                "-n", "50",
                "-e", "8.6", "49.3", "8.7", "49.4"
                // Missing required -p argument
        };

        CommandLineParsingException exception = assertThrows(
                CommandLineParsingException.class, () -> new CoordinateGeneratorRouteCLI(args));
        assertNotNull(exception);
    }

    @Test
    void testInvalidNumberFormat() {
        String[] args = {
                "-n", "invalid",
                "-e", "8.6 49.3 8.7 49.4",
                "-p", "driving-car"
        };

        CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);
        NumberFormatException exception = assertThrows(NumberFormatException.class, cli::createGenerator);
        assertNotNull(exception);
    }

    @Test
    void testInvalidExtentValues() {
        String[] args = {
                "-n", "50",
                "-e", "8.6", "49.3", "8.7", // Missing one extent value
                "-p", "driving-car"
        };

        CoordinateGeneratorRouteCLI coordinateGeneratorRouteCLI = new CoordinateGeneratorRouteCLI(args);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, coordinateGeneratorRouteCLI::createGenerator);
        assertNotNull(exception);
    }

    @Test
    void testEmptyProfileList() {
        String[] args = {
                "-n", "50",
                "-e", "8.6 49.3 8.7 49.4",
                "-p", ""
        };
        CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                cli::createGenerator);
        assertNotNull(exception);
    }

    @ParameterizedTest
    @ValueSource(strings = { "100", "250", "500", "1000" })
    void testMinDistanceParsing(String minDistance) {
        String[] args = {
                "-n", "50",
                "-e", "8.6 49.3 8.7 49.4",
                        "-p", "driving-car",
                "-d", minDistance,
                "-m", "5000"
        };

        CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);
        CoordinateGeneratorRoute generator = cli.createGenerator();
        assertNotNull(generator);
        // Test minDistance using reflection
        assertDoesNotThrow(() -> {
            Field minDistanceField = CoordinateGeneratorRoute.class.getDeclaredField("minDistance");
            minDistanceField.setAccessible(true);
            assertEquals(Double.valueOf(minDistance), minDistanceField.get(generator));
        });
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @ValueSource(strings = {
            "5000,3000",
            "10000,5000",
            "20000,10000"
    })
    void testMaxDistancesParsing(String maxDistances) {
        String[] args = {
                "-n", "50",
                "-e", "8.6 49.3 8.7 49.4",
                "-p", "driving-car,cycling-regular",
                "-m", maxDistances
        };

        CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);
        CoordinateGeneratorRoute generator = cli.createGenerator();

        // Use reflection to access the maxDistanceByProfile field
        assertDoesNotThrow(() -> {
            Field maxDistanceByProfileField = CoordinateGeneratorRoute.class.getDeclaredField("maxDistanceByProfile");
            maxDistanceByProfileField.setAccessible(true);
            Map<String, Double> maxDistanceByProfile = (Map<String, Double>) maxDistanceByProfileField.get(generator);
            assertNotNull(maxDistanceByProfile);
            String[] maxDistancesArray = maxDistances.split(",");
            assertEquals(Double.valueOf(maxDistancesArray[0]), maxDistanceByProfile.get("driving-car"));
            assertEquals(Double.valueOf(maxDistancesArray[1]), maxDistanceByProfile.get("cycling-regular"));
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "5000,3000,1000", // More distances than profiles
            "5000" // Fewer distances than profiles
    })
    void testMismatchedMaxDistancesAndProfiles(String maxDistances) {
        String[] args = {
                "-n", "50",
                "-e", "8.6 49.3 8.7 49.4",
                "-p", "driving-car,cycling-regular", // 2 profiles
                "-m", maxDistances
        };

        CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, cli::createGenerator);
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Number of max distances"),
                "Exception message should mention number of max distances");
        assertTrue(exception.getMessage().contains("must match number of profiles"),
                "Exception message should mention matching profiles");
    }

    @SuppressWarnings("java:S5976")
    @Test
    void testInvalidMaxDistance() {
        String[] args = {
                "-n", "50",
                "-e", "8.6 49.3 8.7 49.4",
                "-p", "driving-car",
                "-m", "invalid"
        };

        CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);
        CoordinateGeneratorRoute generator = cli.createGenerator();
        assertNotNull(generator); // Should still create generator but log warning
    }

    @Test
    void testThreadCount() {
        String[] args = {
                "-n", "50",
                "-e", "8.6 49.3 8.7 49.4",
                "-p", "driving-car",
                "-m", "5000",
                "-t", "8"
        };

        CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);
        CoordinateGeneratorRoute generator = cli.createGenerator();
        assertNotNull(generator);
    }

    @Test
    void testInvalidThreadCount() {
        String[] args = {
                "-n", "50",
                "-e", "8.6 49.3 8.7 49.4",
                "-p", "driving-car",
                "-m", "1000",
                "-t", "invalid"
        };

        CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                cli::createGenerator);
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Invalid number of threads"));
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
                "-n", "50",
                "-e", extentInput,
                "-p", "driving-car",
                "-m", "1000"
        };
        CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);
        double[] extent = cli.parseExtent(extentInput);

        assertArrayEquals(
                new double[] { minLon, minLat, maxLon, maxLat },
                extent,
                0.001,
                "Extent should be parsed correctly regardless of format");
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
                "-n", "50",
                "-e", extentInput,
                "-p", "driving-car",
                "-m", "1000"
        };
        CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cli.parseExtent(extentInput),
                "Should throw exception for invalid extent format");
        assertNotNull(exception);
    }

    @Test
    void testFlexibleExtentCommandLine() {
        String[] args = {
                "-n", "50",
                "-e", "8.6,49.3,8.7,49.4", // comma-separated extent
                "-p", "driving-car",
                "-m", "5000"
        };

        CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);
        CoordinateGeneratorRoute generator = cli.createGenerator();
        assertNotNull(generator);
    }

    @Test
    void testSpaceExtentCommandLine() {
        String[] args = {
                "-n", "50",
                "-e", "8.6 49.3 8.7 49.4", // space-separated extent
                "-p", "driving-car",
                "-m", "5000"
        };

        CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);
        CoordinateGeneratorRoute generator = cli.createGenerator();
        assertNotNull(generator);
    }
}
