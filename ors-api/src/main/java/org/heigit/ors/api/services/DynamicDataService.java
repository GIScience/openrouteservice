package org.heigit.ors.api.services;

import org.apache.log4j.Logger;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.util.StringUtility;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

@Service
public class DynamicDataService {
    private final EngineService engineService;

    private static final Logger LOGGER = Logger.getLogger(DynamicDataService.class.getName());

    @Value("${featurestore.api.url:http://localhost:8080/api/v1}")
    private String featureStoreApiUrl;

    private Boolean enabled;

    private final Set<RoutingProfile> enabledProfiles = ConcurrentHashMap.newKeySet();
    private final Map<String, Instant> lastUpdateTimestamps = new ConcurrentHashMap<>();

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public DynamicDataService(EngineService engineService, EngineProperties engineProperties) {
        this.engineService = engineService;
        this.enabled = engineProperties.getDynamicData().getEnabled();

        if (Boolean.TRUE.equals(this.enabled)) {
            new Thread(this::initialize).start();
        }
    }

    private void initialize() {
        RoutingProfileManager routingProfileManager = engineService.waitForInitializedRoutingProfileManager();
        if (routingProfileManager.isShutdown() || routingProfileManager.hasFailed()) {
            return;
        }
        LOGGER.info("Initializing dynamic data service.");
        
        try {
            // Test connection to FeatureStore API
            if (!StringUtility.isNullOrEmpty(featureStoreApiUrl)) {
                restTemplate.getForObject(featureStoreApiUrl + "/datasets/stats", String.class);
                LOGGER.info("Successfully connected to FeatureStore API.");
            }
        } catch (Exception e) {
            LOGGER.error("Dynamic data service initialization failed due to FeatureStore API connection error. " + e.getMessage());
            enabled = false;
            return;
        }

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

    @Async
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
        if (StringUtility.isNullOrEmpty(featureStoreApiUrl)) return;
        
        for (String key : profile.getDynamicDatasets()) {
            String lastUpdateKey = profile.name() + "." + key;
            Instant lastUpdateTimestamp = lastUpdateTimestamps.getOrDefault(lastUpdateKey, Instant.EPOCH);
            
            try {
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(featureStoreApiUrl + "/matching/edges")
                        .queryParam("datasetKey", key)
                        .queryParam("profile", profile.name())
                        .queryParam("includeDeleted", true);
                
                if (lastUpdateTimestamp.isAfter(Instant.EPOCH)) {
                    builder.queryParam("updated_after", ISO_INSTANT.format(lastUpdateTimestamp));
                }
                
                // Pagination handling
                int page = 0;
                int fetchedCount = 0;
                boolean hasMore = true;
                
                while (hasMore) {
                    UriComponentsBuilder pageBuilder = builder.cloneBuilder()
                            .queryParam("page", page)
                            .queryParam("size", 500)
                            .queryParam("sort", "timestamp,asc"); // Ensure we get oldest first for correct timestamp tracking
                            
                    ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                            pageBuilder.build().encode().toUri(),
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<Map<String, Object>>() {}
                    );
                    
                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        Map<String, Object> pageResult = response.getBody();
                        List<Map<String, Object>> matches = (List<Map<String, Object>>) pageResult.get("content");
                        if (matches == null) matches = Collections.emptyList();
                        
                        for (Map<String, Object> match : matches) {
                            int edgeID = ((Number) match.get("edgeId")).intValue();
                            String value = (String) match.get("value");
                            String tsStr = (String) match.get("timestamp");
                            Instant ts = tsStr != null ? Instant.parse(tsStr) : Instant.now();
                            boolean deleted = Boolean.TRUE.equals(match.get("deleted"));
                            
                            LOGGER.trace("Update dynamic data in dataset '" + key + "' for profile '" + profile.name() + "': edge ID " + edgeID + " -> value '" + value + "'");
                            if (deleted) {
                                profile.unsetDynamicData(key, edgeID);
                            } else {
                                profile.updateDynamicData(key, edgeID, value);
                            }
                            fetchedCount++;
                            
                            if (lastUpdateTimestamp.isBefore(ts)) {
                                lastUpdateTimestamp = ts;
                                lastUpdateTimestamps.put(lastUpdateKey, ts);
                            }
                        }
                        
                        Number numberObj = (Number) pageResult.get("number");
                        Number totalPagesObj = (Number) pageResult.get("totalPages");
                        int pageNum = numberObj != null ? numberObj.intValue() : 0;
                        int totalPages = totalPagesObj != null ? totalPagesObj.intValue() : 0;
                        
                        hasMore = pageNum < totalPages - 1;
                        page++;
                    } else {
                        hasMore = false;
                        LOGGER.error("Failed to fetch dynamic data. Status: " + response.getStatusCode());
                    }
                }
                LOGGER.debug("Fetched " + fetchedCount + " rows for profile '" + profile.name() + "', dataset '" + key + "', lastUpdateTimestamp '" + lastUpdateTimestamp + "'.");
            } catch (Exception e) {
                LOGGER.error("Error during dynamic data update for dataset " + key + ": " + e.getMessage());
            }
        }
    }

    private static String getGraphDate(RoutingProfile profile) {
        return System.getProperty("GRAPH_DATE_OVERRIDE", profile.getGraphhopper().getGraphHopperStorage().getProperties().get("datareader.import.date"));
    }

    public JSONObject getFeatureStoreStats(String profileName) {
        if (StringUtility.isNullOrEmpty(featureStoreApiUrl)) {
            return new JSONObject();
        }
        
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(featureStoreApiUrl + "/datasets/stats")
                    .queryParam("profile", profileName);
                    
            ResponseEntity<Map> response = restTemplate.getForEntity(builder.build().encode().toUri(), Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return new JSONObject(response.getBody());
            }
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("message", "Error FeatureStore stats retrieval: " + e.getMessage());
            JSONObject result = new JSONObject();
            result.put("error", error);
            return result;
        }
        return new JSONObject();
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }
}