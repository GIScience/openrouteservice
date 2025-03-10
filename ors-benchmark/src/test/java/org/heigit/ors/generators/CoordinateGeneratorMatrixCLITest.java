package org.heigit.ors.generators;

import org.heigit.ors.exceptions.CommandLineParsingException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Simple test class for debugging the CoordinateGeneratorMatrixCLI
 */
class CoordinateGeneratorMatrixCLITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinateGeneratorMatrixCLITest.class);

    @Test
    void testBasicCoordinateGeneration() throws CommandLineParsingException, IOException {
        // Sample command line arguments
        String[] args = {
                "-n", "2",                         // Generate 2 coordinate lists per profile
                "-p", "3",                         // 3 points per list
                "-e", "8.67,49.39,8.72,49.42",     // Area around Heidelberg
                "-r", "driving-car,cycling-regular", // Two profiles
                "-m", "2000,1000",                 // Max distances: 2km for driving, 1km for cycling
                "-u", "http://localhost:9082/ors", // Local ORS instance
                "-o", "target/test-output.csv"     // Output to target directory
        };

        LOGGER.info("Starting test with arguments: {}", String.join(" ", args));

        try {
            // Create and parse CLI
            CoordinateGeneratorMatrixCLI cli = new CoordinateGeneratorMatrixCLI(args);
            
            // Create generator
            LOGGER.info("Creating coordinate generator...");
            CoordinateGeneratorMatrix generator = cli.createGenerator();
            
            // Run with limited generation (just one attempt for quick test)
            LOGGER.info("Starting coordinate generation...");
            generator.generate(1);
            
            // Get results
            List<CoordinateGeneratorMatrix.CoordinateList> results = generator.getResult();
            
            // Debug output
            LOGGER.info("Generated {} coordinate lists", results.size());
            for (CoordinateGeneratorMatrix.CoordinateList list : results) {
                LOGGER.info("Profile: {}, Points: {}", list.getProfile(), list.size());
                for (double[] coord : list.getCoordinates()) {
                    LOGGER.info("  Point: {}, {}", coord[0], coord[1]);
                }
            }
            
            // Write output
            generator.writeToCSV(cli.getOutputFile());
            LOGGER.info("Results written to: {}", cli.getOutputFile());
            
        } catch (Exception e) {
            LOGGER.error("Test failed with exception:", e);
            throw e;
        }
    }
}
