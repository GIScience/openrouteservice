package org.heigit.ors.benchmark;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestConfig {
    public static final int BATCH_SIZE_UPTO = 5;
    
    private static final Logger logger = LoggerFactory.getLogger(TestConfig.class);
    
    private final String sourceFile;
    private final String baseUrl;
    private final String apiKey;
    private final String targetProfile;
    private final String range;
    private final String fieldLon;
    private final String fieldLat;
    private final int numConcurrentUsers;
    private final List<Integer> querySizes;
    private final int runTime;
    private final boolean parallelExecution;

    public TestConfig() {
        this.sourceFile = getSystemProperty("source_file", "search.csv");
        this.baseUrl = getSystemProperty("base_url", "http://localhost:8082/ors");
        this.apiKey = getSystemProperty("api_key", "API KEY");
        this.targetProfile = getSystemProperty("profile", "driving-car");
        this.range = getSystemProperty("range", "300");
        this.fieldLon = getSystemProperty("field_lon", "longitude");
        this.fieldLat = getSystemProperty("field_lat", "latitude");
        this.numConcurrentUsers = Integer.parseInt(getSystemProperty("concurrent_users", "1"));
        this.querySizes = parseQuerySizes(getSystemProperty("query_sizes", "1"));
        this.runTime = Integer.parseInt(getSystemProperty("run_time", "60"));
        this.parallelExecution = Boolean.parseBoolean(getSystemProperty("parallel_execution", "true"));
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

    // Getters
    public String getSourceFile() { return sourceFile; }
    public String getBaseUrl() { return baseUrl; }
    public String getApiKey() { return apiKey; }
    public String getTargetProfile() { return targetProfile; }
    public String getRange() { return range; }
    public String getFieldLon() { return fieldLon; }
    public String getFieldLat() { return fieldLat; }

    public int getNumConcurrentUsers() {
        return numConcurrentUsers;
    }

    public List<Integer> getQuerySizes() {
        return querySizes;
    }

    public int getRunTime() {
        return runTime;
    }

    public boolean isParallelExecution() {
        return parallelExecution;
    }
}
