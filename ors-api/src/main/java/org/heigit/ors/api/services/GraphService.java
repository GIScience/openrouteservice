package org.heigit.ors.api.services;

import org.apache.log4j.Logger;
import org.heigit.ors.api.util.AppConfigMigration;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GraphService {
    private static final Logger LOGGER = Logger.getLogger(AppConfigMigration.class.getName());

    public List<ORSGraphManager> graphManagers = new ArrayList<>();

    public void addGraphhopperLocation(ORSGraphManager orsGraphManager) {
        graphManagers.add(orsGraphManager);
    }

    @Scheduled(cron = "${ors.engine.graphservice.schedule.download.cron:0 0 0 31 2 *}")//Default is "never"
    public void checkForUpdatesInRepo() {
        LOGGER.info("Scheduled check for updates in graph repository...");
        for (ORSGraphManager orsGraphManager : graphManagers) {
            orsGraphManager.downloadGraphIfNecessary();
        }
        LOGGER.info("Scheduled check for updates in graph repository done");
    }

    @Scheduled(cron = "${ors.engine.graphservice.schedule.activate.cron:0 0 0 31 2 *}")//Default is "never"
    public void checkForDownloadedGraphsToActivate() {
        LOGGER.info("Scheduled check for downloaded graphs...");
        boolean restartNeeded = false;
        boolean restartAllowed = true;
        for (ORSGraphManager orsGraphManager : graphManagers) {
            if (orsGraphManager.isActive()) {
                LOGGER.info("Scheduled check for downloaded graphs: Download in progress for %s".formatted(orsGraphManager.getRouteProfileName()));
                restartAllowed = false;
            }
            if (orsGraphManager.isGraphDownloadFileAvailable()) {
                LOGGER.info("Scheduled check for downloaded graphs: Downloaded graph available for %s".formatted(orsGraphManager.getRouteProfileName()));
                restartNeeded = true;
            }
        }
        if (restartNeeded && restartAllowed) {
            restartApplication();
        } else {
            LOGGER.info("Scheduled check for downloaded graphs done -> restarting openrouteservice is %s".formatted(
                    !restartNeeded ? "not needed" : restartAllowed ? "needed and allowed" : "needed but not allowed")
            );
        }
    }

    private void restartApplication() {
        LOGGER.info("Scheduled check for downloaded graphs done -> \n\n\n     /!\\ HINT: You can restart openrouteservice to activate downloaded graphs!\n\n");
//        Application.restart();
    }
}
