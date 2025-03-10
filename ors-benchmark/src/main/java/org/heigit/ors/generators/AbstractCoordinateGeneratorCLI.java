package org.heigit.ors.generators;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public abstract class AbstractCoordinateGeneratorCLI {
    protected final Options options;
    protected final CommandLine cmd;

    protected AbstractCoordinateGeneratorCLI(String[] args) {
        this.options = new Options();
        initOptions();
        this.cmd = initCommandLine(args);
    }

    protected final void initOptions() {
        setupOptions();
    }

    protected abstract void setupOptions();
    
    protected final CommandLine initCommandLine(String[] args) {
        return parseCommandLine(args);
    }

    protected abstract CommandLine parseCommandLine(String[] args);

    protected String[] parseProfiles(String profilesInput) {
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

    public abstract void printHelp();

    public boolean hasHelp() {
        return cmd == null || cmd.hasOption("h");

    }

    public String getOutputFile() {
        return cmd.getOptionValue("o", getDefaultOutputFile());
    }

    protected abstract String getDefaultOutputFile();
}
