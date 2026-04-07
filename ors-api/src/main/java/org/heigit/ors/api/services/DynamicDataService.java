package org.heigit.ors.api.services;

import org.apache.log4j.Logger;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.util.StringUtility;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

@Service
public class DynamicDataService {
    private final EngineService engineService;

    private static final Logger LOGGER = Logger.getLogger(DynamicDataService.class.getName());

    private final String featureStoreApiUrl;
    private Boolean enabled;
    private final List<RoutingProfile> enabledProfiles = new ArrayList<>();
    private final Map<String, Instant> lastUpdateTimestamps = new ConcurrentHashMap<>();

    public DynamicDataService(EngineService engineService, EngineProperties engineProperties) {
        this.engineService = engineService;
        enabled = engineProperties.getDynamicData().getEnabled();
        featureStoreApiUrl = engineProperties.getDynamicData().getFeatureStoreApiUrl();
        if (StringUtility.isNullOrEmpty(featureStoreApiUrl)) {
            enabled = false;
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
        if (StringUtility.isNullOrEmpty(featureStoreApiUrl))
            return;
        String graphDate = getGraphDate(profile);
        LOGGER.debug("Fetching dynamic data for profile '" + profile.name() + "' from FeatureStore API: "
                + featureStoreApiUrl + ", graph date '" + graphDate + "'.");
        // TODO: Phase 6.3 - Implement REST API call to fetch dynamic data using
        // RestClient with NDJSON streaming
        // This will replace the old JDBC-based approach
    }

    private static String getGraphDate(RoutingProfile profile) {
        return System.getProperty("GRAPH_DATE_OVERRIDE", profile.getGraphhopper().getGraphHopperStorage().getProperties().get("datareader.import.date"));
    }

    public JSONObject getFeatureStoreStats(String profileName) {
        Map<String, JSONObject> stats = new HashMap<>();
        // TODO: Phase 6.3 - Implement REST API call to fetch stats from FeatureStore
        // using featureStoreApiUrl
        // Currently returning empty stats to maintain API contract
        LOGGER.debug("Fetching FeatureStore stats for profile '" + profileName + "' from: " + featureStoreApiUrl);
        return new JSONObject(stats);
    }

    private String dbTimestampToString(Timestamp timestamp) {
        return ISO_INSTANT.format(timestamp.toLocalDateTime().toInstant(ZoneOffset.UTC));
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }
}
