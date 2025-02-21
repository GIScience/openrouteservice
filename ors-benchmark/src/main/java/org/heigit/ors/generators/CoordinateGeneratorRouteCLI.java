package org.heigit.ors.generators;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoordinateGeneratorRouteCLI {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinateGeneratorRouteCLI.class);
    private final Options options;
    private final CommandLine cmd;

    public CoordinateGeneratorRouteCLI(String[] args) throws ParseException {
        options = new Options();
        setupOptions();
        cmd = new DefaultParser().parse(options, args);
    }

    private void setupOptions() {
        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Show help message")
                .build());

        options.addOption(Option.builder("n")
                .longOpt("num-routes")
                .hasArg()
                .required()
                .type(Number.class)
                .desc("Number of routes to generate")
                .build());

        options.addOption(Option.builder("e")
                .longOpt("extent")
                .hasArgs()
                .numberOfArgs(4)
                .required()
                .desc("Bounding box (minLon minLat maxLon maxLat)")
                .build());

        options.addOption(Option.builder("p")
                .longOpt("profile")
                .hasArg()
                .required()
                .desc("Routing profile (e.g., driving-car)")
                .build());

        options.addOption(Option.builder("u")
                .longOpt("url")
                .hasArg()
                .desc("ORS API base URL")
                .build());

        options.addOption(Option.builder("o")
                .longOpt("output")
                .hasArg()
                .desc("Output CSV file path")
                .build());

        options.addOption(Option.builder("d")
                .longOpt("min-distance")
                .hasArg()
                .type(Number.class)
                .desc("Minimum distance between coordinates in meters (default: 0)")
                .build());
    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("CoordinateGeneratorRoute", options, true);
    }

    public CoordinateGeneratorRoute createGenerator() {
        int numRoutes = Integer.parseInt(cmd.getOptionValue("n"));
        String[] extentValues = cmd.getOptionValues("e");
        double[] extent = new double[4];
        for (int i = 0; i < 4; i++) {
            extent[i] = Double.parseDouble(extentValues[i]);
        }

        String profile = cmd.getOptionValue("p");
        String baseUrl = cmd.getOptionValue("u", "http://localhost:8080/ors");
        double minDistance = Double.parseDouble(cmd.getOptionValue("d", "1"));

        LOGGER.info(
                "Creating CoordinateGeneratorRoute with numRoutes={}, extent={}, profile={}, baseUrl={}, minDistance={}",
                numRoutes, extent, profile, baseUrl, minDistance);

        return new CoordinateGeneratorRoute(numRoutes, extent, profile, baseUrl, minDistance);
    }

    public String getOutputFile() {
        return cmd.getOptionValue("o", "route_coordinates.csv");
    }

    public boolean hasHelp() {
        return cmd.hasOption("h");
    }
}
