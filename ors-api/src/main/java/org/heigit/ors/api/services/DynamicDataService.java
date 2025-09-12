package org.heigit.ors.api.services;

import org.apache.log4j.Logger;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.routing.RoutingProfileManagerStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
        if (!enabled) {
            LOGGER.debug("Dynamic data service is disabled in configuration.");
            return;
        }
        while (!RoutingProfileManagerStatus.isReady() && !RoutingProfileManagerStatus.isShutdown() && !RoutingProfileManagerStatus.hasFailed()) {
            try {
                Thread.sleep(500);
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
            if (profile.getProfileConfiguration().getBuild().getEncoderOptions().getEnableCustomModels())
                enabledProfiles.add(profile);
        });
        if (enabledProfiles.isEmpty()) {
            LOGGER.warn("Dynamic data module activated but no profile has custom models enabled.");
        } else {
            enabledProfiles.forEach(profile -> {
                profile.getProfileConfiguration().getService().getDynamicData().getEnabledDynamicDatasets().forEach(datasetName -> {
                    LOGGER.info("Adding dynamic data support for dataset '" + datasetName + "' to profile '" + profile.name() + "'.");
                    profile.addDynamicData(datasetName);
                });
                fetchDynamicData(profile);
            });
            LOGGER.info("Dynamic data service initialized for profiles: " + enabledProfiles.stream().map(RoutingProfile::name).toList());
        }
    }

    @Async
    @Scheduled(cron = "${ors.engine.dynamic_data.update_schedule:0 * * * * *}") //Default is every minute
    public void update() {
        if (!enabled) {
            LOGGER.debug("Dynamic data updates are disabled, skipping scheduled update.");
            return;
        }
        enabledProfiles.forEach(this::fetchDynamicData);
        LOGGER.info("Dynamic data update completed successfully.");
    }

    private void fetchDynamicData(RoutingProfile profile) {
        String graphDate = profile.getGraphhopper().getGraphHopperStorage().getProperties().get("datareader.data.date");
        profile.getDynamicDatasets().forEach(key -> {
            LOGGER.debug("Fetching dynamic data for profile '" + profile.name() + "', dataset '" + key + "', graph date '" + graphDate + "'.");
            try (Connection con = DriverManager.getConnection(storeURL, storeUser, storePassword)) {
                if (con == null) {
                    LOGGER.error("Database connection is null, cannot fetch dynamic data.");
                    return;
                }
                var result = con.createStatement().executeQuery("""
                        SELECT *
                        FROM feature_map
                        WHERE dataset_key = '%s'
                          AND profile = '%s'
                          AND graph_timestamp = '%s'
                        """.formatted(key, profile.name(), graphDate));
                while (result.next()) {
                    int edgeID = result.getInt("edge_id");
                    String value = result.getString("value");
                    LOGGER.debug("Update dynamic data in dataset '" + key + "' for profile '" + profile.name() + "': edge ID " + edgeID + " -> value '" + value + "'");
                    profile.updateDynamicData(key, edgeID, value);
                }
                // TODO implement more sophisticated update strategy, e.g. only update changed values, inform feature store about new graph date, etc.
            } catch (SQLException e) {
                LOGGER.error("Error during dynamic data update: " + e.getMessage(), e);
            }
        });
    }
}
