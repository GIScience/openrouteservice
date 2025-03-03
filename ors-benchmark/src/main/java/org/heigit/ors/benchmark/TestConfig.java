package org.heigit.ors.benchmark;

import java.util.Arrays;
import java.util.List;

import org.heigit.ors.benchmark.BenchmarkEnums.DirectionsModes;
import static org.heigit.ors.benchmark.BenchmarkEnums.DirectionsModes.AVOID_HIGHWAY;
import static org.heigit.ors.benchmark.BenchmarkEnums.DirectionsModes.BASIC_FASTEST;
import org.heigit.ors.benchmark.BenchmarkEnums.TestUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestConfig {
    private static final Logger logger = LoggerFactory.getLogger(TestConfig.class);

    private final String baseUrl;
    private final String apiKey;
    private final String targetProfile;
    private final String range;
    private final String fieldLon;
    private final String fieldLat;
    private final String fieldStartLon;
    private final String fieldStartLat;
    private final String fieldEndLon;
    private final String fieldEndLat;
    private final int numConcurrentUsers;
    private final List<Integer> querySizes;
    private final boolean parallelExecution;
    private final TestUnit testUnit;
    private final List<String> sourceFiles;
    private final List<String> modes;
    private final List<Integer> ranges;

    public TestConfig() {
        this.baseUrl = getSystemProperty("base_url", "http://localhost:8082/ors");
        this.apiKey = getSystemProperty("api_key", "API KEY");
        this.targetProfile = getSystemProperty("profile", "driving-car");
        this.range = getSystemProperty("range", "300");
        this.fieldLon = getSystemProperty("field_lon", "longitude");
        this.fieldLat = getSystemProperty("field_lat", "latitude");
        this.fieldStartLon = getSystemProperty("field_start_lon", "start_longitude");
        this.fieldStartLat = getSystemProperty("field_start_lat", "start_latitude");
        this.fieldEndLon = getSystemProperty("field_end_lon", "end_longitude");
        this.fieldEndLat = getSystemProperty("field_end_lat", "end_latitude");
        this.numConcurrentUsers = Integer.parseInt(getSystemProperty("concurrent_users", "1"));
        this.querySizes = parseQuerySizes(getSystemProperty("query_sizes", "1"));
        this.parallelExecution = Boolean.parseBoolean(getSystemProperty("parallel_execution", "false"));
        this.testUnit = TestUnit.fromString(getSystemProperty("test_unit", "distance"));
        this.sourceFiles = parseCommaSeparatedStringToStrings(getSystemProperty("source_files", ""));
        this.ranges = parseCommaSeparatedStringToInts(this.range);
        this.modes = parseCommaSeparatedStringToStrings(getSystemProperty("modes", ""));
    }

    private String getSystemProperty(String key, String defaultValue) {
        String value = System.getProperty(key) != null ? System.getProperty(key) : defaultValue;
        logger.debug("Config property {} = {}", key, value);
        return value;
    }

    private List<Integer> parseQuerySizes(String querySizesStr) {
        return Arrays.stream(querySizesStr.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .sorted()
                .toList();
    }

    private List<String> parseCommaSeparatedStringToStrings(String list) {
        return Arrays.stream(list.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private List<Integer> parseCommaSeparatedStringToInts(String rangesStr) {
        return Arrays.stream(rangesStr.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();
    }

    // Getters
    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getTargetProfile() {
        return targetProfile;
    }

    public String getRange() {
        return range;
    }

    public String getFieldLon() {
        return fieldLon;
    }

    public String getFieldLat() {
        return fieldLat;
    }

    public String getFieldStartLon() {
        return fieldStartLon;
    }

    public String getFieldStartLat() {
        return fieldStartLat;
    }

    public String getFieldEndLon() {
        return fieldEndLon;
    }

    public String getFieldEndLat() {
        return fieldEndLat;
    }

    public int getNumConcurrentUsers() {
        return numConcurrentUsers;
    }

    public List<Integer> getQuerySizes() {
        return querySizes;
    }

    public boolean isParallelExecution() {
        return parallelExecution;
    }

    public TestUnit getTestUnit() {
        return testUnit;
    }

    public List<String> getSourceFiles() {
        return sourceFiles;
    }

    public List<Integer> getRanges() {
        return ranges;
    }

    public List<DirectionsModes> getDirectionsModes() {
        return modes.isEmpty() ? List.of(BASIC_FASTEST)
                : modes.stream()
                        .map(DirectionsModes::fromString)
                        .toList();
    }
}
