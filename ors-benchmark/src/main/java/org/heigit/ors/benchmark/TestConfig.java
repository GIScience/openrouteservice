package org.heigit.ors.benchmark;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestConfig {
    public enum TestUnit {
        DISTANCE,
        TIME;

        public static TestUnit fromString(String value) {
            return switch (value.toLowerCase()) {
                case "distance" -> DISTANCE;
                case "time" -> TIME;
                default -> throw new IllegalArgumentException("Invalid test unit: " + value);
            };
        }
    }

    public static final int BATCH_SIZE_UPTO = 5;

    private static final Logger logger = LoggerFactory.getLogger(TestConfig.class);

    private final String baseUrl;
    private final String apiKey;
    private final String targetProfile;
    private final String range;
    private final String fieldLon;
    private final String fieldLat;
    private final int numConcurrentUsers;
    private final List<Integer> querySizes;
    private final boolean parallelExecution;
    private final TestUnit testUnit;
    private final List<String> sourceFiles;
    private final List<Integer> ranges;

    public TestConfig() {
        this.baseUrl = getSystemProperty("base_url", "http://localhost:8082/ors");
        this.apiKey = getSystemProperty("api_key", "API KEY");
        this.targetProfile = getSystemProperty("profile", "driving-car");
        this.range = getSystemProperty("range", "300");
        this.fieldLon = getSystemProperty("field_lon", "longitude");
        this.fieldLat = getSystemProperty("field_lat", "latitude");
        this.numConcurrentUsers = Integer.parseInt(getSystemProperty("concurrent_users", "1"));
        this.querySizes = parseQuerySizes(getSystemProperty("query_sizes", "1"));
        this.parallelExecution = Boolean.parseBoolean(getSystemProperty("parallel_execution", "true"));
        this.testUnit = TestUnit.fromString(getSystemProperty("test_unit", "distance"));
        this.sourceFiles = parseSourceFiles(getSystemProperty("source_files", "search.csv"));
        this.ranges = parseRanges(this.range);
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

    private List<String> parseSourceFiles(String files) {
        return Arrays.stream(files.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private List<Integer> parseRanges(String rangesStr) {
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
}
