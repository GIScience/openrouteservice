package org.heigit.ors.coordinates_generator.cli;

import org.heigit.ors.benchmark.exceptions.CommandLineParsingException;
import org.heigit.ors.coordinates_generator.generators.CoordinateGeneratorMatrix;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MatrixCommandLineParserTest {

        @Test
        void testValidCliArguments() {
                String[] args = {
                                "-n", "50",
                                "-e", "8.6, 49.3, 8.7, 49.4",
                                "-p", "driving-car,cycling-regular",
                                "-u", "http://localhost:8080/ors",
                                "-m", "driving-car:5000,cycling-regular:3000",
                                "-r", "3",
                                "-c", "3",
                                "-t", "4"
                };

                MatrixCommandLineParser cli = new MatrixCommandLineParser(args);
                CoordinateGeneratorMatrix generator = cli.createGenerator();

                assertNotNull(generator);
                assertEquals("matrices.csv", cli.getOutputFile());
                assertFalse(cli.hasHelp());
        }

        @Test
        void testCustomOutputFile() {
                String[] args = {
                                "-n", "50",
                                "-e", "8.6, 49.3, 8.7, 49.4",
                                "-p", "driving-car",
                                "-m", "driving-car:5000",
                                "-o", "custom_matrices.csv"
                };

                MatrixCommandLineParser cli = new MatrixCommandLineParser(args);
                assertEquals("custom_matrices.csv", cli.getOutputFile());
        }

        @ParameterizedTest
        @CsvSource({
                        "driving-car,driving-car:5000",
                        "'driving-car,cycling-regular','driving-car:5000,cycling-regular:3000'",
                        "'driving-car,cycling-regular,foot-walking','driving-car:5000,cycling-regular:3000,foot-walking:1000'"
        })
        void testProfileParsing(String profileInput, String maxDistancesInput) {
                String[] args = {
                                "-n", "50",
                                "-e", "8.6, 49.3, 8.7, 49.4",
                                "-m", maxDistancesInput,
                                "-p", profileInput
                };

                MatrixCommandLineParser cli = new MatrixCommandLineParser(args);
                CoordinateGeneratorMatrix generator = cli.createGenerator();
                assertNotNull(generator);
        }

        @Test
        void testHelpFlag() {
                String[] args = { "-h" };
                MatrixCommandLineParser cli = new MatrixCommandLineParser(args);
                assertTrue(cli.hasHelp());
        }

        @Test
        void testMissingRequiredArgument() {
                String[] args = {
                                "-n", "50",
                                "-e", "8.6, 49.3, 8.7, 49.4"
                                // Missing required -p argument
                };

                CommandLineParsingException exception = assertThrows(
                                CommandLineParsingException.class, () -> new MatrixCommandLineParser(args));
                assertNotNull(exception);
        }

        @Test
        void testInvalidNumberFormat() {
                String[] args = {
                                "-n", "invalid",
                                "-e", "8.6, 49.3, 8.7, 49.4",
                                "-p", "driving-car",
                                "-m", "driving-car:5000"
                };

                MatrixCommandLineParser cli = new MatrixCommandLineParser(args);
                NumberFormatException exception = assertThrows(NumberFormatException.class, cli::createGenerator);
                assertNotNull(exception);
        }

        @Test
        void testInvalidExtentValues() {
                String[] args = {
                                "-n", "50",
                                "-e", "8.6, 49.3, 8.7, ", // Missing one extent value
                                "-p", "driving-car",
                                "-m", "driving-car:5000"
                };

                MatrixCommandLineParser cli = new MatrixCommandLineParser(args);

                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class, cli::createGenerator);
                assertNotNull(exception);
        }

        @Test
        void testEmptyProfileList() {
                String[] args = {
                                "-n", "50",
                                "-e", "8.6, 49.3, 8.7, 49.4",
                                "-p", ""
                };
                MatrixCommandLineParser cli = new MatrixCommandLineParser(args);
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                cli::createGenerator);
                assertNotNull(exception);
        }

        @ParameterizedTest
        @ValueSource(strings = { "2", "3", "4", "5" })
        void testMatrixDimensionsParsing(String dimension) {
                String[] args = {
                                "-n", "50",
                                "-e", "8.6, 49.3, 8.7, 49.4",
                                "-p", "driving-car",
                                "-m", "driving-car:5000",
                                "-r", dimension,
                                "-c", dimension
                };

                MatrixCommandLineParser cli = new MatrixCommandLineParser(args);
                CoordinateGeneratorMatrix generator = cli.createGenerator();
                assertNotNull(generator);

                // Test row and column dimensions using reflection
                assertDoesNotThrow(() -> {
                        Field rowsField = CoordinateGeneratorMatrix.class.getDeclaredField("numRows");
                        rowsField.setAccessible(true);
                        assertEquals(Integer.parseInt(dimension), rowsField.getInt(generator));

                        Field colsField = CoordinateGeneratorMatrix.class.getDeclaredField("numCols");
                        colsField.setAccessible(true);
                        assertEquals(Integer.parseInt(dimension), colsField.getInt(generator));
                });
        }

        @SuppressWarnings("unchecked")
        @ParameterizedTest
        @CsvSource({
                        "driving-car:5000,cycling-regular:3000",
                        "driving-car:10000,cycling-regular:5000",
                        "driving-car:20000,cycling-regular:10000"
        })
        void testMaxDistancesParsing(String maxDistances) {
                String[] args = {
                                "-n", "50",
                                "-e", "8.6, 49.3, 8.7, 49.4",
                                "-p", "driving-car,cycling-regular",
                                "-m", maxDistances
                };

                MatrixCommandLineParser cli = new MatrixCommandLineParser(args);
                CoordinateGeneratorMatrix generator = cli.createGenerator();

                // Use reflection to access the maxDistanceByProfile field
                assertDoesNotThrow(() -> {
                        Field maxDistanceByProfileField = CoordinateGeneratorMatrix.class
                                        .getDeclaredField("maxDistanceByProfile");
                        maxDistanceByProfileField.setAccessible(true);
                        Map<String, Double> maxDistanceByProfile = (Map<String, Double>) maxDistanceByProfileField
                                        .get(generator);
                        assertNotNull(maxDistanceByProfile);

                        String[] entries = maxDistances.split(",");
                        for (String entry : entries) {
                                String[] parts = entry.split(":");
                                assertEquals(Double.valueOf(parts[1]), maxDistanceByProfile.get(parts[0]));
                        }
                });
        }

        @ParameterizedTest
        @ValueSource(strings = {
                        "driving-car:5000,cycling-regular:3000,foot-walking:1000", // More distances than profiles
                        "driving-car:5000" // Fewer distances than profiles (would pass in this impl, as defaults are
                                           // used)
        })
        void testMismatchedMaxDistancesAndProfiles(String maxDistances) {
                String[] args = {
                                "-n", "50",
                                "-e", "8.6, 49.3, 8.7, 49.4",
                                "-p", "driving-car,cycling-regular", // 2 profiles
                                "-m", maxDistances
                };

                MatrixCommandLineParser cli = new MatrixCommandLineParser(args);

                // Unlike RouteCommandLineParser, the maxDistances don't have to match profiles
                // exactly
                // since the implementation maps by profile name
                CoordinateGeneratorMatrix generator = cli.createGenerator();
                assertNotNull(generator);
        }

        @Test
        void testInvalidMaxDistance() {
                String[] args = {
                                "-n", "50",
                                "-e", "8.6, 49.3, 8.7, 49.4",
                                "-p", "driving-car",
                                "-m", "driving-car:invalid"
                };

                MatrixCommandLineParser cli = new MatrixCommandLineParser(args);
                NumberFormatException exception = assertThrows(NumberFormatException.class,
                                cli::createGenerator);
                assertNotNull(exception);
        }

        @Test
        void testThreadCount() {
                String[] args = {
                                "-n", "50",
                                "-e", "8.6, 49.3, 8.7, 49.4",
                                "-p", "driving-car",
                                "-m", "driving-car:5000",
                                "-t", "8"
                };

                MatrixCommandLineParser cli = new MatrixCommandLineParser(args);
                CoordinateGeneratorMatrix generator = cli.createGenerator();
                assertNotNull(generator);

                // Test thread count using reflection
                assertDoesNotThrow(() -> {
                        Field threadCountField = CoordinateGeneratorMatrix.class.getDeclaredField("numThreads");
                        threadCountField.setAccessible(true);
                        assertEquals(8, threadCountField.getInt(generator));
                });
        }

        @Test
        void testInvalidThreadCount() {
                String[] args = {
                                "-n", "50",
                                "-e", "8.6, 49.3, 8.7, 49.4",
                                "-p", "driving-car",
                                "-m", "driving-car:5000",
                                "-t", "invalid"
                };

                MatrixCommandLineParser cli = new MatrixCommandLineParser(args);
                NumberFormatException exception = assertThrows(NumberFormatException.class,
                                cli::createGenerator);
                assertNotNull(exception);
        }

        @ParameterizedTest
        @CsvSource({
                        // Format: extent string, expected values
                        "'8.6, 49.3, 8.7, 49.4', 8.6, 49.3, 8.7, 49.4"
        })
        void testExtentParsing(String extentInput, double minLon, double minLat, double maxLon, double maxLat) {
                // Instead of using reflection to test the private parseExtent method,
                // test the behavior through the public createGenerator method
                String[] args = {
                                "-n", "50",
                                "-e", extentInput,
                                "-p", "driving-car",
                                "-m", "driving-car:5000"
                };

                MatrixCommandLineParser cli = new MatrixCommandLineParser(args);
                CoordinateGeneratorMatrix generator = cli.createGenerator();

                // Verify the generator was created successfully with correct extent values
                // by examining the generator's internal state via reflection
                assertDoesNotThrow(() -> {
                        Field extentField = CoordinateGeneratorMatrix.class.getSuperclass().getDeclaredField("extent");
                        extentField.setAccessible(true);
                        double[] extent = (double[]) extentField.get(generator);

                        assertArrayEquals(
                                        new double[] { minLon, minLat, maxLon, maxLat },
                                        extent,
                                        0.001,
                                        "Extent should be parsed correctly regardless of format");
                });
        }

        @ParameterizedTest
        @ValueSource(strings = {
                        "8.6,49.3,8.7", // Too few values
                        "8.6,invalid,8.7,49.4", // Non-numeric value
        })
        void testInvalidExtentParsing(String extentInput) {
                // Test through the public createGenerator method
                String[] args = {
                                "-n", "50",
                                "-e", extentInput,
                                "-p", "driving-car",
                                "-m", "driving-car:5000"
                };

                MatrixCommandLineParser cli = new MatrixCommandLineParser(args);

                // The createGenerator method should throw an IllegalArgumentException
                // when given an invalid extent
                Exception exception = assertThrows(
                                Exception.class,
                                cli::createGenerator,
                                "Should throw exception for invalid extent format");

                // It could be directly an IllegalArgumentException or nested in a different
                // exception
                assertTrue(exception instanceof IllegalArgumentException ||
                                (exception.getCause() != null
                                                && exception.getCause() instanceof IllegalArgumentException));
        }

        @Test
        void testFlexibleExtentCommandLine() {
                String[] args = {
                                "-n", "50",
                                "-e", "8.6,49.3,8.7,49.4", // comma-separated extent
                                "-p", "driving-car",
                                "-m", "driving-car:5000"
                };

                MatrixCommandLineParser cli = new MatrixCommandLineParser(args);
                CoordinateGeneratorMatrix generator = cli.createGenerator();
                assertNotNull(generator);
        }

        @Test
        void testSpaceExtentCommandLine() {
                String[] args = {
                                "-n", "50",
                                "-e", "8.6, 49.3, 8.7, 49.4", // space-separated extent
                                "-p", "driving-car",
                                "-m", "driving-car:5000"
                };

                MatrixCommandLineParser cli = new MatrixCommandLineParser(args);
                CoordinateGeneratorMatrix generator = cli.createGenerator();
                assertNotNull(generator);
        }
}