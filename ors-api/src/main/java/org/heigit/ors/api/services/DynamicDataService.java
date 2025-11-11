package org.heigit.ors.api.services;

import org.apache.log4j.Logger;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.routing.RoutingProfileManagerStatus;
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

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static org.heigit.ors.api.util.Utils.isJUnitTest;

@Service
public class DynamicDataService {
    private static final Logger LOGGER = Logger.getLogger(DynamicDataService.class.getName());

    private final String storeURL;
    private final String storeUser;
    private final String storePassword;
    private final Boolean enabled;
    private final List<RoutingProfile> enabledProfiles = new ArrayList<>();
    private final Map<String, Instant> lastUpdateTimestamps = new HashMap<>();

    @Autowired
    public DynamicDataService(EngineProperties engineProperties) {
        enabled = engineProperties.getDynamicData().getEnabled();
        storeURL = engineProperties.getDynamicData().getStoreUrl();
        storeUser = engineProperties.getDynamicData().getStoreUser();
        storePassword = engineProperties.getDynamicData().getStorePass();
        if (!Boolean.TRUE.equals(enabled)) {
            LOGGER.debug("Dynamic data service is disabled in configuration.");
            return;
        }
        this.initialize();
    }

    private void initialize() {
        LOGGER.info("Initializing Dynamic data service.");
        while (!RoutingProfileManagerStatus.isReady() && !RoutingProfileManagerStatus.isShutdown() && !RoutingProfileManagerStatus.hasFailed()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("Thread interrupted while waiting for RoutingProfileManager to be ready: " + e.getMessage(), e);
            }
        }
        if (RoutingProfileManagerStatus.isShutdown() || RoutingProfileManagerStatus.hasFailed()) {
            return;
        }
        LOGGER.info("Initializing dynamic data service.");
        RoutingProfileManager.getInstance().getUniqueProfiles().forEach(profile -> {
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
        if (Boolean.FALSE.equals(enabled)) {
            LOGGER.trace("Dynamic data updates are disabled, skipping scheduled update.");
            return;
        }
        enabledProfiles.forEach(this::fetchDynamicData);
        LOGGER.info("Dynamic data update completed successfully.");
    }

    private void fetchDynamicData(RoutingProfile profile) {
        if (StringUtility.isNullOrEmpty(storeURL)) return;
        String graphDate = getGraphDate(profile);
        try (Connection con = DriverManager.getConnection(storeURL, storeUser, storePassword)) {
            try (PreparedStatement stmt = con.prepareStatement("""
                    SELECT *
                        FROM feature_map
                        WHERE dataset_key = ?
                        AND profile = ?
                        AND graph_timestamp = ?
                        AND timestamp > ?
                    """)) {
                stmt.setTimestamp(3, Timestamp.from(Instant.parse(graphDate)), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
                for (String key : profile.getDynamicDatasets()) {
                    String lastUpdateKey = profile.name() + "." + key;
                    Instant lastUpdateTimestamp = lastUpdateTimestamps.getOrDefault(lastUpdateKey, Instant.EPOCH);
                    int fetchedCount = 0;
                    stmt.setString(1, key);
                    stmt.setString(2, profile.name());
                    stmt.setTimestamp(4, Timestamp.from(lastUpdateTimestamp), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
                    ResultSet result = stmt.executeQuery();
                    while (result.next()) {
                        int edgeID = result.getInt("edge_id");
                        String value = result.getString("value");
                        Instant ts = result.getTimestamp("timestamp", Calendar.getInstance(TimeZone.getTimeZone("UTC"))).toInstant();
                        LOGGER.trace("Update dynamic data in dataset '" + key + "' for profile '" + profile.name() + "': edge ID " + edgeID + " -> value '" + value + "'");
                        if (result.getBoolean("deleted")) {
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
                    LOGGER.debug("Fetched " + fetchedCount + " rows for profile '" + profile.name() + "', dataset '" + key + "', graph date '" + graphDate + "', lastUpdateTimestamp '" + lastUpdateTimestamp + "'.");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error during dynamic data update: " + e.getMessage(), e);
        }
    }

    private static String getGraphDate(RoutingProfile profile) {
        return isJUnitTest() ? "2024-09-08T20:21:00Z" : profile.getGraphhopper().getGraphHopperStorage().getProperties().get("datareader.import.date");
    }

    public JSONObject getFeatureStoreStats(String profileName) {
        Map<String, JSONObject> stats = new HashMap<>();
        try (Connection con = DriverManager.getConnection(storeURL, storeUser, storePassword)) {
            try (ResultSet rs = con.createStatement().executeQuery("SELECT dataset_key, last_import, next_import, count_imported, count_features, count_unmatched FROM stats_import")) {
                while (rs.next()) {
                    JSONObject ts = new JSONObject();
                    ts.put("last_import", dbTimestampToString(rs.getTimestamp("last_import")));
                    ts.put("next_import", dbTimestampToString(rs.getTimestamp("next_import")));
                    ts.put("count_imported", rs.getInt("count_imported"));
                    ts.put("count_features", rs.getInt("count_features"));
                    ts.put("count_unmatched", rs.getInt("count_unmatched"));
                    stats.put(rs.getString("dataset_key"), ts);
                }
            }
            try (PreparedStatement statement = con.prepareStatement("SELECT dataset_key, profile, last_match, next_match FROM stats_match WHERE profile = ?")) {
                statement.setString(1, profileName);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        String key = rs.getString("dataset_key");
                        if (stats.containsKey(key)) {
                            stats.get(key).put("last_match", dbTimestampToString(rs.getTimestamp("last_match")));
                            stats.get(key).put("next_match", dbTimestampToString(rs.getTimestamp("next_match")));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error during dynamic data update: " + e.getMessage(), e);
        }
        return new JSONObject(stats);
    }

    private String dbTimestampToString(Timestamp timestamp) {
        return ISO_INSTANT.format(timestamp.toLocalDateTime().toInstant(ZoneOffset.UTC));
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }
}
