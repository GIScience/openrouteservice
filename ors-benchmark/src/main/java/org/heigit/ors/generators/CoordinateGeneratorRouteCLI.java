package org.heigit.ors.generators;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.heigit.ors.exceptions.CommandLineParsingException;

public class CoordinateGeneratorRouteCLI extends AbstractCoordinateGeneratorCLI {

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

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(
                    "Creating CoordinateGeneratorRoute with numRoutes={}, extent={}, profiles={}, baseUrl={}, minDistance={}",
                    numRoutes, extent, java.util.Arrays.toString(profiles), baseUrl, minDistance);
        }

        return new CoordinateGeneratorRoute(numRoutes, extent, profiles, baseUrl, minDistance);
    }
}
