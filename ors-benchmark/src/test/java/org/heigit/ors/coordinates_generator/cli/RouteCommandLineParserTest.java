package org.heigit.ors.coordinates_generator.cli;

import org.heigit.ors.benchmark.exceptions.CommandLineParsingException;
import org.heigit.ors.coordinates_generator.generators.CoordinateGeneratorRoute;
import org.heigit.ors.coordinates_generator.service.CoordinateSnapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RouteCommandLineParserTest {

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

        RouteCommandLineParser cli = new RouteCommandLineParser(args);
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

        RouteCommandLineParser cli = new RouteCommandLineParser(args);
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

        RouteCommandLineParser cli = new RouteCommandLineParser(args);
        CoordinateGeneratorRoute generator = cli.createGenerator();
        assertNotNull(generator);
    }

    @Test
    void testHelpFlag() {
        String[] args = { "-h" };
        RouteCommandLineParser cli = new RouteCommandLineParser(args);
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
                CommandLineParsingException.class, () -> new RouteCommandLineParser(args));
        assertNotNull(exception);
    }

    @Test
    void testInvalidNumberFormat() {
        String[] args = {
                "-n", "invalid",
                "-e", "8.6 49.3 8.7 49.4",
                "-p", "driving-car"
        };

        RouteCommandLineParser cli = new RouteCommandLineParser(args);
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

        RouteCommandLineParser routeCommandLineParser = new RouteCommandLineParser(args);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, routeCommandLineParser::createGenerator);
        assertNotNull(exception);
    }

    @Test
    void testEmptyProfileList() {
        String[] args = {
                "-n", "50",
                "-e", "8.6 49.3 8.7 49.4",
                "-p", ""
        };
        RouteCommandLineParser cli = new RouteCommandLineParser(args);
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

        RouteCommandLineParser cli = new RouteCommandLineParser(args);
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

        RouteCommandLineParser cli = new RouteCommandLineParser(args);
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

        RouteCommandLineParser cli = new RouteCommandLineParser(args);

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

        RouteCommandLineParser cli = new RouteCommandLineParser(args);
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

        RouteCommandLineParser cli = new RouteCommandLineParser(args);
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

        RouteCommandLineParser cli = new RouteCommandLineParser(args);
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
        RouteCommandLineParser cli = new RouteCommandLineParser(args);
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
        RouteCommandLineParser cli = new RouteCommandLineParser(args);
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

        RouteCommandLineParser cli = new RouteCommandLineParser(args);
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

        RouteCommandLineParser cli = new RouteCommandLineParser(args);
        CoordinateGeneratorRoute generator = cli.createGenerator();
        assertNotNull(generator);
    }

    @Test
    void testSnapRadius() {
        String[] args = {
                "-n", "50",
                "-e", "8.6 49.3 8.7 49.4",
                "-p", "driving-car",
                "-m", "5000",
                "-sr", "3000"
        };

        RouteCommandLineParser cli = new RouteCommandLineParser(args);
        CoordinateGeneratorRoute generator = cli.createGenerator();
        assertNotNull(generator);

        // Test snapRadius using reflection
        assertDoesNotThrow(() -> {
            Field coordinateSnapperField = CoordinateGeneratorRoute.class.getDeclaredField("coordinateSnapper");
            coordinateSnapperField.setAccessible(true);
            CoordinateSnapper snapper = (CoordinateSnapper) coordinateSnapperField.get(generator);

            Field snapRadiusField = CoordinateSnapper.class.getDeclaredField("snapRadius");
            snapRadiusField.setAccessible(true);
            assertEquals(3000.0, snapRadiusField.getDouble(snapper), 0.001);
        });
    }
}
