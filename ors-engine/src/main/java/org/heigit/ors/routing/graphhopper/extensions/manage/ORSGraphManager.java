package org.heigit.ors.routing.graphhopper.extensions.manage;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.FlatORSGraphFolderStrategy;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFolderStrategy;
import org.heigit.ors.routing.graphhopper.extensions.manage.remote.*;

import java.io.File;

import static java.util.Optional.ofNullable;

@NoArgsConstructor
@AllArgsConstructor
public class ORSGraphManager {

    private static final Logger LOGGER = Logger.getLogger(ORSGraphManager.class.getName());
    public static final String UPDATE_LOCKFILE_NAME = "update.lock";
    public static final String ACTIVATION_LOCKFILE_NAME = "activation.lock";

    private GraphManagementRuntimeProperties managementRuntimeProperties;
    private ORSGraphFileManager orsGraphFileManager;
    private ORSGraphRepoManager orsGraphRepoManager;

    public static ORSGraphManager initializeGraphManagement(String graphVersion, EngineProperties engineProperties, ProfileProperties profileProperties) {
        GraphManagementRuntimeProperties managementProps = GraphManagementRuntimeProperties.Builder.from(engineProperties, profileProperties, graphVersion).build();
        return initializeGraphManagement(managementProps);
    }

    public static ORSGraphManager initializeGraphManagement(GraphManagementRuntimeProperties managementProps) {
        ORSGraphFolderStrategy orsGraphFolderStrategy = new FlatORSGraphFolderStrategy(managementProps);
        ORSGraphRepoStrategy orsGraphRepoStrategy = new NamedGraphsRepoStrategy(managementProps);
        ORSGraphFileManager orsGraphFileManager = new ORSGraphFileManager(managementProps, orsGraphFolderStrategy);
        orsGraphFileManager.initialize();

        ORSGraphRepoManager orsGraphRepoManager = getOrsGraphRepoManager(managementProps, orsGraphRepoStrategy, orsGraphFileManager);

        ORSGraphManager orsGraphManager = new ORSGraphManager(managementProps, orsGraphFileManager, orsGraphRepoManager);
        orsGraphManager.manageStartup();
        return orsGraphManager;
    }

    public static ORSGraphRepoManager getOrsGraphRepoManager(GraphManagementRuntimeProperties managementProps, ORSGraphRepoStrategy orsGraphRepoStrategy, ORSGraphFileManager orsGraphFileManager) {
        ORSGraphRepoManager orsGraphRepoManager = new NullRepoManager();

        switch (managementProps.getDerivedRepoType()) {
            case HTTP -> {
                LOGGER.debug("Using HttpRepoManager for repoUrl %s".formatted(managementProps.getDerivedRepoBaseUrl()));
                orsGraphRepoManager = new HttpRepoManager(managementProps, orsGraphRepoStrategy, orsGraphFileManager);
            }
            case FILESYSTEM -> {
                LOGGER.debug("Using FileSystemRepoManager for repoUri %s".formatted(managementProps.getDerivedRepoPath()));
                orsGraphRepoManager = new FileSystemRepoManager(managementProps, orsGraphRepoStrategy, orsGraphFileManager);
            }
            case NULL -> {
                LOGGER.debug("No valid repositoryUri configured, using NullRepoManager.");
                orsGraphRepoManager = new NullRepoManager();
            }
        }

        return orsGraphRepoManager;
    }

    public ProfileProperties loadProfilePropertiesFromActiveGraph(ORSGraphManager orsGraphManager, ProfileProperties profileProperties) {
        profileProperties.mergeLoaded(orsGraphManager.getActiveGraphProfileProperties());
        return profileProperties;
    }

    public String getProfileName() {
        return managementRuntimeProperties.getLocalProfileName();
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
        if (managementRuntimeProperties == null) return false;
        if (!managementRuntimeProperties.isEnabled()) return false;
        if (StringUtils.isBlank(managementRuntimeProperties.getRepoName())) return false;

        return managementRuntimeProperties.getDerivedRepoType() != GraphManagementRuntimeProperties.GraphRepoType.NULL;
    }

    public void manageStartup() {
        if (!useGraphRepository()) return;

        orsGraphFileManager.cleanupIncompleteFiles();

        boolean hasActiveGraph = orsGraphFileManager.hasActiveGraph();
        boolean hasDownloadedExtractedGraph = orsGraphFileManager.hasDownloadedExtractedGraph();

        if (!hasActiveGraph && !hasDownloadedExtractedGraph && useGraphRepository()) {
            LOGGER.debug("[%s] No local graph or extracted downloaded graph found - trying to download and extract graph from repository".formatted(getQualifiedProfileName()));
            downloadAndExtractLatestGraphIfNecessary();
            orsGraphFileManager.activateExtractedDownloadedGraph();
        }
        if (!hasActiveGraph && hasDownloadedExtractedGraph) {
            LOGGER.debug("[%s] Found extracted downloaded graph only".formatted(getQualifiedProfileName()));
            orsGraphFileManager.activateExtractedDownloadedGraph();
        }
        if (hasActiveGraph && hasDownloadedExtractedGraph) {
            LOGGER.debug("[%s] Found local graph and extracted downloaded graph".formatted(getQualifiedProfileName()));
            orsGraphFileManager.backupExistingGraph();
            orsGraphFileManager.activateExtractedDownloadedGraph();
        }
        if (hasActiveGraph && !hasDownloadedExtractedGraph) {
            LOGGER.debug("[%s] Found local graph only".formatted(getQualifiedProfileName()));
        }
    }

    public void downloadAndExtractLatestGraphIfNecessary() {
        if (!useGraphRepository()) return;
        if (orsGraphFileManager.isBusy()) {
            LOGGER.debug("[%s] ORSGraphManager is busy - skipping download".formatted(getQualifiedProfileName()));
            return;
        }
        orsGraphRepoManager.downloadGraphIfNecessary();
        orsGraphFileManager.extractDownloadedGraph();
    }

    public boolean hasUpdateLock() {
        File restartLockFile = new File(orsGraphFileManager.getGraphsRootDirAbsPath() + File.separator + UPDATE_LOCKFILE_NAME);
        return restartLockFile.exists();
    }

    public boolean hasActivationLock() {
        File restartLockFile = new File(orsGraphFileManager.getGraphsRootDirAbsPath() + File.separator + ACTIVATION_LOCKFILE_NAME);
        return restartLockFile.exists();
    }

    public void writeOrsGraphInfoFileIfNotExists(ORSGraphHopper gh) {
        orsGraphFileManager.writeOrsGraphInfoFileIfNotExists(gh);
    }

    public GraphInfo getActiveGraphInfo() {
        return orsGraphFileManager.getActiveGraphInfo();
    }

    public ProfileProperties getActiveGraphProfileProperties() {
        return ofNullable(getActiveGraphInfo())
                .map(GraphInfo::getPersistedGraphInfo)
                .map(PersistedGraphInfo::getProfileProperties)
                .orElse(null);
    }
}
