package org.heigit.ors.coordinates_generator.cli;

import org.apache.commons.cli.*;
import org.heigit.ors.benchmark.exceptions.CommandLineParsingException;
import org.heigit.ors.coordinates_generator.generators.CoordinateGeneratorSnapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnappingCommandLineParser extends CommandLineParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(SnappingCommandLineParser.class);
    private static final String OPT_MAX_ATTEMPTS = "max-attempts";

    public SnappingCommandLineParser(String[] args) {
        super(args);
    }

    @Override
    protected void setupOptions() {
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
                .hasArg()
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

        options.addOption(Option.builder("ma")
                .longOpt(OPT_MAX_ATTEMPTS)
                .hasArg()
                .type(Number.class)
                .desc("Maximum number of attempts for coordinate generation (default: 1000)")
                .build());
    }

    @Override
    protected CommandLine parseCommandLine(String[] args) {
        try {
            return new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            printHelp();
            throw new CommandLineParsingException("Failed to parse command line arguments", e);
        }
    }

    @Override
    public void printHelp() {
        new HelpFormatter().printHelp("CoordinateGeneratorSnapping", options, true);
    }

    @Override
    protected String getDefaultOutputFile() {
        return "snapped_coordinates.csv";
    }

    public CoordinateGeneratorSnapping createGenerator() {
        int numPoints = Integer.parseInt(cmd.getOptionValue("n"));
        double[] extent = parseExtent(cmd.getOptionValue("e"));
        String[] profiles = parseProfiles(cmd.getOptionValue("p"));
        double radius = Double.parseDouble(cmd.getOptionValue("r", "350"));
        String baseUrl = cmd.getOptionValue("u", "http://localhost:8080/ors");
        int maxAttempts = Integer.parseInt(
                cmd.getOptionValue(OPT_MAX_ATTEMPTS, "100"));

        LOGGER.info(
                "Creating CoordinateGeneratorSnapping with numPoints={}, extent={}, radius={}, profiles={}, baseUrl={}, maxAttempts={}",
                numPoints, extent, radius, profiles, baseUrl, maxAttempts);

        CoordinateGeneratorSnapping generator = new CoordinateGeneratorSnapping(numPoints, extent, radius, profiles,
                baseUrl, maxAttempts);
        generator.generate();
        return generator;
    }

    /**
     * Parses an extent string into an array of four doubles.
     * Supports comma-separated, space-separated, or mixed formats.
     * 
     * @param extentStr The extent string to parse
     * @return An array of four doubles representing min lon, min lat, max lon, max
     *         lat
     * @throws IllegalArgumentException if the extent cannot be parsed correctly
     */
    public double[] parseExtent(String extentStr) {
        if (extentStr == null || extentStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Extent string cannot be empty");
        }

        // Replace commas with spaces and split by whitespace
        String normalized = extentStr.replace(',', ' ').trim();
        String[] parts = normalized.split("\\s+");

        if (parts.length != 4) {
            throw new IllegalArgumentException(
                    "Extent must contain exactly 4 values (minLon, minLat, maxLon, maxLat), but found " + parts.length);
        }

        double[] extent = new double[4];
        try {
            for (int i = 0; i < 4; i++) {
                extent[i] = Double.parseDouble(parts[i]);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numerical value in extent: " + e.getMessage());
        }

        return extent;
    }
}
