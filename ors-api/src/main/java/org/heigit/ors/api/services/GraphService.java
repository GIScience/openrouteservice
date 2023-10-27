package org.heigit.ors.api.services;

import org.apache.log4j.Logger;
import org.heigit.ors.api.Application;
import org.heigit.ors.routing.graphhopper.extensions.manage.ORSGraphManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class GraphService {
    private static final Logger LOGGER = Logger.getLogger(GraphService.class.getName());

    public List<ORSGraphManager> graphManagers = new ArrayList<>();

    public void addGraphhopperLocation(ORSGraphManager orsGraphManager) {
        graphManagers.add(orsGraphManager);
    }

    AtomicBoolean restartAttemptWasBlocked = new AtomicBoolean(false);

    @Async
    @Scheduled(cron = "${ors.engine.graphservice.schedule.download.cron:0 0 0 31 2 *}")//Default is "never"
    public void checkForUpdatesInRepo() {

        LOGGER.debug("Scheduled repository check...");

        for (ORSGraphManager orsGraphManager : graphManagers) {
            if (orsGraphManager.isActive()) {
                LOGGER.info("Scheduled repository check: [%s] Download or extraction in progress".formatted(orsGraphManager.getProfileWithHash()));
            } else {
                LOGGER.info("Scheduled repository check: [%s] Checking for update.".formatted(orsGraphManager.getProfileWithHash()));
                orsGraphManager.downloadAndExtractLatestGraphIfNecessary();
            }
        }

        LOGGER.debug("Scheduled repository check done");
    }

    @Async
    @Scheduled(cron = "${ors.engine.graphservice.schedule.activate.cron:0 0 0 31 2 *}")//Default is "never"
    public void checkForDownloadedGraphsToActivate() {

        LOGGER.debug("Restart check...");

        boolean restartNeeded = false;
        boolean restartAllowed = true;

        for (ORSGraphManager orsGraphManager : graphManagers) {
            if (orsGraphManager.isActive() || orsGraphManager.hasGraphDownloadFile()) {
                LOGGER.info("Restart check: [%s] Download or extraction in progress".formatted(orsGraphManager.getProfileWithHash()));
                restartAllowed = false;
            }
            if (orsGraphManager.hasDownloadedExtractedGraph()) {
                LOGGER.info("Restart check: [%s] Downloaded extracted graph available".formatted(orsGraphManager.getProfileWithHash()));
                restartNeeded = true;
            }
        }

        if (restartNeeded && restartAllowed) {
            LOGGER.info("Restart check done: restarting openrouteservice");
            restartApplication();
        } else if (!restartAllowed) {
            LOGGER.info("Restart check: Restart currently not allowed, retrying every minute...");
            restartAttemptWasBlocked.set(true);
        } else {
            LOGGER.info("Restart check done: no downloaded graphs found, no restart required");
        }
    }

    @Async
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void repeatedRestartAttempt() {
        if (restartAttemptWasBlocked.get()) {
            LOGGER.info("Repeated attempt to restart application");
            checkForDownloadedGraphsToActivate();
        }
    }

    private void restartApplication() {
        Application.restart();
    }
}
