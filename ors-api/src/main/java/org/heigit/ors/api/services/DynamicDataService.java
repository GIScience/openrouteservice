package org.heigit.ors.api.services;

import org.apache.log4j.Logger;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.routing.RoutingProfileManagerStatus;
import org.heigit.ors.util.StringUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

@Service
public class DynamicDataService {
    private static final Logger LOGGER = Logger.getLogger(DynamicDataService.class.getName());

    private final String storeURL;
    private final String storeUser;
    private final String storePassword;
    private final Boolean enabled;
    private final List<RoutingProfile> enabledProfiles = new ArrayList<>();

    @Autowired
    public DynamicDataService(EngineProperties engineProperties, @Value("${ors.engine.dynamic_data.enabled:false}") Boolean enabled, @Value("${ors.engine.dynamic_data.store_url:}") String storeURL, @Value("${ors.engine.dynamic_data.store_user:}") String storeUser, @Value("${ors.engine.dynamic_data.store_pass:}") String storePassword) {
        this.enabled = enabled;
        this.storeURL = storeURL;
        this.storeUser = storeUser;
        this.storePassword = storePassword;
        if (Boolean.FALSE.equals(enabled)) {
            LOGGER.debug("Dynamic data service is disabled in configuration.");
            return;
        }
        this.initialize();
    }

    private void initialize() {
        if (Boolean.FALSE.equals(enabled)) {
            LOGGER.debug("Dynamic data service is disabled in configuration.");
            return;
        }
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
        } else {
            for (RoutingProfile profile : enabledProfiles) {
                profile.getProfileConfiguration().getService().getDynamicData().getEnabledDynamicDatasets().forEach(datasetName -> {
                    LOGGER.info("Adding dynamic data support for dataset '" + datasetName + "' to profile '" + profile.name() + "'.");
                    profile.addDynamicData(datasetName);
                });
                fetchDynamicData(profile);
            }
            LOGGER.info("Dynamic data service initialized for profiles: " + enabledProfiles.stream().map(RoutingProfile::name).toList());
        }
    }

    public void reinitialize() {
        enabledProfiles.clear();
        this.initialize();
    }

    @Async
    @Scheduled(cron = "${ors.engine.dynamic_data.update_schedule:0 * * * * *}") //Default is every minute
    public void update() {
        if (Boolean.FALSE.equals(enabled)) {
            LOGGER.debug("Dynamic data updates are disabled, skipping scheduled update.");
            return;
        }
        enabledProfiles.forEach(this::fetchDynamicData);
        LOGGER.info("Dynamic data update completed successfully.");
    }

    private void fetchDynamicData(RoutingProfile profile) {
        if (StringUtility.isNullOrEmpty(storeURL))
            return;
        String graphDate = profile.getGraphhopper().getGraphHopperStorage().getProperties().get("datareader.data.date");
        try (Connection con = DriverManager.getConnection(storeURL, storeUser, storePassword)) {
            if (con == null) {
                LOGGER.error("Database connection is null, cannot fetch dynamic data.");
                return;
            }
            try (PreparedStatement stmt = con.prepareStatement("""
                    SELECT *
                        FROM feature_map
                        WHERE dataset_key = ?
                        AND profile = ?
                        AND graph_timestamp = ?
                    """)) {
                stmt.setTimestamp(3, Timestamp.from(Instant.parse(graphDate)), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
                for (String key : profile.getDynamicDatasets()) {
                    LOGGER.debug("Fetching dynamic data for profile '" + profile.name() + "', dataset '" + key + "', graph date '" + graphDate + "'.");
                    stmt.setString(1, key);
                    stmt.setString(2, profile.name());
                    ResultSet result = stmt.executeQuery();
                    while (result.next()) {
                        int edgeID = result.getInt("edge_id");
                        String value = result.getString("value");
                        LOGGER.info("Update dynamic data in dataset '" + key + "' for profile '" + profile.name() + "': edge ID " + edgeID + " -> value '" + value + "'");
                        profile.updateDynamicData(key, edgeID, value);
                    }
                }
            }
            // TODO implement more sophisticated update strategy, e.g. only update changed values, inform feature store about new graph date, etc.
        } catch (SQLException e) {
            LOGGER.error("Error during dynamic data update: " + e.getMessage(), e);
        }
    }
}
