package org.heigit.ors.generators;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoordinateGeneratorSnappingCLI {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinateGeneratorSnappingCLI.class);
    private final Options options;
    private final CommandLine cmd;

    public CoordinateGeneratorSnappingCLI(String[] args) throws ParseException {
        options = new Options();
        setupOptions();
        try {
            cmd = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            LOGGER.error("Error parsing command line arguments: {}", e.getMessage());
            printHelp();
            throw e;
        }

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
                .longOpt("profiles")
                .hasArg()
                .required()
                .desc("Comma-separated routing profiles (e.g., driving-car,cycling-regular)")
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

    private String[] parseProfiles(String profilesInput) {
        if (profilesInput == null || profilesInput.isBlank()) {
            throw new IllegalArgumentException("Profiles must not be empty");
        }

        // Split by both comma and space to support both formats
        String[] profiles = profilesInput.split("[,\\s]+");

        // Remove empty entries and trim whitespace
        return java.util.Arrays.stream(profiles)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    public final void printHelp() {
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
        String[] profiles = parseProfiles(cmd.getOptionValue("p"));
        String baseUrl = cmd.getOptionValue("u", "http://localhost:8080/ors");

        LOGGER.info(
                "Creating CoordinateGeneratorSnapping with numPoints={}, extent={}, radius={}, profiles={}, baseUrl={}",
                numPoints, extent, radius, profiles, baseUrl);

        return new CoordinateGeneratorSnapping(numPoints, extent, radius, profiles, baseUrl);
    }

    public String getOutputFile() {
        return cmd.getOptionValue("o", "snapped_coordinates.csv");
    }

    public boolean hasHelp() {
        boolean hasHelpOption = cmd.hasOption("h");
        LOGGER.debug("hasHelp() called, returning: {}", hasHelpOption);
        return hasHelpOption;
    }
}
