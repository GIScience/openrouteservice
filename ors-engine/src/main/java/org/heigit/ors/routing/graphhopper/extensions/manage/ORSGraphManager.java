package org.heigit.ors.routing.graphhopper.extensions.manage;

import com.graphhopper.GraphHopper;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.heigit.ors.config.EngineConfig;

import java.io.File;

public class ORSGraphManager {

    private static final Logger LOGGER = Logger.getLogger(ORSGraphManager.class.getName());
    private static final String UPDATE_LOCKFILE_NAME = "update.lock";
    private static final String RESTART_LOCKFILE_NAME = "restart.lock";

    private EngineConfig engineConfig;
    private ORSGraphFileManager orsGraphFileManager;
    private ORSGraphRepoManager orsGraphRepoManager;

    public ORSGraphManager() {
    }

    public ORSGraphManager(EngineConfig engineConfig, ORSGraphFileManager orsGraphFileManager, ORSGraphRepoManager orsGraphRepoManager) {
        this.engineConfig = engineConfig;
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
        if (StringUtils.isBlank(engineConfig.getGraphsRepoName())) return false;

        return StringUtils.isNotBlank(engineConfig.getGraphsRepoUrl()) ||
                StringUtils.isNotBlank(engineConfig.getGraphsRepoPath());
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
        File restartLockFile = new File(orsGraphFileManager.getProfileGraphsDirAbsPath() + File.separator + UPDATE_LOCKFILE_NAME);
        return restartLockFile.exists();
    }

    public boolean hasRestartLock() {
        File restartLockFile = new File(orsGraphFileManager.getProfileGraphsDirAbsPath() + File.separator + RESTART_LOCKFILE_NAME);
        return restartLockFile.exists();
    }

    public void writeOrsGraphInfoFileIfNotExists(GraphHopper gh) {
        orsGraphFileManager.writeOrsGraphInfoFileIfNotExists(gh);
    }
}
