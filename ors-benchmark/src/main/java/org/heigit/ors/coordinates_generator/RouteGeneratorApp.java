package org.heigit.ors.coordinates_generator;

import org.heigit.ors.coordinates_generator.cli.RouteCommandLineParser;
import org.heigit.ors.coordinates_generator.generators.CoordinateGeneratorRoute;
import org.heigit.ors.coordinates_generator.model.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class RouteGeneratorApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(RouteGeneratorApp.class);

    public static void main(String[] args) {
        try {
            RouteCommandLineParser cli = new RouteCommandLineParser(args);

            if (cli.hasHelp()) {
                cli.printHelp();
                return;
            }

            LOGGER.info("Creating coordinate generator for routes...");
            CoordinateGeneratorRoute generator = cli.createGenerator();

            LOGGER.info("Generating {} routes...", generator.getNumRoutes());
            generator.generate();
            LOGGER.info("\n");

            List<Route> result = generator.getResult();
            LOGGER.info("Writing {} routes to {}", result.size(), cli.getOutputFile());
            generator.writeToCSV(cli.getOutputFile());

            LOGGER.info("\n");
            LOGGER.info("Successfully generated {} route{}",
                    result.size(),
                    result.size() != 1 ? "s" : "");
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
