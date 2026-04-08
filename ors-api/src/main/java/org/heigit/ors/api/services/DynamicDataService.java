package org.heigit.ors.api.services;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.util.AppInfo;
import org.heigit.ors.util.StringUtility;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class DynamicDataService {
    private final EngineService engineService;
    private final RestClient restClient;

    private static final Logger LOGGER = Logger.getLogger(DynamicDataService.class.getName());
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final String featureStoreApiUrl;
    private Boolean enabled;
    private final List<RoutingProfile> enabledProfiles = new ArrayList<>();
    private final Map<String, Instant> lastUpdateTimestamps = new ConcurrentHashMap<>();

    public DynamicDataService(EngineService engineService, EngineProperties engineProperties, RestClient.Builder restClientBuilder) {
        this.engineService = engineService;
        enabled = engineProperties.getDynamicData().getEnabled();
        featureStoreApiUrl = engineProperties.getDynamicData().getFeatureStoreApiUrl();
        
        // Build RestClient with featureStoreApiUrl as baseUrl
        if (StringUtility.isNullOrEmpty(featureStoreApiUrl)) {
            enabled = false;
            this.restClient = restClientBuilder.build();
        } else {
            this.restClient = restClientBuilder.baseUrl(featureStoreApiUrl).build();
        }
        
        if (!Boolean.TRUE.equals(enabled)) {
            LOGGER.debug("Dynamic data service is disabled in configuration.");
            return;
        }
        this.initialize();
    }

    private void initialize() {
        RoutingProfileManager routingProfileManager = engineService.waitForInitializedRoutingProfileManager();
        if (routingProfileManager.isShutdown() || routingProfileManager.hasFailed()) {
            return;
        }
        LOGGER.info("Initializing dynamic data service with FeatureStore API URL: " + featureStoreApiUrl);
        routingProfileManager.getUniqueProfiles().forEach(profile -> {
            LOGGER.debug("Checking profile: " + profile.name());
            if (Boolean.TRUE.equals(profile.getProfileConfiguration().getBuild().getEncoderOptions().getEnableCustomModels()))
                enabledProfiles.add(profile);
        });
        if (enabledProfiles.isEmpty()) {
            LOGGER.warn("Dynamic data module activated but no profile has custom models enabled.");
        }
        for (RoutingProfile profile : enabledProfiles) {
            for (String datasetName : profile.getProfileConfiguration().getService().getDynamicData().getEnabledDynamicDatasets()) {
                LOGGER.info("Adding dynamic data support for dataset '" + datasetName + "' to profile '" + profile.name() + "'.");
                profile.addDynamicData(datasetName);
            }
            fetchDynamicData(profile);
        }
        LOGGER.info("Dynamic data service initialized for profiles: " + enabledProfiles.stream().map(RoutingProfile::name).toList());
    }

    public void reinitialize() {
        enabledProfiles.clear();
        lastUpdateTimestamps.clear();
        this.initialize();
    }

    @Async("backgroundTaskExecutor")
    @Scheduled(cron = "${ors.engine.dynamic_data.update_schedule:0 * * * * *}") //Default is every minute
    public void update() {
        if (!Boolean.TRUE.equals(enabled)) {
            LOGGER.trace("Dynamic data updates are disabled, skipping scheduled update.");
            return;
        }
        enabledProfiles.forEach(this::fetchDynamicData);
        LOGGER.info("Dynamic data update completed successfully.");
    }

    private void fetchDynamicData(RoutingProfile profile) {
        if (StringUtility.isNullOrEmpty(featureStoreApiUrl))
            return;
        
        String profileName = profile.name();
        
        try {
            if (!areEnabledDatasetsPresent(profile)) {
                LOGGER.warn("No enabled datasets found in FeatureStore for profile '" + profileName
                        + "', skipping polling");
                return;
            }
        } catch (Exception e) {
            LOGGER.error("Error validating dataset presence for profile '" + profileName + "'", e);
            return;
        }

        // Build query parameters using UriComponentsBuilder for proper URL encoding
        String ghGraphDate = null;
        if (profile.getGraphhopper() != null && profile.getGraphhopper().getGraphHopperStorage() != null) {
            ghGraphDate = profile.getGraphhopper().getGraphHopperStorage().getProperties().get("datareader.import.date");
        }
        
        String graphTimestampStr = System.getProperty("GRAPH_DATE_OVERRIDE", ghGraphDate);
        if (graphTimestampStr == null) {
            graphTimestampStr = LocalDateTime.now().format(ISO_FORMATTER);
        } else {
            try {
                if (graphTimestampStr.endsWith("Z") || graphTimestampStr.contains("+")) {
                    graphTimestampStr = formatInstantAsIsoDateTime(Instant.parse(graphTimestampStr));
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to parse graph date: " + graphTimestampStr, e);
            }
        }
        
        Instant lastUpdate = lastUpdateTimestamps.getOrDefault(profileName, Instant.EPOCH);
        String updatedAfter = formatInstantAsIsoDateTime(lastUpdate);
        
        String endpoint = UriComponentsBuilder.fromPath("/matches")
                .queryParam("profile", profileName)
                .queryParam("graphTimestamp", graphTimestampStr)
                .queryParam("orsVersion", AppInfo.VERSION)
                .queryParam("updatedAfter", updatedAfter)
                .toUriString();
        
        LOGGER.debug("Fetching dynamic data for profile '" + profileName + "' from FeatureStore API: "
                + featureStoreApiUrl + endpoint);
        
        try {
            String response = restClient.get()
                    .uri(endpoint)
                    .retrieve()
                    .body(String.class);
            
            if (response == null || response.isEmpty()) {
                LOGGER.warn("Empty response from FeatureStore API for profile '" + profileName + "'");
                return;
            }
            
            parseNdjsonMatches(response, profile, profileName);
            LOGGER.info("Successfully fetched dynamic data for profile '" + profileName + "'");
        } catch (Exception e) {
            LOGGER.error("Error fetching dynamic data for profile '" + profileName + "'", e);
        }
    }

    /**
     * Check if any of the enabled datasets for the profile are present
     * in FeatureStore.
     * Returns true if at least one enabled dataset is present in stats.
     */
    private boolean areEnabledDatasetsPresent(RoutingProfile profile) {
        String profileName = profile.name();
        List<String> enabledDatasets = profile.getProfileConfiguration().getService().getDynamicData()
                .getEnabledDynamicDatasets();

        if (enabledDatasets == null || enabledDatasets.isEmpty()) {
            LOGGER.debug("No enabled datasets configured for profile '" + profileName + "'");
            return false;
        }

        try {
            JsonNode statsResponse = getFeatureStoreStats(profileName);

            // Extract dataset IDs from the stats array
            Set<String> availableDatasets = new HashSet<>();
            if (statsResponse.isArray()) {
                for (JsonNode dataset : statsResponse) {
                    String datasetId = dataset.get("datasetId").asText();
                    availableDatasets.add(datasetId);
                }
            }

            // Check if at least one enabled dataset is present
            for (String enabledDataset : enabledDatasets) {
                if (availableDatasets.contains(enabledDataset)) {
                    LOGGER.info("Dataset '" + enabledDataset + "' is present in FeatureStore for profile '"
                            + profileName + "'");
                    return true;
                } else {
                    LOGGER.warn("Dataset '" + enabledDataset + "' missing in FeatureStore for profile '" + profileName
                            + "'");
                }
            }

            LOGGER.warn("None of the enabled datasets found in FeatureStore for profile '" + profileName + "'");
            return false;
        } catch (Exception e) {
            LOGGER.error("Error checking dataset presence in FeatureStore for profile '" + profileName + "'", e);
            return false;
        }
    }

    /**
     * Format Instant as ISO_LOCAL_DATE_TIME string (e.g., 2024-09-08T20:21:00).
     */
    private String formatInstantAsIsoDateTime(Instant instant) {
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, java.time.ZoneId.of("UTC"));
        return ldt.format(ISO_FORMATTER);
    }

    /**
     * Parse NDJSON response from FeatureStore API using streaming JSON parser.
     * Each line is a JSON object representing a match.
     * Format: {"feature_id":1,"dataset_key":"logie_borders","edge_id":3239,"value":"CLOSED","timestamp":"2024-09-08T20:21:00Z","is_deleted":false}
     */
    private void parseNdjsonMatches(String ndjsonContent, RoutingProfile profile, String profileName) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonFactory jsonFactory = new JsonFactory();
        
        try {
            JsonParser parser = jsonFactory.createParser(ndjsonContent);
            int matchCount = 0;
            
            while (parser.nextToken() != null) {
                if (parser.currentToken().isStructStart()) {
                    matchCount += processSingleMatch(objectMapper, parser, profile, profileName);
                }
            }
            
            parser.close();
            LOGGER.info("Successfully parsed " + matchCount + " dynamic data matches for profile '" + profileName + "'");
        } catch (IOException e) {
            LOGGER.error("Error parsing NDJSON stream for profile '" + profileName + "'", e);
        }
    }

    /**
     * Process a single NDJSON match record and update the profile.
     * Returns 1 if successful, 0 if error occurred.
     */
    private int processSingleMatch(ObjectMapper objectMapper, JsonParser parser, RoutingProfile profile, String profileName) {
        try {
            JsonNode node = objectMapper.readTree(parser);
            
            // Extract mandatory fields
            String datasetKey = node.get("dataset_key").asText();
            int edgeId = node.get("edge_id").asInt();
            String timestamp = node.get("timestamp").asText();
            boolean isDeleted = node.has("is_deleted") && node.get("is_deleted").asBoolean();
            
            // Track timestamp for incremental updates (from Phase 6.3.3)
            Instant matchTimestamp = Instant.parse(timestamp);
            lastUpdateTimestamps.put(profileName, matchTimestamp);
            
            // Update or unset the dynamic data in the profile
            if (isDeleted) {
                profile.unsetDynamicData(datasetKey, edgeId);
            } else {
                String value = node.get("value").asText();
                profile.updateDynamicData(datasetKey, edgeId, value);
            }
            
            return 1;
        } catch (Exception e) {
            LOGGER.warn("Error parsing NDJSON match entry for profile '" + profileName + "'", e);
            return 0;
        }
    }

    /**
     * Fetch dataset statistics from FeatureStore API.
     * Returns a JsonNode array containing dataset information.
     * Format: [{"datasetId": "logie_borders", "totalFeatures": 1000,
     * "matchedFeatures": 1000, ...}]
     */
    public JsonNode getFeatureStoreStats(String profileName) {
        LOGGER.debug("Fetching FeatureStore stats for profile '" + profileName + "' from: " + featureStoreApiUrl);

        try {
            JsonNode response = restClient.get()
                    .uri("/datasets/stats")
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                LOGGER.warn("Null response from FeatureStore stats endpoint");
                return new ObjectMapper().createArrayNode();
            }

            LOGGER.debug("Successfully retrieved FeatureStore stats: " + response);
            return response;
        } catch (Exception e) {
            LOGGER.error("Error fetching FeatureStore stats", e);
            return new ObjectMapper().createArrayNode();
        }
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }
}
