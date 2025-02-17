package org.heigit.ors.benchmark;

import org.apache.commons.cli.*;

public class CoordinateGeneratorMatrixCLI {
    // Logging
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CoordinateGeneratorMatrixCLI.class);
    private final Options options;
    private final CommandLine cmd;

    public CoordinateGeneratorMatrixCLI(String[] args) throws ParseException {
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
                .desc("Number of coordinate pairs to generate")
                .build());

        options.addOption(Option.builder("e")
                .longOpt("extent")
                .hasArgs()
                .numberOfArgs(4)
                .required()
                .desc("Bounding box (minLon minLat maxLon maxLat)")
                .build());

        options.addOption(Option.builder("d")
                .longOpt("distance")
                .hasArgs()
                .numberOfArgs(2)
                .required()
                .desc("Distance range (min max) in meters")
                .build());

        options.addOption(Option.builder("m")
                .longOpt("max-attempts")
                .hasArg()
                .type(Number.class)
                .desc("Maximum number of attempts")
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
        formatter.printHelp("CoordinateGenerator", options, true);
    }

    public CoordinateGeneratorMatrix createGenerator() {
        int numPoints = Integer.parseInt(cmd.getOptionValue("n"));
        String[] extentValues = cmd.getOptionValues("e");
        double[] extent = new double[4];
        for (int i = 0; i < 4; i++) {
            extent[i] = Double.parseDouble(extentValues[i]);
        }
        
        String[] distanceValues = cmd.getOptionValues("d");
        double minDistance = Double.parseDouble(distanceValues[0]);
        double maxDistance = Double.parseDouble(distanceValues[1]);
        
        int maxAttempts = Integer.parseInt(cmd.getOptionValue("m", "1000"));
        String profile = cmd.getOptionValue("p");
        String baseUrl = cmd.getOptionValue("u", "http://localhost:8080/ors");
        LOGGER.info(
                "Creating CoordinateGenerator with numPoints={}, extent={}, minDistance={}, maxDistance={}, maxAttempts={}, profile={}, baseUrl={}",
                numPoints, extent, minDistance, maxDistance, maxAttempts, profile, baseUrl);
        return new CoordinateGeneratorMatrix(
                numPoints, extent, minDistance, maxDistance, maxAttempts, profile, baseUrl
        );
    }

    public String getOutputFile() {
        return cmd.getOptionValue("o", "coordinates.csv");
    }

    public boolean hasHelp() {
        return cmd.hasOption("h");
    }
}
