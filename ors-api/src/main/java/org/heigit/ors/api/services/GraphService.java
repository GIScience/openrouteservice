package org.heigit.ors.api.services;

import org.apache.log4j.Logger;
import org.heigit.ors.api.Application;
import org.heigit.ors.routing.graphhopper.extensions.manage.ORSGraphManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class GraphService {

    // get this value from ors.engine.graph_management.enabled
    @Value("${ors.engine.graph_management.enabled:false}")
    Boolean enabled = false;

    private static final Logger LOGGER = Logger.getLogger(GraphService.class.getName());

    public List<ORSGraphManager> graphManagers = new ArrayList<>();

    public void addGraphManagerInstance(ORSGraphManager orsGraphManager) {
        if (orsGraphManager.useGraphRepository()) {
            graphManagers.add(orsGraphManager);
        }
    }

    AtomicBoolean restartAttemptWasBlocked = new AtomicBoolean(false);

    @Async
    @Scheduled(cron = "${ors.engine.graph_management.download_schedule:0 0 0 31 2 *}")//Default is "never"
    public void checkForUpdatesInRepo() {

        if (!enabled) {
            LOGGER.debug("Graph management is disabled, skipping scheduled repository check...");
            return;
        }

        LOGGER.debug("Scheduled repository check...");

        if (restartAttemptWasBlocked.get()) {
            LOGGER.warn("Skipping scheduled repository check, waiting for restart...");
            return;
        }
        if (isUpdateLocked()) {
            LOGGER.warn("Scheduled repository skipped: lock found - remove lock file manually!");
            return;
        }

        for (ORSGraphManager orsGraphManager : graphManagers) {
            if (orsGraphManager.isBusy()) {
                LOGGER.info("Scheduled repository check: [%s] Download or extraction in progress".formatted(orsGraphManager.getQualifiedProfileName()));
            } else {
                LOGGER.info("Scheduled repository check: [%s] Checking for update.".formatted(orsGraphManager.getQualifiedProfileName()));
                orsGraphManager.downloadAndExtractLatestGraphIfNecessary();
            }
        }

        LOGGER.debug("Scheduled repository check done");
    }

    @Async
    @Scheduled(cron = "${ors.engine.graph_management.activation_schedule:0 0 0 31 2 *}")//Default is "never"
    public void checkForDownloadedGraphsToActivate() {

        if (!enabled) {
            LOGGER.debug("Graph management is disabled, skipping scheduled activation check...");
            return;
        }

        LOGGER.debug("Restart check...");

        // Even if restart is locked: Do the checks to start repeatedRestartAttempts.

        boolean restartNeeded = false;
        boolean restartAllowed = true;

        for (ORSGraphManager orsGraphManager : graphManagers) {
            if (orsGraphManager.isBusy() || orsGraphManager.hasGraphDownloadFile()) {
                if (!restartAttemptWasBlocked.get()) {
                    LOGGER.info("Restart check: [%s] Download or extraction in progress".formatted(orsGraphManager.getQualifiedProfileName()));
                }
                restartAllowed = false;
            }
            if (orsGraphManager.hasDownloadedExtractedGraph()) {
                if (!restartAttemptWasBlocked.get()) {
                    LOGGER.info("Restart check: [%s] Downloaded extracted graph available".formatted(orsGraphManager.getQualifiedProfileName()));
                }
                restartNeeded = true;
            }
        }

        if (!restartNeeded) {
            LOGGER.info("Restart check done: No downloaded graphs found, no restart required");
            return;
        }

        if (!restartAllowed) {
            LOGGER.info("Restart check done: Restart currently not allowed, retrying every minute...");
            restartAttemptWasBlocked.set(true);
            return;
        }

        if (isRestartLocked()) {
            LOGGER.warn("Restart check done: Restart lock found - remove lock file manually! Retrying every minute...");
            restartAttemptWasBlocked.set(true);
            return;
        }

        LOGGER.info("Restart check done: Restarting openrouteservice");
        restartApplication();
    }

    @Async
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void repeatedRestartAttempt() {
        if (restartAttemptWasBlocked.get()) {
            LOGGER.info("Repeated attempt to restart application");
            checkForDownloadedGraphsToActivate();
        }
    }

    private boolean isUpdateLocked() {
        for (ORSGraphManager orsGraphManager : graphManagers) {
            if (orsGraphManager.hasUpdateLock()) {
                return true;
            }
        }
        return false;
    }

    private boolean isRestartLocked() {
        for (ORSGraphManager orsGraphManager : graphManagers) {
            if (orsGraphManager.hasRestartLock()) {
                return true;
            }
        }
        return false;
    }

    private void restartApplication() {
        Application.restart();
    }
}
