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
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.InputStream;

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
    private boolean deferredInitializationPending = false;

    public DynamicDataService(EngineService engineService, EngineProperties engineProperties, RestClient.Builder restClientBuilder) {
        this.engineService = engineService;

        // Log the configuration for debugging
        engineProperties.getDynamicData().logConfiguration();

        enabled = engineProperties.getDynamicData().getEnabled();
        featureStoreApiUrl = engineProperties.getDynamicData().getFeatureStoreApiUrl();

        LOGGER.debug("DynamicDataService constructor: enabled=" + enabled + ", featureStoreApiUrl=" + featureStoreApiUrl);

        // Dynamic data now only supports FeatureStore API (REST endpoint)
        if (StringUtility.isNullOrEmpty(featureStoreApiUrl)) {
            // No FSS API configured
            enabled = false;
            this.restClient = restClientBuilder.build();
        } else {
            this.restClient = restClientBuilder.baseUrl(featureStoreApiUrl).build();
        }
        
        if (!Boolean.TRUE.equals(enabled)) {
            LOGGER.debug("Dynamic data service is disabled in configuration.");
            return;
        }

        LOGGER.info("Dynamic data service will use FeatureStore API (api_url configured).");
        LOGGER.info("Dynamic data service will be initialized after context refresh.");
        deferredInitializationPending = true;
    }

    /**
     * Deferred initialization hook - called after Spring context is fully
     * initialized
     * and the EngineService has completed its initialization of routing profiles.
     * This ensures that the DynamicDataService adds datasets to the final profile
     * instances,
     * not temporary ones that might be recreated during EngineService
     * initialization.
     */
    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshed() {
        if (!deferredInitializationPending) {
            return;
        }
        deferredInitializationPending = false;
        LOGGER.info("Context refresh complete, initializing DynamicDataService.");
        this.initialize();
    }

    private void initialize() {
        RoutingProfileManager routingProfileManager = engineService.waitForInitializedRoutingProfileManager();
        LOGGER.debug("DynamicDataService.initialize() called: shutdown=" + routingProfileManager.isShutdown()
                + ", failed=" + routingProfileManager.hasFailed());
        if (routingProfileManager.isShutdown() || routingProfileManager.hasFailed()) {
            LOGGER.warn("RoutingProfileManager is shutdown or failed, skipping dynamic data initialization");
            return;
        }
        LOGGER.debug("Initializing dynamic data service with FeatureStore API URL: " + featureStoreApiUrl);
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

    /**
     * Synchronous version of update() for manual refresh/admin operations.
     * This method performs the same work as update() but blocks until completion,
     * making it suitable for admin endpoints that need immediate results.
     */
    public void refreshNow() {
        if (!Boolean.TRUE.equals(enabled)) {
            LOGGER.debug("Dynamic data service is disabled, skipping refresh.");
            return;
        }
        LOGGER.info("Manually triggered dynamic data refresh for profiles: "
                + enabledProfiles.stream().map(RoutingProfile::name).toList());
        enabledProfiles.forEach(this::fetchDynamicData);
        LOGGER.info("Manually triggered dynamic data refresh completed.");
    }

    private void fetchDynamicData(RoutingProfile profile) {
        if (StringUtility.isNullOrEmpty(featureStoreApiUrl)) {
            LOGGER.warn("featureStoreApiUrl is null or empty, cannot fetch dynamic data!");
            return;
        }

        LOGGER.debug("Using FSS API URL: " + featureStoreApiUrl);

        String profileName = profile.name();
        LOGGER.debug("fetchDynamicData started for profile: " + profileName);

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
        
        String endpoint = UriComponentsBuilder.fromPath("/api/v1/matches")
                .queryParam("profile", profileName)
                .queryParam("graphTimestamp", graphTimestampStr)
                .queryParam("orsVersion", AppInfo.VERSION)
                .queryParam("updatedAfter", updatedAfter)
                .toUriString();
        
        LOGGER.debug("Fetching dynamic data for profile '" + profileName + "' from FeatureStore API: "
                + featureStoreApiUrl + endpoint);
        
        try {
            restClient.get()
                    .uri(endpoint)
                    .exchange((request, response) -> {
                        if (response.getStatusCode().is2xxSuccessful()) {
                            try (InputStream is = response.getBody()) {
                                parseNdjsonMatches(is, profile, profileName);
                            }
                        } else {
                            LOGGER.warn("Failed to fetch dynamic data for profile '" + profileName + "': "
                                    + response.getStatusCode());
                        }
                        return null;
                    });
            LOGGER.debug("Successfully fetched dynamic data for profile '" + profileName + "'");
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

        LOGGER.debug(
                "Checking enabled datasets for profile '" + profileName + "': " + enabledDatasets.size() + " datasets");

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
                    LOGGER.debug("Dataset '" + enabledDataset + "' is present in FeatureStore for profile '"
                            + profileName + "'");
                    return true;
                } else {
                    LOGGER.debug("Dataset '" + enabledDataset + "' missing in FeatureStore for profile '" + profileName
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
     * Format:
     * {"feature_id":1,"dataset_key":"example_dataset","edge_id":3239,"value":1.0,"timestamp":"2024-09-08T20:21:00Z","is_deleted":false}
     */
    private void parseNdjsonMatches(InputStream stream, RoutingProfile profile, String profileName) {
        LOGGER.debug("Starting NDJSON parsing for profile '" + profileName + "' from stream");
        ObjectMapper objectMapper = new ObjectMapper();
        JsonFactory jsonFactory = new JsonFactory();

        try {
            JsonParser parser = jsonFactory.createParser(stream);
            int applied = 0;
            int deleted = 0;
            int skipped = 0;
            int errors = 0;

            while (parser.nextToken() != null) {
                if (parser.currentToken().isStructStart()) {
                    switch (processSingleMatch(objectMapper, parser, profile, profileName)) {
                        case APPLIED -> applied++;
                        case DELETED -> deleted++;
                        case SKIPPED -> skipped++;
                        case ERROR -> errors++;
                    }
                }
            }

            parser.close();
            LOGGER.info("Parsed " + (applied + deleted + skipped + errors) + " matches for profile '" + profileName
                    + "' (" + applied + " applied, " + deleted + " deleted, " + skipped + " skipped, " + errors + " errors)");
        } catch (IOException e) {
            LOGGER.error("Error parsing NDJSON stream for profile '" + profileName + "'", e);
        }
    }

    private enum MatchOutcome {
        APPLIED, DELETED, SKIPPED, ERROR
    }

    /**
     * Process a single NDJSON match record and update the profile.
     */
    private MatchOutcome processSingleMatch(ObjectMapper objectMapper, JsonParser parser, RoutingProfile profile, String profileName) {
        try {
            JsonNode node = objectMapper.readTree(parser);
            
            // Extract mandatory fields safely using path() instead of get() to handle
            // missing fields
            // Due to @JsonInclude(NON_NULL) in MatchDto, optional fields may be omitted
            // from JSON
            String datasetKey = node.path("dataset_key").asText();
            int edgeId = node.path("edge_id").asInt();

            // FSS uses either 'timestamp' or 'updated_at' depending on API version
            String timestamp = node.has("updated_at") ? node.path("updated_at").asText()
                    : (node.has("timestamp") ? node.path("timestamp").asText() : Instant.now().toString());

            boolean isDeleted = node.has("is_deleted") && node.get("is_deleted").asBoolean();
            
            // Track timestamp for incremental updates (from Phase 6.3.3)
            try {
                Instant matchTimestamp = Instant.parse(timestamp);
                lastUpdateTimestamps.put(profileName, matchTimestamp);
            } catch (Exception timeEx) {
                // If FSS sends just a local time without Z, append Z or parse differently
                try {
                    if (!timestamp.endsWith("Z") && !timestamp.contains("+")) {
                        timestamp = timestamp + "Z";
                    }
                    Instant matchTimestamp = Instant.parse(timestamp);
                    lastUpdateTimestamps.put(profileName, matchTimestamp);
                } catch (Exception timeEx2) {
                    LOGGER.warn("Could not parse timestamp '" + timestamp + "' for dataset=" + datasetKey + ", edgeId="
                            + edgeId);
                }
            }
            
            // Update or unset the dynamic data in the profile
            if (isDeleted) {
                profile.unsetDynamicData(datasetKey, edgeId);
                return MatchOutcome.DELETED;
            } else {
                // Safe extraction of optional value field
                // Prefer numeric read to avoid String intermediate for float JSON values
                Double value = null;
                JsonNode valueNode = node.path("value");
                if (!valueNode.isMissingNode() && !valueNode.isNull()) {
                    if (valueNode.isNumber()) {
                        value = valueNode.asDouble();
                    } else {
                        String strVal = valueNode.asText().trim().toLowerCase();
                        if ("true".equals(strVal)) value = 1.0;
                        else if ("false".equals(strVal)) value = 0.0;
                        else {
                            try {
                                value = Double.parseDouble(strVal);
                            } catch (NumberFormatException e) {
                                LOGGER.warn("Cannot parse '" + strVal + "' as a numeric or boolean value");
                            }
                        }
                    }
                }
                
                if (value == null) {
                    LOGGER.debug("Skipping match with null or invalid value: dataset=" + datasetKey + ", edgeId=" + edgeId);
                    return MatchOutcome.SKIPPED;
                }
                LOGGER.trace("Processing match: dataset=" + datasetKey + ", edgeId=" + edgeId + ", value=" + value);
                profile.updateDynamicData(datasetKey, edgeId, value);
            }

            return MatchOutcome.APPLIED;
        } catch (Exception e) {
            LOGGER.error("Error parsing NDJSON match entry for profile '" + profileName + "': " + e.getMessage(), e);
            return MatchOutcome.ERROR;
        }
    }

    /**
     * Fetch dataset statistics from FeatureStore API.
     * Returns a JsonNode array containing dataset information.
     * Format: [{"datasetId": "example_dataset", "totalFeatures": 1000,
     * "matchedFeatures": 1000, ...}]
     */
    public JsonNode getFeatureStoreStats(String profileName) {
        LOGGER.debug("Fetching FeatureStore stats for profile '" + profileName + "' from: " + featureStoreApiUrl);

        try {
            JsonNode response = restClient.get()
                    .uri("/api/v1/datasets/stats")
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                LOGGER.warn("Null response from FeatureStore stats endpoint");
                return new ObjectMapper().createArrayNode();
            }

            LOGGER.debug("Successfully retrieved FeatureStore stats: " + response);
            return response;
        } catch (HttpClientErrorException e) {
            LOGGER.warn("FeatureStore stats endpoint returned " + e.getStatusCode() + " for profile '"
                    + profileName + "': " + e.getMessage());
            return new ObjectMapper().createArrayNode();
        } catch (Exception e) {
            LOGGER.error("Error fetching FeatureStore stats for profile '" + profileName + "'", e);
            return new ObjectMapper().createArrayNode();
        }
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }
}
