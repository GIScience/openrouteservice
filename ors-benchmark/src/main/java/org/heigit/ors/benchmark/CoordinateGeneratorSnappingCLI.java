package org.heigit.ors.benchmark;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoordinateGeneratorSnappingCLI {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinateGeneratorSnappingCLI.class);
    private final Options options;
    private final CommandLine cmd;

    public CoordinateGeneratorSnappingCLI(String[] args) throws ParseException {
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
                .longOpt("num-points")
                .hasArg()
                .required()
                .type(Number.class)
                .desc("Number of points to generate")
                .build());

        options.addOption(Option.builder("e")
                .longOpt("extent")
                .hasArgs()
                .numberOfArgs(4)
                .required()
                .desc("Bounding box (minLon minLat maxLon maxLat)")
                .build());

        options.addOption(Option.builder("r")
                .longOpt("radius")
                .hasArg()
                .type(Number.class)
                .desc("Search radius in meters (default: 350)")
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
    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("CoordinateGeneratorSnapping", options, true);
    }

    public CoordinateGeneratorSnapping createGenerator() {
        int numPoints = Integer.parseInt(cmd.getOptionValue("n"));
        String[] extentValues = cmd.getOptionValues("e");
        double[] extent = new double[4];
        for (int i = 0; i < 4; i++) {
            extent[i] = Double.parseDouble(extentValues[i]);
        }

        double radius = Double.parseDouble(cmd.getOptionValue("r", "350"));
        String profile = cmd.getOptionValue("p");
        String baseUrl = cmd.getOptionValue("u", "http://localhost:8080/ors");

        LOGGER.info(
                "Creating CoordinateGeneratorSnapping with numPoints={}, extent={}, radius={}, profile={}, baseUrl={}",
                numPoints, extent, radius, profile, baseUrl);

        return new CoordinateGeneratorSnapping(numPoints, extent, radius, profile, baseUrl);
    }

    public String getOutputFile() {
        return cmd.getOptionValue("o", "snapped_coordinates.csv");
    }

    public boolean hasHelp() {
        return cmd.hasOption("h");
    }
}
