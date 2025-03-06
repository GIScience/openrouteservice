package org.heigit.ors.generators;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.heigit.ors.exceptions.CommandLineParsingException;

public class CoordinateGeneratorRouteCLI extends AbstractCoordinateGeneratorCLI {

    private static final String OPT_THREADS = "threads";
    private static final int DEFAULT_THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    public CoordinateGeneratorRouteCLI(String[] args) {
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
                .hasArgs()
                .numberOfArgs(4)
                .required()
                .desc("Bounding box (minLon minLat maxLon maxLat)")
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
        String[] extentValues = cmd.getOptionValues("e");
        double[] extent = new double[4];
        for (int i = 0; i < 4; i++) {
            extent[i] = Double.parseDouble(extentValues[i]);
        }

        String[] profiles = parseProfiles(cmd.getOptionValue("p"));
        String baseUrl = cmd.getOptionValue("u", "http://localhost:8080/ors");
        double minDistance = Double.parseDouble(cmd.getOptionValue("d", "1"));

        // Parse the max distances if provided
        Map<String, Double> maxDistanceByProfile = parseMaxDistances(cmd.getOptionValue("m"), profiles);

        int numThreads = parseNumThreads(cmd.getOptionValue(OPT_THREADS, String.valueOf(DEFAULT_THREAD_COUNT)));

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(
                    "Creating CoordinateGeneratorRoute with numRoutes={}, extent={}, profiles={}, baseUrl={}, minDistance={}, maxDistances={}, numThreads={}",
                    numRoutes, extent, java.util.Arrays.toString(profiles), baseUrl, minDistance, maxDistanceByProfile,
                    numThreads);
        }

        return new CoordinateGeneratorRoute(numRoutes, extent, profiles, baseUrl, minDistance, maxDistanceByProfile,
                numThreads);
    }

    /**
     * Parse max distances from comma-separated string and match them to profiles
     * 
     * @param maxDistancesStr Comma-separated string of maximum distances
     * @param profiles        Array of profile names
     * @return Map of profile to maximum distance
     */
    private Map<String, Double> parseMaxDistances(String maxDistancesStr, String[] profiles) {
        Map<String, Double> maxDistanceByProfile = new HashMap<>();

        if (maxDistancesStr != null && !maxDistancesStr.isBlank()) {
            String[] maxDistances = maxDistancesStr.split(",");

            // Assign max distances to profiles in order
            for (int i = 0; i < Math.min(profiles.length, maxDistances.length); i++) {
                try {
                    double maxDistance = Double.parseDouble(maxDistances[i].trim());
                    maxDistanceByProfile.put(profiles[i], maxDistance);
                } catch (NumberFormatException e) {
                    LOGGER.warn("Invalid max distance for profile {}: {}", profiles[i], maxDistances[i]);
                }
            }

            // Log warning if counts don't match
            if (maxDistances.length < profiles.length) {
                LOGGER.warn(
                        "Fewer max distances ({}) provided than profiles ({}). Some profiles will have no max distance.",
                        maxDistances.length, profiles.length);
            } else if (maxDistances.length > profiles.length) {
                LOGGER.warn("More max distances ({}) provided than profiles ({}). Excess values will be ignored.",
                        maxDistances.length, profiles.length);
            }
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
