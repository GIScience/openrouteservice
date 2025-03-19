package org.heigit.ors;

import org.heigit.ors.cli.SnappingCommandLineParser;
import org.heigit.ors.generators.CoordinateGeneratorSnapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CoordinateGeneratorSnappingApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinateGeneratorSnappingApp.class);

    public static void main(String[] args) {
        try {
            SnappingCommandLineParser cli = new SnappingCommandLineParser(args);

            if (cli.hasHelp()) {
                cli.printHelp();
                return;
            }

            LOGGER.info("Creating coordinate generator for snapping...");
            CoordinateGeneratorSnapping generator = cli.createGenerator();

            LOGGER.info("Generating and snapping {} points...", generator.getNumPoints());
            generator.generate();

            LOGGER.info("\n");
            LOGGER.info("Writing {} snapped points to {}", generator.getResult().size(), cli.getOutputFile());
            generator.writeToCSV(cli.getOutputFile());

            LOGGER.info("Successfully snapped {} coordinate{}",
                    generator.getResult().size(),
                    generator.getResult().size() != 1 ? "s" : "");
            LOGGER.info("Results written to: {}", cli.getOutputFile());

        } catch (NumberFormatException e) {
            LOGGER.error("Error parsing numeric arguments: {}", e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            LOGGER.error("Error writing to output file: {}", e.getMessage());
            System.exit(1);
        }
        System.exit(0);
    }
}
