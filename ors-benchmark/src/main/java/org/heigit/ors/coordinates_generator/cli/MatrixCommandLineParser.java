package org.heigit.ors.coordinates_generator.cli;

import org.apache.commons.cli.*;
import org.heigit.ors.coordinates_generator.generators.CoordinateGeneratorMatrix;
import org.heigit.ors.coordinates_generator.generators.CoordinateGeneratorMatrix.MatrixDimensions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Command-line parser for matrix generation options.
 */
public class MatrixCommandLineParser extends CommandLineParser {
    private static final String OPT_NUM_MATRICES = "n";
    private static final String OPT_EXTENT = "e";
    private static final String OPT_PROFILES = "p";
    private static final String OPT_MAX_DISTANCE = "m";
    private static final String OPT_MATRIX_ROWS = "r";
    private static final String OPT_MATRIX_COLS = "c";
    private static final String OPT_OUTPUT = "o";
    private static final String OPT_THREADS = "t";
    private static final String OPT_BASE_URL = "u";

    public MatrixCommandLineParser(String[] args) {
        super(args);
    }

    @Override
    protected void setupOptions() {
        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Print this help message")
                .build());

        options.addOption(Option.builder(OPT_NUM_MATRICES)
                .longOpt("num")
                .hasArg()
                .argName("NUMBER")
                .desc("Number of matrices to generate (default: 100)")
                .build());

        options.addOption(Option.builder(OPT_EXTENT)
                .longOpt("extent")
                .hasArg()
                .argName("minLon,minLat,maxLon,maxLat")
                .desc("Geographic extent for coordinate generation (default: 8.67,49.39,8.71,49.42)")
                .build());

        options.addOption(Option.builder(OPT_PROFILES)
                .longOpt("profiles")
                .hasArg()
                .argName("PROFILE[,PROFILE...]")
                .desc("Comma-separated list of profiles (default: driving-car)")
                .build());

        options.addOption(Option.builder(OPT_MAX_DISTANCE)
                .longOpt("max-distance")
                .hasArg()
                .argName("PROFILE:DISTANCE[,PROFILE:DISTANCE...]")
                .desc("Maximum distance per profile in meters (default: driving-car:50000)")
                .build());

        options.addOption(Option.builder(OPT_MATRIX_ROWS)
                .longOpt("rows")
                .hasArg()
                .argName("NUMBER")
                .desc("Number of rows in the matrix (default: 2)")
                .build());

        options.addOption(Option.builder(OPT_MATRIX_COLS)
                .longOpt("cols")
                .hasArg()
                .argName("NUMBER")
                .desc("Number of columns in the matrix (default: 2)")
                .build());

        options.addOption(Option.builder(OPT_OUTPUT)
                .longOpt("output")
                .hasArg()
                .argName("FILE")
                .desc("Output CSV file (default: matrices.csv)")
                .build());

        options.addOption(Option.builder(OPT_THREADS)
                .longOpt("threads")
                .hasArg()
                .argName("NUMBER")
                .desc("Number of threads for coordinate generation (default: available processors)")
                .build());

        options.addOption(Option.builder(OPT_BASE_URL)
                .longOpt("base-url")
                .hasArg()
                .argName("URL")
                .desc("Base URL for ORS API (default: http://localhost:8080/ors)")
                .build());
    }

    @Override
    protected CommandLine parseCommandLine(String[] args) {
        try {
            return new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Error parsing command line arguments: " + e.getMessage(), e);
        }
    }

    @Override
    public void printHelp() {
        String header = "Generate coordinates for matrix API testing";
        String footer = "\nExample: java -jar matrix-generator.jar -n 100 -e 8.67,49.39,8.71,49.42 -p driving-car -m driving-car:10000 -r 3 -c 3 -o matrices.csv";

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("matrix-generator", header, options, footer, true);
    }

    @Override
    protected String getDefaultOutputFile() {
        return "matrices.csv";
    }

    public CoordinateGeneratorMatrix createGenerator() {
        int numMatrices = Integer.parseInt(cmd.getOptionValue(OPT_NUM_MATRICES, "100"));
        double[] extent = parseExtent(cmd.getOptionValue(OPT_EXTENT, "8.67,49.39,8.71,49.42"));
        String[] profiles = parseProfiles(cmd.getOptionValue(OPT_PROFILES, "driving-car"));
        String baseUrl = cmd.getOptionValue(OPT_BASE_URL, "http://localhost:8080/ors");
        Map<String, Double> maxDistanceByProfile = parseMaxDistanceMap(cmd.getOptionValue(OPT_MAX_DISTANCE, ""), profiles);
        
        int rows = Integer.parseInt(cmd.getOptionValue(OPT_MATRIX_ROWS, "2"));
        int cols = Integer.parseInt(cmd.getOptionValue(OPT_MATRIX_COLS, "2"));
        MatrixDimensions dimensions = new MatrixDimensions(rows, cols);
        
        int numThreads = Integer.parseInt(cmd.getOptionValue(OPT_THREADS, 
            String.valueOf(Runtime.getRuntime().availableProcessors())));

        return new CoordinateGeneratorMatrix(
                numMatrices, 
                extent, 
                profiles, 
                baseUrl,
                maxDistanceByProfile, 
                dimensions,
                numThreads);
    }

    private double[] parseExtent(String extentStr) {
        String[] parts = extentStr.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Extent must be in format minLon,minLat,maxLon,maxLat");
        }
        return Arrays.stream(parts).mapToDouble(Double::parseDouble).toArray();
    }

    private Map<String, Double> parseMaxDistanceMap(String maxDistanceStr, String[] profiles) {
        Map<String, Double> maxDistanceByProfile = new HashMap<>();
        
        // Set default values
        for (String profile : profiles) {
            maxDistanceByProfile.put(profile, 50000.0); // Default 50km
        }
        
        if (maxDistanceStr != null && !maxDistanceStr.isEmpty()) {
            String[] entries = maxDistanceStr.split(",");
            for (String entry : entries) {
                String[] parts = entry.split(":");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Max distance must be in format PROFILE:DISTANCE");
                }
                maxDistanceByProfile.put(parts[0], Double.parseDouble(parts[1]));
            }
        }
        
        return maxDistanceByProfile;
    }
}