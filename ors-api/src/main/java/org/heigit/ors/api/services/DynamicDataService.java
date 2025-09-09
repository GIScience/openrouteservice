package org.heigit.ors.api.services;

import com.graphhopper.routing.ev.LogieBorders;
import org.apache.log4j.Logger;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.profile.EncodedValuesProperties;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.routing.RoutingProfileManagerStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DynamicDataService {
    private static final Logger LOGGER = Logger.getLogger(DynamicDataService.class.getName());

    private final EngineProperties engineProperties;
    private final Boolean enabled;
    private final List<RoutingProfile> enabledProfiles = new ArrayList<>();

    @Autowired
    public DynamicDataService(EngineProperties engineProperties, @Value("${ors.engine.dynamic_data.enabled:false}") Boolean enabled) {
        this.engineProperties = engineProperties;
        this.enabled = enabled;
        if (!enabled) {
            LOGGER.debug("Dynamic data service is disabled in configuration.");
            return;
        }
        while (!RoutingProfileManagerStatus.isReady()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("Thread interrupted while waiting for RoutingProfileManager to be ready: " + e.getMessage(), e);
            }
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
                List<String> enabledDynamicDatasets = profile.getProfileConfiguration().getBuild().getEncodedValues().getEnabledDynamicDatasets();
                enabledDynamicDatasets.forEach(datasetName -> {
                    LOGGER.info("Adding dynamic data support for dataset '" + datasetName + "' to profile '" + profile.name() + "'.");
                    profile.getGraphhopper().addSparseEncodedValue(datasetName);
                });
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
        try {
            // Placeholder for dynamic data update logic
            LOGGER.info("Dynamic data update completed successfully.");
        } catch (Exception e) {
            LOGGER.error("Error during dynamic data update: " + e.getMessage(), e);
        }
    }
}
