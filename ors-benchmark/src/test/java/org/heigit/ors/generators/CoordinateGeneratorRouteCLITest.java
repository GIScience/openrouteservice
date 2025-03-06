package org.heigit.ors.generators;

import java.lang.reflect.Field;
import java.util.Map;

import org.heigit.ors.exceptions.CommandLineParsingException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CoordinateGeneratorRouteCLITest {

    @Test
    void testValidCliArguments() {
        String[] args = {
            "-n", "50",
            "-e", "8.6", "49.3", "8.7", "49.4",
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
            "-e", "8.6", "49.3", "8.7", "49.4",
            "-p", "driving-car",
            "-o", "custom_routes.csv"
        };

        CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);
        assertEquals("custom_routes.csv", cli.getOutputFile());
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
            "-n", "50",
            "-e", "8.6", "49.3", "8.7", "49.4",
            "-p", profileInput
        };

        CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);
        CoordinateGeneratorRoute generator = cli.createGenerator();
        assertNotNull(generator);
    }

    @Test
    void testHelpFlag() {
        String[] args = { "-h" };
        CommandLineParsingException exception = assertThrows(
                CommandLineParsingException.class,
                () -> new CoordinateGeneratorRouteCLI(args));
        assertNotNull(exception);
        assertEquals("org.heigit.ors.exceptions.CommandLineParsingException: Failed to parse command line arguments",
                exception.toString());
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
            "-e", "8.6", "49.3", "8.7", "49.4",
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

        CommandLineParsingException exception = assertThrows(
                CommandLineParsingException.class, () -> new CoordinateGeneratorRouteCLI(args));
        assertNotNull(exception);
    }

    @Test
    void testEmptyProfileList() {
        String[] args = {
            "-n", "50",
            "-e", "8.6", "49.3", "8.7", "49.4",
            "-p", ""
        };
        CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                cli::createGenerator);
        assertNotNull(exception);
    }

    @Test
    void testMinDistanceParsing() {
        String[] args = {
            "-n", "50",
            "-e", "8.6", "49.3", "8.7", "49.4",
            "-p", "driving-car",
            "-d", "250"
        };

        CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);
        CoordinateGeneratorRoute generator = cli.createGenerator();
        assertNotNull(generator);
        // We can't directly access the minDistance field, but we can verify the generator was created
    }

    @Test
    void testMaxDistancesParsing() throws Exception {
        String[] args = {
            "-n", "50",
            "-e", "8.6", "49.3", "8.7", "49.4",
            "-p", "driving-car,cycling-regular",
            "-m", "5000,3000"
        };

        CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);
        CoordinateGeneratorRoute generator = cli.createGenerator();
        
        // Use reflection to access the maxDistanceByProfile field
        Field maxDistancesField = CoordinateGeneratorRoute.class.getDeclaredField("maxDistanceByProfile");
        maxDistancesField.setAccessible(true);
        Map<String, Double> maxDistances = (Map<String, Double>) maxDistancesField.get(generator);
        
        assertEquals(2, maxDistances.size());
        assertEquals(5000.0, maxDistances.get("driving-car"));
        assertEquals(3000.0, maxDistances.get("cycling-regular"));
    }

    @Test
    void testUnequalMaxDistancesAndProfiles() {
        String[] args = {
            "-n", "50",
            "-e", "8.6", "49.3", "8.7", "49.4",
            "-p", "driving-car,cycling-regular,walking",
            "-m", "5000,3000" // Fewer max distances than profiles
        };

        CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);
        CoordinateGeneratorRoute generator = cli.createGenerator();
        assertNotNull(generator);
    }

    @Test
    void testInvalidMaxDistance() {
        String[] args = {
            "-n", "50",
            "-e", "8.6", "49.3", "8.7", "49.4",
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
            "-e", "8.6", "49.3", "8.7", "49.4",
            "-p", "driving-car",
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
            "-e", "8.6", "49.3", "8.7", "49.4",
            "-p", "driving-car",
            "-t", "invalid"
        };

        CoordinateGeneratorRouteCLI cli = new CoordinateGeneratorRouteCLI(args);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                cli::createGenerator);
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Invalid number of threads"));
    }
}
