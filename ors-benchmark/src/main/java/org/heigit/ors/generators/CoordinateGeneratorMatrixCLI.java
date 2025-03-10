package org.heigit.ors.generators;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.heigit.ors.exceptions.CommandLineParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoordinateGeneratorMatrixCLI extends AbstractCoordinateGeneratorCLI {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinateGeneratorMatrixCLI.class);

    public CoordinateGeneratorMatrixCLI(String[] args) {
        super(args);
    }

    @Override
    protected void setupOptions() {
        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Show help message")
                .build());

        options.addOption(Option.builder("n")
                .longOpt("num-lists")
                .hasArg()
                .required()
                .type(Number.class)
                .desc("Number of coordinate lists to generate per profile")
                .build());

        options.addOption(Option.builder("p")
                .longOpt("points-per-list")
                .hasArg()
                .type(Number.class)
                .desc("Number of points in each coordinate list (default: 3)")
                .build());

        options.addOption(Option.builder("e")
                .longOpt("extent")
                .hasArg()
                .required()
                .desc("Bounding box (minLon,minLat,maxLon,maxLat) or (minLon minLat maxLon maxLat)")
                .build());

        options.addOption(Option.builder("r")
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
                .desc("Output file path")
                .build());

        options.addOption(Option.builder("m")
                .longOpt("max-distances")
                .hasArg()
                .required()
                .desc("Maximum distances from center point in meters, comma-separated in profile order (e.g., 5000,3000)")
                .build());
        
        options.addOption(Option.builder("t")
                .longOpt("threads")
                .hasArg()
                .type(Number.class)
                .desc("Number of threads to use for generation (default: available processors)")
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
        new HelpFormatter().printHelp("CoordinateListGenerator", options, true);
    }

    @Override
    protected String getDefaultOutputFile() {
        return "coordinate_lists.csv";
    }

    public CoordinateGeneratorMatrix createGenerator() {
        int numLists = Integer.parseInt(cmd.getOptionValue("n"));
        int pointsPerList = Integer.parseInt(cmd.getOptionValue("p", "3"));
        double[] extent = parseExtent(cmd.getOptionValue("e"));
        String[] profiles = parseProfiles(cmd.getOptionValue("r"));
        String baseUrl = cmd.getOptionValue("u", "http://localhost:8080/ors");
        Map<String, Double> maxDistanceByProfile = parseMaxDistances(cmd.getOptionValue("m"), profiles);
        int numThreads = cmd.hasOption("t") ? Integer.parseInt(cmd.getOptionValue("t")) 
                                           : Runtime.getRuntime().availableProcessors();
        
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(
                    "Creating CoordinateListGenerator with numLists={}, pointsPerList={}, extent={}, profiles={}, baseUrl={}, maxDistances={}, threads={}",
                    numLists, pointsPerList, java.util.Arrays.toString(extent), java.util.Arrays.toString(profiles), 
                    baseUrl, maxDistanceByProfile, numThreads);
        }
        
        return new CoordinateGeneratorMatrix(numLists, pointsPerList, extent, profiles, baseUrl, maxDistanceByProfile, numThreads);
    }

    /**
     * Parse extent from string input, supporting both comma-separated and
     * space-separated formats
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

    public static void main(String[] args) {
        try {
            CoordinateGeneratorMatrixCLI cli = new CoordinateGeneratorMatrixCLI(args);

            if (cli.hasHelp()) {
                cli.printHelp();
                return;
            }

            LOGGER.info("Creating coordinate list generator...");
            CoordinateGeneratorMatrix generator = cli.createGenerator();

            LOGGER.info("Generating coordinate lists...");
            generator.generate();

            List<CoordinateGeneratorMatrix.CoordinateList> result = generator.getResult();
            LOGGER.info("Writing {} coordinate lists to {}", result.size(), cli.getOutputFile());
            generator.writeToCSV(cli.getOutputFile());

            LOGGER.info("Successfully generated {} coordinate list{}",
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