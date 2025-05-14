package org.heigit.ors.coordinates_generator.cli;

import org.apache.commons.cli.*;
import org.heigit.ors.benchmark.exceptions.CommandLineParsingException;
import org.heigit.ors.coordinates_generator.generators.CoordinateGeneratorRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class RouteCommandLineParser extends CommandLineParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(RouteCommandLineParser.class);

    private static final String OPT_THREADS = "threads";
    private static final String OPT_SNAP_RADIUS = "snap-radius";
    private static final int DEFAULT_THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    public RouteCommandLineParser(String[] args) {
        super(args);
    }

    @Override
    protected void setupOptions() {
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
                .hasArg()
                .required()
                .desc("Bounding box (minLon,minLat,maxLon,maxLat) or (minLon minLat maxLon maxLat)")
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

        options.addOption(Option.builder("d")
                .longOpt("min-distance")
                .hasArg()
                .type(Number.class)
                .desc("Minimum distance between coordinates in meters (default: 0)")
                .build());

        options.addOption(Option.builder("m")
                .longOpt("max-distances")
                .hasArg()
                .desc("Maximum distances between coordinates in meters, comma-separated in profile order (e.g., 5000,3000)")
                .build());

        options.addOption(Option.builder("t")
                .longOpt(OPT_THREADS)
                .hasArg()
                .desc("Number of threads to use (default: " + DEFAULT_THREAD_COUNT + ")")
                .build());

        options.addOption(Option.builder("sr")
                .longOpt(OPT_SNAP_RADIUS)
                .hasArg()
                .type(Number.class)
                .desc("Search radius in meters for coordinate snapping (default: 1000)")
                .build());
    }

    @Override
    protected CommandLine parseCommandLine(String[] args) {
        try {
            return new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            // If help is within the arguments, print help message
            if (args != null && args.length == 1 && (args[0].equals("-h") || args[0].equals("--help"))) {
                printHelp();
            } else {
                printHelp();
                throw new CommandLineParsingException("Failed to parse command line arguments", e);
            }
            return null;
        }
    }

    @Override
    public void printHelp() {
        new HelpFormatter().printHelp("CoordinateGeneratorRoute", options, true);
    }

    @Override
    protected String getDefaultOutputFile() {
        return "route_coordinates.csv";
    }

    public CoordinateGeneratorRoute createGenerator() {
        int numRoutes = Integer.parseInt(cmd.getOptionValue("n"));

        // Use the new extent parser
        double[] extent = parseExtent(cmd.getOptionValue("e"));

        String[] profiles = parseProfiles(cmd.getOptionValue("p"));
        String baseUrl = cmd.getOptionValue("u", "http://localhost:8080/ors");
        double minDistance = Double.parseDouble(cmd.getOptionValue("d", "1"));
        double snapRadius = Double.parseDouble(cmd.getOptionValue(OPT_SNAP_RADIUS, "1000"));

        // Parse the max distances if provided
        Map<String, Double> maxDistanceByProfile = parseMaxDistances(cmd.getOptionValue("m"), profiles);

        int numThreads = parseNumThreads(cmd.getOptionValue(OPT_THREADS, String.valueOf(DEFAULT_THREAD_COUNT)));

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(
                    "Creating CoordinateGeneratorRoute with numRoutes={}, extent={}, profiles={}, baseUrl={}, minDistance={}, maxDistances={}, numThreads={}, snapRadius={}",
                    numRoutes, extent, java.util.Arrays.toString(profiles), baseUrl, minDistance, maxDistanceByProfile,
                    numThreads, snapRadius);
        }

        return new CoordinateGeneratorRoute(numRoutes, extent, profiles, baseUrl, minDistance, maxDistanceByProfile,
                numThreads, snapRadius);
    }

    /**
     * Parse extent from string input, supporting both comma-separated and
     * space-separated formats
     * 
     * @param extentStr String containing the extent coordinates
     * @return Array of 4 doubles representing the extent [minLon, minLat, maxLon,
     *         maxLat]
     * @throws IllegalArgumentException if the extent cannot be parsed correctly
     */
    protected double[] parseExtent(String extentStr) {
        if (extentStr == null || extentStr.isBlank()) {
            throw new IllegalArgumentException("Extent must not be empty");
        }

        // Replace commas with spaces and split by any whitespace
        String[] parts = extentStr.replace(',', ' ').trim().split("\\s+");

        if (parts.length != 4) {
            throw new IllegalArgumentException(
                    String.format("Extent must contain exactly 4 coordinates, found %d", parts.length));
        }

        double[] extent = new double[4];
        for (int i = 0; i < 4; i++) {
            try {
                extent[i] = Double.parseDouble(parts[i]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        String.format("Invalid coordinate value at position %d: %s", i, parts[i]));
            }
        }

        return extent;
    }

    /**
     * Parse max distances from comma-separated string and match them to profiles
     * 
     * @param maxDistancesStr Comma-separated string of maximum distances
     * @param profiles        Array of profile names
     * @return Map of profile to maximum distance
     * @throws IllegalArgumentException if the number of max distances doesn't match
     *                                  the number of profiles
     */
    private Map<String, Double> parseMaxDistances(String maxDistancesStr, String[] profiles) {
        Map<String, Double> maxDistanceByProfile = new HashMap<>();

        if (maxDistancesStr != null && !maxDistancesStr.isBlank()) {
            String[] maxDistances = maxDistancesStr.split(",");
            if (maxDistances == null) {
                throw new IllegalArgumentException("Failed to split max distances string");
            }

            // Validate that counts match
            if (maxDistances.length != profiles.length) {
                throw new IllegalArgumentException(String.format(
                        "Number of max distances (%d) must match number of profiles (%d)",
                        maxDistances.length, profiles.length));
            }

            // Assign max distances to profiles in order
            for (int i = 0; i < profiles.length; i++) {
                try {
                    double maxDistance = Double.parseDouble(maxDistances[i].trim());
                    maxDistanceByProfile.put(profiles[i], maxDistance);
                } catch (NumberFormatException e) {
                    LOGGER.warn("Invalid max distance for profile {}: {}", profiles[i], maxDistances[i]);
                }
            }
        } else {
            throw new IllegalArgumentException("Max distances must be provided");
        }

        return maxDistanceByProfile;
    }

    private int parseNumThreads(String numThreads) {
        try {
            return Integer.parseInt(numThreads);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number of threads: " + e.getMessage());
        }
    }
}
