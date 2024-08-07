package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import org.apache.log4j.Logger;
import org.heigit.ors.config.EngineConfig;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphInfo;
import org.heigit.ors.routing.graphhopper.extensions.manage.ORSGraphInfoV1;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static com.google.common.base.Strings.isNullOrEmpty;

public class FileSystemRepoManager extends AbstractRepoManager implements ORSGraphRepoManager {

    private static final Logger LOGGER = Logger.getLogger(FileSystemRepoManager.class.getName());
    private String routeProfileName;
    private String graphsRepoPath;
    private String graphsRepoName;
    private String graphsProfileGroup;
    private String graphsRepoCoverage;
    private String graphsRepoGraphVersion;
    private ORSGraphFileManager orsGraphFileManager;
    private ORSGraphRepoStrategy orsGraphRepoStrategy;

    public FileSystemRepoManager() {
    }

    public FileSystemRepoManager(EngineConfig engineConfig, String routeProfileName, ORSGraphRepoStrategy orsGraphRepoStrategy, ORSGraphFileManager orsGraphFileManager) {
        this.graphsRepoPath = engineConfig.getGraphsRepoPath();
        this.graphsRepoName = engineConfig.getGraphsRepoName();
        this.graphsRepoCoverage = engineConfig.getGraphsExtent();
        this.graphsProfileGroup = engineConfig.getGraphsProfileGroup();
        this.graphsRepoGraphVersion = engineConfig.getGraphVersion();
        this.routeProfileName = routeProfileName;
        this.orsGraphRepoStrategy = orsGraphRepoStrategy;
        this.orsGraphFileManager = orsGraphFileManager;
    }

    public void setGraphsRepoName(String graphsRepoName) {
        this.graphsRepoName = graphsRepoName;
    }

    public void setGraphsRepoCoverage(String graphsRepoCoverage) {
        this.graphsRepoCoverage = graphsRepoCoverage;
    }

    public void setGraphsRepoGraphVersion(String graphsRepoGraphVersion) {
        this.graphsRepoGraphVersion = graphsRepoGraphVersion;
    }

    public void setOrsGraphFileManager(ORSGraphFileManager orsGraphFileManager) {
        this.orsGraphFileManager = orsGraphFileManager;
    }

    String getProfileDescriptiveName() {
        return orsGraphFileManager.getProfileDescriptiveName();
    }

    public String getGraphsRepoPath() {
        return graphsRepoPath;
    }

    public void setGraphsRepoPath(String graphsRepoPath) {
        this.graphsRepoPath = graphsRepoPath;
    }

    public void setRouteProfileName(String routeProfileName) {
        this.routeProfileName = routeProfileName;
    }

    public void setGraphsProfileGroup(String graphsProfileGroup) {
        this.graphsProfileGroup = graphsProfileGroup;
    }

    public void setOrsGraphRepoStrategy(ORSGraphRepoStrategy orsGraphRepoStrategy) {
        this.orsGraphRepoStrategy = orsGraphRepoStrategy;
    }

    @Override
    public void downloadGraphIfNecessary() {
        if (isNullOrEmpty(graphsRepoPath) || isNullOrEmpty(graphsRepoName) || isNullOrEmpty(graphsRepoCoverage) || isNullOrEmpty(graphsRepoGraphVersion)) {
            LOGGER.debug("[%s] ORSGraphManager is not configured - skipping check".formatted(getProfileDescriptiveName()));
            return;
        }
        if (orsGraphFileManager.isBusy()) {
            LOGGER.debug("[%s] ORSGraphManager is busy - skipping check".formatted(getProfileDescriptiveName()));
            return;
        }

        LOGGER.debug("[%s] Checking for possible graph update from remote repository...".formatted(getProfileDescriptiveName()));
        try {
            ORSGraphInfoV1 previouslyDownloadedGraphInfo = orsGraphFileManager.getDownloadedGraphInfo();
            File downloadedCompressedGraphFile = orsGraphFileManager.getDownloadedCompressedGraphFile();
            GraphInfo activeGraphInfo = orsGraphFileManager.getActiveGraphInfo();
            GraphInfo downloadedExtractedGraphInfo = orsGraphFileManager.getDownloadedExtractedGraphInfo();
            GraphInfo newlyDownloadedGraphInfo = downloadLatestGraphInfoFromRepository();

            if (!shouldDownloadGraph(
                    getDateOrEpocStart(newlyDownloadedGraphInfo),
                    getDateOrEpocStart(activeGraphInfo),
                    getDateOrEpocStart(downloadedExtractedGraphInfo),
                    getDateOrEpocStart(downloadedCompressedGraphFile, previouslyDownloadedGraphInfo))) {
                return;
            }

            Path latestCompressedGraphInRepoPath = Path.of(graphsRepoPath, graphsRepoName, graphsProfileGroup, graphsRepoCoverage, graphsRepoGraphVersion, orsGraphRepoStrategy.getRepoCompressedGraphFileName());
            long start = System.currentTimeMillis();
            downloadFile(latestCompressedGraphInRepoPath, downloadedCompressedGraphFile);

            long end = System.currentTimeMillis();
            LOGGER.info("[%s] Download finished after %d ms".formatted(getProfileDescriptiveName(), end - start));
        } catch (Exception e) {
            LOGGER.error("[%s] Caught an exception during graph download check or graph download:".formatted(getProfileDescriptiveName()), e);
        }
    }

    GraphInfo downloadLatestGraphInfoFromRepository() {
        GraphInfo latestGraphInfoInRepo = new GraphInfo();
        LOGGER.debug("[%s] Checking latest graphInfo in remote repository...".formatted(getProfileDescriptiveName()));

        Path latestGraphInfoInRepoPath = Path.of(graphsRepoPath, graphsRepoName, graphsProfileGroup, graphsRepoCoverage, graphsRepoGraphVersion, orsGraphRepoStrategy.getRepoGraphInfoFileName());
        if (!latestGraphInfoInRepoPath.toFile().exists()) {
            LOGGER.info("[%s] No graphInfo found in remote repository: %s".formatted(getProfileDescriptiveName(), latestGraphInfoInRepoPath.toFile().getAbsolutePath()));
            return latestGraphInfoInRepo;
        }

        File downloadedGraphInfoFile = orsGraphFileManager.getDownloadedGraphInfoFile();
        downloadFile(latestGraphInfoInRepoPath, downloadedGraphInfoFile);

        if (downloadedGraphInfoFile.exists()) {
            Path latestCompressedGraphInRepoPath = Path.of(graphsRepoPath, graphsRepoName, graphsProfileGroup, graphsRepoCoverage, graphsRepoGraphVersion, orsGraphRepoStrategy.getRepoCompressedGraphFileName());
            URI uri = latestCompressedGraphInRepoPath.toUri();
            latestGraphInfoInRepo.setRemoteUri(uri);

            ORSGraphInfoV1 orsGraphInfoV1 = orsGraphFileManager.readOrsGraphInfoV1(downloadedGraphInfoFile);
            latestGraphInfoInRepo.withPersistedInfo(orsGraphInfoV1);
        } else {
            LOGGER.error("[%s] Invalid download path for graphInfo file: %s".formatted(getProfileDescriptiveName(), latestGraphInfoInRepoPath));
        }

        return latestGraphInfoInRepo;
    }

    public void downloadFile(Path repoPath, File localPath) {
        if (repoPath != null) {
            try {
                Files.copy(repoPath, localPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
