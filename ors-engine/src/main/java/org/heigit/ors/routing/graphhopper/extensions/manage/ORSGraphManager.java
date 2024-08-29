package org.heigit.ors.routing.graphhopper.extensions.manage;

import com.graphhopper.GraphHopper;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;
import org.heigit.ors.routing.graphhopper.extensions.manage.remote.ORSGraphRepoManager;

import java.io.File;

public class ORSGraphManager {

    private static final Logger LOGGER = Logger.getLogger(ORSGraphManager.class.getName());
    private static final String UPDATE_LOCKFILE_NAME = "update.lock";
    private static final String RESTART_LOCKFILE_NAME = "restart.lock";

    private EngineProperties engineProperties;
    private ORSGraphFileManager orsGraphFileManager;
    private ORSGraphRepoManager orsGraphRepoManager;

    public ORSGraphManager() {
    }

    public ORSGraphManager(EngineProperties engineProperties, ORSGraphFileManager orsGraphFileManager, ORSGraphRepoManager orsGraphRepoManager) {
        this.engineProperties = engineProperties;
        this.orsGraphFileManager = orsGraphFileManager;
        this.orsGraphRepoManager = orsGraphRepoManager;
    }

    public String getQualifiedProfileName() {
        return orsGraphFileManager.getProfileDescriptiveName();
    }

    public String getActiveGraphDirAbsPath() {
        return orsGraphFileManager.getActiveGraphDirAbsPath();
    }

    public boolean isBusy() {
        return orsGraphFileManager.isBusy();
    }

    public boolean hasGraphDownloadFile() {
        return orsGraphFileManager.hasGraphDownloadFile();
    }

    public boolean hasDownloadedExtractedGraph() {
        return orsGraphFileManager.hasDownloadedExtractedGraph();
    }

    public boolean useGraphRepository() {
        if (StringUtils.isBlank(engineProperties.getGraphManagement().getRepositoryName())) return false;

        return engineProperties.getGraphManagement().getRepositoryUri() != null;
    }

    public void manageStartup() {
        orsGraphFileManager.cleanupIncompleteFiles();

        boolean hasActiveGraph = orsGraphFileManager.hasActiveGraph();
        boolean hasDownloadedExtractedGraph = orsGraphFileManager.hasDownloadedExtractedGraph();

        if (!hasActiveGraph && !hasDownloadedExtractedGraph && useGraphRepository()) {
            LOGGER.info("[%s] No local graph or extracted downloaded graph found - trying to download and extract graph from repository".formatted(getQualifiedProfileName()));
            downloadAndExtractLatestGraphIfNecessary();
            orsGraphFileManager.activateExtractedDownloadedGraph();
        }
        if (!hasActiveGraph && hasDownloadedExtractedGraph) {
            LOGGER.info("[%s] Found extracted downloaded graph only".formatted(getQualifiedProfileName()));
            orsGraphFileManager.activateExtractedDownloadedGraph();
        }
        if (hasActiveGraph && hasDownloadedExtractedGraph) {
            LOGGER.info("[%s] Found local graph and extracted downloaded graph".formatted(getQualifiedProfileName()));
            orsGraphFileManager.backupExistingGraph();
            orsGraphFileManager.activateExtractedDownloadedGraph();
        }
        if (hasActiveGraph && !hasDownloadedExtractedGraph) {
            LOGGER.info("[%s] Found local graph only".formatted(getQualifiedProfileName()));
        }
    }

    public void downloadAndExtractLatestGraphIfNecessary() {
        if (orsGraphFileManager.isBusy()) {
            LOGGER.info("[%s] ORSGraphManager is busy - skipping download".formatted(getQualifiedProfileName()));
            return;
        }
        orsGraphRepoManager.downloadGraphIfNecessary();
        orsGraphFileManager.extractDownloadedGraph();
    }

    public boolean hasUpdateLock() {
        File restartLockFile = new File(orsGraphFileManager.getGraphsRootDirAbsPath() + File.separator + UPDATE_LOCKFILE_NAME);
        return restartLockFile.exists();
    }

    public boolean hasRestartLock() {
        File restartLockFile = new File(orsGraphFileManager.getGraphsRootDirAbsPath() + File.separator + RESTART_LOCKFILE_NAME);
        return restartLockFile.exists();
    }

    public void writeOrsGraphInfoFileIfNotExists(GraphHopper gh) {
        orsGraphFileManager.writeOrsGraphInfoFileIfNotExists(gh);
    }
}